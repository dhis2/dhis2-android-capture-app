import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "org.dhis2.mobile.plugin"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testOptions.targetSdk = libs.versions.targetSdk.get().toInt()
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
    implementation(project(":commonskmm"))

    implementation(libs.androidx.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.dhis2.mobile.designsystem)
    implementation(libs.androidx.webkit)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.dhis2.android.sdk)
    implementation(libs.analytics.timber)
    implementation(libs.kotlin.serialization.json)

    coreLibraryDesugaring(libs.desugar)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.mockitoKotlin)
    testImplementation(libs.test.mockitoInline)
    testImplementation(libs.test.kotlinCoroutines)
    testImplementation(libs.test.turbine)
}
