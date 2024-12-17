package com.autotest.LiuMa.common.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.autotest.LiuMa.database.domain.StorySatistics;
import com.autotest.LiuMa.dto.tapdHeaderDTO;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.*;


public class TapdUtils {

    public static tapdHeaderDTO getCookie() throws InterruptedException {
        //如果将chromedriver.exe所在的当前路径添加到了path则不需要在此设置，如果无效可以重启idea再次尝试。
        //System.setProperty("webdriver.chrome.drive","C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
        //开启会话，即打开浏览器
        String url = "https://www.tapd.cn/cloud_logins/login?source=button";

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless").addArguments("--disable-blink-features=AutomationControlled").addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.79 Safari/537.36").addArguments("--no-sandbox").addArguments("--disable-dev-shm-usage");
        ChromeDriver chromeDriver = new ChromeDriver(options);
        chromeDriver.get(url);
        Thread.sleep(2000);
        chromeDriver.findElementById("username").sendKeys("17688912727");
        chromeDriver.findElementById("password_input").sendKeys("Johnson123.");
        chromeDriver.findElementById("protocol-checkbox").click();
        chromeDriver.findElementById("tcloud_login_button").click();
        //程序暂停4秒
        Thread.sleep(4000);
        WebDriver.Options manage = chromeDriver.manage();
        Set<Cookie> cookies = manage.getCookies();
        tapdHeaderDTO tapdHeaderDTO = new tapdHeaderDTO();
        for (Cookie cookie : cookies) {
            System.out.println(cookie.getName()+":"+cookie.getValue());
            if("tapdsession".equals(cookie.getName())){
                tapdHeaderDTO.setTapdCookie(cookie.getName()+"="+cookie.getValue());
//                return cookie.getName()+"="+cookie.getValue();
//                System.out.println(cookie.getName()+"="+cookie.getValue());
            }
            if ("dsc-token".equals(cookie.getName())) {
                tapdHeaderDTO.setDscToken(cookie.getValue());
            }
        }
        System.out.println(tapdHeaderDTO.toString());
        //结束会话，关闭浏览器
        chromeDriver.quit();
        return tapdHeaderDTO;
    }

    public static JSONObject getStoriesInterfaceBody(String workspace_id,String query_token, List<String> statusStories,String dsc_token) {


            // 原始json字符串
            String jsonStr = "{\"exclude_workspace_configs\":[],\"workspace_id\":\"22274921\",\"category_id\":\"\",\"query_token\":\"debf7d323240ff519647f2a45f1c7ddc\",\"list_type\":\"tree\",\"conf_id\":\"1122274921001006273\",\"confIdType\":\"URL\",\"sort_name\":\"\",\"order\":\"\",\"perpage\":50,\"page\":1,\"need_category_counts\":1,\"entity_types\":[\"story\"],\"selected_workspace_ids\":[],\"location\":\"/prong/stories/stories_list\",\"menu_workitem_type_id\":\"\",\"identifier\":\"app_for_list_tools\",\"hide_not_match_condition_sub_node\":1,\"useScene\":\"storyList\",\"filter_data\":{\"filter_expr\":{\"data\":[{\"fieldOption\":\"like\",\"fieldType\":\"input\",\"entity\":\"story\",\"fieldSystemName\":\"name\",\"fieldDisplayName\":\"标题\",\"selectOption\":[],\"value\":\"\",\"fieldIsSystem\":\"1\"},{\"fieldOption\":\"in\",\"fieldType\":\"multi_select\",\"entity\":\"story\",\"fieldSystemName\":\"status\",\"fieldDisplayName\":\"状态\",\"selectOption\":[],\"value\":[\"测试通过\"],\"fieldIsSystem\":\"1\"},{\"fieldOption\":\"in\",\"fieldType\":\"user_chooser\",\"entity\":\"story\",\"fieldSystemName\":\"owner\",\"fieldDisplayName\":\"处理人\",\"selectOption\":[],\"value\":\"\",\"fieldIsSystem\":\"1\"},{\"fieldOption\":\"in\",\"fieldType\":\"select\",\"entity\":\"story\",\"fieldSystemName\":\"iteration_id\",\"fieldDisplayName\":\"迭代\",\"selectOption\":[],\"value\":[],\"fieldIsSystem\":\"1\"},{\"fieldOption\":\"in\",\"fieldType\":\"select\",\"entity\":\"story\",\"fieldSystemName\":\"priority\",\"fieldDisplayName\":\"优先级\",\"selectOption\":[],\"value\":[],\"fieldIsSystem\":\"1\"}],\"optionType\":\"AND\",\"needInit\":true},\"filter_type\":\"base\"},\"dsc_token\":\"2bxpEKPIsEVSDT9Y\"}";

            // 解析json字符串为JSONObject
            JSONObject json = JSONObject.parseObject(jsonStr);

            json.put("workspace_id", workspace_id);
            json.put("query_token", query_token);

            json.put("dsc_token", dsc_token);

            // 从JSONObject中获取filter_data对象
            JSONObject filterData = json.getJSONObject("filter_data");

            // 从filter_data对象中获取filter_expr对象
            JSONObject filterExpr = filterData.getJSONObject("filter_expr");

            // 从filter_expr对象中获取data数组
            JSONArray dataArray = filterExpr.getJSONArray("data");

            // 遍历data数组，找到"value": ["产品测试中"]这一行并修改值
            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject fieldObj = dataArray.getJSONObject(i);
                if (fieldObj.getString("fieldDisplayName").equals("状态")) {
                    if (statusStories.size() > 0) {
                        JSONArray objects = new JSONArray();
                        objects.addAll(statusStories);
                        fieldObj.put("value", objects);
                    }
                    break;
                }
            }

            // 将修改后的dataArray放回filter_expr对象中
            filterExpr.put("data", dataArray);

            // 将修改后的filter_expr对象放回filter_data对象中
            filterData.put("filter_expr", filterExpr);

            // 将修改后的filter_data对象放回JSONObject中
            json.put("filter_data", filterData);

            // 打印修改后的json字符串
            return json;
        }
    public static JSONObject getHighStoriesInterfaceBody(String workspace_id, List<String> statusStories) {

        String partJsonStr= "{\"data\": [{\"id\": \"9\",\"fieldLabel\": \"状态\",\"fieldOption\": \"in\",\"fieldType\": \"multi_select\",\"fieldSystemName\": \"status\",\"value\": [\"产品测试中\", \"测试通过\", \"Coding Review\", \"产品已实现\", \"集成测试\", \"集成测试通过\", \"预发布集成测试\", \"预发布集成测试通过\"],\"fieldIsSystem\": \"1\",\"entity\": \"story\"}],\"optionType\": \"AND\",\"needInit\": \"false\"}";
        // 原始json字符串

        String JsonStr = "{\"workspace_ids\":\"22274921\",\"search_data\":\"{\\\"data\\\":[{\\\"fieldDisplayName\\\":\\\"标题\\\",\\\"fieldIsSystem\\\":\\\"1\\\",\\\"fieldOption\\\":\\\"in\\\",\\\"fieldSystemName\\\":\\\"status\\\",\\\"fieldType\\\":\\\"multi_select\\\",\\\"selectOption\\\":[],\\\"value\\\":[\\\"产品测试中\\\",\\\"测试通过\\\"],\\\"entity\\\":\\\"story\\\",\\\"id\\\":\\\"1\\\",\\\"fieldLabel\\\":\\\"状态\\\"}],\\\"optionType\\\":\\\"AND\\\",\\\"needInit\\\":\\\"1\\\"}\",\"obj_type\":\"story\",\"search_type\":\"advanced\",\"page\":1,\"perpage\":\"50\",\"parallel_token\":\"\",\"order_field\":\"created\",\"order_value\":\"desc\",\"show_fields\":[],\"display_mode\":\"list\",\"version\":\"1.1.0\",\"only_gen_token\":0,\"exclude_workspace_configs\":[],\"dsc_token\":\"Thl9KMVZO96RmwsP\"}";
        // 解析json字符串为JSONObject
        JSONObject jsonPart = JSONObject.parseObject(partJsonStr);
        JSONObject json = JSONObject.parseObject(JsonStr);

        json.put("workspace_id", workspace_id);

//        json.put("dsc_token", dsc_token);


        // 从filter_expr对象中获取data数组
        JSONArray dataArrayPart = jsonPart.getJSONArray("data");

        // 遍历data数组，找到"value": ["产品测试中"]这一行并修改值
        for (int i = 0; i < dataArrayPart.size(); i++) {
            JSONObject fieldObj = dataArrayPart.getJSONObject(i);
            if (fieldObj.getString("fieldLabel").equals("状态")) {
                if (statusStories.size() > 0) {
                    JSONArray objects = new JSONArray();
                    objects.addAll(statusStories);
                    fieldObj.put("value", objects);
                }
                break;
            }
        }

        // 打印修改后的json字符串

        json.put("search_data", jsonPart.toJSONString());
        return json;
    }

    public static JSONObject getHighStoriesInterfaceBodyResolved(String workspace_id, String shortIds) {

        String partJsonStr= "{\"data\": [{\"id\": \"1\",\"fieldLabel\": \"状态\",\"fieldOption\": \"in\",\"fieldType\": \"multi_select\",\"fieldSystemName\": \"status\",\"value\": [\"产品已上线\"],\"fieldIsSystem\": \"1\",\"entity\": \"story\"},{\"id\": \"2\",\"fieldLabel\": \"ID\",\"fieldOption\": \"like\",\"fieldType\": \"input\",\"fieldSystemName\": \"id\",\"value\": \"1005114\",\"fieldIsSystem\": \"1\",\"entity\": \"story\"}],\"optionType\": \"AND\",\"needInit\": \"false\"}";
        // 原始json字符串

        String JsonStr = "{\"workspace_ids\":\"22274921\",\"search_data\":\"{\\\"data\\\":[{\\\"fieldDisplayName\\\":\\\"标题\\\",\\\"fieldIsSystem\\\":\\\"1\\\",\\\"fieldOption\\\":\\\"in\\\",\\\"fieldSystemName\\\":\\\"status\\\",\\\"fieldType\\\":\\\"multi_select\\\",\\\"selectOption\\\":[],\\\"value\\\":[\\\"产品测试中\\\",\\\"测试通过\\\"],\\\"entity\\\":\\\"story\\\",\\\"id\\\":\\\"1\\\",\\\"fieldLabel\\\":\\\"状态\\\"}],\\\"optionType\\\":\\\"AND\\\",\\\"needInit\\\":\\\"1\\\"}\",\"obj_type\":\"story\",\"search_type\":\"advanced\",\"page\":1,\"perpage\":\"50\",\"parallel_token\":\"\",\"order_field\":\"created\",\"order_value\":\"desc\",\"show_fields\":[],\"display_mode\":\"list\",\"version\":\"1.1.0\",\"only_gen_token\":0,\"exclude_workspace_configs\":[],\"dsc_token\":\"Thl9KMVZO96RmwsP\"}";
        // 解析json字符串为JSONObject
        JSONObject jsonPart = JSONObject.parseObject(partJsonStr);
        JSONObject json = JSONObject.parseObject(JsonStr);

        json.put("workspace_id", workspace_id);

//        json.put("dsc_token", dsc_token);


        // 从filter_expr对象中获取data数组
        JSONArray dataArrayPart = jsonPart.getJSONArray("data");

        // 遍历data数组，找到"value": ["产品测试中"]这一行并修改值
        for (int i = 0; i < dataArrayPart.size(); i++) {
            JSONObject fieldObj = dataArrayPart.getJSONObject(i);
            if (fieldObj.getString("fieldLabel").equals("ID")) {
                fieldObj.put("value", shortIds);
            }
            }


        // 打印修改后的json字符串

        json.put("search_data", jsonPart.toJSONString());
        return json;
    }

    public static JSONObject generateTokenFromArray(String workspace_id,List<String> statusStories,String dsc_token) {
        String jsonStr = "{\"workspace_id\":\"22274921\",\"data\":{\"filter_expr\":{\"data\":" +
                "[{\"fieldOption\":\"like\",\"fieldType\":\"input\",\"entity\":\"story\"," +
                "\"fieldSystemName\":\"name\",\"fieldDisplayName\":\"标题\",\"selectOption\":[]," +
                "\"value\":\"\",\"fieldIsSystem\":\"1\"},{\"fieldOption\":\"in\",\"fieldType\":" +
                "\"multi_select\",\"entity\":\"story\",\"fieldSystemName\":\"status\",\"fieldDisplayName\":" +
                "\"状态\",\"selectOption\":[],\"value\":[\"集成测试通过\"],\"fieldIsSystem\":\"1\"},{" +
                "\"fieldOption\":\"in\",\"fieldType\":\"user_chooser\",\"entity\":\"story\",\"fieldSystemName\":" +
                "\"owner\",\"fieldDisplayName\":\"处理人\",\"selectOption\":[],\"value\":\"\",\"fieldIsSystem\":" +
                "\"1\"},{\"fieldOption\":\"in\",\"fieldType\":\"select\",\"entity\":\"story\",\"fieldSystemName\":" +
                "\"iteration_id\",\"fieldDisplayName\":\"迭代\",\"selectOption\":[],\"value\":\"\",\"fieldIsSystem\":" +
                "\"1\"},{\"fieldOption\":\"in\",\"fieldType\":\"select\",\"entity\":\"story\",\"fieldSystemName\":" +
                "\"priority\",\"fieldDisplayName\":\"优先级\",\"selectOption\":[],\"value\":[],\"fieldIsSystem\":\"1\"}]," +
                "\"optionType\":\"AND\",\"needInit\":true},\"filter_type\":\"base\",\"selected_workspace_ids\":[]," +
                "\"show_fields\":[],\"hide_not_match_condition_node\":0,\"hide_not_match_condition_sub_node\":1}," +
                "\"dsc_token\":\"BKchB1JbdvulFftV\"}";

        // 解析json字符串为JSONObject
        JSONObject json = JSONObject.parseObject(jsonStr);

        // workspaceIdData对象
//        JSONObject workspaceIdData = json.getJSONObject("workspace_id");

        json.put("workspace_id", workspace_id);

        json.put("dsc_token", dsc_token);


        // 从JSONObject中获取filter_data对象
        JSONObject filterData = json.getJSONObject("data");

        // 从filter_data对象中获取filter_expr对象
        JSONObject filterExpr = filterData.getJSONObject("filter_expr");

        // 从filter_expr对象中获取data数组
        JSONArray dataArray = filterExpr.getJSONArray("data");

        // 遍历data数组，找到"value": ["产品测试中"]这一行并修改值
        for (int i = 0; i < dataArray.size(); i++) {
            JSONObject fieldObj = dataArray.getJSONObject(i);
            if (fieldObj.getString("fieldDisplayName").equals("状态")) {
                if (statusStories.size() > 0) {
                    JSONArray objects = new JSONArray();
                    objects.addAll(statusStories);
                    fieldObj.put("value", objects);
                }
                break;
            }
        }

        // 将修改后的dataArray放回filter_expr对象中
        filterExpr.put("data", dataArray);

        // 将修改后的filter_expr对象放回filter_data对象中
        filterData.put("filter_expr", filterExpr);

//        // 将修改后的filter_data对象放回JSONObject中
//        json.put("filter_data", filterData);

        // 打印修改后的json字符串
        return json;
    }

    public static Map<String, Integer> countOccurrence(List<StorySatistics> list) {
        Map<String, Integer> map = new HashMap<>();
        for (StorySatistics storySatistics : list) {
            Integer count = map.get(storySatistics.getStatusSort());
            if (count == null) {
                count = 0;
            }
            map.put(storySatistics.getStatusSort(), ++count);
        }
        return map;
    }

    public static void main(String[] args) throws InterruptedException {
//        tapdHeaderDTO cookie = getCookie();
        List<String> statusStories = new ArrayList<String>();
        statusStories.add("集成测试通过");
        statusStories.add("集成测试");

//        String s = generateTokenFromArray("12345678",statusStories,"12345678");
//        String s = getStoriesInterfaceBody("12345678","fdasfsdafds1fdasfasdf1fsadfasdf1",statusStories,"12345678");
//        System.out.println(s);

        String sss = "1005111 1005101 1005096 1005083 1005062 1005048 1004973 1004968 1004967 1005114 1005112 1005008 1005030 ";

//        String s = getHighStoriesInterfaceBody("123",  statusStories).toJSONString();
//        System.out.println(s);
        JSONObject highStoriesInterfaceBodyResolved = getHighStoriesInterfaceBodyResolved("22274921", sss);
        System.out.println(highStoriesInterfaceBodyResolved.toJSONString());

    }
}
