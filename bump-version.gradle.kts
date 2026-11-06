// Script to bump the app version (patch) and build release APK
// Usage: ./gradlew -I bump-version.gradle.kts bumpAndBuildRelease

import java.io.File

val appDir = projectDir
val buildFile = File(appDir, "app/build.gradle.kts")

fun bumpVersion() {
    println("Reading current version from build.gradle.kts...")
    val content = buildFile.readText()
    
    // Extract current versionName
    val versionNameMatch = Regex("versionName\\s*=\\s*\"([^\"]+)\"").find(content)
    if (versionNameMatch == null) {
        throw RuntimeException("Could not find versionName in build.gradle.kts")
    }
    val currentVersion = versionNameMatch.groupValues[1]
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
    
    // Extract current versionCode
    val versionCodeMatch = Regex("versionCode\\s*=\\s*(\\d+)").find(content)
    if (versionCodeMatch == null) {
        throw RuntimeException("Could not find versionCode in build.gradle.kts")
    }
    val currentCode = versionCodeMatch.groupValues[1].toInt()
    val newCode = currentCode + 1
    println("Bumping versionCode from $currentCode to $newCode")
    
    // Update build.gradle.kts
    val updatedContent = content
        .replace("versionName = \"$currentVersion\"", "versionName = \"$newVersion\"")
        .replace("versionCode = $currentCode", "versionCode = $newCode")
    
    buildFile.writeText(updatedContent)
    println("Updated build.gradle.kts")
    
    return newVersion
}

// Run version bump
val newVersion = bumpVersion()

// Now build the release APK
println("Building release APK...")
project.gradle.rootProject.tasks.getByName("assembleRelease").execute()

println("Done! New version: $newVersion")
println("APK location: app/build/outputs/apk/release/app-release.apk")