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
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidLibrary {
        namespace = "org.dhis2.mobile.sync"
        compileSdk = libs.versions.sdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
        androidResources { enable = true }
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {}.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    jvm("desktop")

    sourceSets {

        commonMain {
            resources.srcDirs("src/commonMain/composeResources")

            dependencies {
                implementation(project(":commonskmm"))

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(libs.compose.material3.window)
                implementation(libs.lifecycle.runtime.compose)

                // Koin
                api(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.composeVM)
            }
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.koin.test)
            implementation(libs.koin.test.junit5)
            implementation(libs.test.kotlinCoroutines)
            implementation(libs.test.mockitoKotlin)
        }

        androidMain.dependencies {
            implementation(libs.dhis2.android.sdk)
            api(libs.analytics.timber)
            implementation(libs.androidx.work)
            compileOnly(libs.androidx.compose.preview)
            compileOnly(libs.androidx.compose.uitooling)
            api(libs.koin.work)
        }

        getByName("androidHostTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.junit.jupiter)
                implementation(libs.test.turbine)
                implementation(libs.test.kotlinCoroutines)
                implementation(libs.test.mockitoKotlin)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.test.junit.ext)
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
    packageOfResClass = "org.dhis2.mobile.sync.resources"
    generateResClass = always
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
}
