// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenLocal()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
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
    id("org.jlleitschuh.gradle.ktlint").version("11.3.2")
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
        listOf("RELEASE", "FINAL", "GA").any { it -> version.toUpperCase().contains(it) }
    val regex = """^[0-9,.v-]+(-r)?$""".toRegex()
    !stableKeyword && !(version matches regex)
}

allprojects {
    configurations.all {
        resolutionStrategy {
            eachDependency {
                if (requested.group == "org.jacoco")
                    useVersion("0.8.10")
            }
        }
    }

    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.google.com")
        }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")

            authentication {
                create<BasicAuthentication>("basic")
            }

            val mapboxDownloadsToken = System.getenv("MAPBOX_DOWNLOADS_TOKEN")
                ?: project.properties["MAPBOX_DOWNLOADS_TOKEN"] ?: ""
            credentials {
                // This should always be `mapbox` (not your username).
                username = "mapbox"
                password = mapboxDownloadsToken as String
            }
        }
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
        debug.set(true)
        verbose.set(true)
        android.set(true)
        outputToConsole.set(true)
        enableExperimentalRules.set(true)
        filter {
            excludes.add("**/*.kts")
            exclude { element -> element.file.path.contains("androidTest") }
            exclude { element -> element.file.path.contains("dhis2-android-sdk") }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}



