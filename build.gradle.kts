// Top-level build file where you can add configuration options common to all sub-projects/modules.
group = "org.hisp.dhis"
version = libs.versions.vName.get()

buildscript {
    dependencies {
        classpath(libs.gradlePlugin)
        classpath(libs.kotlinPlugin)
        classpath(libs.jacoco)
        classpath(libs.kotlinSerialization)
    }
}

plugins {
    alias(libs.plugins.ktlint)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.cyclonedx)
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.lint) apply false

}

// Variables to hold aggregated test results
var totalTestsRun: Long = 0
var totalTestsPassed: Long = 0
var totalTestsFailed: Long = 0
var totalTestsSkipped: Long = 0
var totalModules: MutableList<String> = mutableListOf()
var failedTests: MutableList<String> = mutableListOf()

sonar {
    properties {
        val branch = System.getenv("GIT_BRANCH")
        val targetBranch = System.getenv("GIT_BRANCH_DEST")
        val pullRequestId = System.getenv("PULL_REQUEST")


        property("sonar.projectKey", "dhis2_dhis2-android-capture-app")
        property("sonar.organization", "dhis2")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectName", "android capture app")

        // Workaround for SCANGRADLE-410: sonar-scanner-gradle 7.3.1.8318 leaves
        // sonar.java.binaries empty under AGP 9, breaking analysis of remaining
        // .java sources. Remove once the upstream fix is released.
        property("sonar.exclusions", "**/*.java")

        // GitHub Actions always defines PULL_REQUEST, resolving it to an empty
        // string on push events, so a null check alone sends push builds down the
        // pull-request path with a blank sonar.pullrequest.key. Since scanner
        // 7.3.x that is rejected outright and the analysis fails.
        if (pullRequestId.isNullOrEmpty()) {
            property("sonar.branch.name", branch)
        } else {
            property("sonar.pullrequest.base", targetBranch)
            property("sonar.pullrequest.branch", branch)
            property("sonar.pullrequest.key", pullRequestId)
        }
    }
}

allprojects {
    configurations.all {
        resolutionStrategy {
            cacheDynamicVersionsFor(10, TimeUnit.MINUTES)
            cacheChangingModulesFor(0, TimeUnit.SECONDS)
            eachDependency {
                if (requested.group == "org.jacoco")
                    useVersion("0.8.10")
            }
        }
    }

    // JUnit Jupiter must never reach a test configuration. useJUnitPlatform() is set
    // nowhere in this build, so a Jupiter @Test is not run -- it is silently never
    // collected: the class compiles, the build goes green, and no result file is
    // written. Failing to resolve is the loud alternative.
    configurations.matching { it.name.contains("test", ignoreCase = true) }.configureEach {
        exclude(group = "org.junit.jupiter")
    }

    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    gradle.projectsEvaluated {
        tasks.withType<JavaCompile> {
            options.compilerArgs.addAll(
                listOf(
                    "-Xmaxerrs",
                    "1000"
                )
            )
        }
    }

    ktlint {
        version.set("1.7.1")
        debug.set(true)
        verbose.set(true)
        android.set(true)
        outputToConsole.set(true)
        enableExperimentalRules.set(true)
        filter {
            excludes.add("**/*.kts")
            exclude { element -> element.file.path.contains("androidTest") }
            exclude { element -> element.file.path.contains("generated") }
            exclude { element -> element.file.path.contains("dhis2-android-sdk") }
        }
    }

    tasks.withType<AbstractTestTask> {
        // ensures test results are not cached between test runs
        outputs.upToDateWhen { false }
        afterSuite(
            KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
                if (result.resultType == TestResult.ResultType.FAILURE) {
                    synchronized(rootProject) {
                        val testName = desc.className + "." + desc.name
                        failedTests.add(testName)
                    }
                }
                if (desc.parent == null) {
                    synchronized(rootProject) {
                        totalModules.add(project.name)
                        totalTestsRun += result.testCount
                        totalTestsPassed += result.successfulTestCount
                        totalTestsFailed += result.failedTestCount
                        totalTestsSkipped += result.skippedTestCount
                    }
                }
            })
        )
    }
}

// One command for humans and CI, so "what CI runs" has a single definition.
// run_tests.sh calls this; ci.yml deliberately still calls the tasks directly, because
// its unit-tests job already needs the separate lint-check job and would run ktlint twice.
//
// No mustRunAfter is needed here: jacocoReport already orders itself after the unit
// tests. The dependsOn loop in jacoco/jacoco.gradle.kts runs inside that task's
// registration action, which Gradle realises after AGP has created the test tasks, so
// findByName does resolve them. Verified with `./gradlew :app:jacocoReport --dry-run`.
val verificationTaskNames = listOf(
    "ktlintCheck",
    "testDebugUnitTest",
    "testDhis2DebugUnitTest",
    "testAndroidHostTest",
    "jacocoReport",
)

tasks.register("verifyAll") {
    group = "verification"
    description = "Runs ktlint, every unit-test task and the coverage reports. Mirrors CI."

    // A Provider, not a plain list: with org.gradle.configureondemand=true the subprojects
    // are not all configured while this script runs, so resolving the tasks eagerly here
    // yields an empty set. A Provider is resolved when the task graph is built, by which
    // point the allprojects { } block above has forced every project to configure.
    dependsOn(
        provider {
            rootProject.allprojects
                .flatMap { project ->
                    verificationTaskNames.mapNotNull { project.tasks.findByName(it) }
                }
                .also { resolved ->
                    // A verification task that resolves to nothing passes without running
                    // anything -- exactly the silent success this task exists to prevent.
                    check(resolved.isNotEmpty()) {
                        "verifyAll resolved no tasks to run."
                    }
                }
        },
    )
}

// Initialize extra properties on the root project for storing totals
rootProject.ext.set("totalTestsRun", 0L)
rootProject.ext.set("totalTestsPassed", 0L)
rootProject.ext.set("totalTestsFailed", 0L)
rootProject.ext.set("totalTestsSkipped", 0L)
rootProject.ext.set("totalModules", mutableListOf<String>())

gradle.addBuildListener(object : BuildAdapter() {
    override fun buildFinished(result: BuildResult) {
        println("================================================")
        println("           AGGREGATED TEST RESULTS")
        println("================================================")
        println("  Modules:  ${totalModules.joinToString(", ")}")
        println("  Total Tests Run: $totalTestsRun")
        println("  Total Passed:   $totalTestsPassed")
        println("  Total Failed:   $totalTestsFailed")
        println("  Total Skipped:  $totalTestsSkipped")
        println("================================================")
        if (totalTestsFailed > 0) {
            println("  Failed Tests:")
            failedTests.forEach {
                println("   ***  $it")
            }
            println("================================================")
        }
    }
})
