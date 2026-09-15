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
        // Opt in to a JVM test target. Without it the AGP KMP library plugin registers no test task
        // and androidHostTest is silently never compiled or run.
        withHostTestBuilder {}.configure {}
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

        getByName("androidHostTest").dependencies {
            implementation(kotlin("test"))
        }
    }
}

// Tests of androidMain code need the DHIS2 SDK *classes* at runtime: the label mapping takes real
// TrackedEntityInstance values, built through the SDK's own builders rather than mocked. The SDK is
// compileOnly here — the host supplies it through its class loader — so it is on the compile
// classpath but not the runtime one, and such a test would die with NoClassDefFoundError.
//
// Extending rather than declaring keeps the version wherever it was already chosen.
configurations.named("androidHostTestRuntimeOnly") {
    extendsFrom(configurations.getByName("androidMainCompileOnly"))
}
