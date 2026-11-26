import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }

    android {
        namespace = "org.dhis2.mobile.extensibility"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
        withHostTestBuilder {}.configure {
            isReturnDefaultValues = true
        }
        withDeviceTestBuilder {}.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":commonskmm"))

            // Koin
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)

        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.koin.test)
            implementation(libs.test.kotlinCoroutines)
            implementation(libs.test.turbine)
        }

        androidMain.dependencies {
            implementation(libs.dhis2.android.sdk)
        }

        getByName("androidHostTest") {
            dependencies {
                // Mockito is JVM-only, so it stays out of commonTest, which also compiles for iOS.
                implementation(libs.test.mockitoKotlin)
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

dependencies {
    coreLibraryDesugaring(libs.desugar)
}
