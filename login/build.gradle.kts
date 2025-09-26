import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.compose)
    id("com.android.library")
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    jvm("desktop")

    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
    }

    sourceSets {
        commonMain {
            resources.srcDirs("src/commonMain/composeResources")

            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(compose.material3)
                api(compose.materialIconsExtended)
                val designSystem = libs.dhis2.mobile.designsystem
                implementation("${designSystem.get().group}:${designSystem.get().name}:${designSystem.get().version}") {
                    isChanging = true
                }
                implementation(libs.compose.material3.window)
                implementation(compose.components.resources)
                implementation(project(":commonskmm"))
                implementation(libs.navigation.compose)
                implementation(compose.components.uiToolingPreview)

                // Koin
                api(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.composeVM)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                // Koin Test features
                implementation(libs.koin.test)
                implementation(libs.koin.test.junit5)
                implementation(libs.koin.test.junit4)
                implementation(libs.test.turbine)
                implementation(libs.test.kotlinCoroutines)
                implementation(libs.test.mockitoKotlin)
                implementation(compose.components.resources)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.compose.preview)
                implementation(libs.dhis2.android.sdk)
                // Koin support for Android
                implementation(libs.koin.android)
                implementation(libs.koin.androidx.compose)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.browser)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
    }

}

compose.resources {
    publicResClass = false
    packageOfResClass = "org.dhis2.mobile.login.resources"
    generateResClass = always
}

android {
    namespace = "org.dhis2.mobile.login"
    compileSdk = libs.versions.sdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("debug") {

        }
        getByName("release") {

        }
    }
    buildFeatures.buildConfig = true

    flavorDimensions += listOf("default")
    productFlavors {
        create("dhis2") {
            buildConfigField("String", "LOGIN_TEST", "\"test\"")
        }
        create("dhis2PlayServices") {
            buildConfigField("String", "LOGIN_TEST", "\"test\"")

        }
        create("dhis2Training") {
            buildConfigField("String", "LOGIN_TEST", "\"test\"")
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
}

dependencies {
    testImplementation(libs.junit.jupiter)
    debugImplementation(libs.androidx.compose.preview)
    debugImplementation(libs.androidx.compose.uitooling)
    androidTestImplementation(libs.test.compose.ui.test.junit4.android)
    debugImplementation(libs.test.ui.test.manifest)
}