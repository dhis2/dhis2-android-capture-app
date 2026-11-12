import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "org.dhis2.mobile.extension.web"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
        // The host pages and scripts the WebView loads are Android assets.
        androidResources { enable = true }
        withHostTestBuilder {}.configure {}
    }

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":commonskmm"))
            implementation(project(":extension-host"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)

            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.dhis2.android.sdk)
            implementation(libs.androidx.webkit)
            implementation(libs.kotlin.serialization.json)
            implementation(libs.analytics.timber)
        }

        getByName("androidHostTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.test.junit)
                implementation(libs.test.kotlinCoroutines)
                implementation(libs.test.mockitoKotlin)
            }
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
}
