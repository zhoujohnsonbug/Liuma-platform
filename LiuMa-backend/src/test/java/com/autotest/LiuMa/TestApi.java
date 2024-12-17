package com.autotest.LiuMa;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.autotest.LiuMa.common.utils.CacheUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.scheduling.annotation.Scheduled;

import javax.naming.Name;
import java.io.PrintWriter;
import java.util.*;

public class TestApi {


    @Scheduled(cron = "5 * * * * ?")
    public static void main(String[] args) throws InterruptedException {
        List<String> names = new ArrayList<>(Arrays.asList("蔡娇","杨姗","蒋伟明", "周泽强", "陈威"));
        testApi(names);
    }

    public static void testApi(List<String> names) throws InterruptedException {
        Map<String, String> nameMap = new HashMap<>();

        List<String> strings = Arrays.asList("testing","status_5","status_8", "status_9", "status_10", "status_11");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("# --------------本周需求进度--------------- \n");

        for (int i = 0; i < strings.size(); i++) {
            if ("testing".equals(strings.get(i))) {
                stringBuilder.append("## 产品测试中 ");
            } else if ("status_5".equals(strings.get(i))) {
                stringBuilder.append("## 测试通过 ");
            }else if ("status_8".equals(strings.get(i))) {
                stringBuilder.append("## 集成测试 ");
            }else if ("status_9".equals(strings.get(i))) {
                stringBuilder.append("## <font color=\"warning\">集成测试通过 </font>");
            }else if ("status_10".equals(strings.get(i))) {
                stringBuilder.append("## <font color=\"warning\">预发布测试 </font>");
            }else if ("status_11".equals(strings.get(i))) {
                stringBuilder.append("## <font color=\"info\">预发布测试通过 </font>");
            }
            String url = "https://api.tapd.cn/stories?workspace_id=22274921&status="+ strings.get(i);
            String release_url = "https://api.tapd.cn/releases?workspace_id=22274921&id=";
            String api_user = "s8vyzbK7";
            String api_password = "79A09465-341B-205A-84C4-5E0857587D7B";
            String body = HttpRequest.get(url).basicAuth(api_user, api_password).execute().body();
            Thread.currentThread().sleep(1000);
            JsonParser jsonParser = new JsonParser();
            JsonObject parse = (JsonObject) jsonParser.parse(body);
            JsonArray data = parse.get("data").getAsJsonArray();

            stringBuilder.append("<font color=\"warning\">" + data.size() + "</font>个 \n");

            for (int j = 0; j < data.size(); j++) {
                String id = data.get(j).getAsJsonObject().get("Story").getAsJsonObject().get("id").toString().replace("\"","");
                String short_id = id.substring(id.length() - 7);
                String name = data.get(j).getAsJsonObject().get("Story").getAsJsonObject().get("name").toString().replace("\"","");
                String owner = data.get(j).getAsJsonObject().get("Story").getAsJsonObject().get("owner").toString().replace("\"","");
                String release_id = data.get(j).getAsJsonObject().get("Story").getAsJsonObject().get("release_id").toString().replace("\"","");
                String release_body = HttpRequest.get(release_url+release_id).basicAuth(api_user, api_password).execute().body();
//            System.out.println(release_body);
                JsonParser jsonParser1 = new JsonParser();
                JsonObject parse1 = (JsonObject) jsonParser1.parse(release_body);
                JsonArray data1 = parse1.get("data").getAsJsonArray();
//            System.out.println(data1.get(0).getAsJsonObject().get("Release").getAsJsonObject().get("name"));
                String release_name = data1.get(0).getAsJsonObject().get("Release").getAsJsonObject().get("name").toString().replace("\"","");

                if (data.size() != 0) {
                    stringBuilder.append(">"+String.valueOf(j+1)+"、 ["+short_id + "]"+"(https://www.tapd.cn/22274921/prong/stories/view/"+id+")"+"_");
                    stringBuilder.append(name + " ");
                    stringBuilder.append("    "+release_name + " \n");
                    stringBuilder.append("    处理人："+owner + " \n");
                }
//                System.out.println(short_id);
//                System.out.println(data.get(j).getAsJsonObject().get("Story").getAsJsonObject().get("name"));
//                System.out.println(data.get(j).getAsJsonObject().get("Story").getAsJsonObject().get("owner"));
            }
            stringBuilder.append("\n");
        }

//
//    List<String> names = (List<String>) CacheUtils.get("name");
        if (names.size() > 0) {
            String s = names.get(0);
            stringBuilder.append("本周跟进人：" + s + "@" + s + "，请在周五下班前同步进度！\n");
            names.remove(names.get(0));
        } else {
            names.addAll(Arrays.asList("蔡娇", "杨姗", "蒋伟明", "周泽强", "陈威"));
            String s = names.get(0);
            stringBuilder.append("本周跟进人：" + s + "@" + s + "，请在周五下班前同步进度！\n");
            names.remove(names.get(0));
        }

//    stringBuilder.append("本周跟进人：蔡娇@蔡娇，请在周五下班前同步进度！");
        System.out.println(stringBuilder.toString());

    }
}
