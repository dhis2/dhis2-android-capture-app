import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "org.dhis2.mobile.plugin"
    compileSdk = libs.versions.sdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testOptions.targetSdk = libs.versions.sdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    api(project(":plugin-sdk"))
    implementation(project(":commonskmm"))

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.dhis2.android.sdk)
    implementation(libs.analytics.timber)
    implementation(libs.kotlin.serialization.json)
    // LocalResourceReader (@ExperimentalResourceApi) — PluginSlot injects a
    // filesystem-backed ResourceReader so the plugin's CMP Resources resolve
    // from the extracted bundle without touching AssetManager.
    implementation(libs.compose.components.resources)

    coreLibraryDesugaring(libs.desugar)
}
