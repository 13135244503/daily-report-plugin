package com.dailyreport.action;

import com.dailyreport.ai.CodingPlanAIAnalyzer;
import com.dailyreport.config.PluginGlobalConfig;
import com.dailyreport.config.ProjectConfig;
import com.dailyreport.component.PluginConfigComponent;
import com.dailyreport.generator.MultiProjectReportGenerator;
import com.dailyreport.git.MultiProjectGitAnalyzer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.Desktop;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 生成报告 Action
 */
public class GenerateReportAction extends AnAction {
    
    private static final Logger LOG = Logger.getInstance(GenerateReportAction.class);
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 获取配置
        PluginGlobalConfig config = PluginConfigComponent.getInstance();
        
        // 检查是否有启用的项目
        List<ProjectConfig> enabledProjects = config.getProjects().stream()
                .filter(ProjectConfig::isEnabled)
                .toList();
        
        if (enabledProjects.isEmpty()) {
            Messages.showWarningDialog(
                    "请先在设置中添加并启用至少一个项目",
                    "提示"
            );
            return;
        }
        
        // 弹窗选择报告类型
        String[] options = {"日报", "周报"};
        int choice = Messages.showDialog(
                "请选择报告类型:",
                "生成工作报告",
                options,
                0,
                Messages.getQuestionIcon()
        );
        
        if (choice < 0) {
            return; // 用户取消
        }
        
        boolean isDaily = (choice == 0);
        LocalDate reportDate = LocalDate.now();
        
        // 启动后台任务生成报告
        new Task.Modal(e.getProject(), "生成报告中...", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    indicator.setIndeterminate(true);
                    indicator.setText("正在解析 Git 日志...");
                    
                    // 1. 解析 Git 日志
                    MultiProjectGitAnalyzer gitAnalyzer = new MultiProjectGitAnalyzer();
                    Map.Entry<LocalDateTime, LocalDateTime> timeRange;
                    
                    if (isDaily) {
                        timeRange = gitAnalyzer.getDailyReportTimeRange(reportDate);
                        indicator.setText("正在解析今日 Git 日志...");
                    } else {
                        timeRange = gitAnalyzer.getWeeklyReportTimeRange(reportDate);
                        indicator.setText("正在解析本周 Git 日志...");
                    }
                    
                    List<String> projectPaths = new ArrayList<>();
                    for (ProjectConfig project : enabledProjects) {
                        projectPaths.add(project.getPath());
                    }
                    
                    Map<String, MultiProjectGitAnalyzer.GitStats> projectStatsMap = 
                            gitAnalyzer.analyzeProjects(projectPaths, timeRange.getKey(), timeRange.getValue());
                    
                    // 2. 调用 AI 生成总结
                    indicator.setText("正在调用 Coding Plan AI 生成总结...");
                    String aiSummary = "";
                    
                    try {
                        CodingPlanAIAnalyzer aiAnalyzer = new CodingPlanAIAnalyzer(config);
                        String reportType = isDaily ? "今日" : "本周";
                        aiSummary = aiAnalyzer.generateSummary(projectStatsMap, reportType);
                    } catch (Exception ex) {
                        LOG.warn("AI 总结失败，使用本地总结", ex);
                        aiSummary = "AI 总结生成失败，降级为本地总结。\n\n" +
                                "Git 提交统计：" + projectStatsMap.size() + "个项目，" +
                                "总提交数：" + projectStatsMap.values().stream()
                                        .filter(MultiProjectGitAnalyzer.GitStats::isValid)
                                        .mapToInt(MultiProjectGitAnalyzer.GitStats::getCommitCount)
                                        .sum();
                    }
                    
                    // 3. 生成报告文件
                    indicator.setText("正在生成报告文件...");
                    MultiProjectReportGenerator reportGenerator = new MultiProjectReportGenerator();
                    String filePath;
                    
                    if (isDaily) {
                        filePath = reportGenerator.generateDailyReport(
                                projectStatsMap, reportDate, 
                                config.getOutputRootPath(), aiSummary);
                    } else {
                        filePath = reportGenerator.generateWeeklyReport(
                                projectStatsMap, reportDate, 
                                config.getOutputRootPath(), aiSummary);
                    }
                    
                    // 4. 完成提示
                    ApplicationManager.getApplication().invokeLater(() -> {
                        int result = Messages.showYesNoDialog(
                                "报告生成成功！\n文件路径：" + filePath,
                                "生成完成",
                                Messages.getInformationIcon()
                        );
                        
                        if (result == Messages.YES) {
                            // 打开报告文件
                            try {
                                Desktop.getDesktop().open(new java.io.File(filePath));
                            } catch (Exception ex) {
                                LOG.error("打开文件失败", ex);
                            }
                        }
                    });
                    
                } catch (Exception ex) {
                    LOG.error("报告生成失败", ex);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        Messages.showErrorDialog(
                                "报告生成失败：" + ex.getMessage(),
                                "错误"
                        );
                    });
                }
            }
        }.queue();
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        // 设置菜单项可见性
        e.getPresentation().setEnabledAndVisible(true);
    }
}