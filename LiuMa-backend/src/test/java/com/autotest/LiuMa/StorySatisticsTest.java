package com.autotest.LiuMa;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.autotest.LiuMa.common.utils.TapdUtils;
import com.autotest.LiuMa.database.domain.StorySatistics;
import com.autotest.LiuMa.database.mapper.StorySatisticsMapper;
import com.autotest.LiuMa.dto.tapdHeaderDTO;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static com.autotest.LiuMa.common.utils.OkHttpUtil.post;

@SpringBootTest
@RunWith(SpringRunner.class)
public class StorySatisticsTest {

    @Autowired
    StorySatisticsMapper storySatisticsMapper;


    @org.junit.Test
    public void saveStorySatistics1() throws InterruptedException {

        //查询出所有为删除的需求id，并拼接成字符串
        StringBuilder sb = new StringBuilder();
        List<StorySatistics> allStorySatistics = storySatisticsMapper.getAllStorySatistics();
        for (StorySatistics allStorySatistic : allStorySatistics) {
            sb.append(allStorySatistic.getShortId()+" ");
        }
        String s = sb.toString();
        String substring = s.trim().substring(0, s.length() - 1);

        // 检查已经上线的，并将状态置为 deleted=1；
        String workspace_id = "22274921";
        JSONObject highStoriesInterfaceBodyResolved = TapdUtils.getHighStoriesInterfaceBodyResolved(workspace_id, substring);

        String storyUrlHigh="https://www.tapd.cn/api/search_filter/search_filter/search";
        //        #################headers的获取############
        Map<String, String> headersHigh = new HashMap<String, String>();
        tapdHeaderDTO cookieHigh = TapdUtils.getCookie();
        headersHigh.put("cookie", cookieHigh.getTapdCookie());
        headersHigh.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");

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
    }




    @org.junit.Test
    public void queryStorySatistics() {
        StorySatistics storySatisticsByStoryId = storySatisticsMapper.getStorySatisticsByStoryId("1234");
        System.out.println(storySatisticsByStoryId);
        StorySatistics storySatisticsByStoryId1 = storySatisticsMapper.getStorySatisticsByStoryId("1122274921001005083");
        System.out.println(storySatisticsByStoryId1);
    }


    @org.junit.Test
    public void queryStorySatistics1() {
        StringBuilder sb = new StringBuilder();
        List<StorySatistics> allStorySatistics = storySatisticsMapper.getAllStorySatistics();
        for (StorySatistics allStorySatistic : allStorySatistics) {
            sb.append(allStorySatistic.getShortId()+" ");
        }
        String s = sb.toString();
        String substring = s.trim().substring(0, s.length() - 1);

    }


}
