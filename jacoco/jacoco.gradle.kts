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
    } + fileTree("${buildDir}/classes/kotlin/android/main") { exclude(excludes) } // KMP
}

// Source sets whose classes are reported: main code, the dhis2 flavor and debug build
// type the reports use, and KMP's Android side.
val reportedSourceDirs = listOf("main", "debug", "dhis2", "commonMain", "androidMain")
    .flatMap { listOf("src/$it/java", "src/$it/kotlin") }

fun JacocoReport.reportInto(outputDir: String, xmlFile: String) {
    group = "Coverage"
    sourceDirectories.setFrom(reportedSourceDirs)
    reports {
        xml.required.set(true)
        xml.outputLocation.set(file("${buildDir}/$outputDir/$xmlFile"))
        html.required.set(true)
        html.outputLocation.set(file("${buildDir}/$outputDir"))
    }
}

tasks.register("jacocoReport", JacocoReport::class) {
    description = "Generate XML/HTML unit test coverage reports"
    reportInto("coverage-report", "jacocoTestReport.xml")

    val testTasks = listOf("testDhis2DebugUnitTest", "testDebugUnitTest", "testAndroidHostTest")
    // Lazy: this script is applied before AGP registers these tasks.
    dependsOn(tasks.matching { it.name in testTasks })

    classDirectories.setFrom(reportClasses())

    executionData.setFrom(
        fileTree("${buildDir}/jacoco") {
            // Not desktopTest: it runs the desktop classes, not the Android ones reported.
            include(testTasks.map { "$it.exec" })
        },
    )
}

// Instrumented runs execute every module's classes as the app's ASM pipeline rewrote them,
// not as each module compiled them. The app stages those rewritten classes here, one
// directory per module, for jacocoAndroidTestReport.
val instrumentedClassesDir = file("${rootDir}/app/build/coverage-classes")

// Only the app has an instrumented suite, and one .ec covers its whole process, so every
// module reads the app's execution data: from connected runs locally, from BrowserStack
// on CI.
val instrumentedExecutionData = file("${rootDir}/app/build/outputs/code_coverage")

pluginManager.withPlugin("com.android.application") {
    tasks.register("stageInstrumentedClasses") {
        group = "Coverage"
        description = "Stage the classes packaged in the dhis2Debug APK, per module"

        val projectJars = configurations.named("dhis2DebugRuntimeClasspath").map { config ->
            config.incoming.artifactView {
                attributes {
                    attribute(
                        ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE,
                        "android-asm-instrumented-jars",
                    )
                    attribute(Attribute.of("asm-transformed-variant", String::class.java), "dhis2Debug")
                }
                componentFilter { it is ProjectComponentIdentifier }
            }.artifacts
        }
        val appClasses = file(
            "${buildDir}/intermediates/classes/dhis2Debug/transformDhis2DebugClassesWithAsm/dirs",
        )

        dependsOn("transformDhis2DebugClassesWithAsm")
        inputs.files(projectJars.map { it.artifactFiles })
        outputs.dir(instrumentedClassesDir)

        doLast {
            delete(instrumentedClassesDir)
            copy {
                from(appClasses)
                into(instrumentedClassesDir.resolve(project.name))
            }
            projectJars.get().forEach { artifact ->
                val id = artifact.id.componentIdentifier as ProjectComponentIdentifier
                copy {
                    from(zipTree(artifact.file))
                    into(instrumentedClassesDir.resolve(id.projectName))
                }
            }
        }
    }
}

// Reported separately from the unit tests because each set of execution data must be read
// against the classes that produced it. Never compiles: it reads only staged classes, so
// run :app:stageInstrumentedClasses first when reporting locally.
tasks.register("jacocoAndroidTestReport", JacocoReport::class) {
    description = "Generate XML/HTML instrumented test coverage reports from .ec files"
    reportInto("coverage-report-androidTest", "jacocoAndroidTestReport.xml")

    classDirectories.setFrom(
        fileTree(instrumentedClassesDir.resolve(project.name)) { exclude(excludes) },
    )

    executionData.setFrom(
        fileTree(instrumentedExecutionData) {
            include("**/*.ec")
        },
    )
}
