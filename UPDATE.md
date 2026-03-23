# 更新日志

本文档记录插件的版本更新历史和新功能。

## [1.0.2] - 2025-03-23

### 新增功能

#### 🌐 中文翻译功能
- 支持将中文翻译为多种编程命名格式
- 集成百度翻译和有道翻译API
- 支持的命名格式：
  - 变量名 (snake_case)
  - 常量名 (UPPER_SNAKE_CASE)
  - 驼峰命名 (camelCase)
  - 方法名 (methodName)
  - 类名 (ClassName)
  - 文件名 (file_name)

#### ⌨️ 自定义快捷键
- 翻译功能支持自定义快捷键
- 快捷键输入框支持直接按键盘输入
- 自动识别并显示快捷键组合（Ctrl、Shift、Alt、Meta）
- 默认快捷键：Ctrl+Shift+T（可在配置中修改）

#### 🧪 翻译测试功能
- 新增"测试翻译"按钮
- 在配置页面直接测试翻译API是否正常工作
- 测试成功后显示示例翻译结果
- 测试失败时显示详细错误信息

#### 📋 翻译对话框
- 新增翻译对话框界面
- 输入中文后按回车自动翻译
- 根据配置显示不同的命名格式
- 点击翻译结果自动复制到剪贴板
- 复制成功后有视觉反馈（背景变绿）

#### ⚙️ 翻译配置页面
- 新增"翻译配置"标签页
- 支持选择翻译服务提供商（百度/有道）
- 支持配置API密钥：
  - 百度翻译：App ID、密钥
  - 有道翻译：App Key、App Secret
- 支持自定义快捷键
- 支持选择要显示的命名格式（可多选）
- 所有配置自动持久化

### 改进

- 扩展配置系统，新增TranslationConfig配置类
- 优化配置界面，添加翻译相关配置项
- 改进快捷键输入体验，支持键盘直接输入
- 添加翻译测试功能，方便用户验证配置

### 技术细节

#### 新增文件
- `src/main/java/com/dailyreport/translation/TranslationService.java` - 翻译服务实现
- `src/main/java/com/dailyreport/ui/TranslationDialog.java` - 翻译对话框
- `src/main/java/com/dailyreport/action/TranslateAction.java` - 翻译Action
- `src/test/java/com/dailyreport/test/TranslationTest.java` - 翻译功能测试

#### 修改文件
- `src/main/java/com/dailyreport/config/PluginGlobalConfig.java` - 添加TranslationConfig配置类
- `src/main/java/com/dailyreport/ui/PluginConfigurable.java` - 添加翻译配置界面
- `src/main/resources/META-INF/plugin.xml` - 注册翻译Action

### 使用说明

1. 配置翻译服务：
   - 打开 `File` -> `Settings` -> `Tools` -> `日报周报插件`
   - 切换到"翻译配置"标签页
   - 选择翻译服务并配置API密钥

2. 设置快捷键：
   - 点击"快捷键"输入框
   - 直接按下想要的快捷键组合

3. 测试翻译：
   - 点击"测试翻译"按钮
   - 查看测试结果

4. 使用翻译功能：
   - 按下快捷键或通过菜单打开翻译对话框
   - 输入中文，按回车翻译
   - 点击结果复制到剪贴板

---

## [1.0.0] - 初始版本

### 初始功能

- 📊 多项目Git日志分析
- 🤖 AI智能总结（支持华为云、百度千帆、OpenAI、Anthropic）
- ⏰ 支持日报、周报、月报
- 📝 Markdown格式报告生成
- 🔧 可配置的AI服务参数

---

**注意：** 版本号规则为 `主版本号.次版本号.修订号`，每次更新修订号+1。