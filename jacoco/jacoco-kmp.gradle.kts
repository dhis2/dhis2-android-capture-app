apply(plugin = "jacoco")

// Check if this is a Kotlin Multiplatform project
val isKmpProject = plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")
val isAndroidProject = plugins.hasPlugin("com.android.library") || plugins.hasPlugin("com.android.application")

tasks.register("jacocoReport", JacocoReport::class) {
    group = "Coverage"
    description = "Generate XML/HTML code coverage reports"

    // Set source directories based on project type
    if (isKmpProject) {
        // For KMP projects, include all source sets
        sourceDirectories.setFrom(
            layout.projectDirectory.dir("src/commonMain/kotlin"),
            layout.projectDirectory.dir("src/androidMain/kotlin"),
            layout.projectDirectory.dir("src/commonTest/kotlin"),
            layout.projectDirectory.dir("src/androidUnitTest/kotlin")
        )
    } else {
        // For Android projects
        sourceDirectories.setFrom("${project.projectDir}/src/main/java")
    }

    val excludes = mutableSetOf<String>(
        "android/databinding/**/*.class",
        "**/android/databinding/*Binding.class",
        "**/android/databinding/*",
        "**/androidx/databinding/*",
        "**/BR.*",
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/*MapperImpl*.*",
        "**/*\$ViewInjector*.*",
        "**/*\$ViewBinder*.*",
        "**/BuildConfig.*",
        "**/*Component*.*",
        "**/*BR*.*",
        "**/Manifest*.*",
        "**/*\$Lambda\$*.*",
        "**/*Companion*.*",
        "**/*Module*.*",
        "**/*Dagger*.*",
        "**/*MembersInjector*.*",
        "**/*_MembersInjector.class",
        "**/*_Factory*.*",
        "**/*_Provide*Factory*.*",
        "**/*Extensions*.*",
        "**/*\$Result.*",
        "**/*\$Result\$*.*",
        "**/*JsonAdapter.*",
        "**/databinding/*.*",
        "**/customviews/*.*",
        "**/ui/*.class",
        "**/*Activity.*",
        "**/Activity*.*",
        "**/*Activity*.*",
        "**/*Fragment.*",
        "**/Fragment*.*",
        "**/*View.*",
        "**/*Adapter.*",
        "**/*Contract*.*",
        "**/*Bindings*.*",
        "**/AutoValue*.*",
        "**/*\$*",
        "**/*Navigator.*",
        "**/*\$*\$*.*",
        "**/animations/*.*",
        "**/*Holder*.*",
        "**/*Dialog*.*",
        "**/*Service*.*",
        "**/*Button*.*",
        "**/SearchTEList.*",
        "**/lambda\$*\$*.*"
    )

    // Set class directories based on project type
    val classDirectoriesList = mutableListOf<Any>()
    
    if (isAndroidProject) {
        // Android project class directories
        val javaClassesApp = fileTree(layout.buildDirectory.dir("intermediates/javac/dhisDebug")){
            exclude(excludes)
        }
        val kotlinClassesApp = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/dhisDebug")){
            exclude(excludes)
        }
        val javaClasses = fileTree(layout.buildDirectory.dir("intermediates/javac/debug")){
            exclude(excludes)
        }
        val kotlinClasses = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")){
            exclude(excludes)
        }
        
        classDirectoriesList.addAll(listOf(javaClassesApp, kotlinClassesApp, javaClasses, kotlinClasses))
    }
    
    if (isKmpProject) {
        // KMP project class directories
        val commonClasses = fileTree(layout.buildDirectory.dir("classes/kotlin/common")){
            exclude(excludes)
        }
        val androidClasses = fileTree(layout.buildDirectory.dir("classes/kotlin/android")){
            exclude(excludes)
        }
        val jvmClasses = fileTree(layout.buildDirectory.dir("classes/kotlin/jvm")){
            exclude(excludes)
        }
        
        classDirectoriesList.addAll(listOf(commonClasses, androidClasses, jvmClasses))
    }
    
    classDirectories.setFrom(files(classDirectoriesList))

    // Execution data - look for both .exec and .ec files
    val unitTestsData = fileTree(layout.buildDirectory.dir("jacoco")) {
        include("*.exec")
    }
    val androidTestsData = fileTree(layout.buildDirectory.dir("outputs/code_coverage")) {
        include(listOf("**/*.ec"))
    }
    
    executionData.setFrom(files(listOf(unitTestsData, androidTestsData)))

    // Add test task dependencies based on project type
    if (isAndroidProject) {
        val testTask = tasks.findByName("testDebugUnitTest")
        val androidTestTask = tasks.findByName("connectedDebugAndroidTest")
        
        if (testTask != null) dependsOn(testTask)
        if (androidTestTask != null) dependsOn(androidTestTask)
    }
    
    if (isKmpProject) {
        val commonTestTask = tasks.findByName("test")
        val androidTestTask = tasks.findByName("androidTest")
        
        if (commonTestTask != null) dependsOn(commonTestTask)
        if (androidTestTask != null) dependsOn(androidTestTask)
    }

    fun JacocoReportsContainer.reports() {
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("coverage-report/jacocoTestReport.xml"))

        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("coverage-report"))
    }

    reports {
        reports()
    }
}