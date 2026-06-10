apply(plugin = "jacoco")

tasks.register("jacocoReport", JacocoReport::class) {
    group = "Coverage"
    description = "Generate XML/HTML code coverage reports for coverage.ec"

    listOf(
        "compileDhis2DebugJavaWithJavac",
        "compileDhis2DebugKotlin",
        "compileDebugJavaWithJavac",
        "compileDebugKotlin",
        "testDhis2DebugUnitTest",
        "testDebugUnitTest",
    ).forEach { taskName ->
        tasks.findByName(taskName)?.let { dependsOn(it) }
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

    val javaClassesApp = fileTree(
        "${buildDir}/intermediates/javac/dhis2Debug/compileDhis2DebugJavaWithJavac/classes",
    ) {
        exclude(excludes)
    }
    val kotlinClassesApp = fileTree(
        "${buildDir}/intermediates/built_in_kotlinc/dhis2Debug/compileDhis2DebugKotlin/classes",
    ) {
        exclude(excludes)
    }
    val javaClasses = fileTree(
        "${buildDir}/intermediates/javac/debug/compileDebugJavaWithJavac/classes",
    ) {
        exclude(excludes)
    }
    val kotlinClasses = fileTree(
        "${buildDir}/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes",
    ) {
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

    val unitTestsData = fileTree("${buildDir}/jacoco") {
        include("*.exec")
    }
    val androidTestsData = fileTree("${buildDir}/outputs/code_coverage") {
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
        xml.outputLocation.set(file("${buildDir}/coverage-report/jacocoTestReport.xml"))

        html.required.set(true)
        html.outputLocation.set(file("${buildDir}/coverage-report"))
    }

    reports {
        reports()
    }
}

/*android {
    buildTypes {
        getByName("debug") {
            // jacoco test coverage reports both for
            // androidTest and test source sets
            testCoverageEnabled = false
        }
    }
}*/
