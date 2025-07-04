import java.util.Locale

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
        google()
    }
    dependencies {
        classpath(libs.gradlePlugin)
        classpath(libs.kotlinPlugin)
        classpath(libs.hiltPlugin)
        classpath(libs.jacoco)
        classpath(libs.kotlinSerialization)
    }
}

plugins {
    id("org.jlleitschuh.gradle.ktlint").version("11.5.1")
    id("org.sonarqube").version("3.5.0.2730")
    id("com.github.ben-manes.versions").version("0.46.0")
}

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

val isNonStable: (String) -> Boolean = { version ->
    val stableKeyword =
        listOf("RELEASE", "FINAL", "GA").any { it -> version.uppercase().contains(it) }
    val regex = """^[0-9,.v-]+(-r)?$""".toRegex()
    !stableKeyword && !(version matches regex)
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
        maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.google.com")
        }
        maven { url = uri("https://jitpack.io") }
        mavenLocal()
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
        version.set("0.50.0")
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
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}



