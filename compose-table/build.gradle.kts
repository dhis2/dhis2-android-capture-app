import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose.compiler)
}
apply(from = "${project.rootDir}/jacoco/jacoco.gradle.kts")

android {
    compileSdk = libs.versions.sdk.get().toInt()
    namespace = "org.dhis2.composetable"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testOptions.targetSdk = libs.versions.sdk.get().toInt()

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
    flavorDimensions += listOf("default")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.bundles.table.implementation)
    debugImplementation(libs.bundles.table.debugImplementation)
    testImplementation(libs.bundles.table.test)
    androidTestImplementation(libs.bundles.table.androidTest)
    implementation(libs.dhis2.mobile.designsystem)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose)
}
