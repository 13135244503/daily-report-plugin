package com.dailyreport.ai;

import com.dailyreport.config.PluginGlobalConfig;
import com.dailyreport.git.MultiProjectGitAnalyzer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 适配 Coding Plan 接口的 AI 分析器
 */
public class CodingPlanAIAnalyzer {
    
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final PluginGlobalConfig config;
    private final OkHttpClient client;
    private final Gson gson;
    
    public CodingPlanAIAnalyzer(PluginGlobalConfig config) {
        this.config = config;
        this.gson = new Gson();
        
        // 初始化 HTTP 客户端
        this.client = new OkHttpClient.Builder()
                .connectTimeout(config.getAiConfig().getTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getAiConfig().getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getAiConfig().getTimeout(), TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * 调用 Coding Plan 接口生成工作总结
     */
    public String generateSummary(Map<String, MultiProjectGitAnalyzer.GitStats> projectStatsMap, String reportType) {
        try {
            // 构建 Prompt
            String promptContent = buildPrompt(projectStatsMap, reportType);
            
            // 根据协议类型构建请求体
            String requestBody;
            if ("OpenAI".equals(config.getAiConfig().getProtocol())) {
                requestBody = buildOpenAIRequest(promptContent);
            } else if ("HuaweiCloud".equals(config.getAiConfig().getProtocol())) {
                requestBody = buildHuaweiCloudRequest(promptContent);
            } else {
                requestBody = buildAnthropicRequest(promptContent);
            }
            
            // 构建请求
            Request.Builder requestBuilder = new Request.Builder()
                    .url(config.getAiConfig().getFullApiUrl())
                    .post(RequestBody.create(requestBody, JSON))
                    .addHeader("Content-Type", "application/json");
            
            // 根据协议类型添加认证头
            if (config.getAiConfig().needsBearerToken()) {
                requestBuilder.addHeader("Authorization", "Bearer " + config.getAiConfig().getApiKey());
            } else {
                requestBuilder.addHeader("Authorization", config.getAiConfig().getApiKey());
                requestBuilder.addHeader("X-API-Key", config.getAiConfig().getApiKey());
            }
            
            Request request = requestBuilder.build();
            
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("API 调用失败：" + response.code() + " - " + response.message());
                }
                
                String responseBody = response.body().string();
                return parseResponse(responseBody);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return "AI 总结生成失败：" + e.getMessage();
        }
    }
    
    /**
     * 构建 Prompt
     */
    private String buildPrompt(Map<String, MultiProjectGitAnalyzer.GitStats> projectStatsMap, String reportType) {
        StringBuilder sb = new StringBuilder();
        sb.append("请基于以下多个项目的 Git 提交记录和代码改动，总结").append(reportType)
          .append("的工作内容，要求：\n");
        sb.append("1. 按项目维度分点总结核心工作（功能开发/BUG 修复/优化等）\n");
        sb.append("2. 突出每个项目的技术关键点、解决的问题和工作成果\n");
        sb.append("3. 语言简洁专业，符合职场工作报告规范，避免冗余\n\n");
        sb.append("多项目 Git 记录：\n\n");
        
        for (Map.Entry<String, MultiProjectGitAnalyzer.GitStats> entry : projectStatsMap.entrySet()) {
            MultiProjectGitAnalyzer.GitStats stats = entry.getValue();
            if (stats.isValid()) {
                sb.append("### 项目：").append(entry.getKey()).append("\n");
                sb.append("- 提交数：").append(stats.getCommitCount()).append("\n");
                sb.append("- 新增行数：").append(stats.getLinesAdded())
                  .append(" | 删除行数：").append(stats.getLinesRemoved()).append("\n");
                sb.append("- 修改文件：").append(String.join(", ", stats.getModifiedFiles())).append("\n");
                sb.append("- 提交记录：\n");
                
                for (MultiProjectGitAnalyzer.CommitInfo commit : stats.getCommits()) {
                    sb.append("  - [").append(commit.getCommitId().substring(0, 7))
                      .append("] " ).append(commit.getMessage())
                      .append(" (作者：").append(commit.getAuthor()).append(")\n");
                }
                
                // 添加未提交的改动信息
                if (!stats.getUncommittedChanges().isEmpty()) {
                    sb.append("- 未提交的改动：\n");
                    for (MultiProjectGitAnalyzer.UncommittedChange change : stats.getUncommittedChanges()) {
                        sb.append("  - [").append(change.getStatus())
                          .append("] " ).append(change.getFilePath())
                          .append(" (新增：").append(change.getLinesAdded())
                          .append(" | 删除：").append(change.getLinesRemoved()).append(")\n");
                    }
                }
                sb.append("\n");
            } else {
                sb.append("### 项目：").append(entry.getKey())
                  .append(" (无效项目：").append(stats.getErrorMessage()).append(")\n\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 构建 OpenAI 协议请求体
     */
    private String buildOpenAIRequest(String promptContent) {
        JsonObject request = new JsonObject();
        request.addProperty("model", config.getAiConfig().getModel());
        request.addProperty("temperature", 0.2);
        request.addProperty("max_tokens", 1000);
        
        var messages = new ArrayList<JsonObject>();
        var message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", promptContent);
        messages.add(message);
        
        request.add("messages", gson.toJsonTree(messages));
        request.addProperty("apiKey", config.getAiConfig().getApiKey());
        
        return gson.toJson(request);
    }
    
    /**
     * 构建 Anthropic 协议请求体
     */
    private String buildAnthropicRequest(String promptContent) {
        JsonObject request = new JsonObject();
        request.addProperty("model", config.getAiConfig().getModel());
        request.addProperty("temperature", 0.2);
        request.addProperty("max_tokens", 1000);
        
        var messages = new ArrayList<JsonObject>();
        var message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", promptContent);
        messages.add(message);
        
        request.add("messages", gson.toJsonTree(messages));
        request.addProperty("apiKey", config.getAiConfig().getApiKey());
        
        return gson.toJson(request);
    }
    
    /**
     * 构建华为云协议请求体
     */
    private String buildHuaweiCloudRequest(String promptContent) {
        JsonObject request = new JsonObject();
        request.addProperty("model", config.getAiConfig().getModel());
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
     * 解析响应
     */
    private String parseResponse(String responseBody) {
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
            
            // Anthropic 格式
            if (json.has("content")) {
                var content = json.getAsJsonArray("content");
                if (content.size() > 0) {
                    var textBlock = content.get(0).getAsJsonObject();
                    if (textBlock.has("text")) {
                        return textBlock.get("text").getAsString();
                    }
                }
            }
            
            return "无法解析 AI 响应";
            
        } catch (Exception e) {
            return "响应解析失败：" + e.getMessage();
        }
    }
    
    /**
     * 测试连接
     */
    public boolean testConnection() {
        try {
            String testPrompt = "Hello, this is a test connection.";
            String requestBody;
            
            if ("OpenAI".equals(config.getAiConfig().getProtocol())) {
                requestBody = buildOpenAIRequest(testPrompt);
            } else if ("HuaweiCloud".equals(config.getAiConfig().getProtocol())) {
                requestBody = buildHuaweiCloudRequest(testPrompt);
            } else {
                requestBody = buildAnthropicRequest(testPrompt);
            }
            
            // 构建请求
            Request.Builder requestBuilder = new Request.Builder()
                    .url(config.getAiConfig().getFullApiUrl())
                    .post(RequestBody.create(requestBody, JSON))
                    .addHeader("Content-Type", "application/json");
            
            // 根据协议类型添加认证头
            if (config.getAiConfig().needsBearerToken()) {
                requestBuilder.addHeader("Authorization", "Bearer " + config.getAiConfig().getApiKey());
            } else {
                requestBuilder.addHeader("Authorization", config.getAiConfig().getApiKey());
                requestBuilder.addHeader("X-API-Key", config.getAiConfig().getApiKey());
            }
            
            Request request = requestBuilder.build();
            
            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}