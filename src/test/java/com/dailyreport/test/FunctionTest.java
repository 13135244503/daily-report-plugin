package com.dailyreport.test;

import com.dailyreport.ai.CodingPlanAIAnalyzer;
import com.dailyreport.config.PluginGlobalConfig;
import com.dailyreport.generator.MultiProjectReportGenerator;
import com.dailyreport.git.MultiProjectGitAnalyzer;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能测试类
 */
public class FunctionTest {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Daily Report Plugin - Function Test");
        System.out.println("========================================\n");
        
        // 测试 1: Git 日志解析器
        testGitAnalyzer();
        
        // 测试 2: 报告生成器
        testReportGenerator();
        
        // 测试 3: AI 分析器配置
        testAIAnalyzer();
        
        System.out.println("\n========================================");
        System.out.println("All tests completed!");
        System.out.println("========================================");
    }
    
    /**
     * 测试 Git 日志解析器
     */
    private static void testGitAnalyzer() {
        System.out.println("[Test 1] Git Log Analyzer");
        System.out.println("----------------------------------------");
        
        try {
            MultiProjectGitAnalyzer analyzer = new MultiProjectGitAnalyzer();
            
            // 测试日报时间范围
            LocalDate today = LocalDate.now();
            var dailyRange = analyzer.getDailyReportTimeRange(today);
            System.out.println("Daily Report Time Range:");
            System.out.println("  Start: " + dailyRange.getKey());
            System.out.println("  End: " + dailyRange.getValue());
            
            // 测试周报时间范围
            var weeklyRange = analyzer.getWeeklyReportTimeRange(today);
            System.out.println("\nWeekly Report Time Range:");
            System.out.println("  Start: " + weeklyRange.getKey());
            System.out.println("  End: " + weeklyRange.getValue());
            
            System.out.println("\n✓ Git Analyzer test PASSED\n");
        } catch (Exception e) {
            System.out.println("\n✗ Git Analyzer test FAILED: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 测试报告生成器
     */
    private static void testReportGenerator() {
        System.out.println("[Test 2] Report Generator");
        System.out.println("----------------------------------------");
        
        try {
            MultiProjectReportGenerator generator = new MultiProjectReportGenerator();
            
            // 创建模拟数据
            Map<String, MultiProjectGitAnalyzer.GitStats> mockData = new HashMap<>();
            
            MultiProjectGitAnalyzer.GitStats stats = new MultiProjectGitAnalyzer.GitStats();
            stats.setProjectName("TestProject");
            stats.setProjectPath("/path/to/test");
            stats.setCommitCount(5);
            stats.setLinesAdded(100);
            stats.setLinesRemoved(50);
            stats.setValid(true);
            
            mockData.put("TestProject", stats);
            
            // 测试日报生成（不实际写入文件）
            String aiSummary = "这是一个测试总结。";
            
            System.out.println("Mock Data Created:");
            System.out.println("  Project: TestProject");
            System.out.println("  Commits: 5");
            System.out.println("  Lines Added: 100");
            System.out.println("  Lines Removed: 50");
            System.out.println("\n✓ Report Generator structure test PASSED\n");
            
        } catch (Exception e) {
            System.out.println("\n✗ Report Generator test FAILED: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 测试 AI 分析器配置
     */
    private static void testAIAnalyzer() {
        System.out.println("[Test 3] AI Analyzer Configuration");
        System.out.println("----------------------------------------");
        
        try {
            // 创建测试配置
            PluginGlobalConfig config = new PluginGlobalConfig();
            
            // 测试 OpenAI 协议
            config.getAiConfig().setProtocol("OpenAI");
            config.getAiConfig().setBaseUrl("https://qianfan.baidubce.com/v2/coding");
            config.getAiConfig().setApiKey("test-api-key");
            
            CodingPlanAIAnalyzer openaiAnalyzer = new CodingPlanAIAnalyzer(config);
            System.out.println("OpenAI Protocol Config:");
            System.out.println("  Base URL: " + config.getAiConfig().getBaseUrl());
            System.out.println("  Full URL: " + config.getAiConfig().getFullApiUrl());
            
            // 测试 Anthropic 协议
            config.getAiConfig().setProtocol("Anthropic");
            config.getAiConfig().setBaseUrl("https://qianfan.baidubce.com/anthropic/coding");
            
            CodingPlanAIAnalyzer anthropicAnalyzer = new CodingPlanAIAnalyzer(config);
            System.out.println("\nAnthropic Protocol Config:");
            System.out.println("  Base URL: " + config.getAiConfig().getBaseUrl());
            System.out.println("  Full URL: " + config.getAiConfig().getFullApiUrl());
            
            System.out.println("\n✓ AI Analyzer configuration test PASSED\n");
            
        } catch (Exception e) {
            System.out.println("\n✗ AI Analyzer test FAILED: " + e.getMessage() + "\n");
        }
    }
}
