import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.atomicfu)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidLibrary {
        namespace = "org.dhis2.mobile.commons"
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

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.compose.material3.window)
            implementation(libs.lifecycle.runtime.compose)

            // Koin
            api(libs.koin.core)
            implementation(libs.ktxml)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeVM)

            // Design system
            implementation(libs.dhis2.mobile.designsystem)

            //dates
            implementation(libs.kotlinx.datetime)

            // Atomicfu
            implementation(libs.atomicfu)

            //Coil
            api(libs.coil)
            api(libs.coil.network)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.koin.test)
            implementation(libs.koin.test.junit5)
            implementation(libs.koin.test.junit4)
            implementation(libs.test.kotlinCoroutines)
            implementation(libs.test.mockitoKotlin)
        }

        androidMain.dependencies {
            implementation(libs.dhis2.android.sdk)
            implementation(libs.test.espresso.idlingresource)
            api(libs.analytics.timber)
            implementation(libs.androidx.browser)
            // Sentry
            api(libs.analytics.sentry)
            implementation(libs.androidx.work)
            compileOnly(libs.androidx.compose.preview)
            compileOnly(libs.androidx.compose.uitooling)
        }

        getByName("androidHostTest") {
            dependencies {
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.test.junit.ext)
                implementation(libs.test.espresso)
                implementation(libs.test.espresso.idlingresource)
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
    publicResClass = true
    packageOfResClass = "org.dhis2.mobile.commons.resources"
    generateResClass = always
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
}
