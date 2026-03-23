# IntelliJ IDEA 日报周报插件

一个用于IntelliJ IDEA的自动化日报/周报生成插件，支持多项目Git日志分析和AI智能总结。

## 功能特性

- 📊 **多项目支持**：同时分析多个Git项目的提交记录
- 🔄 **未提交改动分析**：检测并分析未提交的代码改动，包括修改、新增、删除和未跟踪的文件
- 🤖 **AI智能总结**：集成AI模型自动生成工作总结，包含已提交和未提交的工作内容
- ⏰ **灵活时间维度**：支持日报、周报、月报等多种时间范围
- 📝 **Markdown格式**：生成结构化的Markdown格式报告
- 🔧 **可配置**：支持自定义AI模型、API密钥等配置
- 🌐 **中文翻译**：支持将中文翻译为多种编程命名格式（变量名、常量名、驼峰命名、方法名、类名、文件名）

## 支持的AI服务

- 华为云ModelArts（默认）
- 百度千帆Coding Plan
- OpenAI兼容接口
- Anthropic兼容接口

## 快速开始

### 1. 安装插件

1. 下载编译好的插件包：`build/libs/daily-report-plugin-1.0.0.jar`
2. 在IntelliJ IDEA中：`File` -> `Settings` -> `Plugins`
3. 点击齿轮图标 -> `Install Plugin from Disk...`
4. 选择下载的jar文件
5. 重启IDEA

### 2. 配置插件

1. 打开设置：`File` -> `Settings` -> `Tools` -> `日报周报插件`
2. 在"项目配置"标签页添加你的项目路径
3. 在"AI配置"标签页配置AI服务：
   - 选择协议类型（华为云/百度千帆/OpenAI/Anthropic）
   - 输入Base URL
   - 输入API Key
   - 输入模型名称
   - 设置请求超时时间
4. 点击"测试连接"验证配置

### 3. 生成报告

1. 在IDEA菜单栏选择：`Tools` -> `生成工作报告`
2. 选择报告类型（日报/周报/月报）
3. 选择时间范围
4. 点击生成

### 4. 使用翻译功能

1. 配置翻译服务（见下方"翻译配置"）
2. 按下自定义快捷键（默认：Ctrl+Shift+T，可在配置中修改）
3. 或通过 `Tools` 菜单选择 `中文转编程命名`
4. 在弹出的对话框中输入中文
5. 按回车键翻译
6. 点击任意翻译结果复制到剪贴板

## 配置说明

### AI配置

| 协议类型 | Base URL | 默认模型 |
|---------|----------|---------|
| HuaweiCloud | https://api.modelarts-maas.com/v2/chat/completions | qwen3-30b-a3b |
| Baidu Qianfan | https://qianfan.baidubce.com/v2/coding/chat/completions | qianfan-code-latest |
| OpenAI | https://api.openai.com/v1/chat/completions | gpt-3.5-turbo |
| Anthropic | https://api.anthropic.com/v1/messages | claude-3-sonnet |

### 项目配置

- **项目路径**：Git项目的根目录
- **启用状态**：是否在报告中包含该项目

### 翻译配置

#### 支持的翻译服务

| 服务提供商 | 需要的配置 |
|-----------|-----------|
| 百度翻译 | App ID、密钥 |
| 有道翻译 | App Key、App Secret |

#### 配置步骤

1. 打开设置：`File` -> `Settings` -> `Tools` -> `日报周报插件`
2. 切换到"翻译配置"标签页
3. 选择翻译服务提供商（百度/有道）
4. 填写相应的API密钥：
   - **百度翻译**：需要 App ID 和密钥
   - **有道翻译**：需要 App Key 和 App Secret
5. 配置快捷键：
   - 点击"快捷键"输入框
   - 直接按下你想要的快捷键组合（如 Ctrl+Shift+Y）
   - 输入框会自动显示识别到的快捷键
6. 选择要显示的命名格式（可多选）：
   - 变量名 (snake_case)
   - 常量名 (UPPER_SNAKE_CASE)
   - 驼峰命名 (camelCase)
   - 方法名 (methodName)
   - 类名 (ClassName)
   - 文件名 (file_name)
7. 点击"测试翻译"按钮验证配置

#### API获取方式

**百度翻译API：**
1. 访问 [百度翻译开放平台](https://fanyi-api.baidu.com/)
2. 注册并登录
3. 进入管理控制台
4. 创建应用，获取 App ID 和密钥

**有道翻译API：**
1. 访问 [有道智云](https://open.youdao.com/)
2. 注册并登录
3. 创建应用，获取 App Key 和 App Secret

## 开发构建

详细的编译构建指南请参考 [编译构建指南.md](./编译构建指南.md)

## 项目结构

```
daily-report-plugin/
├── src/
│   ├── main/
│   │   ├── java/com/dailyreport/
│   │   │   ├── action/          # 菜单动作
│   │   │   ├── ai/              # AI集成
│   │   │   ├── config/          # 配置管理
│   │   │   ├── generator/        # 报告生成
│   │   │   ├── git/             # Git分析
│   │   │   └── ui/              # 用户界面
│   │   └── resources/
│   │       └── META-INF/
│   │           └── plugin.xml     # 插件配置
│   └── test/                    # 测试代码
├── build.gradle.kts               # Gradle构建配置
├── gradle-8.5/                 # Gradle运行时
└── gradlew.bat                  # Gradle包装脚本
```

## 依赖项

- IntelliJ Platform SDK
- JGit (Git操作)
- OkHttp (HTTP客户端)
- Gson (JSON处理)

## 许可证

本项目采用MIT许可证。

## 贡献

欢迎提交Issue和Pull Request！

## 联系方式

如有问题或建议，请通过GitHub Issues联系我们。