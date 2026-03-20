package com.dailyreport.test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 本地测试类 - 测试百度千帆Coding Plan API调用
 */
public class AITest {
    
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson gson = new Gson();
    
    public static void main(String[] args) {
        // 配置参数 - 使用您提供的API Key
        String apiKey = "QACA1FgCqpbcMIqKo0SzBSAcAsk5MQgrRkYFy1g36kYZrh2ErDP14EvkHpw7moNmUNacgX_BrJIbQZu50b56ag"; // 替换为实际的完整API Key
        String fullUrl = "https://api.modelarts-maas.com/v2/chat/completions";
        String model = "qwen3-30b-a3b";
        int timeout = 30;
        
        // 测试百度千帆API
        System.out.println("========================================");
        System.out.println("华为云AI模型测试");
        System.out.println("========================================");
        testApiCall(fullUrl, apiKey, model, timeout);
    }
    
    private static void testApiCall(String fullUrl, String apiKey, String model, int timeout) {
        System.out.println("API地址: " + fullUrl);
        System.out.println("模型: " + model);
        System.out.println("API Key: " + maskApiKey(apiKey));
        System.out.println("超时时间: " + timeout + "秒");
        System.out.println();
        
        // 初始化HTTP客户端
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
        
        // 构建测试请求
        String requestBody = buildQianfanRequest("请用一句话介绍你自己。", model);
        
        System.out.println("请求体: " + requestBody);
        System.out.println();
        
        // 发送请求
        try {
            Request request = new Request.Builder()
                    .url(fullUrl)
                    .post(RequestBody.create(requestBody, JSON))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + apiKey) // 华为云需要Bearer前缀
                    .build();
            
            System.out.println("发送请求中...");
            long startTime = System.currentTimeMillis();
            
            try (Response response = client.newCall(request).execute()) {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                System.out.println("请求完成，耗时: " + duration + "毫秒");
                System.out.println("响应状态码: " + response.code());
                System.out.println("响应消息: " + response.message());
                System.out.println();
                
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    System.out.println("响应内容:");
                    System.out.println(responseBody);
                    System.out.println();
                    
                    // 解析响应
                    String result = parseQianfanResponse(responseBody);
                    System.out.println("========================================");
                    System.out.println("AI回复:");
                    System.out.println(result);
                    System.out.println("========================================");
                    System.out.println();
                    System.out.println("✅ 测试成功！");
                } else {
                    String responseBody = response.body().string();
                    System.out.println("❌ 请求失败！");
                    System.out.println("错误响应:");
                    System.out.println(responseBody);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ 发生异常:");
            e.printStackTrace();
        }
    }
    
    /**
     * 构建百度千帆请求体
     */
    private static String buildQianfanRequest(String promptContent, String model) {
        JsonObject request = new JsonObject();
        request.addProperty("model", model);
        request.addProperty("temperature", 0.2);
        request.addProperty("max_tokens", 1000);
        
        // 添加safety参数
        JsonObject safety = new JsonObject();
        safety.addProperty("input_level", "standard");
        request.add("safety", safety);
        
        // 添加web_search参数
        JsonObject webSearch = new JsonObject();
        webSearch.addProperty("enable", true);
        webSearch.addProperty("enable_trace", true);
        request.add("web_search", webSearch);
        
        // 添加messages
        var messages = new ArrayList<JsonObject>();
        var message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", promptContent);
        messages.add(message);
        
        request.add("messages", gson.toJsonTree(messages));
        
        return gson.toJson(request);
    }
    
    /**
     * 解析百度千帆响应
     */
    private static String parseQianfanResponse(String responseBody) {
        try {
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            
            // OpenAI 格式
            if (json.has("choices")) {
                var choices = json.getAsJsonArray("choices");
                if (choices.size() > 0) {
                    var choice = choices.get(0).getAsJsonObject();
                    if (choice.has("message")) {
                        var message = choice.getAsJsonObject("message");
                        if (message.has("content")) {
                            return message.get("content").getAsString();
                        }
                    }
                }
            }
            
            return "无法解析 AI 响应";
            
        } catch (Exception e) {
            return "响应解析失败：" + e.getMessage();
        }
    }
    
    /**
     * 脱敏API Key
     */
    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}