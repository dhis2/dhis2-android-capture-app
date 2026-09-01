import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

group = "org.dhis2.mobile"
// Shared with :plugin-sdk-gradle so the library and its packaging plugin always publish together.
version = libs.versions.pluginSdk.get()

kotlin {
    androidLibrary {
        namespace = "org.dhis2.mobile.plugin.sdk"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeVM)
            implementation(libs.kotlin.serialization.json)
        }

        androidMain.dependencies {
            // The plugin API now exposes D2 itself, so this artifact compiles against the DHIS2 SDK.
            // compileOnly: at runtime the classes come from the host's class loader, and a second
            // copy inside a plugin DEX is what produces ClassCastException.
            compileOnly(libs.dhis2.android.sdk)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlin.serialization.json)
            implementation(libs.test.kotlinCoroutines)
        }
    }
}
