plugins {
    id("com.android.library")
}
apply(from = "${project.rootDir}/jacoco/jacoco.gradle.kts")

android {
    compileSdk = libs.versions.sdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.sdk.get().toInt()
    }
    namespace = "com.tbuonomo.viewpagerdotsindicator"
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.dynamicanimation)
    implementation(libs.androidx.viewpager2)
    testImplementation(libs.test.junit)
}
