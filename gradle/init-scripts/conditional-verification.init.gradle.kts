
/**
 * Gradle Init Script for Conditional Dependency Verification
 *
 * This init script enables strict dependency verification only for release builds.
 * For debug builds, verification is disabled to speed up development.
 *
 * Usage:
 *   Place this file in: gradle/init-scripts/conditional-verification.init.gradle.kts
 *   Or specify via command line: gradlew --init-script gradle/init-scripts/conditional-verification.init.gradle.kts build
 */

gradle.settingsEvaluated {
    // Check which tasks are being requested
    val requestedTasks = gradle.startParameter.taskNames

    val isReleaseBuild = requestedTasks.any { taskName ->
        taskName.contains("Release", ignoreCase = true) ||
        taskName.contains("release", ignoreCase = true)
    }

    val isDebugOnlyBuild = requestedTasks.any { taskName ->
        taskName.contains("Debug", ignoreCase = true) ||
        taskName.contains("debug", ignoreCase = true)
    } && !isReleaseBuild

    // If it's a debug-only build, disable verification
    if (isDebugOnlyBuild || requestedTasks.isEmpty() ||
        requestedTasks.any { it in listOf("help", "tasks", "projects", "dependencies", "clean") }) {

        println("🔓 Dependency verification DISABLED (debug/development build)")
        gradle.startParameter.isDependencyVerificationEnabled = false

    } else if (isReleaseBuild) {
        println("🔒 Dependency verification ENABLED (release build)")
        gradle.startParameter.isDependencyVerificationEnabled = true
    }
}

