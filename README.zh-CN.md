[English](README.md) | [**简体中文**](README.zh-CN.md)

# Maid Native 1

这是一款轻量级的原生 Android AI 聊天客户端，支持兼容 OpenAI API 的服务（包括本地运行的 [llama.cpp](https://github.com/ggml-org/llama.cpp) 服务器）。

Maid Native 1 采用 Kotlin 和 Jetpack Compose 构建，专注于提供灵活的本地大语言模型（LLM）访问方式、高级生成控制功能以及可扩展的工具集。

## 功能特性

Maid Native 1 在原版 Maid Native Android 应用程序的基础上，整合了来自 `billertwerson-gif` 分支的功能与改进，并针对本地及远程大语言模型的自定义配置进行了进一步开发。

### 连接与模型选择

- 支持局域网（LAN）端点发现，可自动查找兼容的本地推理服务器。
- 提供端点预设功能，方便在不同已配置的服务器之间快速切换。
- 支持搜索的模型选择功能，并可选用分段式模型名称标签。
- 优化了上下文容量及 Token 使用情况的显示（在服务器提供相关数据时）。
- 支持为 API 和 MCP 连接配置自定义 CA 信任证书。

### 生成与对话

- 提供丰富的采样控制参数，包括 Temperature（温度）、Top P、Top K、Min P、频率惩罚（Frequency Penalty）和存在惩罚（Presence Penalty）。
- 支持折叠推理过程输出；针对兼容的端点，还提供可配置的“思考”（Thinking）选项。
- 提供响应统计信息，包括生成速度、耗时以及 Token 计数（在服务器支持的情况下）。
- 显示上下文及累计 Token 使用情况。
- 提供多种对话辅助功能，例如自动生成本地对话标题，以及用于展开长文本响应的控制选项。

### 工具与可扩展性

- 内置多种可选的设备端工具，涵盖日期与时间获取、掷骰子、URL 内容抓取以及 JavaScript 代码执行。
- 支持针对单个工具设置权限，提供“关闭”、“询问”和“允许”三种模式。
- 支持编辑工具调用的参数及执行结果。
- 支持关联文本文件，适用于各类对话及文档处理工作流。
- 支持通过 HTTP 流（Streamable HTTP）连接远程 MCP 服务器，并可配置 Bearer Token 认证及针对单个工具的权限控制。

### 个性化与易用性

- 提供 AMOLED 纯黑主题，并支持自定义强调色。
- 支持自定义用户与助手的显示名称及头像。
- 支持自定义输入区域的视觉元素（Composer Artwork）。
- 提供多种界面语言选择（包含不同地区的语言变体）。
- 针对模型选择、上下文显示及对话控制功能进行了多项优化。 ### 安全与数据管理

- 使用 Android Keystore 对 API 凭据进行加密存储。
- 支持为 API 和 MCP 连接配置可选证书。
- 提供对话导入、导出及备份功能。
- 支持本地保存对话历史，以便离线浏览。

具体功能的可用性取决于所连接的服务器、模型能力、Android 版本以及当前的应用程序配置。

如需了解实现细节，请参阅本仓库中的源代码及相关文档。

功能的可用性取决于所连接的端点（Endpoint）及模型。工具调用（Tool calling）、媒体输入、推理控制以及使用统计信息等功能，均需服务器端提供相应的支持。

## 要求

- Android SDK（需符合本项目配置的编译 SDK 及构建工具版本）。
- JDK 21。
- 已通过 `ANDROID_HOME` 或 `local.properties` 配置好 Android SDK 环境。
- 运行应用程序所需的兼容 Android 设备或模拟器。

## 构建

克隆代码仓库：

```bash
git clone https://github.com/FelixAgairu/maid-native-1.git
cd maid-native-1
```

如有必要，请在 `local.properties` 文件中配置 Android SDK 的路径：

```properties
sdk.dir=/path/to/android-sdk
```

构建调试版 (debug) APK：

```bash
./gradlew assembleDebug
```

构建发布版 (release) APK：

```bash
./gradlew assembleRelease
```

运行单元测试：

```bash
./gradlew test
```

或者，也可以使用提供的构建脚本：

```bash
./build.sh debug
./build.sh release
./build.sh test
```

APK 输出目录：

```text
app/build/outputs/apk/debug/
app/build/outputs/apk/release/
```

发布版签名需要配置相应的密钥库 (keystore)。请参阅代码仓库中的发布签名配置说明，并确保所有签名凭据均未纳入版本控制系统。

## 连接至本地大语言模型 (LLM)

1. 启动一个兼容 OpenAI API 的推理服务器（例如 llama.cpp）。
2. 确保您的 Android 设备能够通过网络访问该服务器。
3. 在应用中添加服务器的端点 URL。
4. 如有需要，配置身份验证信息。
5. 选择可用模型，即可开始对话。

例如，如果服务器在 `8080` 端口上提供了兼容 OpenAI 的 API，其端点 URL 可能如下所示：

```text
http://192.168.1.100:8080/v1
```

请将上述地址替换为您服务器的局域网 (LAN) IP 地址。在不可信的网络环境下进行通信时，请务必使用 HTTPS 或其他具备适当安全保障的连接方式。

## 致谢

Maid Native 1 基于以下项目构建：

- [Mobile-Artificial-Intelligence/maid](https://github.com/Mobile-Artificial-Intelligence/maid)
- [HatsyRei/maid-native](https://github.com/HatsyRei/maid-native)
- [billertwerson-gif](https://github.com/billertwerson-gif)

请参阅随附的许可文件，了解适用的许可条款及署名要求。

## 许可协议

本项目采用 Mozilla Public License 2.0 协议发布，并受 `LICENSE` 文件中的条款以及相关第三方许可协议的约束。
