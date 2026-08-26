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

    // Only genuinely generated code belongs here. The previous list excluded 82% of the
    // app module's compiled classes -- every Activity, Fragment, Dialog, Adapter, View,
    // anything with "Module" in its name, everything under a ui package, and via `**/*$*`
    // every nested class, lambda and coroutine state machine. What it reported was not
    // the project's coverage but the coverage of the 18% that survived the filter.
    //
    // Android UI classes are excluded no longer: instrumented tests now contribute their
    // execution data, so those classes are genuinely exercised and hiding them
    // understates the suite that covers them.
    val excludes = mutableSetOf<String>(
        // Android resource and build plumbing -- no source to cover.
        "**/R.class",
        "**/R\$*.class",
        "**/BR.*",
        "**/BuildConfig.*",
        "**/Manifest*.*",

        // Data Binding / View Binding generated classes.
        "android/databinding/**/*.class",
        "**/android/databinding/*",
        "**/androidx/databinding/*",
        "**/databinding/*.*",
        "**/*Binding.class",

        // Dagger / Hilt generated classes.
        "**/Dagger*.*",
        "**/*_Factory*.*",
        "**/*_Provide*Factory*.*",
        "**/*_MembersInjector.class",
        "**/*_HiltModules*.*",

        // Other annotation processors.
        "**/*JsonAdapter.*",
        "**/AutoValue*.*",
        "**/*_Impl*.*",

        // Compiler output with no corresponding source lines.
        "**/*\$WhenMappings.class",
        "**/*\$\$serializer.class",
        "**/ComposableSingletons*.*",

        // Test code itself.
        "**/*Test*.*",
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
