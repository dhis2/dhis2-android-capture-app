import org.gradle.kotlin.dsl.implementation

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.compose)
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
            // Koin
            api(libs.koin.core)
            implementation(libs.ktxml)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeVM)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {

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

android {
    namespace = "org.dhis2.mobile.commonskmm"
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

