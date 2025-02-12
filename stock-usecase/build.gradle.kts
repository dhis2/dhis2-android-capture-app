import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
    id("kotlinx-serialization")
    alias(libs.plugins.kotlin.compose.compiler)
}
apply(from = "${project.rootDir}/jacoco/jacoco.gradle.kts")

repositories {
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

base {
    archivesName.set("psm-v" + libs.versions.vName.get())
}

android {
    compileSdk = libs.versions.sdk.get().toInt()
    namespace = "org.dhis2.android.rtsm"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testOptions.targetSdk = libs.versions.sdk.get().toInt()
        multiDexEnabled = true

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["dagger.hilt.disableCrossCompilationRootValidation"] = "true"
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
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
        viewBinding = true
        dataBinding = true
    }

    packaging {
        resources {
            excludes.addAll(
                mutableSetOf(
                    "META-INF/DEPENDENCIES",
                    "META-INF/ASL2.0",
                    "META-INF/NOTICE",
                    "META-INF/LICENSE",
                    "META-INF/proguard/androidx-annotations.pro",
                    "META-INF/gradle/incremental.annotation.processors"
                )
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {

    implementation(project(":commons"))
    implementation(project(":compose-table"))
    implementation(project(":dhis2-mobile-program-rules"))
    implementation(project(":dhis_android_analytics"))

    implementation(libs.bundles.stock.implementation)
    testImplementation(project(":dhis_android_analytics"))
    coreLibraryDesugaring(libs.bundles.stock.core)
    kapt(libs.bundles.stock.kapt)
    debugImplementation(libs.bundles.stock.debugImplementation)
    releaseImplementation(libs.bundles.stock.releaseImplementation)
    testImplementation(libs.bundles.stock.test)

    debugImplementation(libs.analytics.flipper.network) {
        exclude("com.squareup.okhttp3")
    }
}

kapt {
    correctErrorTypes = true
}
