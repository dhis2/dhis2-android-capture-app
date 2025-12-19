import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.compose)
    id("com.android.library")
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }
    jvm("desktop")



    sourceSets {

        commonMain {
            resources.srcDirs("src/commonMain/composeResources")

            dependencies {

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

        }

        androidUnitTest.dependencies {

        }

        androidInstrumentedTest.dependencies {
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

android {
    namespace = "org.dhis2.mobile.sync"
    compileSdk = libs.versions.sdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        val bitriseSentryDSN = System.getenv("SENTRY_DSN") ?: ""
        buildConfigField("String", "SENTRY_DSN", "\"${bitriseSentryDSN}\"")
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dependencies {
        coreLibraryDesugaring(libs.desugar)
    }
}

dependencies {
    debugImplementation(libs.androidx.compose.preview)
    debugImplementation(libs.androidx.compose.uitooling)
}
