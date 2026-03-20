package com.dailyreport.generator;

import com.dailyreport.git.MultiProjectGitAnalyzer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 多项目报告生成器
 */
public class MultiProjectReportGenerator {
    
    /**
     * 生成日报
     */
    public String generateDailyReport(
            Map<String, MultiProjectGitAnalyzer.GitStats> projectStatsMap,
            LocalDate date,
            String outputRootPath,
            String aiSummary) throws IOException {
        
        // 确保输出目录存在
        File outputDir = new File(outputRootPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        // 构建文件名
        String fileName = date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "-多项目工作日报.md";
        String filePath = outputRootPath + File.separator + fileName;
        
        // 构建报告内容
        StringBuilder content = buildReportContent(projectStatsMap, "日报", 
                date.atStartOfDay(), date.atTime(23, 59, 59), aiSummary);
        
        // 写入文件
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content.toString());
        }
        
        return filePath;
    }
    
    /**
     * 生成周报
     */
    public String generateWeeklyReport(
            Map<String, MultiProjectGitAnalyzer.GitStats> projectStatsMap,
            LocalDate date,
            String outputRootPath,
            String aiSummary) throws IOException {
        
        // 确保输出目录存在
        File outputDir = new File(outputRootPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        // 计算周数
        WeekFields weekFields = WeekFields.of(Locale.CHINA);
        int year = date.getYear();
        int week = date.get(weekFields.weekOfWeekBasedYear());
        
        // 构建文件名
        String fileName = year + "年第" + week + "周 - 多项目工作周报.md";
        String filePath = outputRootPath + File.separator + fileName;
        
        // 获取本周一和本周日
        LocalDateTime monday = date.with(java.time.DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime sunday = date.with(java.time.DayOfWeek.SUNDAY).atTime(23, 59, 59);
        
        // 构建报告内容
        StringBuilder content = buildReportContent(projectStatsMap, "周报", monday, sunday, aiSummary);
        
        // 写入文件
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content.toString());
        }
        
        return filePath;
    }
    
    /**
     * 构建报告内容
     */
    private StringBuilder buildReportContent(
            Map<String, MultiProjectGitAnalyzer.GitStats> projectStatsMap,
            String reportType,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String aiSummary) {
        
        StringBuilder content = new StringBuilder();
        
        // 标题
        content.append("# 多项目工作").append(reportType).append("\n\n");
        
        // 基本信息
        content.append("## 基本信息\n\n");
        content.append("- 时间范围：")
               .append(startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
               .append(" 至 ")
               .append(endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
               .append("\n");
        
        // 统计项目数
        int totalProjects = projectStatsMap.size();
        long validProjects = projectStatsMap.values().stream()
                .filter(MultiProjectGitAnalyzer.GitStats::isValid)
                .count();
        int invalidProjects = totalProjects - (int) validProjects;
        
        content.append("- 统计项目数：").append(validProjects)
               .append("（共配置 ").append(totalProjects)
               .append(" 个，").append(invalidProjects)
               .append(" 个非 Git 项目/禁用）\n");
        
        // 总提交数和代码行数统计
        int totalCommits = 0;
        int totalAdded = 0;
        int totalRemoved = 0;
        
        for (MultiProjectGitAnalyzer.GitStats stats : projectStatsMap.values()) {
            if (stats.isValid()) {
                totalCommits += stats.getCommitCount();
                totalAdded += stats.getLinesAdded();
                totalRemoved += stats.getLinesRemoved();
            }
        }
        
        content.append("- 总提交数：").append(totalCommits).append("\n");
        content.append("- 总新增代码行数：").append(totalAdded)
               .append(" | 总删除代码行数：").append(totalRemoved)
               .append(" | 总净增行数：").append(totalAdded - totalRemoved)
               .append("\n\n");
        
        // 各项目详情
        content.append("## 各项目详情\n\n");
        
        for (Map.Entry<String, MultiProjectGitAnalyzer.GitStats> entry : projectStatsMap.entrySet()) {
            MultiProjectGitAnalyzer.GitStats stats = entry.getValue();
            
            content.append("### 项目：").append(entry.getKey());
            if (!stats.isValid()) {
                content.append(" (无效项目：").append(stats.getErrorMessage()).append(")");
            }
            content.append("\n\n");
            
            if (stats.isValid()) {
                content.append("- 提交数：").append(stats.getCommitCount()).append("\n");
                content.append("- 新增行数：").append(stats.getLinesAdded())
                       .append(" | 删除行数：").append(stats.getLinesRemoved())
                       .append(" | 净增行数：").append(stats.getLinesAdded() - stats.getLinesRemoved())
                       .append("\n");
                
                // 文件类型分布
                content.append("- 文件类型分布：");
                StringBuilder fileTypeStr = new StringBuilder();
                for (Map.Entry<String, Integer> fileType : stats.getFileTypeDistribution().entrySet()) {
                    if (fileTypeStr.length() > 0) {
                        fileTypeStr.append(", ");
                    }
                    fileTypeStr.append(fileType.getKey()).append(":").append(fileType.getValue());
                }
                content.append(fileTypeStr.length() > 0 ? fileTypeStr.toString() : "无数据").append("\n");
                
                // 修改文件列表
                content.append("- 修改文件列表：\n");
                for (String file : stats.getModifiedFiles()) {
                    content.append("  - ").append(file).append("\n");
                }
                
                // Git 提交记录
                content.append("- Git 提交记录：\n");
                for (MultiProjectGitAnalyzer.CommitInfo commit : stats.getCommits()) {
                    content.append("  - [")
                           .append(commit.getCommitId().substring(0, 7))
                           .append("] ")
                           .append(commit.getMessage())
                           .append(" (作者：").append(commit.getAuthor())
                           .append(", 时间：")
                           .append(commit.getCommitTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                           .append(")\n");
                }
            } else {
                content.append("- 状态：无效项目 (").append(stats.getErrorMessage()).append(")\n");
            }
            
            content.append("\n");
        }
        
        // AI 生成的工作总结
        content.append("## 工作内容总结\n\n");
        content.append(aiSummary != null ? aiSummary : "AI 总结生成失败").append("\n\n");

        return content;
    }
}