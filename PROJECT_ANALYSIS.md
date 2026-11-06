# Maid Native 1 - Project Analysis

## Overview
Maid Native 1 is a lightweight native Android AI chat client for OpenAI-compatible APIs, including local llama.cpp servers. Built with Kotlin and Jetpack Compose.

## Repository Status
- **Branch**: main
- **Commit**: 41d025c "Revise LICENSE.md for clarity and references"
- **Status**: Clean working tree
- **Remote**: https://github.com/FelixAgairu/maid-native-1.git

## Project Structure

### Root Directory
```
maid-native-1/
├── .git/                          # Git repository
├── .gitignore                     # Ignored files
├── LICENCE.MPL2.FelixAgairu        # Main license (MPL 2.0)
├── LICENSE.MIT.HatsyRei           # Upstream MIT license
├── LICENSE.MIT.billertwerson-gif  # Fork MIT license
├── LICENSE.md                     # License overview
├── Logo.svg                       # Project logo
├── README.md                      # Main documentation
├── README.zh-CN.md                # Chinese documentation
├── app/                           # Main Android app module
├── build.gradle.kts               # Top-level build config
├── build.sh                       # Build script
├── docs/                          # Documentation
├── gradle/                        # Gradle configuration
├── gradle.properties              # Gradle properties
├── gradlew                        # Gradle wrapper
├── gradlew.bat                    # Windows Gradle wrapper
├── settings.gradle.kts            # Gradle settings
└── tools/                         # Development tools
```

### App Module Structure
```
app/
├── build.gradle.kts               # Module build config
├── proguard-rules.pro             # ProGuard rules
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml    # Android manifest
│   │   ├── assets/
│   │   │   └── i18n/               # 66+ language translations
│   │   ├── java/com/hatsyrei/maidnative/
│   │   │   ├── MainActivity.kt      # Main activity
│   │   │   ├── data/               # Data layer
│   │   │   ├── domain/             # Domain layer
│   │   │   ├── ui/                 # UI layer
│   │   │   └── ...                 # Other packages
│   │   └── res/                   # Resources
│   └── test/                      # Unit tests
└── ...
```

## Build Configuration

### Dependencies (from libs.versions.toml)
- **AGP**: 9.4.1
- **Kotlin**: 2.4.21
- **Compose BOM**: 2026.09.00
- **AndroidX**: Various latest versions
- **OkHttp**: 5.5.0
- **Room**: 2.8.5
- **Coroutines**: 1.11.0
- **Markdown Renderer**: 0.46.0

### Android Configuration
- **Namespace**: com.hatsyrei.maidnative
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36 (Android 14)
- **Compile SDK**: 37
- **Version**: 1.9.0 (code 23)

## Key Features

### 1. Connection & Model Selection
- LAN endpoint discovery for local inference servers
- Endpoint presets for switching between configured servers
- Searchable model selection with segmented model-name tags
- Context capacity and token usage visibility
- Custom CA trust for API and MCP connections

### 2. Generation & Conversation
- Advanced sampling controls (temperature, Top P, Top K, Min P, penalties)
- Collapsible reasoning output
- Response statistics (generation speed, timing, token counts)
- Context and cumulative token-usage indicators
- Conversation conveniences (automatic titles, long response controls)

### 3. Tools & Extensibility
- On-device tools (date/time, dice rolling, URL fetching, JavaScript)
- Per-tool permission controls (Off, Ask, Allow)
- Editable tool-call arguments and results
- Linked text files for workflows
- Remote MCP servers over Streamable HTTP
- Bearer-token authentication

### 4. Personalization & Usability
- AMOLED true-black theme
- Customizable accent color
- Custom user/assistant names and profile pictures
- Custom composer artwork
- 66+ interface languages with regional variants

### 5. Security & Data Management
- Encrypted storage using Android Keystore
- Certificate configuration for connections
- Conversation import/export and backup
- Local conversation history

## Localization
- **66 languages** supported
- **246 UI strings** per language
- **Bundled in assets/i18n/**
- Offline language switching
- Search by native names, English names, and language tags

## Build Instructions

### Prerequisites
- Android SDK (configured via ANDROID_HOME or local.properties)
- JDK 21
- Android device or emulator

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Alternative build script
./build.sh debug
./build.sh release
./build.sh test
```

### APK Output
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

## Testing
- **212 JVM tests** (all passing)
- **Android Lint**: 0 errors, 16 warnings
- **Validation scripts**: `tools/validate_i18n.py`

## License
- **Main License**: Mozilla Public License 2.0 (MPL-2.0)
- **Upstream Components**: MIT licensed
- **Third-party dependencies**: Various licenses

## Potential Goals

Since the repository is clean and up-to-date with no specific issues indicated, here are potential next steps:

1. **Build Verification**: Verify the project builds successfully
2. **Code Review**: Review code quality and best practices
3. **Testing**: Run tests to ensure everything works
4. **Documentation**: Review and improve documentation
5. **Localization**: Validate and improve translations
6. **Feature Enhancement**: Add new features or improve existing ones
7. **Bug Fixes**: Identify and fix any bugs
8. **Security Review**: Review security implementations

## Next Steps

To proceed with development, the following actions could be taken:

1. **Build the project** to verify it compiles and runs
2. **Run tests** to ensure all functionality works correctly
3. **Review the codebase** for quality and potential improvements
4. **Check for any open issues** on GitHub
5. **Identify specific features** to enhance or bugs to fix

## Notes

- The project uses modern Android development tools and libraries
- It supports a wide range of languages and devices
- Security is a priority with encrypted storage and certificate handling
- The codebase appears well-organized and maintainable
