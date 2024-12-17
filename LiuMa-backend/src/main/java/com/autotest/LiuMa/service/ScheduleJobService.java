package com.autotest.LiuMa.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.autotest.LiuMa.common.constants.*;
import com.autotest.LiuMa.common.utils.TapdUtils;
import com.autotest.LiuMa.database.domain.*;
import com.autotest.LiuMa.database.mapper.*;
import com.autotest.LiuMa.dto.StatisticsDTO;
import com.autotest.LiuMa.dto.tapdHeaderDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.autotest.LiuMa.common.utils.OkHttpUtil.post;


@Service
@Transactional(rollbackFor = Exception.class)
public class ScheduleJobService {

    @Resource
    private EngineMapper engineMapper;

    @Resource
    private TaskMapper taskMapper;

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private PlanMapper planMapper;

    @Resource
    private PlanScheduleMapper planScheduleMapper;

    @Resource
    private PlanCollectionMapper planCollectionMapper;

    @Resource
    private StatisticsMapper statisticsMapper;

    @Resource
    private ProjectMapper projectMapper;

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private RunService runService;

    @Resource
    private DeviceService deviceService;

//    @Resource
//    private TestBackUpFileService testBackUpFileService;

//    @Resource
//    private TesterRankMapper testerRankMapper;

    @Resource
    private TesterOrderMapper testerOrderMapper;

    @Resource
    private StorySatisticsMapper storySatisticsMapper;

    public void updateLostHeartbeatEngine(){
        Long minLastHeartbeatTime = System.currentTimeMillis() - 3*60*1000; // 三分钟没有心跳监控则离线
        engineMapper.updateLostHeartbeatEngine(minLastHeartbeatTime);
    }

    public void updateTimeoutTask(){
        Long minLastUploadTime = System.currentTimeMillis() - 10*60*1000;   // 十分钟内没有结果返回则任务超时
        Long minLastToRunTime = System.currentTimeMillis() - 2*60*60*1000;   // 两小时内没有执行则任务超时
        List<Report> reports = reportMapper.selectTimeoutReport(minLastUploadTime, minLastToRunTime);
        for(Report report:reports){
            reportMapper.updateReportStatus(ReportStatus.DISCONTINUE.toString(), report.getId());
            taskMapper.updateTask(ReportStatus.DISCONTINUE.toString(), report.getTaskId());
            reportMapper.updateReportEndTime(report.getId(), System.currentTimeMillis(), System.currentTimeMillis());
            // 释放设备
            runService.stopDeviceWhenRunEnd(report.getTaskId());
        }
    }

    public void updateTimeoutDevice(){
        List<Device> devices = deviceMapper.selectTimeoutDevice();
        for (Device device:devices){
            deviceService.coldDevice(device);
        }
    }

    public void runSchedulePlan(){
        long currentTime = System.currentTimeMillis();
        Long minNextRunTime = currentTime - 30*1000;
        Long maxNextRunTime = currentTime + 30*1000;   // 查找下次执行时间在前后半分钟内的计划
        List<PlanSchedule> planSchedules = planScheduleMapper.getToRunPlanScheduleList(minNextRunTime, maxNextRunTime);
        for(PlanSchedule planSchedule: planSchedules){
            Plan plan = planMapper.getPlanDetail(planSchedule.getPlanId());
            String runName = "【定时执行】" + plan.getName() +"-"+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(planSchedule.getNextRunTime()));
            Task task = new Task();
            task.setId(UUID.randomUUID().toString());
            task.setName(runName);
            task.setStatus(ReportStatus.PREPARED.toString());
            task.setType(TaskType.SCHEDULE.toString());
            task.setEngineId(plan.getEngineId());
            task.setProjectId(plan.getProjectId());
            task.setCreateUser(plan.getCreateUser());
            task.setUpdateUser(plan.getCreateUser());
            task.setCreateTime(System.currentTimeMillis());
            task.setUpdateTime(System.currentTimeMillis());
            taskMapper.addTask(task);
            // 预设报告
            Report report = new Report();
            report.setId(UUID.randomUUID().toString());
            report.setName(runName);
            report.setTaskId(task.getId());
            report.setEnvironmentId(plan.getEnvironmentId());
            report.setDeviceId(null);
            report.setSourceType(ReportSourceType.PLAN.toString());
            report.setSourceId(plan.getId());
            report.setStatus(ReportStatus.PREPARED.toString());
            report.setProjectId(plan.getProjectId());
            report.setCreateUser(plan.getCreateUser());
            report.setUpdateUser(plan.getCreateUser());
            report.setCreateTime(System.currentTimeMillis());
            report.setUpdateTime(System.currentTimeMillis());
            reportMapper.addReport(report);
            // 统计报告用例数
            ReportStatistics reportStatistics = new ReportStatistics();
            reportStatistics.setId(UUID.randomUUID().toString());
            reportStatistics.setReportId(report.getId());
            reportStatistics.setPassCount(0);
            reportStatistics.setErrorCount(0);
            reportStatistics.setFailCount(0);
            Integer total = planCollectionMapper.getPlanCaseCount(plan.getId());
            reportStatistics.setTotal(total);
            reportMapper.addReportStatistics(reportStatistics);
            // 回写定时任务表下次执行时间
            while (!planSchedule.getFrequency().equals(PlanFrequency.ONLY_ONE.toString()) && planSchedule.getNextRunTime() < maxNextRunTime){ // 找到大于当前时间的日期
                planSchedule.setNextRunTime(PlanService.getNextRunTime(planSchedule.getNextRunTime(), planSchedule.getFrequency()));
            }
            planScheduleMapper.updatePlanSchedule(planSchedule);
        }
    }

    public void statisticsData(){
        // 所有项目
        List<String> projectIds = projectMapper.getAllProjectId();
        // 当前日期
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(date);
        // 总数统计
        HashMap<String, SumStatistics> sumStatisticsMap = new HashMap<>();
        // 每日统计
        HashMap<String, DailyStatistics> dailyStatisticsMap = new HashMap<>();

        for(String projectId: projectIds){
            SumStatistics sum = new SumStatistics();
            sum.setId(UUID.randomUUID().toString());
            sum.setProjectId(projectId);
            JSONObject prObj = new JSONObject();
            prObj.put("xAxis", new JSONArray());
            prObj.put("planRunTotal", new JSONArray());
            prObj.put("planRunPass", new JSONArray());
            prObj.put("planRunPassRate", new JSONArray());
            prObj.put("yMaxLeft", 0);
            sum.setPlanRunWeekTop(prObj.toString());
            JSONObject cfObj = new JSONObject();
            cfObj.put("x", new JSONArray());
            cfObj.put("y", new JSONArray());
            sum.setCaseFailWeekTop(cfObj.toString());
            sumStatisticsMap.put(projectId, sum);
            DailyStatistics daily = new DailyStatistics();
            daily.setId(UUID.randomUUID().toString());
            daily.setProjectId(projectId);
            daily.setStatDate(currentDate);
            dailyStatisticsMap.put(projectId, daily);
        }

        List<StatisticsDTO> caseTotal = statisticsMapper.getCaseCountByProject();
        for(StatisticsDTO statisticsDTO: caseTotal){
            SumStatistics sum = sumStatisticsMap.get(statisticsDTO.getProjectId());
            DailyStatistics daily = dailyStatisticsMap.get(statisticsDTO.getProjectId());
            if(statisticsDTO.getName().equals("API")){
                sum.setApiCaseTotal(statisticsDTO.getCount());
                daily.setApiCaseSum(statisticsDTO.getCount());
            }else if(statisticsDTO.getName().equals("WEB")){
                sum.setWebCaseTotal(statisticsDTO.getCount());
                daily.setWebCaseSum(statisticsDTO.getCount());
            }else {
                sum.setAppCaseTotal(statisticsDTO.getCount());
                daily.setAppCaseSum(statisticsDTO.getCount());
            }
        }
        List<StatisticsDTO> caseNewToday = statisticsMapper.getCaseTodayNewCountByProject();
        for(StatisticsDTO statisticsDTO: caseNewToday){
            DailyStatistics daily = dailyStatisticsMap.get(statisticsDTO.getProjectId());
            if(statisticsDTO.getName().equals("API")){
                daily.setApiCaseNew(statisticsDTO.getCount());
            }else if(statisticsDTO.getName().equals("WEB")){
                daily.setWebCaseNew(statisticsDTO.getCount());
            }else {
                daily.setAppCaseNew(statisticsDTO.getCount());
            }
        }
        List<StatisticsDTO> caseNewWeek = statisticsMapper.getCaseWeekNewCountByProject();
        for (StatisticsDTO statisticsDTO: caseNewWeek){
            SumStatistics sum = sumStatisticsMap.get(statisticsDTO.getProjectId());
            if (statisticsDTO.getName().equals("API")){
                sum.setApiCaseNewWeek(statisticsDTO.getCount());
            }else if(statisticsDTO.getName().equals("WEB")){
                sum.setWebCaseNewWeek(statisticsDTO.getCount());
            }else {
                sum.setAppCaseNewWeek(statisticsDTO.getCount());
            }
        }
        List<StatisticsDTO> caseRunToday = statisticsMapper.getCaseTodayRunCountByProject();
        for (StatisticsDTO statisticsDTO: caseRunToday){
            DailyStatistics daily = dailyStatisticsMap.get(statisticsDTO.getProjectId());
            if (statisticsDTO.getName().equals("API")){
                daily.setApiCaseRun(statisticsDTO.getCount());
                daily.setApiCasePassRate(statisticsDTO.getPassRate());
            }else if(statisticsDTO.getName().equals("WEB")){
                daily.setWebCaseRun(statisticsDTO.getCount());
                daily.setWebCasePassRate(statisticsDTO.getPassRate());
            }else {
                daily.setAppCaseRun(statisticsDTO.getCount());
                daily.setAppCasePassRate(statisticsDTO.getPassRate());
            }
        }
        List<StatisticsDTO> caseRunTotal = statisticsMapper.getCaseTotalRunCountByProject();
        for (StatisticsDTO statisticsDTO: caseRunTotal){
            SumStatistics sum = sumStatisticsMap.get(statisticsDTO.getProjectId());
            sum.setCaseRunTotal(statisticsDTO.getCount());
        }
        List<StatisticsDTO> caseRunTotalToday = statisticsMapper.getCaseTotalTodayRunCountByProject();
        for (StatisticsDTO statisticsDTO: caseRunTotalToday){
            SumStatistics sum = sumStatisticsMap.get(statisticsDTO.getProjectId());
            sum.setCaseRunToday(statisticsDTO.getCount());
        }
        List<StatisticsDTO> planRunTop = statisticsMapper.getPlanRunTopByProject();
        for (StatisticsDTO statisticsDTO: planRunTop){
            SumStatistics sum = sumStatisticsMap.get(statisticsDTO.getProjectId());
            JSONObject planRunObj = JSONObject.parseObject(sum.getPlanRunWeekTop());
            planRunObj.getJSONArray("xAxis").add(statisticsDTO.getName());
            planRunObj.getJSONArray("planRunTotal").add(statisticsDTO.getCount());
            planRunObj.getJSONArray("planRunPass").add(statisticsDTO.getPass());
            planRunObj.getJSONArray("planRunPassRate").add(statisticsDTO.getPassRate());
            if (planRunObj.getInteger("yMaxLeft") < statisticsDTO.getCount()){
                planRunObj.put("yMaxLeft", statisticsDTO.getCount());
            }
            sum.setPlanRunWeekTop(planRunObj.toString());
        }
        List<StatisticsDTO> caseFailTop = statisticsMapper.getCaseFailTopByProject();
        for (StatisticsDTO statisticsDTO: caseFailTop){
            SumStatistics sum = sumStatisticsMap.get(statisticsDTO.getProjectId());
            JSONObject caseFailObj = JSONObject.parseObject(sum.getCaseFailWeekTop());
            caseFailObj.getJSONArray("x").add(statisticsDTO.getCount());
            caseFailObj.getJSONArray("y").add(statisticsDTO.getName());
            sum.setCaseFailWeekTop(caseFailObj.toString());
        }
        List<SumStatistics> sumStatisticsList = new ArrayList<>(sumStatisticsMap.values());
        statisticsMapper.updateSumData(sumStatisticsList);
        List<DailyStatistics> dailyStatisticsList = new ArrayList<>(dailyStatisticsMap.values());
        statisticsMapper.updateDailyData(dailyStatisticsList);
    }

//    自动备份数据库
//    public void AutoBackUpFile(){
////        判断是否有大于30天的文件，如果有则删除
//        List<TestBackUpFile> getTestBackUpFileThirtyDays = testBackUpFileService.getTestBackUpFileThirtyDay();
//        if (getTestBackUpFileThirtyDays.size() > 0) {
//            for (TestBackUpFile getTestBackUpFileThirtyDay : getTestBackUpFileThirtyDays) {
//                testBackUpFileService.deleteFile(getTestBackUpFileThirtyDay);
//            }
//        }
//
//        String type = "1";
//        String project_id = "5c8b77de-0de6-4c4f-af00-58b4cd632660";  //暂时写死 project_id
//        String user= "system_admin_user";
//        testBackUpFileService.backupFile(type,project_id,user);
//
//    }


//    定时提醒每周需求进度

//    public void AutoRemindRequireProcedure(String code) throws InterruptedException {
////        Map<String, String> nameMap = new HashMap<>();
//
//
//
//        List<String> strings = Arrays.asList("testing","status_5","status_6","status_7","status_8", "status_9", "status_10", "status_11");
//
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("# --------------本周需求进度--------------- \n\n");
//
//        Integer test_pass1=0;
//        Integer test_pass2=0;
//        for (int i = 0; i < strings.size(); i++) {
//            if ("testing".equals(strings.get(i))) {
//                stringBuilder.append("## 产品测试中 ");
//            } else if ("status_7".equals(strings.get(i))) {
//                stringBuilder.append("## 测试通过 ");
//            }else if ("status_8".equals(strings.get(i))) {
//                stringBuilder.append("## 集成测试 ");
//            }else if ("status_9".equals(strings.get(i))) {
//                stringBuilder.append("## <font color=\\\"warning\\\">集成测试通过 </font>");
//            }else if ("status_10".equals(strings.get(i))) {
//                stringBuilder.append("## <font color=\\\"warning\\\">预发布测试 </font>");
//            }else if ("status_11".equals(strings.get(i))) {
//                stringBuilder.append("## <font color=\\\"info\\\">预发布测试通过 </font>");
//            }
//            String url = "https://api.tapd.cn/stories?workspace_id=22274921&status="+ strings.get(i);
//            String release_url = "https://api.tapd.cn/releases?workspace_id=22274921&id=";
//            String api_user = "s8vyzbK7";
//            String api_password = "79A09465-341B-205A-84C4-5E0857587D7B";
//            String body = HttpRequest.get(url).basicAuth(api_user, api_password).execute().body();
//            Thread.currentThread().sleep(2000);
//            JsonParser jsonParser = new JsonParser();
//            JsonObject parse = (JsonObject) jsonParser.parse(body);
//            JsonArray data = parse.get("data").getAsJsonArray();
//
//            if ("status_5".equals(strings.get(i)) || "status_6".equals(strings.get(i)) || "status_7".equals(strings.get(i))) {
//                test_pass1 = test_pass1 + data.size();
//            } else {
//                test_pass2 = data.size();
//            }
//            if("status_7".equals(strings.get(i))){
//                stringBuilder.append("<font color=\\\"warning\\\">" + test_pass1 + "</font>个 \n");
//            } else if ("status_5".equals(strings.get(i))||"status_6".equals(strings.get(i))) {
//
//            }else{stringBuilder.append("<font color=\\\"warning\\\">" + test_pass2 + "</font>个 \n");};
//
//
//            for (int j = 0; j < data.size(); j++) {
//                String id = data.get(j).getAsJsonObject().get("Story").getAsJsonObject().get("id").toString().replace("\"","");
//                String short_id = id.substring(id.length() - 7);
//                String name = data.get(j).getAsJsonObject().get("Story").getAsJsonObject().get("name").toString().replace("\"","");
//                String owner = data.get(j).getAsJsonObject().get("Story").getAsJsonObject().get("owner").toString().replace("\"","");
//                String release_id = data.get(j).getAsJsonObject().get("Story").getAsJsonObject().get("release_id").toString().replace("\"","");
//                String release_body = HttpRequest.get(release_url+release_id).basicAuth(api_user, api_password).execute().body();
//                Thread.currentThread().sleep(3000);
//                JsonParser jsonParser1 = new JsonParser();
//                JsonObject parse1 = (JsonObject) jsonParser1.parse(release_body);
//                JsonArray data1 = parse1.get("data").getAsJsonArray();
//                String release_name;
//                if (data1.size() > 0) {
//                    release_name = data1.get(0).getAsJsonObject().get("Release").getAsJsonObject().get("name").toString().replace("\"","");
//                }else {
//                    release_name = "暂无计划";
//                }
//
//
//                if (data.size() != 0) {
//                    stringBuilder.append(">"+String.valueOf(j+1)+"、 ["+short_id + "]"+"(https://www.tapd.cn/22274921/prong/stories/view/"+id+")"+"_");
//                    stringBuilder.append(name + " ");
//                    stringBuilder.append("    "+release_name + " \n");
//                    stringBuilder.append("    处理人："+owner + " \n");
//                }
//            }
//            stringBuilder.append("\n");
//        }
//        String follow_name = getName(code);
//        if (code == "wen") {
//            stringBuilder.append("本周跟进人：" + follow_name + "@" + follow_name + "，请在周五下班前同步进度！\n");
//        } else if (code == "fri") {
//            stringBuilder.append("@所有人，周五啦，各位小伙伴辛苦了，请及时更新测试进度哦！本周跟进人：" + follow_name + "@" + follow_name + "，请在周五下班前同步进度！\n");
//        } else if (code == "sun") {
//            stringBuilder.append("本周跟进人：" + follow_name + "@" + follow_name + "，请提醒测试人员验证相关需求！\n");
//        }
//
//        sendNotification(stringBuilder.toString());
//    }

    public String generateMessage() {
        //查询出所有为删除的需求id，并拼接成字符串
        StringBuilder sb = new StringBuilder();
        List<StorySatistics> allStorySatistics = storySatisticsMapper.getAllStorySatistics();
        for (StorySatistics allStorySatistic : allStorySatistics) {
            sb.append(allStorySatistic.getShortId()+" ");
        }
        String s = sb.toString();
        // 检查已经上线的，并将状态置为 deleted=1；
        String workspace_id = "22274921";
        String storyUrlHigh = "https://www.tapd.cn/api/search_filter/search_filter/search";
        tapdHeaderDTO cookieHigh = null;
        try {
            cookieHigh = TapdUtils.getCookie();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //        #################headers的获取############
        Map<String, String> headersHigh = new HashMap<String, String>();
        headersHigh.put("cookie", cookieHigh.getTapdCookie());
//        headersHigh.put("cookie", "tapdsession=169418957579e1b8fee32d46a3f305bfc26ed9df11ac8eec62dc5e13afbefa94962f884685");
        headersHigh.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
        if (!"".equals(s)) {
            JSONObject highStoriesInterfaceBodyResolved = TapdUtils.getHighStoriesInterfaceBodyResolved(workspace_id, s);
            // 检查 是否已上线的请求
            String storiesDataHighResolved = post(new OkHttpClient(), storyUrlHigh, highStoriesInterfaceBodyResolved, Headers.of(headersHigh));
            JSONObject jsonObjectResolved = JSONObject.parseObject(storiesDataHighResolved);
            JSONObject dataObjectResolved = jsonObjectResolved.getJSONObject("data");
            JSONArray storiesListResolved = dataObjectResolved.getJSONArray("list");
            if (storiesListResolved.size() > 0) {
                for (int i = 0; i < storiesListResolved.size(); i++) {
                    JSONObject story = storiesListResolved.getJSONObject(i);
                    String shortId = story.getString("short_id");
                    StorySatistics storySatistics = new StorySatistics();
                    String id = story.getString("id");
                    String statusAlias = story.getString("status_alias");
                    String status = story.getString("status");
                    String owner = story.getString("owner");
                    JSONObject releaseInfo = story.getJSONObject("release_info");
                    String releasePlan;
                    if (releaseInfo != null) {
                        releasePlan = releaseInfo.getString("name");
                    } else {
                        releasePlan = "暂无发布计划";
                    }

                    storySatistics.setStoryId(id);
                    storySatistics.setStatusAlias(statusAlias);
                    storySatistics.setStatus(status);
                    storySatistics.setOwner(owner);
                    storySatistics.setReleasePlan(releasePlan);
                    storySatistics.setUpdateTime(System.currentTimeMillis());
                    storySatisticsMapper.updateStorySatistics(storySatistics);
                    storySatisticsMapper.deleteStorySatistics(shortId);
                }
            }
        }
        List<String> statusListHigh = new ArrayList<String>();
        //        #################body的构造############
        statusListHigh.add("产品测试中");
        statusListHigh.add("测试通过");
        statusListHigh.add("Coding Review");
        statusListHigh.add("产品已实现");
        statusListHigh.add("集成测试");
        statusListHigh.add("集成测试通过");
        statusListHigh.add("预发布集成测试");
        statusListHigh.add("预发布集成测试通过");
//        statusListHigh.add("产品已上线");
        JSONObject highStoriesInterfaceBody = TapdUtils.getHighStoriesInterfaceBody(workspace_id, statusListHigh);
        String storiesDataHigh = post(new OkHttpClient(), storyUrlHigh, highStoriesInterfaceBody, Headers.of(headersHigh));
//        System.out.println("storiesData:"+storiesDataHigh);
        //        #################数据的解析############
        JSONObject jsonObject1 = JSONObject.parseObject(storiesDataHigh);
        JSONObject dataObject1 = jsonObject1.getJSONObject("data");
        JSONArray storiesList = dataObject1.getJSONArray("list");
        if (storiesList.size() > 0) {
            for (int i = 0; i < storiesList.size(); i++) {
                StorySatistics storySatistics = new StorySatistics();
                JSONObject story = storiesList.getJSONObject(i);
                String id = story.getString("id");
                String name = story.getString("name");
                String shortId = story.getString("short_id");
                String statusAlias = story.getString("status_alias");
                String status = story.getString("status");
                String owner = story.getString("owner");
                JSONObject releaseInfo = story.getJSONObject("release_info");
                String releasePlan;
                if (releaseInfo != null) {
                    releasePlan = releaseInfo.getString("name");
                } else {
                    releasePlan = "暂无发布计划";
                }

                String storyUrl1= "https://www.tapd.cn/"+"22274921/prong/stories/view/"+id;

//                System.out.println("------------------------------------------------------");
//                System.out.println("id:"+id);
//                System.out.println("name:"+name);
//                System.out.println("shortId:"+shortId);
//                System.out.println("statusAlias:"+statusAlias);
                if ("测试通过".equals(statusAlias) || "Coding Review".equals(statusAlias) || "产品已实现".equals(statusAlias)) {
                    statusAlias="测试通过";
                }
//                System.out.println("status:"+status);
//                System.out.println("owner:"+owner);
//                System.out.println("releasePlan:"+releasePlan);
//                System.out.println("storyUrl:"+storyUrl1);
                StorySatistics storySatisticsByStoryId = storySatisticsMapper.getStorySatisticsByStoryId(id);
                if (storySatisticsByStoryId != null ) {
                    if (!status.equals(storySatisticsByStoryId.getStatus())) {
                        storySatistics.setStoryId(id);
                        storySatistics.setStatusAlias(statusAlias);
                        storySatistics.setStatus(status);
                        //状态中文容易变更优化为状态代码判断
//                        if ("产品测试中".equals(statusAlias)) {
//                            storySatistics.setStatusSort("1");
//                        } else if ("测试通过".equals(statusAlias)) {
//                            storySatistics.setStatusSort("2");
//                        }else if ("集成测试".equals(statusAlias)) {
//                            storySatistics.setStatusSort("3");
//                        }else if ("集成测试通过".equals(statusAlias)) {
//                            storySatistics.setStatusSort("4");
//                        }else if ("预发布测试".equals(statusAlias)) {
//                            storySatistics.setStatusSort("5");
//                        }else if ("预发布测试通过".equals(statusAlias)) {
//                            storySatistics.setStatusSort("6");
//                        }else {
//                            storySatistics.setStatusSort("1000");
//                        }
                        //UI_design : "UI设计中" auditing : "产品评审中" developing : "实现中" planning : "产品规划中" rejected : "已拒绝"
                        // resolved : "产品已上线" status_1 : "前端开发中" status_2 : "开发中" status_3 : "Coding Review" status_4 : "产品已实现"
                        // status_5 : "测试通过" status_6 : "规划中" status_7 : "已实现" status_8 : "集成测试" status_9 : "集成测试通过"
                        // status_10 : "预发布集成测试" status_11 : "预发布集成测试通过" status_12 : "转测试" testing : "产品测试中"
                        if ("testing".equals(status)) {
                            storySatistics.setStatusSort("6");
                        } else if ("status_3".equals(status)||"status_5".equals(status)||"status_4".equals(status)) {
                            storySatistics.setStatusSort("5");
                        }else if ("status_8".equals(status)) {
                            storySatistics.setStatusSort("4");
                        }else if ("status_9".equals(status)) {
                            storySatistics.setStatusSort("3");
                        }else if ("status_10".equals(status)) {
                            storySatistics.setStatusSort("2");
                        }else if ("status_11".equals(status)) {
                            storySatistics.setStatusSort("1");
                        }else {
                            storySatistics.setStatusSort("-1000");
                        }
                        storySatistics.setOwner(owner);
                        storySatistics.setReleasePlan(releasePlan);
                        storySatistics.setUpdateTime(System.currentTimeMillis());
                        storySatisticsMapper.updateStorySatistics(storySatistics);
                    }
                } else {
                    storySatistics.setId(UUID.randomUUID().toString());
                    storySatistics.setStoryId(id);
                    storySatistics.setShortId(shortId);
                    storySatistics.setStoryName(name);
                    storySatistics.setStatusAlias(statusAlias);
                    storySatistics.setStatus(status);
                    if ("testing".equals(status)) {
                        storySatistics.setStatusSort("6");
                    } else if ("status_3".equals(status)||"status_5".equals(status)||"status_4".equals(status)) {
                        storySatistics.setStatusSort("5");
                    }else if ("status_8".equals(status)) {
                        storySatistics.setStatusSort("4");
                    }else if ("status_9".equals(status)) {
                        storySatistics.setStatusSort("3");
                    }else if ("status_10".equals(status)) {
                        storySatistics.setStatusSort("2");
                    }else if ("status_11".equals(status)) {
                        storySatistics.setStatusSort("1");
                    }else {
                        storySatistics.setStatusSort("-1000");
                    }
                    storySatistics.setOwner(owner);
                    storySatistics.setReleasePlan(releasePlan);
                    storySatistics.setStoryUrl(storyUrl1);
                    storySatistics.setDeleted("0");
                    storySatistics.setWorkspaceId(workspace_id);
                    storySatistics.setCreateTime(System.currentTimeMillis());
                    storySatistics.setUpdateTime(System.currentTimeMillis());
                    storySatisticsMapper.saveStorySatistics(storySatistics);
                }
            }
        }

        //查询出最新的需求内容
        List<StorySatistics> allStorySatistics1 = storySatisticsMapper.getAllStorySatistics();

        Collections.sort(allStorySatistics1, new Comparator<StorySatistics>() {
            @Override
            public int compare(StorySatistics o1, StorySatistics o2) {
                return Integer.parseInt(o1.getStatusSort())-Integer.parseInt(o2.getStatusSort());
            }
        });
        Map<String, Integer> map = TapdUtils.countOccurrence(allStorySatistics1);

        StringBuilder sbAll = new StringBuilder();
//        sbAll.append("# --------------本周需求进度--------------- \n\n");
        sbAll.append("@所有人，本周需发布的需求，截止今天的测试情况：总共"+allStorySatistics1.size()+"个需求,其中：");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {

            if ("1".equals(entry.getKey())) {
                sbAll.append("预发布集成测试通过 " + map.get(entry.getKey()) + " 个 ");
            }else if ("2".equals(entry.getKey())) {
                sbAll.append("预发布集成测试 " + map.get(entry.getKey()) + " 个 ");
            }else if ("3".equals(entry.getKey())) {
                sbAll.append("集成测试通过 " + map.get(entry.getKey()) + " 个 ");
            }else if ("4".equals(entry.getKey())) {
                sbAll.append("集成测试 " + map.get(entry.getKey()) + " 个 ");
            }else if ("5".equals(entry.getKey())) {
                sbAll.append("测试通过 " + map.get(entry.getKey()) + " 个 ");
            }else if ("6".equals(entry.getKey())) {
                sbAll.append("测试中 " + map.get(entry.getKey()) + " 个 ");
            }

        }
        sbAll.append("\n");
        boolean added_status_11 = false;
        boolean added_status_10 = false;
        boolean added_status_9 = false;
        boolean added_status_8 = false;
        boolean added_status_5 = false;
        boolean added_testing = false;
//        sbAll.append(" 预发布集成测试通过： \n");
        for (int i = 0; i < allStorySatistics1.size(); i++) {
            StorySatistics storySatistics = allStorySatistics1.get(i);
            String status = storySatistics.getStatus();
            if ("status_11".equals(status)) {
                if (!added_status_11) {
                    sbAll.append(" 预发布集成测试通过： \n");
                    added_status_11 = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            } else if ("status_10".equals(status)) {
                if (!added_status_10) {
                    sbAll.append(" 预发布集成测试： \n");
                    added_status_10 = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            } else if ("status_9".equals(status)) {
                if (!added_status_9) {
                    sbAll.append(" 集成测试通过： \n");
                    added_status_9 = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            }else if ("status_8".equals(status)) {
                if (!added_status_8) {
                    sbAll.append(" 集成测试： \n");
                    added_status_8 = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            }else if ("status_3".equals(status)||"status_4".equals(status)||"status_5".equals(status)) {
                if (!added_status_5) {
                    sbAll.append(" 测试通过： \n");
                    added_status_5 = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            }else if ("testing".equals(status)) {
                if (!added_testing) {
                    sbAll.append(" 产品测试中： \n");
                    added_testing = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            }
        }

        String s1 = sbAll.toString();
        System.out.println(s1);
        return s1;
    }

    public void AutoRemindRequireProcedureNew() throws InterruptedException {
        /**
         * 1、获取cookie
         * 2、生成最新的数据库记录
         * 3、调用数据库中排序，获取第一个人的数据
         * 4、获取后，执行定时任务，发送数据库的任务进度和对应的测试人员
         * 5、修改最新的数据
         * **/
        //查询出所有为删除的需求id，并拼接成字符串
        StringBuilder sb = new StringBuilder();
        List<StorySatistics> allStorySatistics = storySatisticsMapper.getAllStorySatistics();
        for (StorySatistics allStorySatistic : allStorySatistics) {
            sb.append(allStorySatistic.getShortId()+" ");
        }
        String s = sb.toString();
        // 检查已经上线的，并将状态置为 deleted=1；
        String workspace_id = "22274921";
        String storyUrlHigh = "https://www.tapd.cn/api/search_filter/search_filter/search";
        tapdHeaderDTO cookieHigh = TapdUtils.getCookie();
        //        #################headers的获取############
        Map<String, String> headersHigh = new HashMap<String, String>();
        headersHigh.put("cookie", cookieHigh.getTapdCookie());
//        headersHigh.put("cookie", "tapdsession=169418957579e1b8fee32d46a3f305bfc26ed9df11ac8eec62dc5e13afbefa94962f884685");
        headersHigh.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
        if (!"".equals(s)) {
            JSONObject highStoriesInterfaceBodyResolved = TapdUtils.getHighStoriesInterfaceBodyResolved(workspace_id, s);
            // 检查 是否已上线的请求
            String storiesDataHighResolved = post(new OkHttpClient(), storyUrlHigh, highStoriesInterfaceBodyResolved, Headers.of(headersHigh));
            JSONObject jsonObjectResolved = JSONObject.parseObject(storiesDataHighResolved);
            JSONObject dataObjectResolved = jsonObjectResolved.getJSONObject("data");
            JSONArray storiesListResolved = dataObjectResolved.getJSONArray("list");
            if (storiesListResolved.size() > 0) {
                for (int i = 0; i < storiesListResolved.size(); i++) {
                    JSONObject story = storiesListResolved.getJSONObject(i);
                    String shortId = story.getString("short_id");
                    StorySatistics storySatistics = new StorySatistics();
                    String id = story.getString("id");
                    String statusAlias = story.getString("status_alias");
                    String status = story.getString("status");
                    String owner = story.getString("owner");
                    JSONObject releaseInfo = story.getJSONObject("release_info");
                    String releasePlan;
                    if (releaseInfo != null) {
                        releasePlan = releaseInfo.getString("name");
                    } else {
                        releasePlan = "暂无发布计划";
                    }

                    storySatistics.setStoryId(id);
                    storySatistics.setStatusAlias(statusAlias);
                    storySatistics.setStatus(status);
                    storySatistics.setOwner(owner);
                    storySatistics.setReleasePlan(releasePlan);
                    storySatistics.setUpdateTime(System.currentTimeMillis());
                    storySatisticsMapper.updateStorySatistics(storySatistics);
                    storySatisticsMapper.deleteStorySatistics(shortId);
                }
            }
        }
        List<String> statusListHigh = new ArrayList<String>();
        //        #################body的构造############
        statusListHigh.add("产品测试中");
        statusListHigh.add("测试通过");
        statusListHigh.add("Coding Review");
        statusListHigh.add("产品已实现");
        statusListHigh.add("集成测试");
        statusListHigh.add("集成测试通过");
        statusListHigh.add("预发布集成测试");
        statusListHigh.add("预发布集成测试通过");
//        statusListHigh.add("产品已上线");
        JSONObject highStoriesInterfaceBody = TapdUtils.getHighStoriesInterfaceBody(workspace_id, statusListHigh);
        String storiesDataHigh = post(new OkHttpClient(), storyUrlHigh, highStoriesInterfaceBody, Headers.of(headersHigh));
//        System.out.println("storiesData:"+storiesDataHigh);
        //        #################数据的解析############
        JSONObject jsonObject1 = JSONObject.parseObject(storiesDataHigh);
        JSONObject dataObject1 = jsonObject1.getJSONObject("data");
        JSONArray storiesList = dataObject1.getJSONArray("list");
        if (storiesList.size() > 0) {
            for (int i = 0; i < storiesList.size(); i++) {
                StorySatistics storySatistics = new StorySatistics();
                JSONObject story = storiesList.getJSONObject(i);
                String id = story.getString("id");
                String name = story.getString("name");
                String shortId = story.getString("short_id");
                String statusAlias = story.getString("status_alias");
                String status = story.getString("status");
                String owner = story.getString("owner");
                JSONObject releaseInfo = story.getJSONObject("release_info");
                String releasePlan;
                if (releaseInfo != null) {
                    releasePlan = releaseInfo.getString("name");
                } else {
                    releasePlan = "暂无发布计划";
                }

                String storyUrl1= "https://www.tapd.cn/"+"22274921/prong/stories/view/"+id;

                System.out.println("------------------------------------------------------");
                System.out.println("id:"+id);
                System.out.println("name:"+name);
                System.out.println("shortId:"+shortId);
                System.out.println("statusAlias:"+statusAlias);
                if ("测试通过".equals(statusAlias) || "Coding Review".equals(statusAlias) || "产品已实现".equals(statusAlias)) {
                    statusAlias="测试通过";
                }
                System.out.println("status:"+status);
                System.out.println("owner:"+owner);
                System.out.println("releasePlan:"+releasePlan);
                System.out.println("storyUrl:"+storyUrl1);
                StorySatistics storySatisticsByStoryId = storySatisticsMapper.getStorySatisticsByStoryId(id);
                if (storySatisticsByStoryId != null ) {
                    if (!status.equals(storySatisticsByStoryId.getStatus())) {
                        storySatistics.setStoryId(id);
                        storySatistics.setStatusAlias(statusAlias);
                        storySatistics.setStatus(status);
                        storySatistics.setOwner(owner);
                        storySatistics.setReleasePlan(releasePlan);
                        storySatistics.setUpdateTime(System.currentTimeMillis());
                        storySatisticsMapper.updateStorySatistics(storySatistics);
                    }
                } else {
                    storySatistics.setId(UUID.randomUUID().toString());
                    storySatistics.setStoryId(id);
                    storySatistics.setShortId(shortId);
                    storySatistics.setStoryName(name);
                    storySatistics.setStatusAlias(statusAlias);
                    storySatistics.setStatus(status);
                    storySatistics.setOwner(owner);
                    storySatistics.setReleasePlan(releasePlan);
                    storySatistics.setStoryUrl(storyUrl1);
                    storySatistics.setDeleted("0");
                    storySatistics.setWorkspaceId(workspace_id);
                    storySatistics.setCreateTime(System.currentTimeMillis());
                    storySatistics.setUpdateTime(System.currentTimeMillis());
                    storySatisticsMapper.saveStorySatistics(storySatistics);
                }
            }
        }

        //查询出最新的需求内容
        List<StorySatistics> allStorySatistics1 = storySatisticsMapper.getAllStorySatistics();
        Map<String, Integer> map = TapdUtils.countOccurrence(allStorySatistics1);

        StringBuilder sbAll = new StringBuilder();
        sbAll.append("# --------------本周需求进度--------------- \n\n");
        sbAll.append("## @所有人，本周需发布的需求，截止今天的测试情况：总共"+allStorySatistics1.size()+"个需求,其中：");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            sbAll.append(entry.getKey()+" <font color=\\\"warning\\\">" + entry.getValue() + "</font>个 \t");
        }
        sbAll.append("\n");
        for (int i = 0; i < allStorySatistics1.size(); i++) {
            StorySatistics storySatistics = allStorySatistics1.get(i);
            sbAll.append(i+1 + "、[" + storySatistics.getShortId() + "](" + storySatistics.getStoryUrl() + ")_" + storySatistics.getStoryName() + "(" + storySatistics.getStatusAlias() + ")" + "  处理人：" + storySatistics.getOwner() + " "
                    + storySatistics.getReleasePlan()+"\n");
        }
        sbAll.append("本周跟进人:" + "周泽强 @周泽强" + "请及时跟进需求进度！");


        String s1 = sbAll.toString();
        System.out.println(s1);
//        sendNotification(s1);

    }

    public void sendNotification(String message){

        String webhook_url = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=a1faa6a3-9481-40d7-bdaa-e29f78114af5";
        String body = "{\n" +
                "  \"msgtype\": \"markdown\",\n" +
                "  \"markdown\": {\n" +
                "    \"content\":" +"\""+ message +"\""+
                "}}";
        System.out.println(body);
        HttpUtil.post(webhook_url, body);
    }

//    public String getName(String code) {
//        List<TesterRank> thur = testerRankMapper.getTesterRankByCode(code);
////        System.out.println(thur);
//        if (thur.size() > 0) {
//            TesterRank testerRank = thur.get(0);
//            String name = testerRank.getName();
//            testerRankMapper.updateTesterRank(testerRank);
//            return name;
//
//        } else {
//            TesterRank testerRank = new TesterRank();
//            testerRank.setCode(code);
//            testerRankMapper.updateTesterRankUnfinished(testerRank);
//            List<TesterRank> thur1 = testerRankMapper.getTesterRankByCode(code);
//            TesterRank testerRank1 = thur1.get(0);
//            String name = testerRank1.getName();
//            testerRankMapper.updateTesterRank(testerRank1);
//            return name;
//        }
//    }
}
