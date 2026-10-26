import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "org.dhis2.mobile.app"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
        androidResources { enable = true }
        withHostTestBuilder {}.configure {}
    }

    jvm("desktop")

    // navigation3 publishes no iosX64 artifact, so Intel simulators are not supported.
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.ui)
                implementation(libs.compose.material3)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.ui.uiToolingPreview)
                implementation(libs.dhis2.mobile.designsystem)
                implementation(libs.navigation3.ui)
                implementation(libs.lifecycle.viewmodel.navigation3)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.composeVM)
                implementation(project(":commonskmm"))
                implementation(project(":login"))
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.test.kotlinCoroutines)
                implementation(libs.test.mockitoKotlin)
                implementation(libs.test.turbine)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                // Provides Dispatchers.Main on the JVM
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "org.dhis2.mobile.app.resources"
    generateResClass = always
}

compose.desktop {
    application {
        mainClass = "org.dhis2.mobile.app.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "DHIS2Desktop"
            packageVersion = "1.0.0"
        }
    }
}
