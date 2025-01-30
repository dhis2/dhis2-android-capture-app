plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.7.3"
    id("com.android.library")
    alias(libs.plugins.kotlin.compose.compiler)
}

repositories{
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    mavenCentral()
    google()
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
            api(compose.materialIconsExtended)
            implementation(libs.dhis2.mobile.designsystem)
            implementation(libs.compose.material3.window)

            // Koin
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeVM)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.androidx.compose.preview)
            implementation(libs.dhis2.android.sdk)
            // Koin support for Android
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
        }

        androidUnitTest.dependencies {  }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
    }
}

android {
    namespace = "org.dhis2.mobile.aggregates"
    compileSdk = libs.versions.sdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    dependencies {
        coreLibraryDesugaring(libs.desugar)
    }
}

dependencies {
    debugImplementation(libs.androidx.compose.preview)
    debugImplementation(libs.androidx.ui.tooling)
}