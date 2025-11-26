import kotlin.text.set

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.compose)
    id("com.android.library")
    alias(libs.plugins.kotlin.compose.compiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
    }

    sourceSets {
        commonMain{
            resources.srcDirs("src/commonMain/composeResources")
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
            api(compose.materialIconsExtended)
            val designSystem = libs.dhis2.mobile.designsystem
            implementation("${designSystem.get().group}:${designSystem.get().name}:${designSystem.get().version}"){
                isChanging= true
            }
            implementation(libs.compose.material3.window)
            implementation(compose.components.resources)
            implementation(project(":commonskmm"))

            // Koin
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeVM)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            // Koin Test features
            implementation(libs.koin.test)
            implementation(libs.koin.test.junit5)
            implementation(libs.koin.test.junit4)
            implementation(libs.test.turbine)
            implementation(libs.test.kotlinCoroutines)
            implementation(libs.test.mockitoKotlin)
            implementation(compose.components.resources)
        }

        androidMain.dependencies {
            implementation(libs.androidx.compose.preview)
            implementation(libs.dhis2.android.sdk)
            // Koin support for Android
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(project(":commons"))
            implementation(project(":dhis2_android_maps"))
        }

        androidUnitTest.dependencies {  }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "org.dhis2.mobile.aggregates.resources"
    generateResClass = always
}

android {
    namespace = "org.dhis2.mobile.aggregates"
    compileSdk = libs.versions.sdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")

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
    testImplementation(libs.junit.jupiter)
    debugImplementation(libs.androidx.compose.preview)
    debugImplementation(libs.androidx.compose.uitooling)
}