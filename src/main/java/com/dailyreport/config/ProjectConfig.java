package com.dailyreport.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * 单个项目配置
 */
@XStreamAlias("project")
public class ProjectConfig {
    
    @XStreamAsAttribute
    private String name;
    
    @XStreamAsAttribute
    private String path;
    
    @XStreamAsAttribute
    private boolean enabled;
    
    public ProjectConfig() {
    }
    
    public ProjectConfig(String name, String path, boolean enabled) {
        this.name = name;
        this.path = path;
        this.enabled = enabled;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public String toString() {
        return "ProjectConfig{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
