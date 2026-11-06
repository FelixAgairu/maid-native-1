import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Read version from temporary file created by bumpAndBuildRelease task
// Fall back to version.properties if temp file doesn't exist
val versionTempFile = File(projectDir, ".version_temp")
val versionNameProp = if (versionTempFile.exists()) {
    val content = versionTempFile.readText()
    val parts = content.split("|")
    parts.getOrNull(0) ?: "1.9.0"
} else {
    val props = Properties()
    val versionFile = File(projectDir.parentFile, "version.properties")
    if (versionFile.exists()) {
        versionFile.inputStream().use { props.load(it) }
        props.getProperty("versionName", "1.9.0")
    } else {
        "1.9.0"
    }
}
val versionCodeProp = if (versionTempFile.exists()) {
    val content = versionTempFile.readText()
    val parts = content.split("|")
    parts.getOrNull(1)?.toInt() ?: 0
} else {
    val props = Properties()
    val versionFile = File(projectDir.parentFile, "version.properties")
    if (versionFile.exists()) {
        versionFile.inputStream().use { props.load(it) }
        props.getProperty("versionCode")?.toInt() ?: 0
    } else {
        0
    }
}

android {
    namespace = "com.hatsyrei.maidnative"
    // 37 is the floor imposed by markdown-renderer since 0.43.0. targetSdk stays at 36
    // deliberately: compiling against newer APIs is independent of opting in to
    // new runtime behavior.
    compileSdk = 37

    defaultConfig {
        applicationId = "com.hatsyrei.maidnative"
        minSdk = 26
        targetSdk = 36
        
        // Read version from temporary file created by bumpAndBuildRelease task
        val versionTempFile = File(projectDir, ".version_temp")
        versionCode = if (versionTempFile.exists()) {
            val content = versionTempFile.readText()
            val parts = content.split("|")
            parts.getOrNull(1)?.toInt() ?: 0
        } else {
            val props = Properties()
            val versionFile = File(projectDir.parentFile, "version.properties")
            if (versionFile.exists()) {
                versionFile.inputStream().use { props.load(it) }
                props.getProperty("versionCode")?.toInt() ?: 0
            } else {
                0
            }
        }
        versionName = if (versionTempFile.exists()) {
            val content = versionTempFile.readText()
            val parts = content.split("|")
            parts.getOrNull(0) ?: "1.9.0"
        } else {
            val props = Properties()
            val versionFile = File(projectDir.parentFile, "version.properties")
            if (versionFile.exists()) {
                versionFile.inputStream().use { props.load(it) }
                props.getProperty("versionName", "1.9.0")
            } else {
                "1.9.0"
            }
        }

        // androidx.graphics and DataStore ship small native libs; only arm64 devices are targeted.
        ndk {
            abiFilters += providers.gradleProperty("maid.testAbi").orElse("arm64-v8a").get()
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("maid-release-key.jks")
            storePassword = "maidrelease"
            keyAlias = "maid"
            keyPassword = "maidrelease"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    // In-app language switching must work offline, including in future app bundles.
    bundle { language { enableSplit = false } }

    androidResources {
        // OkHttp reads this only to parse cookies, and no client here has a CookieJar.
        ignoreAssetsPatterns += "!PublicSuffixDatabase.list"
    }

    packaging {
        resources {
            // Coroutines debug agent, kotlin-reflect metadata and library version stamps: unused at runtime.
            excludes += listOf(
                "DebugProbesKt.bin",
                "kotlin/**.kotlin_builtins",
                "META-INF/*.version",
                "META-INF/version-control-info.textproto",
            )
        }
    }

    // Needed to strip the dependencies' native libs; must be an installed NDK.
    ndkVersion = providers.gradleProperty("maid.ndkVersion").orElse("27.1.12297006").get()
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.javascriptengine)
    implementation(libs.androidx.concurrent.futures.ktx)
    implementation(libs.markdown.renderer)
    implementation(libs.markdown.renderer.m3)
    implementation(libs.markdown.renderer.coil3)
    implementation(libs.coil.network.okhttp)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
    testImplementation(libs.org.json)
}

// Task to bump version and build release APK
val bumpAndBuildRelease = tasks.register("bumpAndBuildRelease") {
    notCompatibleWithConfigurationCache("Reads and writes version.properties")
    doFirst {
        val versionFile = File(projectDir.parentFile, "version.properties")
        
        // Read current version from version.properties
        val props = Properties()
        if (versionFile.exists()) {
            val inputStream = versionFile.inputStream()
            try {
                props.load(inputStream)
            } finally {
                inputStream.close()
            }
        }
        
        var currentVersion = props.getProperty("versionName", "1.9.0")
        println("Current version: $currentVersion")
        
        // Parse version components
        val parts = currentVersion.split(".")
        if (parts.size != 3) {
            throw RuntimeException("Version must be in format X.Y.Z, got: $currentVersion")
        }
        
        val major = parts[0].toInt()
        val minor = parts[1].toInt()
        val patch = parts[2].toInt()
        
        // Bump patch version
        val newPatch = patch + 1
        val newVersion = "$major.$minor.$newPatch"
        println("Bumping to version: $newVersion")
        
        // Use Unix timestamp (seconds) as versionCode
        val timestamp = (System.currentTimeMillis() / 1000).toInt()
        println("Setting versionCode to timestamp: $timestamp")
        
        // Update version.properties
        props.setProperty("versionName", newVersion)
        props.setProperty("versionCode", timestamp.toString())
        val os = versionFile.outputStream()
        try {
            props.store(os, null)
        } finally {
            os.close()
        }
        println("Updated version.properties")
        
        // Write version to a temporary file for the build to read
        val versionTempFile = File(projectDir, ".version_temp")
        versionTempFile.writeText("$newVersion|$timestamp")
        println("Wrote version to temp file: $newVersion|$timestamp")
    }
}

// Make assembleRelease depend on bumpAndBuildRelease
afterEvaluate {
    tasks.named("assembleRelease") {
        dependsOn(bumpAndBuildRelease)
    }
    
    // Also make the android block depend on bumpAndBuildRelease
    tasks.named("processReleaseManifest") {
        dependsOn(bumpAndBuildRelease)
    }
    
    // Task to rename APK file with version information
    tasks.register("renameApk") {
        doLast {
            val versionTempFile = File(projectDir, ".version_temp")
            val versionInfo = if (versionTempFile.exists()) {
                val content = versionTempFile.readText()
                val parts = content.split("|")
                parts.getOrNull(0) ?: "1.9.0"
            } else {
                "1.9.0"
            }
            
            val apkFile = File(projectDir, "build/outputs/apk/release/app-release.apk")
            if (apkFile.exists()) {
                val newName = "MaidNative-$versionInfo.apk"
                val newApkFile = File(apkFile.parentFile, newName)
                apkFile.renameTo(newApkFile)
                println("Renamed APK from ${apkFile.name} to $newName")
            }
        }
    }
    
    // Make assembleRelease depend on renameApk
    tasks.named("assembleRelease") {
        finalizedBy("renameApk")
    }
}
