package com.dailyreport.component;

import com.dailyreport.config.PluginGlobalConfig;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 插件配置持久化组件
 */
@State(
    name = "DailyReportPluginConfig",
    storages = @Storage("daily-report-plugin-config.xml")
)
public class PluginConfigComponent implements PersistentStateComponent<PluginGlobalConfig> {
    
    private PluginGlobalConfig config = new PluginGlobalConfig();
    
    @Override
    public @Nullable PluginGlobalConfig getState() {
        return config;
    }
    
    @Override
    public void loadState(@NotNull PluginGlobalConfig state) {
        this.config = state;
    }
    
    /**
     * 获取配置实例
     */
    public static PluginGlobalConfig getInstance() {
        PluginConfigComponent component = com.intellij.openapi.components.ServiceManager.getService(PluginConfigComponent.class);
        return component != null ? component.getState() : new PluginGlobalConfig();
    }
    
    /**
     * 保存配置
     */
    public static void saveConfig(PluginGlobalConfig newConfig) {
        PluginConfigComponent component = com.intellij.openapi.components.ServiceManager.getService(PluginConfigComponent.class);
        if (component != null) {
            component.loadState(newConfig);
        }
    }
}