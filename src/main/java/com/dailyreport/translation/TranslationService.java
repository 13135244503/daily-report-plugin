package com.dailyreport.translation;

import com.dailyreport.config.PluginGlobalConfig;
import com.dailyreport.config.PluginGlobalConfig.TranslationConfig;
import com.intellij.openapi.diagnostic.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class TranslationService {
    
    private static final Logger LOG = Logger.getInstance(TranslationService.class);
    
    private final TranslationConfig config;
    
    public TranslationService(PluginGlobalConfig config) {
        this.config = config.getTranslationConfig();
    }
    
    public TranslationResult translate(String chineseText) throws Exception {
        if ("Baidu".equals(config.getProvider())) {
            return translateWithBaidu(chineseText);
        } else if ("Youdao".equals(config.getProvider())) {
            return translateWithYoudao(chineseText);
        } else {
            throw new Exception("不支持的翻译服务提供商: " + config.getProvider());
        }
    }
    
    private TranslationResult translateWithBaidu(String chineseText) throws Exception {
        String appId = config.getBaiduAppId();
        String key = config.getBaiduKey();
        
        if (appId == null || appId.isEmpty() || key == null || key.isEmpty()) {
            throw new Exception("百度翻译配置不完整，请检查 AppID 和密钥");
        }
        
        String salt = String.valueOf(System.currentTimeMillis());
        String sign = generateBaiduSign(appId, chineseText, salt, key);
        
        String apiUrl = "https://fanyi-api.baidu.com/api/trans/vip/translate";
        String params = String.format("q=%s&from=zh&to=en&appid=%s&salt=%s&sign=%s",
                URLEncoder.encode(chineseText, "UTF-8"),
                appId,
                salt,
                sign);
        
        String response = sendHttpRequest(apiUrl, params, "POST");
        
        return parseBaiduResponse(response);
    }
    
    private TranslationResult translateWithYoudao(String chineseText) throws Exception {
        String appKey = config.getYoudaoAppKey();
        String appSecret = config.getYoudaoAppSecret();
        
        if (appKey == null || appKey.isEmpty() || appSecret == null || appSecret.isEmpty()) {
            throw new Exception("有道翻译配置不完整，请检查 AppKey 和 AppSecret");
        }
        
        String salt = String.valueOf(System.currentTimeMillis());
        String curtime = String.valueOf(System.currentTimeMillis() / 1000);
        String sign = generateYoudaoSign(appKey, chineseText, salt, curtime, appSecret);
        
        String apiUrl = "https://openapi.youdao.com/api";
        String params = String.format("q=%s&from=zh-CHS&to=en&appKey=%s&salt=%s&sign=%s&signType=v3&curtime=%s",
                URLEncoder.encode(chineseText, "UTF-8"),
                appKey,
                salt,
                sign,
                curtime);
        
        String response = sendHttpRequest(apiUrl, params, "POST");
        
        return parseYoudaoResponse(response);
    }
    
    private String generateBaiduSign(String appId, String query, String salt, String key) throws Exception {
        String str = appId + query + salt + key;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = md.digest(str.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    private String generateYoudaoSign(String appKey, String query, String salt, String curtime, String appSecret) throws Exception {
        String str = appKey + truncate(query) + salt + curtime + appSecret;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = md.digest(str.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    private String truncate(String q) {
        if (q == null) {
            return null;
        }
        int len = q.length();
        return len <= 20 ? q : (q.substring(0, 10) + len + q.substring(len - 10));
    }
    
    private String sendHttpRequest(String apiUrl, String params, String method) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes(StandardCharsets.UTF_8));
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("HTTP请求失败，状态码: " + responseCode);
        }
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        
        return response.toString();
    }
    
    private TranslationResult parseBaiduResponse(String response) throws Exception {
        if (response.contains("error_code")) {
            String errorMsg = extractErrorMessage(response);
            throw new Exception("百度翻译错误: " + errorMsg);
        }
        
        String englishText = extractTranslation(response);
        return new TranslationResult(englishText);
    }
    
    private TranslationResult parseYoudaoResponse(String response) throws Exception {
        if (response.contains("\"errorCode\"") && !response.contains("\"errorCode\":\"0\"")) {
            String errorMsg = extractYoudaoErrorMessage(response);
            throw new Exception("有道翻译错误: " + errorMsg);
        }
        
        String englishText = extractYoudaoTranslation(response);
        return new TranslationResult(englishText);
    }
    
    private String extractTranslation(String response) {
        int start = response.indexOf("\"dst\":\"") + 7;
        int end = response.indexOf("\"", start);
        return unescapeUnicode(response.substring(start, end));
    }
    
    private String extractYoudaoTranslation(String response) {
        int start = response.indexOf("\"translation\":[\"") + 16;
        int end = response.indexOf("\"]", start);
        return unescapeUnicode(response.substring(start, end));
    }
    
    private String extractErrorMessage(String response) {
        int start = response.indexOf("\"error_msg\":\"") + 12;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }
    
    private String extractYoudaoErrorMessage(String response) {
        int start = response.indexOf("\"errorMsg\":\"") + 11;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }
    
    private String unescapeUnicode(String str) {
        return str.replace("\\u0026", "&")
                .replace("\\u003c", "<")
                .replace("\\u003e", ">")
                .replace("\\u0027", "'")
                .replace("\\u0022", "\"");
    }
    
    public static class TranslationResult {
        private final String englishText;
        
        public TranslationResult(String englishText) {
            this.englishText = englishText;
        }
        
        public String getEnglishText() {
            return englishText;
        }
        
        public String toVariableName() {
            return toSnakeCase(englishText);
        }
        
        public String toConstantName() {
            return toSnakeCase(englishText).toUpperCase();
        }
        
        public String toCamelCase() {
            String[] words = englishText.toLowerCase().split("\\s+");
            StringBuilder result = new StringBuilder(words[0]);
            for (int i = 1; i < words.length; i++) {
                result.append(Character.toUpperCase(words[i].charAt(0)))
                        .append(words[i].substring(1));
            }
            return result.toString();
        }
        
        public String toMethodName() {
            return "get" + toPascalCase(englishText);
        }
        
        public String toClassName() {
            return toPascalCase(englishText);
        }
        
        public String toFileName() {
            return toSnakeCase(englishText);
        }
        
        private String toSnakeCase(String text) {
            return text.toLowerCase().replaceAll("\\s+", "_");
        }
        
        private String toPascalCase(String text) {
            String[] words = text.toLowerCase().split("\\s+");
            StringBuilder result = new StringBuilder();
            for (String word : words) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1));
            }
            return result.toString();
        }
    }
}