apply(plugin = "jacoco")

tasks.register("jacocoReport", JacocoReport::class) {
    group = "Coverage"
    description = "Generate XML/HTML code coverage reports for coverage.ec"

    // Make sure tests are run before generating the report
    // Only add dependencies if the tasks exist
    val testTask = tasks.findByName("testDebugUnitTest")
    val androidTestTask = tasks.findByName("connectedDebugAndroidTest")
    
    if (testTask != null) {
        dependsOn(testTask)
    }
    if (androidTestTask != null) {
        dependsOn(androidTestTask)
    }

    sourceDirectories.setFrom("${project.projectDir}/src/main/java")

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

    val javaClassesApp = fileTree(layout.buildDirectory.dir("intermediates/javac/dhisDebug")){
        exclude(
            excludes
        )
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

    classDirectories.setFrom(
        files(
            listOf(
                javaClassesApp,
                kotlinClassesApp,
                javaClasses,
                kotlinClasses
            )
        )
    )

    val unitTestsData = fileTree(layout.buildDirectory.dir("jacoco")) {
        include("*.exec")
    }
    val androidTestsData = fileTree(layout.buildDirectory.dir("outputs/code_coverage")) {
        include(listOf("**/*.ec"))
    }

    executionData.setFrom(
        files(
            listOf(
                unitTestsData,
                androidTestsData
            )
        )
    )

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


