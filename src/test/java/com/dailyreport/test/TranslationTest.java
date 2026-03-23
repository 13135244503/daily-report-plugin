package com.dailyreport.test;

import com.dailyreport.translation.TranslationService.TranslationResult;

public class TranslationTest {
    
    public static void main(String[] args) {
        System.out.println("测试翻译结果格式化功能...\n");
        
        String englishText = "user profile management";
        TranslationResult result = new TranslationResult(englishText);
        
        System.out.println("英文原文: " + result.getEnglishText());
        System.out.println("变量名 (snake_case): " + result.toVariableName());
        System.out.println("常量名 (UPPER_SNAKE_CASE): " + result.toConstantName());
        System.out.println("驼峰命名 (camelCase): " + result.toCamelCase());
        System.out.println("方法名 (methodName): " + result.toMethodName());
        System.out.println("类名 (ClassName): " + result.toClassName());
        System.out.println("文件名 (file_name): " + result.toFileName());
        
        System.out.println("\n测试完成！");
    }
}