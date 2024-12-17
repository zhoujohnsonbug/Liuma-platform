package com.autotest.LiuMa;

import com.alibaba.fastjson.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpUtils {

    public static String sendGet(String url, Map<String, String> headers) throws Exception {
        // 创建请求连接
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // 设置请求方式为GET
        con.setRequestMethod("GET");

        // 添加请求头
        if (headers != null && headers.size() > 0) {
            for (String key : headers.keySet()) {
                con.setRequestProperty(key, headers.get(key));
            }
        }

        // 发送GET请求
        int responseCode = con.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) { //成功
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } else {
            throw new Exception("请求失败，响应码：" + responseCode);
        }

    }

    public static String sendPost(String url, Map<String, String> headers, String body) throws Exception {
        // 创建请求连接
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // 设置请求方式为POST
        con.setRequestMethod("POST");

        // 添加请求头
        if (headers != null && headers.size() > 0) {
            for (String key : headers.keySet()) {
                con.setRequestProperty(key, headers.get(key));
            }
        }

        // 发送POST请求
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(body);
        wr.flush();
        wr.close();

        // 处理响应结果
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            throw new Exception("请求失败，响应码：" + responseCode);
        }
    }

    public static void main(String[] args) {
//        String url = "https://www.tapd.cn/api/aggregation/user_and_workspace_aggregation/get_user_and_workspace_basic_info?workspace_id=0&location=/my_worktable/index/done";
//        Map<String, String> headers = new HashMap<String, String>();
//        headers.put("Cookie", "tapdsession=16937895043e21c8cef96325391b63d02aeb95e402dd4e791807fd560578763712903e034c;");
//        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
//
//        try {
//            String response = sendGet(url, headers);
//            System.out.println(response);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        List aList = new ArrayList();
        aList.add("a");
        aList.add("b");
        JSONArray objects = new JSONArray();
        objects.addAll(aList);
        System.out.println(objects.toJSONString());
    }
}
