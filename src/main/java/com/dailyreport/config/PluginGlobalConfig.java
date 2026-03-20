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
    
    public PluginGlobalConfig() {
        this.projects = new ArrayList<>();
        this.outputRootPath = System.getProperty("user.home") + "/工作报告/";
        this.aiConfig = new AIConfig();
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
}