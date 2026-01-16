import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlin.compose.compiler)
}
apply(from = "${project.rootDir}/jacoco/jacoco.gradle.kts")

android {
    compileSdk = libs.versions.sdk.get().toInt()
    namespace = "org.dhis2.form"

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
    flavorDimensions += listOf("default")

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        dataBinding = true
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":commons"))
    implementation(project(":commonskmm"))
    implementation(project(":dhis2_android_maps"))
    implementation(project(":dhis2-mobile-program-rules"))
    implementation(libs.androidx.activity.compose)
    testImplementation(libs.bundles.form.test)
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.compose.ui.test)
    androidTestApi(libs.test.mockitoCore)
    androidTestApi(libs.test.mockitoKotlin)
    androidTestApi(libs.test.dexmaker.mockitoInline)
    debugImplementation(libs.androidx.compose.uitooling)
    debugImplementation(libs.test.ui.test.manifest)

    coreLibraryDesugaring(libs.desugar)
}
