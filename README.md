[**English**](README.md) | [简体中文](README.zh-CN.md)

# Maid Native 1

A lightweight native Android AI chat client for OpenAI-compatible APIs, including local [llama.cpp](https://github.com/ggml-org/llama.cpp) servers.

Built with Kotlin and Jetpack Compose, Maid Native 1 focuses on flexible local LLM access, advanced generation controls, and extensible tools.

## Features

Maid Native 1 builds on the original Maid Native Android application and incorporates additional functionality and improvements from the `billertwerson-gif` fork, along with further development for customized local and remote LLM setups.

### Connection and model selection

- LAN endpoint discovery for finding compatible local inference servers.
- Endpoint presets for switching between configured servers.
- Searchable model selection with optional segmented model-name tags.
- Improved visibility of context capacity and token usage when reported by the server.
- Optional custom CA trust for API and MCP connections.

### Generation and conversation

- Additional sampling controls for temperature, Top P, Top K, Min P, frequency penalty, and presence penalty.
- Collapsible reasoning output and a configurable Thinking option for compatible endpoints.
- Response statistics, including generation speed, timing, and token counts where available.
- Context and cumulative token-usage indicators.
- Conversation conveniences such as local automatic titles and controls for expanding long responses.

### Tools and extensibility

- Optional on-device tools for date and time, dice rolling, URL fetching, and JavaScript execution.
- Per-tool permission controls with Off, Ask, and Allow modes.
- Editable tool-call arguments and results.
- Linked text files for supported conversation and document workflows.
- Remote MCP servers over Streamable HTTP, with optional bearer-token authentication and per-tool permissions.

### Personalization and usability

- AMOLED true-black theme and customizable accent color.
- Custom user and assistant display names and profile pictures.
- Custom composer artwork.
- Selectable interface languages, including regional variants.
- Improvements to model selection, context visibility, and conversation controls.

### Security and data management

- Encrypted storage of API credentials using Android Keystore.
- Optional certificate configuration for API and MCP connections.
- Conversation import and export, along with backup functionality.
- Local conversation history for offline browsing.

Feature availability depends on the connected server, model capabilities, Android version, and current app configuration.

For implementation details, consult the source code and the relevant documentation in this repository.

Feature availability depends on the connected endpoint and model. Tool calling, media input, reasoning controls, and usage statistics require appropriate server support.

## Requirements

- Android SDK with the project's configured compile SDK and build-tools versions.
- JDK 21.
- Android SDK environment configured through `ANDROID_HOME` or `local.properties`.
- A compatible Android device or emulator for running the app.

## Build

Clone the repository:

```bash
git clone https://github.com/FelixAgairu/maid-native-1.git
cd maid-native-1
```

If necessary, configure your Android SDK location in `local.properties`:

```properties
sdk.dir=/path/to/android-sdk
```

Build a debug APK:

```bash
./gradlew assembleDebug
```

Build a release APK:

```bash
./gradlew assembleRelease
```

Run the unit tests:

```bash
./gradlew test
```

Alternatively, use the provided build script:

```bash
./build.sh debug
./build.sh release
./build.sh test
```

APK output directories:

```text
app/build/outputs/apk/debug/
app/build/outputs/apk/release/
```

Release signing requires a configured release keystore. See the repository's release-signing configuration and keep all signing credentials outside version control.

## Connecting to a Local LLM

1. Start an OpenAI-compatible inference server, such as llama.cpp.
2. Ensure your Android device can reach the server over your network.
3. Add the server's endpoint URL in the app.
4. Configure authentication if required.
5. Select an available model and start chatting.

For example, a server exposing the OpenAI-compatible API at port `8080` might use an endpoint URL such as:

```text
http://192.168.1.100:8080/v1
```

Replace the address with your server's LAN IP. Use HTTPS or another appropriately secured connection when communicating over untrusted networks.

## Credits

Maid Native 1 builds on work from:

- [Mobile-Artificial-Intelligence/maid](https://github.com/Mobile-Artificial-Intelligence/maid)
- [HatsyRei/maid-native](https://github.com/HatsyRei/maid-native)
- [billertwerson-gif/maid-native](https://github.com/billertwerson-gif/maid-native)

See the accompanying license files for the applicable licensing terms and attribution requirements.

## License

This project is distributed under the Mozilla Public License 2.0, subject to the terms of `LICENSE` and the applicable third-party licenses.
