import org.gradle.kotlin.dsl.implementation
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.compose)
    id("com.android.library")
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.atomicfu)
    alias(libs.plugins.kotlin.serialization)
}


repositories{
    maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
    mavenCentral()
    google()
}

kotlin {

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

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.components.resources)

            // Koin
            api(libs.koin.core)
            implementation(libs.ktxml)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeVM)
            implementation(libs.dhis2.mobile.designsystem)
            //dates
            implementation(libs.kotlinx.datetime)

            // Atomicfu
            implementation(libs.atomicfu)

        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.koin.test)
            implementation(libs.koin.test.junit5)
            implementation(libs.koin.test.junit4)
            implementation(libs.test.kotlinCoroutines)
            implementation(libs.test.mockitoKotlin)
        }

        androidMain.dependencies {
            implementation(libs.dhis2.android.sdk)
            implementation(libs.test.espresso.idlingresource)
            api(libs.analytics.timber)
            // Sentry
            api(libs.analytics.sentry)
        }

        androidUnitTest.dependencies {

        }

        androidInstrumentedTest.dependencies {
            dependencies {
                implementation(libs.test.junit.ext)
                implementation(libs.test.espresso)
                implementation(libs.test.espresso.idlingresource)
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
    packageOfResClass = "org.dhis2.mobile.commons.resources"
    generateResClass = always
}

android {
    namespace = "org.dhis2.mobile.commonskmm"
    compileSdk = libs.versions.sdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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

