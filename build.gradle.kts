// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
        google()
    }
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
}

// Variables to hold aggregated test results
var totalTestsRun: Long = 0
var totalTestsPassed: Long = 0
var totalTestsFailed: Long = 0
var totalTestsSkipped: Long = 0
var totalModules: MutableList<String> = mutableListOf()
var failedTests: MutableList<String> = mutableListOf()

sonarqube {
    properties {
        val branch = System.getenv("GIT_BRANCH")
        val targetBranch = System.getenv("GIT_BRANCH_DEST")
        val pullRequestId = System.getenv("PULL_REQUEST")


        property("sonar.projectKey", "dhis2_dhis2-android-capture-app")
        property("sonar.organization", "dhis2")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectName", "android capture app")

        if (pullRequestId == null) {
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
            cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
            eachDependency {
                if (requested.group == "org.jacoco")
                    useVersion("0.8.10")
            }
        }
    }

    repositories {
        mavenLocal()
        maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.google.com")
        }
        maven { url = uri("https://jitpack.io") }
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
