package com.autotest.LiuMa.job;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.autotest.LiuMa.common.utils.NameOrderUtils;
import com.autotest.LiuMa.common.utils.SpringUtil;
import com.autotest.LiuMa.common.utils.TapdUtils;
import com.autotest.LiuMa.database.domain.StorySatistics;
import com.autotest.LiuMa.database.domain.TesterOrder;
import com.autotest.LiuMa.database.mapper.StorySatisticsMapper;
import com.autotest.LiuMa.database.mapper.TesterOrderMapper;
import com.autotest.LiuMa.dto.tapdHeaderDTO;
import com.autotest.LiuMa.service.QuartzJob.BaseJob;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.autotest.LiuMa.common.utils.OkHttpUtil.post;


@Component("wjxNotificationJob")
public class WjxNotificationJob implements BaseJob {

//    @Autowired
//    private StorySatisticsMapper storySatisticsMapper;
//
//    @Autowired
//    private TesterOrderMapper testerOrderMapper;
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            String group = context.getTrigger().getKey().getGroup();
            AutoRemindRequireProcedureNew(group);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public void AutoRemindRequireProcedureNew(String jobId) throws InterruptedException {
        /**
         * 1、获取cookie
         * 2、生成最新的数据库记录
         * 3、调用数据库中排序，获取第一个人的数据
         * 4、获取后，执行定时任务，发送数据库的任务进度和对应的测试人员
         * 5、修改最新的数据
         * **/
        //查询出所有为删除的需求id，并拼接成字符串
        StringBuilder sb = new StringBuilder();
        StorySatisticsMapper storySatisticsMapper = SpringUtil.getBean(StorySatisticsMapper.class);
        TesterOrderMapper testerOrderMapper = SpringUtil.getBean(TesterOrderMapper.class);
        NameOrderUtils nameOrderUtils = SpringUtil.getBean(NameOrderUtils.class);
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
//        sbAll.append("## @所有人，本周需发布的需求，截止今天的测试情况：总共"+allStorySatistics1.size()+"个需求,其中：");
//        for (Map.Entry<String, Integer> entry : map.entrySet()) {
//            sbAll.append(entry.getKey()+" <font color=\\\"warning\\\">" + entry.getValue() + "</font>个 \t");
//        }
//        sbAll.append("\n");
//        for (int i = 0; i < allStorySatistics1.size(); i++) {
//            StorySatistics storySatistics = allStorySatistics1.get(i);
//            sbAll.append(i+1 + "、[" + storySatistics.getShortId() + "](" + storySatistics.getStoryUrl() + ")_" + storySatistics.getStoryName() + "(" + storySatistics.getStatusAlias() + ")" + "  处理人：" + storySatistics.getOwner() + " "
//                    + storySatistics.getReleasePlan()+"\n");
//        }
//        TesterOrder testerOrderById = testerOrderMapper.getTesterOrderByJobId(jobId);
//        String firstString = nameOrderUtils.getFirstString(testerOrderById.getRanker());
//        sbAll.append("本周跟进人:" + firstString+" @"+firstString + "请及时跟进需求进度！");
//
//
//        String s1 = sbAll.toString();
//        System.out.println(s1);
        sbAll.append("@所有人，本周需发布的需求，截止今天的测试情况：总共"+allStorySatistics1.size()+"个需求,其中：");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {

            if ("1".equals(entry.getKey())) {
                sbAll.append("预发布集成测试通过 " + "<font color=\\\"info\\\">" + map.get(entry.getKey()) + "</font> 个 ");
            }else if ("2".equals(entry.getKey())) {
                sbAll.append("预发布集成测试 " + "<font color=\\\"warning\\\">" + map.get(entry.getKey()) + "</font> 个 ");
            }else if ("3".equals(entry.getKey())) {
                sbAll.append("集成测试通过 " + "<font color=\\\"warning\\\">" + map.get(entry.getKey()) + "</font> 个 ");
            }else if ("4".equals(entry.getKey())) {
                sbAll.append("集成测试 " + "<font color=\\\"warning\\\">" + map.get(entry.getKey()) + "</font> 个 ");
            }else if ("5".equals(entry.getKey())) {
                sbAll.append("测试通过 " + "<font color=\\\"warning\\\">" + map.get(entry.getKey()) + "</font> 个 ");
            }else if ("6".equals(entry.getKey())) {
                sbAll.append("测试中 " + "<font color=\\\"warning\\\">" + map.get(entry.getKey()) + "</font> 个 ");
            }

        }
        sbAll.append("\n");
        boolean added_status_11 = false;
        boolean added_status_10 = false;
        boolean added_status_9 = false;
        boolean added_status_8 = false;
        boolean added_status_5 = false;
        boolean added_testing = false;
//        sbAll.append("[太阳] 预发布集成测试通过： \n");
        for (int i = 0; i < allStorySatistics1.size(); i++) {
            StorySatistics storySatistics = allStorySatistics1.get(i);
            String status = storySatistics.getStatus();
            if ("status_11".equals(status)) {
                if (!added_status_11) {
                    sbAll.append("<font color=\\\"info\\\">--预发布集成测试通过--</font> \n");
                    added_status_11 = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            } else if ("status_10".equals(status)) {
                if (!added_status_10) {
                    sbAll.append("<font color=\\\"warning\\\">--预发布集成测试--</font> \n");
                    added_status_10 = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            } else if ("status_9".equals(status)) {
                if (!added_status_9) {
                    sbAll.append("<font color=\\\"warning\\\">--集成测试通过--</font> \n");
                    added_status_9 = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            }else if ("status_8".equals(status)) {
                if (!added_status_8) {
                    sbAll.append("<font color=\\\"warning\\\">--集成测试--</font> \n");
                    added_status_8 = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            }else if ("status_3".equals(status)||"status_4".equals(status)||"status_5".equals(status)) {
                if (!added_status_5) {
                    sbAll.append("<font color=\\\"warning\\\">--测试通过--</font> \n");
                    added_status_5 = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            }else if ("testing".equals(status)) {
                if (!added_testing) {
                    sbAll.append("<font color=\\\"warning\\\">--测试中--</font> \n");
                    added_testing = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            }
        }
        TesterOrder testerOrderById = testerOrderMapper.getTesterOrderByJobId(jobId);
        String firstString = nameOrderUtils.getFirstString(testerOrderById.getRanker());
        sbAll.append("本周跟进人:" + firstString+" @"+firstString + "请及时跟进需求进度！");
        sbAll.append("如需同步最新的进度，可访问测试平台："+"[手工生成通知](http://101.37.26.140:8888/#/testManage/testerNotification)");
        String s1 = sbAll.toString();
        System.out.println(s1);
        sendNotification(s1);

    }

    public void sendNotification(String message){

        String webhook_url = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=a1faa6a3-9481-40d7-bdaa-e29f78114af5";
        String body = "{\n" +
                "  \"msgtype\": \"markdown\",\n" +
                "  \"markdown\": {\n" +
                "    \"content\":" +"\""+ message +"\""+
                "}}";
//        System.out.println(body);
        HttpUtil.post(webhook_url, body);
    }

    public WjxNotificationJob() {}
}
