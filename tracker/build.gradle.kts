import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.compose.compiler)
}

kotlin {
    androidLibrary {
        namespace = "org.dhis2.mobile.tracker"
        compileSdk = libs.versions.sdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
        androidResources { enable = true }
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {}.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    jvm("desktop")

    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
    }

    sourceSets {
        commonMain {
            resources.srcDirs("src/commonMain/composeResources")
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)
            api(libs.compose.material3.iconsExtendend)
            val designSystem = libs.dhis2.mobile.designsystem
            implementation("${designSystem.get().group}:${designSystem.get().name}:${designSystem.get().version}") {
                isChanging = true
            }
            implementation(libs.kotlinx.datetime)
            implementation(libs.compose.material3.window)
            implementation(project(":commonskmm"))
            implementation(libs.androidx.compose.paging)

            // Koin
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeVM)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            // Koin Test features
            implementation(libs.koin.test)
            implementation(libs.test.turbine)
            implementation(libs.test.kotlinCoroutines)
            implementation(libs.test.mockitoKotlin)
            implementation(libs.compose.components.resources)
        }

        androidMain.dependencies {
            implementation(libs.androidx.compose.preview)
            implementation(libs.dhis2.android.sdk)
            // Koin support for Android
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(project(":commons"))
            implementation(project(":dhis2_android_maps"))
            compileOnly(libs.androidx.compose.uitooling)
        }

        getByName("androidHostTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.junit.jupiter)
                implementation(libs.test.turbine)
                implementation(libs.test.kotlinCoroutines)
                implementation(libs.test.mockitoKotlin)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.compose.desktop.common)
            }
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "org.dhis2.mobile.tracker.resources"
    generateResClass = always
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
}
