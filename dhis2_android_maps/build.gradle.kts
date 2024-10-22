import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    alias(libs.plugins.kotlin.compose.compiler)
}
apply(from = "${project.rootDir}/jacoco/jacoco.gradle.kts")

android {
    compileSdk = libs.versions.sdk.get().toInt()
    namespace = "org.dhis2.maps"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testOptions.targetSdk = libs.versions.sdk.get().toInt()
        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("debug") {
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        dataBinding = true
    }
    flavorDimensions += listOf("default")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":commons"))
    api(libs.maps.maplibre) {
        exclude("com.google.android.gms")
    }
    api(libs.maps.geojson) {
        exclude("com.google.android.gms")
    }
    implementation(libs.maps.markerViewPlugin) {
        exclude("com.mapbox.mapboxsdk", "mapbox-android-sdk")
    }
    implementation(libs.maps.annotationPlugin) {
        exclude("com.mapbox.mapboxsdk", "mapbox-android-sdk")
    }

    testImplementation(libs.bundles.map.test)
    coreLibraryDesugaring(libs.desugar)
}
