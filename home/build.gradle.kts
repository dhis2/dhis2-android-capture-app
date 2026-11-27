import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

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
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "org.dhis2.mobile.home"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        enableCoreLibraryDesugaring = true
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
        androidResources { enable = true }
        withHostTestBuilder {}.configure {
            isReturnDefaultValues = true
        }
        withDeviceTestBuilder {}.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    jvm("desktop")

    iosArm64()
    iosSimulatorArm64()

    // jvmCommonMain holds the JVM-only code shared by android and desktop, so commonMain stays
    // compilable for iOS.
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvmCommon") {
                withJvm()
                withCompilations { it.platformType == KotlinPlatformType.androidJvm }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":commonskmm"))

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.material3.window)
            implementation(libs.compose.ui.uiToolingPreview)
            implementation(libs.lifecycle.runtime.compose)

            // Koin
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeVM)

            // Design system
            implementation(libs.dhis2.mobile.designsystem)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.koin.test)
            implementation(libs.test.kotlinCoroutines)
            implementation(libs.test.turbine)
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.dhis2.android.sdk)
            compileOnly(libs.androidx.compose.preview)
            compileOnly(libs.androidx.compose.uitooling)
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
                // "Run Preview" on a device installs this test APK and starts ui-tooling's
                // PreviewActivity, so the activity must be in its manifest.
                implementation(project.dependencies.platform(libs.androidx.compose.bom))
                implementation(libs.androidx.compose.uitooling)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.compose.desktop.common)
            }
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "org.dhis2.mobile.home.resources"
    generateResClass = always
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
    // Android Studio renders @Preview through ui-tooling, so it must be on the runtime classpath.
    "androidRuntimeClasspath"(libs.androidx.compose.uitooling)
}
