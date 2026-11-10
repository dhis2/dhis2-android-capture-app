import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.compose.compiler)
}

kotlin {
    android {
        namespace = "org.dhis2.mobile.extension.host"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
        withHostTestBuilder {}.configure {}
    }

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":commonskmm"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)

            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.test.kotlinCoroutines)
            implementation(libs.test.turbine)
        }

        androidMain.dependencies {
            implementation(libs.dhis2.android.sdk)
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
}
