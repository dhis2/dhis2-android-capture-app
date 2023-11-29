plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
    id("kotlinx-serialization")
}
apply(from = "${project.rootDir}/jacoco/jacoco.gradle.kts")

android {
    compileSdk = libs.versions.sdk.get().toInt()
    namespace = "org.dhis2.ui"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.sdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        getByName("debug") {
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    flavorDimensions += listOf("default")

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtensionVersion.get()
    }
}

dependencies {

    implementation(libs.bundles.uicomponents.implementation)
    api(libs.bundles.uicomponents.api)
    debugApi(libs.bundles.uicomponents.debugapi)
    testImplementation(libs.bundles.uicomponents.test)
    androidTestImplementation(libs.bundles.uicomponents.androidtest)
}
