package com.autotest.LiuMa;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.autotest.LiuMa.common.utils.TapdUtils;
import com.autotest.LiuMa.dto.tapdHeaderDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
public class OkHttpUtil {

    public static final String MEDIA_TYPE_JSON = "application/json; charset=utf-8";

    private OkHttpUtil() {
    }

    /**
     * 获取默认的OkHttpClient
     * @return
     */
    public static OkHttpClient getOkHttpClient() {
        return getOkHttpClient(60, 60, 60);
    }

    public static OkHttpClient getOkHttpClient(int connectTimeout, int readTimeOut, int writeTimeOut) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.connectTimeout(connectTimeout, TimeUnit.SECONDS);
        builder.readTimeout(readTimeOut, TimeUnit.SECONDS);
        builder.writeTimeout(writeTimeOut, TimeUnit.SECONDS);
        return builder.build();
    }

    /**
     * get请求
     * @param okHttpClient
     * @param url
     * @param headers header参数
     * @return
     */
    public static String get(OkHttpClient okHttpClient, String url, Headers headers) {
        log.info("okHttpClient get url:{}.", url);
        Request request = new Request.Builder().url(url).headers(headers).get().build();

        String responseData = request(okHttpClient, url, request);
        log.info("okHttpClient get url:{},request responseData====> {}", url, responseData);
        return responseData;
    }

    public static String get(OkHttpClient okHttpClient, String url) {
        Headers headers = new Headers.Builder().build();
        return get( okHttpClient, url, headers);
    }

    /**
     * GET请求。使用默认的 okHttpClient 和 headers
     * @param url
     * @return
     */
    public static String get(String url) {
        OkHttpClient okHttpClient = getOkHttpClient();
        Headers headers = new Headers.Builder().build();
        return get( okHttpClient, url, headers);
    }

    /**
     * post请求，获取响应结果
     *
     * @param okHttpClient
     * @param url
     * @param bodyJson
     * @param headers
     * @return
     */
    public static String post(OkHttpClient okHttpClient, String url, JSONObject bodyJson, Headers headers) {
        log.info("okHttpClient post url:{}, body====> {}", url, bodyJson);
        MediaType mediaTypeJson = MediaType.parse(MEDIA_TYPE_JSON);
        RequestBody requestBody = RequestBody.create(mediaTypeJson, JSON.toJSONString(bodyJson));
        Request request = new Request.Builder().url(url).headers(headers).post(requestBody).build();

        String responseData = request(okHttpClient, url, request);
        log.info("okHttpClient post url:{},post responseData====> {}", url, responseData);
        return responseData;
    }

//    请求form-data数据
    public static String post_fd(OkHttpClient okHttpClient, String url, JSONObject bodyJson, Headers headers) throws IOException {
        log.info("okHttpClient post url:{}, body====> {}", url, bodyJson);
        MediaType mediaTypeJson = MediaType.parse("multipart/form-data");
        RequestBody requestBody = new FormBody.Builder()
                .add("UserName", bodyJson.getString("UserName"))
                .add("Password", bodyJson.getString("Password"))
                .build();
        Request request = new Request.Builder().url(url).headers(headers).post(requestBody).build();

//        String responseData = request(okHttpClient, url, request);
        Response response = okHttpClient.newCall(request).execute();
//        log.info("okHttpClient post url:{},post responseData====> {}", url, responseData);
        String header = response.header("Set-Cookie");

        // 关闭 OkHttpClient
        response.close();
        okHttpClient.dispatcher().executorService().shutdown();
        return header.substring(0, header.indexOf(";"));
    }

    public static String post_urlencoded(OkHttpClient okHttpClient, String url, JSONObject bodyJson, Headers headers) throws IOException {
        log.info("okHttpClient post url:{}, body====> {}", url, bodyJson);
        MediaType mediaTypeJson = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody requestBody = new FormBody.Builder()
                .add("submitdata", bodyJson.getString("submitdata"))
                .build();
        Request request = new Request.Builder().url(url).headers(headers).post(requestBody).build();

        Response response = okHttpClient.newCall(request).execute();
        String response_data = response.body().string();
//        log.info("okHttpClient post url:{},post responseData====> {}", url, responseData);
        // 关闭 OkHttpClient
        response.close();
        okHttpClient.dispatcher().executorService().shutdown();
        return response_data;
    }


    public static String post(OkHttpClient okHttpClient, String url, JSONObject bodyJson) {
        Headers headers = new Headers.Builder().build();
        return post( okHttpClient,  url,  bodyJson, headers);
    }

    /**
     * post请求。使用默认的 okHttpClient 和 headers
     * @param url
     * @param bodyJson
     * @return
     */
    public static String post( String url, JSONObject bodyJson) {
        //使用默认的 okHttpClient
        OkHttpClient okHttpClient = getOkHttpClient();
        Headers headers = new Headers.Builder().build();
        //如果需要自定义 okHttpClient或headers传参，可以调用以下方法
        return post( okHttpClient,  url,  bodyJson, headers);
    }

    /**
     * 获取响应结果
     *
     * @param okHttpClient
     * @param url
     * @param request
     * @return
     */
    public static String request(OkHttpClient okHttpClient, String url, Request request) {
        String responseData = "";
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response != null && response.body() != null) {
                return response.body().string();
            }
        } catch (Exception e) {
            log.error("okHttpClient getResponse error.url:{}", url, e);
        }

        return responseData;
    }

    /**
     * 上传文件
     *
     * @param okHttpClient  okHttp客户端
     * @param url 上传文件的url
     * @param fileKey       文件对应的key
     * @param formDataJson  form-data参数
     * @param headers
     * @param file
     * @return
     */
    public static String uploadFile(OkHttpClient okHttpClient, String url,
                                    String fileKey, File file, JSONObject formDataJson, Headers headers) {
        log.info("uploadFile url:{}, uploadFile formDataJson====> {}", url, formDataJson);
        // 支持传文件的同时，传参数。
        MultipartBody requestBody = getMultipartBody(fileKey, file,  formDataJson);

        // 构建request请求体
        Request request = new Request.Builder().url(url).headers(headers).post(requestBody).build();

        String responseData = request(okHttpClient, url, request);

        // 会在本地产生临时文件，用完后需要删除
        if (file.exists()) {
            file.delete();
        }
        return responseData;

    }

    /**
     * 上传文件
     * @param url
     * @param fileKey form-data文件对应的key
     * @param multipartFile 文件上传对应的 multipartFile
     * @param formDataJson form-data参数
     * @return
     */
    public static String uploadFile(String url,
                                    String fileKey, MultipartFile multipartFile, JSONObject formDataJson) {
        //使用默认的okHttpClient
        OkHttpClient okHttpClient = getOkHttpClient();
        Headers headers = new Headers.Builder().build();
        return uploadFile(okHttpClient, url, fileKey, getFile(multipartFile), formDataJson, headers);
    }

    public static String uploadFile(OkHttpClient okHttpClient, String url,
                                    String fileKey, File file, JSONObject formDataJson) {
        Headers headers = new Headers.Builder().build();
        return uploadFile(okHttpClient, url,  fileKey, file, formDataJson, headers);
    }

    /**
     * 上传文件
     * 使用默认的okHttpClient
     *
     * @param url
     * @param fileKey form-data文件对应的key
     * @param file 文件
     * @param formDataJson form-data参数
     * @return
     */
    public static String uploadFile(String url,
                                    String fileKey, File file, JSONObject formDataJson) {
        //使用默认的okHttpClient
        OkHttpClient okHttpClient = getOkHttpClient();
        Headers headers = new Headers.Builder().build();
        return uploadFile(okHttpClient, url, fileKey, file, formDataJson, headers);
    }

    /**
     * 上传文件用。构建form-data 参数
     *
     * @param fileKey       文件对应的key
     * @param file          文件
     * @param formDataJson  form-data参数
     * @return
     */
    public static MultipartBody getMultipartBody(String fileKey, File file, JSONObject formDataJson) {
        RequestBody fileBody = RequestBody.create(MultipartBody.FORM, file);

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        // 设置传参为form-data格式
        bodyBuilder.setType(MultipartBody.FORM);
        bodyBuilder.addFormDataPart(fileKey, file.getName(), fileBody);
        // 添加 form-data参数
        for (Map.Entry<String, Object> entry : formDataJson.entrySet()) {
            //参数通过 bodyBuilder.addFormDataPart(key, value) 添加
            bodyBuilder.addFormDataPart(entry.getKey(), Objects.toString(entry.getValue(),""));
        }
        return bodyBuilder.build();
    }

    /**
     * 获取文件
     * @param multipartFile
     * @return
     */
    public static File getFile(MultipartFile multipartFile) {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try {
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        } catch (IOException e) {
            log.error("copyInputStreamToFile error.", e);
        }
        return file;
    }

    public static void main(String[] args) throws InterruptedException {
//        String tokenUrl="https://www.tapd.cn/api/basic/token/generate_token_from_array";
//        Map<String, String> headers = new HashMap<String, String>();
//        tapdHeaderDTO cookie = TapdUtils.getCookie();
//        headers.put("cookie", cookie.getTapdCookie());
////        headers.put("content-type", "application/json");
////        headers.put("Content-Length", "2436");
////        headers.put("Host", "www.tapd.cn");
////        headers.put("Connection", "keep-alive");
////        headers.put("Accept-Encoding", "gzip, deflate, br");
////        headers.put("Accept", "*/*");
//        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
//        List<String> statusList = new ArrayList<String>();
//        statusList.add("集成测试通过");
//        String workspace_id = "22274921";
//        JSONObject storiesInterfaceBody = TapdUtils.generateTokenFromArray(workspace_id, statusList, cookie.getDscToken());
//        System.out.println(storiesInterfaceBody);
//        System.out.println(headers);
//        System.out.println(tokenUrl);
//        String post = post(new OkHttpClient(), tokenUrl, storiesInterfaceBody, Headers.of(headers));
//        System.out.println("post:"+post);
//
//
//
//        String s= "{\"exclude_workspace_configs\":[],\"workspace_id\":\"22274921\",\"category_id\":\"\",\"query_token\":\"abc4b35f6976ead8e9d7970bc381d0435a3\",\"list_type\":\"tree\",\"conf_id\":\"1122274921001006273\",\"confIdType\":\"URL\",\"sort_name\":\"\",\"order\":\"\",\"perpage\":50,\"page\":1,\"need_category_counts\":1,\"entity_types\":[\"story\"],\"selected_workspace_ids\":[],\"location\":\"/prong/stories/stories_list\",\"menu_workitem_type_id\":\"\",\"identifier\":\"app_for_list_tools\",\"hide_not_match_condition_sub_node\":1,\"useScene\":\"storyList\",\"filter_data\":{\"filter_expr\":{\"data\":[{\"fieldOption\":\"like\",\"fieldType\":\"input\",\"entity\":\"story\",\"fieldSystemName\":\"name\",\"fieldDisplayName\":\"标题\",\"selectOption\":[],\"value\":\"\",\"fieldIsSystem\":\"1\"},{\"fieldOption\":\"in\",\"fieldType\":\"multi_select\",\"entity\":\"story\",\"fieldSystemName\":\"status\",\"fieldDisplayName\":\"状态\",\"selectOption\":[],\"value\":[\"预发布集成测试\",\"预发布集成测试通过\"],\"fieldIsSystem\":\"1\"},{\"fieldOption\":\"in\",\"fieldType\":\"user_chooser\",\"entity\":\"story\",\"fieldSystemName\":\"owner\",\"fieldDisplayName\":\"处理人\",\"selectOption\":[],\"value\":\"\",\"fieldIsSystem\":\"1\"},{\"fieldOption\":\"in\",\"fieldType\":\"select\",\"entity\":\"story\",\"fieldSystemName\":\"iteration_id\",\"fieldDisplayName\":\"迭代\",\"selectOption\":[],\"value\":[],\"fieldIsSystem\":\"1\"},{\"fieldOption\":\"in\",\"fieldType\":\"select\",\"entity\":\"story\",\"fieldSystemName\":\"priority\",\"fieldDisplayName\":\"优先级\",\"selectOption\":[],\"value\":[],\"fieldIsSystem\":\"1\"}],\"optionType\":\"AND\",\"needInit\":true},\"filter_type\":\"base\"},\"dsc_token\":\"1232bxpEKPIsEVSDT9Y\"}";
//        JSONObject jsonObject = JSONObject.parseObject(s);
//        String storyUrl = "https://www.tapd.cn/api/entity/stories/stories_list";
//        String post2 = post(new OkHttpClient(), storyUrl, jsonObject, Headers.of(headers));
//        System.out.println("post2:"+post2);
    }

}


