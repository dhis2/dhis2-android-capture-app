plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.compose.compiler)
}

kotlin {

    android {
        namespace = "org.jica.mch"
        compileSdk {
            version = release(libs.versions.sdk.get().toInt())
        }
        minSdk = libs.versions.minSdk.get().toInt()

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    jvm("desktop")

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.ui)
                implementation(libs.compose.material3)
                val designSystem = libs.dhis2.mobile.designsystem
                implementation("${designSystem.get().group}:${designSystem.get().name}:${designSystem.get().version}") {
                    isChanging = true
                }
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.fragmentKtx)
                implementation(libs.androidx.compose.preview)
                implementation(libs.androidx.compose.uitooling)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.test.junit.ext)
                implementation(libs.test.testCore)
                implementation(libs.test.testRunner)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.compose.desktop.common)
            }
        }
    }
}
