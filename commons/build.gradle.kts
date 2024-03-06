plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
}

apply(from = "${project.rootDir}/jacoco/jacoco.gradle.kts")

repositories {
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

android {
    compileSdk = libs.versions.sdk.get().toInt()
    namespace = "org.dhis2.commons"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.sdk.get().toInt()
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

    flavorDimensions("default")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        dataBinding = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtensionVersion.get()
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    api(project(":ui-components"))

    api(project(":core"))
/*    api(libs.dhis2.android.sdk) {
        exclude("org.hisp.dhis", "core-rules")
        exclude("com.facebook.flipper")
    }*/


    api(libs.dhis2.ruleengine) {
        exclude("junit", "junit")
    }

    kapt(libs.metadata.jvm)
    api(libs.google.autoValue)
    kapt(libs.google.autoValue)
    api(libs.androidx.coreKtx)
    api(libs.androidx.appcompat)
    api(libs.androidx.fragmentKtx)
    api(libs.androidx.liveDataKtx)
    api(libs.androidx.viewModelKtx)
    api(libs.androidx.lifecycleExtensions)
    api(libs.androidx.recyclerView)
    debugApi(libs.androidx.compose.uitooling)
    api(libs.androidx.compose)
    api(libs.androidx.compose.constraintlayout)
    api(libs.androidx.compose.preview)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.livedata)

    api(libs.google.material)
    api(libs.google.gson)
    api(libs.dagger)
    kapt(libs.dagger.compiler)
    api(libs.google.material.themeadapter)
    api(libs.barcodeScanner.zxing)
    api(libs.rx.java)
    api(libs.rx.android)
    api(libs.analytics.timber)
    api(libs.github.glide)
    kapt(libs.github.glide.compiler)
    api(libs.barcodeScanner.scanner) {
        exclude("com.google.zxing", "core")
    }
    api(libs.barcodeScanner.zxing.android) {
        exclude("com.google.zxing", "core")
    }
    api(libs.rx.binding)
    api(libs.rx.binding.compat)
    testApi(libs.test.junit)
    androidTestApi(libs.test.mockitoCore)
    androidTestApi(libs.test.mockitoKotlin)
    androidTestApi(libs.test.dexmaker.mockitoInline)
    androidTestApi(libs.test.junit.ext)
    androidTestApi(libs.test.espresso)
    androidTestApi(libs.test.espresso.idlingresource)
    api(libs.test.espresso.idlingresource)
    api(libs.test.espresso.idlingconcurrent)
    api(libs.analytics.sentry)
    implementation(libs.github.treeView)
}
