apply(plugin = "jacoco")

// Generated code only. Anything hand-written, including Android UI classes, is
// measured.
val excludes = setOf(
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

// The Sentry plugin rewrites app classes through AGP's ASM pipeline, and tests run
// against the rewritten bytes. JaCoCo only credits classes whose checksum matches, so the
// report reads the rewritten classes when they exist. Resolved lazily: the directory is
// only there once the transform has run.
fun reportClasses() = provider {
    listOf("dhis2Debug", "debug").flatMap { variant ->
        val capitalized = variant.replaceFirstChar { it.uppercase() }
        val asmClasses = file(
            "${buildDir}/intermediates/classes/$variant/transform${capitalized}ClassesWithAsm/dirs",
        )
        val dirs = if (asmClasses.exists()) {
            listOf(asmClasses)
        } else {
            listOf(
                file("${buildDir}/intermediates/javac/$variant/compile${capitalized}JavaWithJavac/classes"),
                file("${buildDir}/intermediates/built_in_kotlinc/$variant/compile${capitalized}Kotlin/classes"),
            )
        }
        dirs.map { fileTree(it) { exclude(excludes) } }
    }
}

tasks.register("jacocoReport", JacocoReport::class) {
    group = "Coverage"
    description = "Generate XML/HTML unit test coverage reports"

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

    classDirectories.setFrom(reportClasses())

    executionData.setFrom(
        fileTree("${buildDir}/jacoco") {
            include("*.exec")
        },
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

// Instrumented coverage, reported separately from the unit tests because each set of
// execution data must be read against the classes that produced it. No compile
// dependencies: on CI the classes come from the job that built the APK.
tasks.register("jacocoAndroidTestReport", JacocoReport::class) {
    group = "Coverage"
    description = "Generate XML/HTML instrumented test coverage reports from .ec files"

    sourceDirectories.setFrom("${project.projectDir}/src/main/java")

    classDirectories.setFrom(reportClasses())

    executionData.setFrom(
        fileTree("${buildDir}/outputs/code_coverage") {
            include("**/*.ec")
        },
    )

    reports {
        xml.required.set(true)
        xml.outputLocation.set(file("${buildDir}/coverage-report-androidTest/jacocoAndroidTestReport.xml"))

        html.required.set(true)
        html.outputLocation.set(file("${buildDir}/coverage-report-androidTest"))
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
