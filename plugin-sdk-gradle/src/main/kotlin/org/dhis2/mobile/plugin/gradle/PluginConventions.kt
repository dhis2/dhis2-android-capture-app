package org.dhis2.mobile.plugin.gradle

/**
 * The conventions every DHIS2 plugin has to keep, and the rules that check them.
 *
 * Pure on purpose — plain strings in, findings out, no Gradle types — so the rules are unit-tested
 * directly, the way [ToolchainPreflight] and [ClassesJarInspector] already are.
 *
 * These five are here rather than in a plugin author's own repo because they are properties of the
 * *plugin system*, not of any one plugin: each names a way a bundle loads and then fails on a
 * device, which is the worst place to find out. A rule that lives in a paragraph in every fork is a
 * rule that rots in every fork independently.
 *
 * What is deliberately absent: anything needing to understand the code rather than find a token.
 * A plugin's own architecture — what its repository returns, how its Composables bound themselves —
 * is the plugin's business, and this plugin has no business inventing types for it to implement.
 */
internal object PluginConventions {
    /** One thing a project got wrong, in a form a build error can print verbatim. */
    data class Violation(
        val rule: String,
        val where: String,
        val detail: String,
    ) {
        override fun toString(): String = "[$rule] $where: $detail"
    }

    /**
     * A dependency as declared, flattened to what the rules need.
     *
     * @property sourceSet e.g. `commonMain`, `androidMain`, `commonTest`.
     * @property bucket the configuration kind: `api`, `implementation`, `compileOnly`, `runtimeOnly`.
     * @property coordinates `group:name`, with no version.
     */
    data class DeclaredDependency(
        val sourceSet: String,
        val bucket: String,
        val coordinates: String,
    )

    /** A Kotlin source file, already read. */
    data class SourceFile(
        val path: String,
        val sourceSet: String,
        val text: String,
    )

    const val SDK_IN_SHARED_SOURCE = "sdk-in-shared-source"
    const val CONTEXT_IN_SHARED_SOURCE = "context-in-shared-source"
    const val HOST_DEP_NOT_COMPILE_ONLY = "host-dep-not-compile-only"
    const val RESOURCES_MUST_BE_IMPLEMENTATION = "resources-must-be-implementation"
    const val CAP_BEFORE_ENRICHMENT = "cap-before-enrichment"

    /**
     * Groups the Capture App puts on a plugin's runtime classpath itself.
     *
     * Bundling any of them produces a second copy of a class the host already owns, which surfaces
     * as `ClassCastException` or `NoSuchMethodError` at composition — never at build time. Shared
     * with [AndroidPluginWiring.notPackagedDependencies], whose warning is this list's complement:
     * a dependency that is *not* host-provided and *not* `compileOnly` is a `NoClassDefFoundError`
     * waiting to happen.
     */
    val HOST_PROVIDED: List<String> =
        listOf(
            "org.jetbrains.compose",
            "org.jetbrains.kotlin",
            "org.jetbrains.kotlinx:kotlinx-coroutines",
            "org.dhis2.mobile:plugin-sdk",
            "org.hisp.dhis",
            "org.hisp.dhis.mobile",
            "io.insert-koin",
            "androidx.compose",
            "androidx.lifecycle",
        )

    /**
     * The one host-provided artifact that must NOT be `compileOnly`.
     *
     * The Compose Resources generator uses the declaration itself as its opt-in signal: declare it
     * `compileOnly` and no `Res` class is generated, so every `Res.string.*` stops resolving. The
     * runtime classes still come from the host either way.
     */
    const val COMPOSE_RESOURCES = "org.jetbrains.compose.components:components-resources"

    /**
     * Dependencies a plugin author never declared and cannot change.
     *
     * The Kotlin plugin puts `kotlin-stdlib` on every source set's `api` itself. It is host-provided
     * — so the broad group match is right — but telling an author to make it `compileOnly` is
     * telling them to fix something they did not write, and the rule would fail every plugin
     * project on earth. Found by running this check against the sample on its first real build,
     * which is the only reason it is not a bug shipped to plugin authors.
     */
    private val PLUGIN_MANAGED =
        listOf(
            "org.jetbrains.kotlin:kotlin-stdlib",
            "org.jetbrains.kotlin:kotlin-test",
        )

    /**
     * Suppression marker, honoured per file. Read before comments are stripped, so it works written
     * as an ordinary comment.
     */
    const val IGNORE_MARKER = "plugin-conventions:ignore"

    /**
     * The DHIS2 **SDK**'s package, not every DHIS2 package.
     *
     * `org.hisp.dhis.android` rather than `org.hisp.dhis`, because the DHIS2 design system is
     * `org.hisp.dhis.mobile.ui.designsystem` — also host-provided, also `compileOnly`, and
     * deliberately a Compose Multiplatform library so that a plugin's `commonMain` UI can use it.
     * The broader pattern flagged every plugin importing `SurfaceColor` or `DHIS2Theme` into a
     * Composable, which is precisely what plugin authors are told to do: a plugin should look like
     * the app it renders inside. Note `HOST_PROVIDED` already lists `org.hisp.dhis.mobile`
     * separately, so the dependency side of this was right and only the source side was not.
     *
     * What this rule is really about stays intact: `D2` is the Android SDK, needs an Android
     * `Context`, a database and an HTTP stack, and so has no common-source equivalent.
     */
    private val SDK_PACKAGE = Regex("""\borg\.hisp\.dhis\.android\b""")
    private val PLUGIN_CONTEXT = Regex("""\bDhis2PluginContext\b""")

    /**
     * `.with*()` … `blockingGet()` … `take(` — enriching every row to display a few.
     *
     * The SDK's children appenders are what cost: resolving a tracked entity drags in an enrollment,
     * its attribute values and an org unit, so a programme with hundreds of rows reads hundreds of
     * records to render three. Order and cap first, then enrich only what survives.
     *
     * This is a nudge and not a proof — assigning the query to a local first evades it — which is
     * why the message says what to do rather than merely what is wrong.
     */
    private val CAP_AFTER_ENRICHMENT =
        Regex("""\.with[A-Z]\w*\(\)[\s\S]{0,400}?\.blockingGet\(\)[\s\S]{0,120}?\.take\(""")

    /**
     * Tokens that end a statement, used to reject a match that spans two of them.
     *
     * A character window alone does not do this. Comments are blanked before matching, so eighty
     * lines of comment between two unrelated calls collapse to eighty newlines and fit inside any
     * generous window — which is how the first version of this rule reported a chain that did not
     * exist. Requiring the match to stay inside one statement is what actually separates them.
     */
    private val STATEMENT_BREAK = Regex("""(^|\s)(val|var|fun|return)\s|;""")

    private val BLOCK_COMMENT = Regex("""/\*[\s\S]*?\*/""")
    private val LINE_COMMENT = Regex("""//[^\n]*""")

    /** Every violation in [sources] and [dependencies], worst first is not meaningful — all of them. */
    fun violations(
        sources: List<SourceFile>,
        dependencies: List<DeclaredDependency>,
    ): List<Violation> = sourceViolations(sources) + dependencyViolations(dependencies)

    private fun sourceViolations(sources: List<SourceFile>): List<Violation> =
        sources.flatMap { file ->
            val suppressed = IGNORE_MARKER in file.text
            // Comments are blanked, not deleted, so reported line numbers still match the file. A
            // rule quoted in a KDoc is documentation, not a breach — the sample learned that one the
            // hard way, with its own architecture rules cited in the docstring of the file that
            // enforces them.
            val code = file.text.blankComments()

            buildList {
                if (file.isShared) {
                    code.lineMatches(SDK_PACKAGE).forEach { (line, text) ->
                        add(
                            Violation(
                                rule = SDK_IN_SHARED_SOURCE,
                                where = "${file.path}:$line",
                                detail =
                                    "the DHIS2 SDK belongs in androidMain — `D2` is the " +
                                        "Android SDK and has no common-source equivalent — but this " +
                                        "is ${file.sourceSet}: $text",
                            ),
                        )
                    }
                    code.lineMatches(PLUGIN_CONTEXT).forEach { (line, text) ->
                        add(
                            Violation(
                                rule = CONTEXT_IN_SHARED_SOURCE,
                                where = "${file.path}:$line",
                                detail =
                                    "shared source must not reference Dhis2PluginContext; " +
                                        "pass plain data and callbacks so a @Preview can render it " +
                                        "without a server: $text",
                            ),
                        )
                    }
                }

                if (!suppressed) {
                    CAP_AFTER_ENRICHMENT
                        .findAll(code)
                        .firstOrNull { match -> !STATEMENT_BREAK.containsMatchIn(match.value) }
                        ?.let { match ->
                            add(
                                Violation(
                                    rule = CAP_BEFORE_ENRICHMENT,
                                    where = "${file.path}:${code.lineOf(match.range.first)}",
                                    detail =
                                        "this enriches every row with `.with…()` and caps " +
                                            "afterwards with `take(`, so resolving rows nobody sees. " +
                                            "Order and cap first, then re-query only those uids with " +
                                            "the children you need. If this really is intentional, put " +
                                            "`$IGNORE_MARKER $CAP_BEFORE_ENRICHMENT` in a comment in " +
                                            "this file",
                                ),
                            )
                        }
                }
            }
        }

    private fun dependencyViolations(dependencies: List<DeclaredDependency>): List<Violation> =
        dependencies.mapNotNull { dependency ->
            // Test source sets are exempt: a unit test has no host to borrow classes from, so it
            // declares the real thing. That is why the sample's commonTest uses `implementation`.
            if (!dependency.sourceSet.endsWith("Main")) return@mapNotNull null

            // Not the author's to change, so not the author's to be told about.
            if (PLUGIN_MANAGED.any { dependency.coordinates.startsWith(it) }) return@mapNotNull null

            val isResources = dependency.coordinates == COMPOSE_RESOURCES
            val isCompileOnly = dependency.bucket == "compileOnly"

            when {
                isResources && isCompileOnly ->
                    Violation(
                        rule = RESOURCES_MUST_BE_IMPLEMENTATION,
                        where = "${dependency.sourceSet} ${dependency.bucket}",
                        detail =
                            "$COMPOSE_RESOURCES must be `implementation`: the Compose Resources " +
                                "generator treats the declaration as its opt-in signal, and compileOnly " +
                                "silently generates no Res class, so every Res.string.* stops resolving",
                    )

                isResources -> null

                isCompileOnly -> null

                HOST_PROVIDED.any { dependency.coordinates.startsWith(it) } ->
                    Violation(
                        rule = HOST_DEP_NOT_COMPILE_ONLY,
                        where = "${dependency.sourceSet} ${dependency.bucket}",
                        detail =
                            "${dependency.coordinates} is provided by the Capture App at runtime, " +
                                "so declare it `compileOnly`. Bundling a class the host already owns is " +
                                "what produces ClassCastException at composition, and no build error " +
                                "precedes it",
                    )

                else -> null
            }
        }

    /**
     * Shared source: a source set whose name begins with `common`.
     *
     * By name rather than by which targets it feeds, because with a single Android target every
     * common source set is also reachable from that target and a set-difference classifies nothing.
     * The day a Desktop target exists the honest definition is "reachable from more than one
     * target", and this is the line to change.
     */
    private val SourceFile.isShared: Boolean get() = sourceSet.startsWith("common")

    private fun String.blankComments(): String =
        replace(BLOCK_COMMENT) { match -> "\n".repeat(match.value.count { it == '\n' }) }
            .replace(LINE_COMMENT, "")

    private fun String.lineMatches(pattern: Regex): List<Pair<Int, String>> =
        lineSequence()
            .withIndex()
            .filter { (_, line) -> pattern.containsMatchIn(line) }
            .map { (index, line) -> index + 1 to line.trim() }
            .toList()

    private fun String.lineOf(offset: Int): Int = take(offset).count { it == '\n' } + 1
}
