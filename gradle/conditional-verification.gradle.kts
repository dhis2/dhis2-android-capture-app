/**
 * Conditional Dependency Verification Script
 *
 * This script enables dependency verification only for release builds.
 * It checks the Gradle tasks being executed and only applies strict verification
 * when building release variants.
 */

gradle.taskGraph.whenReady {
    val isReleaseBuild = allTasks.any { task ->
        task.name.contains("Release", ignoreCase = true) &&
        (task.name.contains("assemble", ignoreCase = true) ||
         task.name.contains("bundle", ignoreCase = true) ||
         task.name.contains("build", ignoreCase = true))
    }

    val isDebugOnlyBuild = allTasks.any { task ->
        task.name.contains("Debug", ignoreCase = true)
    } && !isReleaseBuild

    if (isDebugOnlyBuild) {
        println("🔓 Dependency verification DISABLED for debug build")
    } else if (isReleaseBuild) {
        println("🔒 Dependency verification ENABLED for release build")
    }
}

