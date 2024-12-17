package com.autotest.LiuMa.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
//      String tokenUrl="https://www.tapd.cn/api/basic/token/generate_token_from_array";
//        String storyUrl="https://www.tapd.cn/api/entity/stories/stories_list";
//    //        #################headers的获取############
//        Map<String, String> headers = new HashMap<String, String>();
//        tapdHeaderDTO cookie = TapdUtils.getCookie();
//        headers.put("cookie", cookie.getTapdCookie());
//        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
//        List<String> statusList = new ArrayList<String>();
//        //        #################body的构造############
//        statusList.add("测试通过");
//        statusList.add("Coding Review");
//        statusList.add("产品已实现");
////        statusList.add("集成测试");
////        statusList.add("集成测试通过");
////        statusList.add("预发布集成测试");
////        statusList.add("预发布集成测试通过");
//        String workspace_id = "22274921";
//        JSONObject tokenBody = TapdUtils.generateTokenFromArray(workspace_id, statusList, cookie.getDscToken());
//        String postTokenData = post(new OkHttpClient(), tokenUrl, tokenBody, Headers.of(headers));
//        JSONObject jsonObject = JSONObject.parseObject(postTokenData);
//        JSONObject data = jsonObject.getJSONObject("data");
//        String queryToken = data.getString("query_token");
//        JSONObject storiesInterfaceBody = TapdUtils.getStoriesInterfaceBody(workspace_id,queryToken, statusList, cookie.getDscToken());
//        System.out.println(storiesInterfaceBody);
//        System.out.println(headers);
//        System.out.println(storyUrl);
//        //        #################数据的请求############
//
//        String storiesData = post(new OkHttpClient(), storyUrl, storiesInterfaceBody, Headers.of(headers));
//        System.out.println("postTokenData:"+postTokenData);
//        System.out.println("storiesData:"+storiesData);
//
//        //        #################数据的解析############
//        JSONObject jsonObject1 = JSONObject.parseObject(storiesData);
//        JSONObject dataObject1 = jsonObject1.getJSONObject("data");
//        JSONArray storiesList = dataObject1.getJSONArray("stories_list");
//        if (storiesList.size() > 0) {
//            for (int i = 0; i < storiesList.size(); i++) {
//                JSONObject story1 = storiesList.getJSONObject(i);
//                JSONObject story = story1.getJSONObject("Story");
//                String id = story.getString("id");
//                String name = story.getString("name");
//                String shortId = story.getString("short_id");
//                String statusAlias = story.getString("status_alias");
//                String status = story.getString("status");
//                String owner = story.getString("owner");
//                JSONObject releaseInfo = story.getJSONObject("release_info");
//                String releasePlan;
//                if (releaseInfo != null) {
//                    releasePlan = releaseInfo.getString("name");
//                } else {
//                    releasePlan = "暂无发布计划";
//                }
//
//                String storyUrl1= "https://www.tapd.cn/"+"22274921/prong/stories/view/"+id;
//
//                System.out.println("------------------------------------------------------");
//                System.out.println("id:"+id);
//                System.out.println("name:"+name);
//                System.out.println("shortId:"+shortId);
//                System.out.println("statusAlias:"+statusAlias);
//                if ("测试通过".equals(statusAlias) || "Coding Review".equals(statusAlias) || "产品已实现".equals(statusAlias)) {
//                    System.out.println("这个改为测试通过哈");
//                }
//                System.out.println("status:"+status);
//                System.out.println("owner:"+owner);
//                System.out.println("releasePlan:"+releasePlan);
//                System.out.println("storyUrl:"+storyUrl1);
//        }
//        }

//        测试一下高级查询
        String storyUrlHigh="https://www.tapd.cn/api/search_filter/search_filter/search";
        //        #################headers的获取############
        Map<String, String> headersHigh = new HashMap<String, String>();
        tapdHeaderDTO cookieHigh = TapdUtils.getCookie();
        headersHigh.put("cookie", cookieHigh.getTapdCookie());
        headersHigh.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
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

        String workspace_id = "22274921";
        JSONObject highStoriesInterfaceBody = TapdUtils.getHighStoriesInterfaceBody(workspace_id, statusListHigh);
        String storiesDataHigh = post(new OkHttpClient(), storyUrlHigh, highStoriesInterfaceBody, Headers.of(headersHigh));
//        System.out.println("storiesData:"+storiesDataHigh);
        //        #################数据的解析############
        JSONObject jsonObject1 = JSONObject.parseObject(storiesDataHigh);
        JSONObject dataObject1 = jsonObject1.getJSONObject("data");
        JSONArray storiesList = dataObject1.getJSONArray("list");
        if (storiesList.size() > 0) {
            for (int i = 0; i < storiesList.size(); i++) {
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
                    System.out.println("这个改为测试通过哈");
                }
                System.out.println("status:"+status);
                System.out.println("owner:"+owner);
                System.out.println("releasePlan:"+releasePlan);
                System.out.println("storyUrl:"+storyUrl1);
        }
    }
    }

}


