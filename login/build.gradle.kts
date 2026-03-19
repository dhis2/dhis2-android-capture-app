plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

apply(from = "${project.rootDir}/jacoco/jacoco-kmp.gradle.kts")

kotlin {

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }

    androidLibrary {
        namespace = "org.dhis2.mobile.login"
        compileSdk = libs.versions.sdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        enableCoreLibraryDesugaring = true
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
        androidResources { enable = true }
        packaging {
            resources {
                pickFirsts.add("values*/**")
            }
        }
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder { sourceSetTreeName = "test" }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    jvm("desktop")

    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
    }

    sourceSets {
        commonMain {
            resources.srcDirs("src/commonMain/composeResources")

            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(compose.material3)
                api(compose.materialIconsExtended)
                val designSystem = libs.dhis2.mobile.designsystem
                implementation("${designSystem.get().group}:${designSystem.get().name}:${designSystem.get().version}") {
                    isChanging = true
                }
                implementation(libs.compose.material3.window)
                implementation(compose.components.resources)
                implementation(project(":commonskmm"))
                implementation(libs.navigation.compose)
                implementation(compose.components.uiToolingPreview)

                // Koin
                api(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.composeVM)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.test.turbine)
                implementation(libs.test.kotlinCoroutines)
                implementation(libs.test.mockitoKotlin)
                implementation(compose.components.resources)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.compose.preview)
                implementation(libs.dhis2.android.sdk)
                // Koin support for Android
                implementation(libs.koin.android)
                implementation(libs.koin.androidx.compose)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.browser)
                implementation(libs.androidx.compose.uitooling)
            }
        }

        getByName("androidHostTest") {
            dependencies {
                implementation(libs.junit.jupiter)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.test.compose.ui.test.junit4.android)
                implementation(libs.test.ui.test.manifest)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
    }

}

compose.resources {
    publicResClass = false
    packageOfResClass = "org.dhis2.mobile.login.resources"
    generateResClass = always
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
}
