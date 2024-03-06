import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlinx-serialization")
    id("dagger.hilt.android.plugin")
}
apply(from = "${project.rootDir}/jacoco/jacoco.gradle.kts")

repositories {
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
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

    ndkVersion = libs.versions.ndk.get()
    compileSdk = libs.versions.sdk.get().toInt()
    namespace = "org.dhis2"
    testNamespace = "org.dhis2.test"

    base {
        archivesName.set("dhis2-v" + libs.versions.vName.get())
    }

    defaultConfig {
        applicationId = "com.dhis2"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.sdk.get().toInt()
        versionCode = libs.versions.vCode.get().toInt()
        versionName = libs.versions.vName.get()
        testInstrumentationRunner = "org.dhis2.Dhis2Runner"
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true

        val defMapboxToken =
            "pk.eyJ1IjoiZGhpczJhbmRyb2lkIiwiYSI6ImNrcWt1a2hzYzE5Ymsyb254MWtlbGt4Y28ifQ.JrP61q9BFTVEKO4SwRUwDw"
        val mapboxAccessToken = System.getenv("MAPBOX_ACCESS_TOKEN") ?: defMapboxToken
        val bitriseSentryDSN = System.getenv("SENTRY_DSN") ?: ""

        buildConfigField("String", "SDK_VERSION", "\"" + "1.8.2-eyeseetea-fork-1" + "\"")
        buildConfigField("String", "MAPBOX_ACCESS_TOKEN", "\"" + mapboxAccessToken + "\"")
        buildConfigField("String", "MATOMO_URL", "\"https://usage.analytics.dhis2.org/matomo.php\"")
        buildConfigField("long", "VERSION_CODE", "${defaultConfig.versionCode}")
        buildConfigField("String", "VERSION_NAME", "\"${defaultConfig.versionName}\"")
        buildConfigField("String", "SENTRY_DSN", "\"${bitriseSentryDSN}\"")

        manifestPlaceholders["appAuthRedirectScheme"] = ""

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
        javaCompileOptions
            .annotationProcessorOptions.arguments["dagger.hilt.disableModulesHaveInstallInCheck"] =
            "true"
    }
    packagingOptions {
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

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }

    buildTypes {

        getByName("debug") {
            // custom application suffix which allows to
            // install debug and release builds at the same time
            applicationIdSuffix = ".debug"

            // Using dataentry.jks to sign debug build type.
            signingConfig = signingConfigs.getByName("debug")

            buildConfigField("int", "MATOMO_ID", "2")
            buildConfigField("String", "BUILD_DATE", "\"" + getBuildDate() + "\"")
            buildConfigField("String", "GIT_SHA", "\"" + getCommitHash() + "\"")
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("int", "MATOMO_ID", "1")
            buildConfigField("String", "BUILD_DATE", "\"" + getBuildDate() + "\"")
            buildConfigField("String", "GIT_SHA", "\"" + getCommitHash() + "\"")
        }
    }

    flavorDimensions("default")

    productFlavors {
        create("dhis") {
            applicationId = "com.dhis2"
            dimension = "default"
            versionCode = libs.versions.vCode.get().toInt()
            versionName = libs.versions.vName.get()
        }

        create("dhisPlayServices") {
            applicationId = "com.dhis2"
            dimension = "default"
            versionCode = libs.versions.vCode.get().toInt()
            versionName = libs.versions.vName.get()
        }

        create("dhisUITesting") {
            applicationId = "com.dhis2"
            dimension = "default"
            versionCode = libs.versions.vCode.get().toInt()
            versionName = libs.versions.vName.get()
        }
        create("widp") {
            applicationId = "com.eyeseetea.widp"
            dimension = "default"
            versionCode = libs.versions.vCode.get().toInt()
            versionName = "2.8.2-widp-fork-1"
        }

        create("psi") {
            applicationId = "org.dhis2.psi"
            dimension = "default"
            versionCode = libs.versions.vCode.get().toInt()
            versionName = "2.8.2-psi-fork-2"
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
    }

    configurations.all {
        resolutionStrategy {
            preferProjectModules()
            force("junit:junit:4.12", "com.squareup.okhttp3:okhttp:3.12.0")
            setForcedModules("com.squareup.okhttp3:okhttp:3.12.0")
        }
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtensionVersion.get()
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":viewpagerdotsindicator"))
    implementation(project(":dhis_android_analytics"))
    implementation(project(":form"))
    implementation(project(":commons"))
    implementation(project(":dhis2_android_maps"))
    implementation(project(":compose-table"))
    implementation(project(":stock-usecase"))

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
    implementation(libs.google.flexbox)
    implementation(libs.google.guava)
    implementation(libs.github.pinlock)
    implementation(libs.github.fancyshowcase)
    implementation(libs.lottie)
    implementation(libs.dagger.hilt.android)
    implementation(libs.rx.kotlin)
    implementation(libs.network.gsonconverter)
    implementation(libs.network.okhttp)
    implementation(libs.dates.jodatime)
    implementation(libs.analytics.matomo)
    implementation(libs.analytics.rxlint)
    implementation(libs.analytics.customactivityoncrash)
    implementation(platform(libs.dispatcher.dispatchBOM))
    implementation(libs.dispatcher.dispatchCore)

    coreLibraryDesugaring(libs.desugar)

    debugImplementation(libs.analytics.flipper)
    debugImplementation(libs.analytics.soloader)
    debugImplementation(libs.analytics.flipper.network)
    debugImplementation(libs.analytics.flipper.leak)
    debugImplementation(libs.analytics.leakcanary)
    debugImplementation(libs.test.ui.test.manifest)

    releaseImplementation(libs.analytics.leakcanary.noop)
    releaseImplementation(libs.analytics.flipper.noop)

    "dhisPlayServicesImplementation"(libs.google.auth)
    "dhisPlayServicesImplementation"(libs.google.auth.apiphone)

    kapt(libs.dagger.compiler)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.dagger.hilt.compiler)
    kapt(libs.deprecated.autoValueParcel)

    testImplementation(libs.test.archCoreTesting)
    testImplementation(libs.test.testCore)
    testImplementation(libs.test.mockitoCore)
    testImplementation(libs.test.mockitoInline)
    testImplementation(libs.test.mockitoKotlin)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.kotlinCoroutines)
    testImplementation(libs.test.turbine)

    androidTestUtil(libs.test.orchestrator)

    androidTestImplementation(libs.test.testRunner)
    androidTestImplementation(libs.test.espresso.intents)
    androidTestImplementation(libs.test.espresso.contrib)
    androidTestImplementation(libs.test.espresso.accessibility)
    androidTestImplementation(libs.test.espresso.web)
    androidTestImplementation(libs.test.uiautomator)
    androidTestImplementation(libs.test.testCore)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.test.coreKtx)
    androidTestImplementation(libs.test.junitKtx)
    androidTestImplementation(libs.test.mockito.android)
    androidTestImplementation(libs.test.mockitoCore)
    androidTestImplementation(libs.test.support.annotations)
    androidTestImplementation(libs.test.espresso.idlingresource)
    androidTestImplementation(libs.test.rx2.idler)
    androidTestImplementation(libs.test.compose.ui.test)
    androidTestImplementation(libs.test.hamcrest)
    androidTestImplementation(libs.dispatcher.dispatchEspresso)


    //Eyeseetea
    implementation(libs.eyeseetea.atv)
    implementation(libs.eyeseetea.markwon)
    implementation(libs.eyeseetea.coroutinesCore)
    implementation(libs.eyeseetea.coroutinesAndroid)
}
