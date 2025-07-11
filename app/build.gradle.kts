@file:Suppress("UnstableApiUsage")

import com.android.build.api.variant.impl.VariantOutputImpl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose.compiler)
}
apply(from = "${project.rootDir}/jacoco/jacoco.gradle.kts")

repositories {
    maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
    mavenCentral()
}

android {

    val getBuildDate by extra {
        fun(): String {
            return SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())
        }
    }

    val getCommitHash by extra {
        fun(): String {
            val stdout = ByteArrayOutputStream()
            exec {
                commandLine("git", "rev-parse", "--short", "HEAD")
                standardOutput = stdout
            }
            return stdout.toString().trim()
        }
    }

    signingConfigs {
        create("release") {
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            System.getenv("SIGNING_KEYSTORE_PATH")?.let { path ->
                storeFile = file(path)
            }
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
        }
        create("training") {
            keyAlias = System.getenv("TRAINING_KEY_ALIAS")
            keyPassword = System.getenv("TRAINING_KEY_PASSWORD")
            System.getenv("TRAINING_STORE_FILE")?.let { path ->
                storeFile = file(path)
            }
            storePassword = System.getenv("TRAINING_STORE_PASSWORD")
        }
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        animationsDisabled = true
    }

    sourceSets {
        getByName("androidTest") {
            java.setSrcDirs(listOf("src/androidTest/java"))
            resources.setSrcDirs(listOf("src/androidTest/java"))
        }
    }

    namespace = "org.dhis2"
    testNamespace = "org.dhis2.test"

    base {
        archivesName.set("dhis2-v" + libs.versions.vName.get())
    }

    defaultConfig {
        applicationId = "com.dhis2"
        compileSdk = libs.versions.sdk.get().toInt()
        targetSdk = libs.versions.sdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        versionCode = libs.versions.vCode.get().toInt()
        versionName = libs.versions.vName.get()
        testInstrumentationRunner = "org.dhis2.Dhis2Runner"
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true

        val bitriseSentryDSN = System.getenv("SENTRY_DSN") ?: ""

        buildConfigField("String", "SDK_VERSION", "\"" + libs.versions.dhis2sdk.get() + "\"")
        buildConfigField("String", "MATOMO_URL", "\"https://usage.analytics.dhis2.org/matomo.php\"")
        buildConfigField("long", "VERSION_CODE", "${defaultConfig.versionCode}")
        buildConfigField("String", "VERSION_NAME", "\"${defaultConfig.versionName}\"")
        buildConfigField("String", "SENTRY_DSN", "\"${bitriseSentryDSN}\"")

        manifestPlaceholders["appAuthRedirectScheme"] = ""

    }
    packaging {
        jniLibs {
            excludes.addAll(listOf("META-INF/licenses/**"))
        }
        resources {
            excludes.addAll(
                listOf(
                    "META-INF/LICENSE",
                    "META-INF/rxjava.properties",
                    "LICENSE.txt",
                    "META-INF/DEPENDENCIES",
                    "META-INF/ASL2.0",
                    "META-INF/NOTICE",
                    "META-INF/LICENSE",
                    "META-INF/rxjava.properties",
                    "**/attach_hotspot_windows.dll",
                    "META-INF/licenses/**",
                    "META-INF/AL2.0",
                    "META-INF/LGPL2.1",
                    "META-INF/proguard/androidx-annotations.pro",
                    "META-INF/gradle/incremental.annotation.processors"
                )
            )
        }
    }

    buildTypes {

        getByName("debug") {
            // custom application suffix which allows to
            // install debug and release builds at the same time
            applicationIdSuffix = ".debug"

            buildConfigField("int", "MATOMO_ID", "2")
            buildConfigField("String", "BUILD_DATE", "\"" + getBuildDate() + "\"")
            buildConfigField("String", "GIT_SHA", "\"" + getCommitHash() + "\"")
        }
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("int", "MATOMO_ID", "1")
            buildConfigField("String", "BUILD_DATE", "\"" + getBuildDate() + "\"")
            buildConfigField("String", "GIT_SHA", "\"" + getCommitHash() + "\"")
        }
    }
    flavorDimensions += listOf("default")

    productFlavors {
        create("dhis2") {
            signingConfig = signingConfigs.getByName("release")
        }
        create("dhis2PlayServices") {
            signingConfig = signingConfigs.getByName("release")
        }
        create("dhis2Training") {
            signingConfig = signingConfigs.getByName("training")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }

    configurations.all {
        resolutionStrategy {
            preferProjectModules()
            force(
                "junit:junit:4.13.2",
                "com.squareup.okhttp3:okhttp:4.12.0",
                "com.squareup.okhttp3:mockwebserver:4.12.0",
                "com.squareup.okhttp3:logging-interceptor:4.12.0"
            )
            setForcedModules(
                "com.squareup.okhttp3:okhttp:4.12.0",
                "com.squareup.okhttp3:mockwebserver:4.12.0",
                "com.squareup.okhttp3:logging-interceptor:4.12.0"
            )
            cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    androidComponents {
        onVariants { variant ->
            val buildType = variant.buildType
            val flavorName = variant.flavorName

            // Apply suffix only for training flavor in release buildType
            if (buildType == "release" && flavorName == "dhis2Training") {
                variant.applicationId.set("${variant.applicationId.get()}.training")
            }

            variant.outputs.forEach { output ->
                if (output is VariantOutputImpl) {
                    val suffix = when {
                        buildType == "release" && flavorName == "dhis2Training" -> "-training"
                        buildType == "release" && flavorName == "dhis2PlayServices" -> "-googlePlay"
                        else -> ""
                    }

                    output.outputFileName = "dhis2-v${libs.versions.vName.get()}$suffix.apk"
                }
            }

        }
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
        // Enable debug logs
        arg("ksp.logging.level", "DEBUG")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":dhis_android_analytics"))
    implementation(project(":form"))
    implementation(project(":commons"))
    implementation(project(":dhis2_android_maps"))
    implementation(project(":compose-table"))
    implementation(project(":stock-usecase"))
    implementation(project(":dhis2-mobile-program-rules"))
    implementation(project(":tracker"))
    implementation(project(":aggregates"))
    implementation(project(":commonskmm"))

    implementation(libs.security.conscrypt)
    implementation(libs.security.rootbeer)
    implementation(libs.security.openId)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.work)
    implementation(libs.androidx.workrx)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.material3)
    implementation(libs.google.guava)
    implementation(libs.github.pinlock)
    implementation(libs.github.fancyshowcase)
    implementation(libs.lottie)
    implementation(libs.network.okhttp)
    implementation(libs.dates.jodatime)
    implementation(libs.analytics.matomo)
    implementation(libs.analytics.rxlint)
    implementation(libs.analytics.customactivityoncrash)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation("androidx.browser:browser:1.7.0")

    coreLibraryDesugaring(libs.desugar)

    "dhis2PlayServicesImplementation"(libs.google.auth)
    "dhis2PlayServicesImplementation"(libs.google.auth.apiphone)

    ksp(libs.dagger.compiler)

    testImplementation(libs.test.archCoreTesting)
    testImplementation(libs.test.testCore)
    testImplementation(libs.test.mockitoCore)
    testImplementation(libs.test.mockitoInline)
    testImplementation(libs.test.mockitoKotlin)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.kotlinCoroutines)
    testImplementation(libs.test.turbine)
    testImplementation(libs.test.androidx.paging)
    androidTestUtil(libs.test.orchestrator)

    androidTestImplementation(libs.test.testRunner)
    androidTestImplementation(libs.test.espresso.intents)
    androidTestImplementation(libs.test.espresso.contrib)
    androidTestImplementation(libs.test.uiautomator)
    androidTestImplementation(libs.test.testCore)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.test.junitKtx)
    androidTestImplementation(libs.test.mockitoCore)
    androidTestImplementation(libs.test.dexmaker.mockitoInline)
    androidTestImplementation(libs.test.mockitoKotlin)
    androidTestImplementation(libs.test.support.annotations)
    androidTestImplementation(libs.test.espresso.idlingresource)
    androidTestImplementation(libs.test.rx2.idler)
    androidTestImplementation(libs.test.compose.ui.test)
    androidTestImplementation(libs.test.hamcrest)
}
