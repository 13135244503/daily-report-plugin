package com.dailyreport.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.ArrayList;
import java.util.List;

/**
 * 插件全局配置
 */
@XStreamAlias("pluginConfig")
public class PluginGlobalConfig {
    
    @XStreamImplicit(itemFieldName = "project")
    private List<ProjectConfig> projects;
    
    private String outputRootPath;
    
    private AIConfig aiConfig;
    
    private TranslationConfig translationConfig;
    
    public PluginGlobalConfig() {
        this.projects = new ArrayList<>();
        this.outputRootPath = System.getProperty("user.home") + "/工作报告/";
        this.aiConfig = new AIConfig();
        this.translationConfig = new TranslationConfig();
    }
    
    public List<ProjectConfig> getProjects() {
        return projects;
    }
    
    public void setProjects(List<ProjectConfig> projects) {
        this.projects = projects;
    }
    
    public String getOutputRootPath() {
        return outputRootPath;
    }
    
    public void setOutputRootPath(String outputRootPath) {
        this.outputRootPath = outputRootPath;
    }
    
    public AIConfig getAiConfig() {
        return aiConfig;
    }
    
    public void setAiConfig(AIConfig aiConfig) {
        this.aiConfig = aiConfig;
    }
    
    public TranslationConfig getTranslationConfig() {
        return translationConfig;
    }
    
    public void setTranslationConfig(TranslationConfig translationConfig) {
        this.translationConfig = translationConfig;
    }
    
    /**
     * AI 配置（适配 Coding Plan 专属接口）
     */
    @XStreamAlias("aiConfig")
    public static class AIConfig {
        
        // 协议类型：OpenAI、Anthropic 或 HuaweiCloud
        private String protocol;
        
        // Base URL（完整API地址）
        private String baseUrl;
        
        // API Key（脱敏存储）
        private String apiKey;
        
        // 模型名称
        private String model;
        
        // 请求超时时间（秒）
        private int timeout;
        
        public AIConfig() {
            this.protocol = "HuaweiCloud";
            this.baseUrl = "https://api.modelarts-maas.com/v2/chat/completions";
            this.model = "qwen3-30b-a3b";
            this.timeout = 30;
        }
        
        public String getProtocol() {
            return protocol;
        }
        
        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getModel() {
            return model;
        }
        
        public void setModel(String model) {
            this.model = model;
        }
        
        public int getTimeout() {
            return timeout;
        }
        
        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
        
        /**
         * 获取完整接口路径
         */
        public String getFullApiUrl() {
            return baseUrl;
        }
        
        /**
         * 是否需要Bearer前缀
         */
        public boolean needsBearerToken() {
            return "HuaweiCloud".equals(protocol);
        }
    }
    
    /**
     * 翻译配置
     */
    @XStreamAlias("translationConfig")
    public static class TranslationConfig {
        
        private String provider;
        private boolean showVariableName;
        private boolean showConstantName;
        private boolean showCamelCase;
        private boolean showMethodName;
        private boolean showClassName;
        private boolean showFileName;
        
        private String baiduAppId;
        private String baiduKey;
        private String youdaoAppKey;
        private String youdaoAppSecret;
        
        public TranslationConfig() {
            this.provider = "Baidu";
            this.showVariableName = true;
            this.showConstantName = true;
            this.showCamelCase = true;
            this.showMethodName = true;
            this.showClassName = true;
            this.showFileName = true;
        }
        
        public String getProvider() {
            return provider;
        }
        
        public void setProvider(String provider) {
            this.provider = provider;
        }
        
        public boolean isShowVariableName() {
            return showVariableName;
        }
        
        public void setShowVariableName(boolean showVariableName) {
            this.showVariableName = showVariableName;
        }
        
        public boolean isShowConstantName() {
            return showConstantName;
        }
        
        public void setShowConstantName(boolean showConstantName) {
            this.showConstantName = showConstantName;
        }
        
        public boolean isShowCamelCase() {
            return showCamelCase;
        }
        
        public void setShowCamelCase(boolean showCamelCase) {
            this.showCamelCase = showCamelCase;
        }
        
        public boolean isShowMethodName() {
            return showMethodName;
        }
        
        public void setShowMethodName(boolean showMethodName) {
            this.showMethodName = showMethodName;
        }
        
        public boolean isShowClassName() {
            return showClassName;
        }
        
        public void setShowClassName(boolean showClassName) {
            this.showClassName = showClassName;
        }
        
        public boolean isShowFileName() {
            return showFileName;
        }
        
        public void setShowFileName(boolean showFileName) {
            this.showFileName = showFileName;
        }
        
        public String getBaiduAppId() {
            return baiduAppId;
        }
        
        public void setBaiduAppId(String baiduAppId) {
            this.baiduAppId = baiduAppId;
        }
        
        public String getBaiduKey() {
            return baiduKey;
        }
        
        public void setBaiduKey(String baiduKey) {
            this.baiduKey = baiduKey;
        }
        
        public String getYoudaoAppKey() {
            return youdaoAppKey;
        }
        
        public void setYoudaoAppKey(String youdaoAppKey) {
            this.youdaoAppKey = youdaoAppKey;
        }
        
        public String getYoudaoAppSecret() {
            return youdaoAppSecret;
        }
        
        public void setYoudaoAppSecret(String youdaoAppSecret) {
            this.youdaoAppSecret = youdaoAppSecret;
        }
    }
}