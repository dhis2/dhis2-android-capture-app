plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.7.1"
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

    jvm("desktop").

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
            api(compose.materialIconsExtended)
            implementation(libs.dhis2.mobile.designsystem)
            implementation(libs.compose.material3.window)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.androidx.compose.preview)
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
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
dependencies {
    debugImplementation(libs.androidx.compose.preview)
    debugImplementation(libs.androidx.ui.tooling)
}