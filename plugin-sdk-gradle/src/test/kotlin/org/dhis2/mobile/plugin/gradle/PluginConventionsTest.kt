package org.dhis2.mobile.plugin.gradle

import org.dhis2.mobile.plugin.gradle.PluginConventions.DeclaredDependency
import org.dhis2.mobile.plugin.gradle.PluginConventions.SourceFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The conventions rules, as data in and findings out.
 *
 * Each test names the on-device failure the rule prevents, because that is the only reason any of
 * these is worth failing a build over.
 */
class PluginConventionsTest {

    private fun shared(text: String) = SourceFile("src/commonMain/Thing.kt", "commonMain", text)

    private fun android(text: String) =
        SourceFile("src/androidMain/Repo.kt", "androidMain", text)

    private fun rulesOf(vararg sources: SourceFile) =
        PluginConventions.violations(sources.toList(), emptyList()).map { it.rule }

    private fun rulesOf(vararg dependencies: DeclaredDependency) =
        PluginConventions.violations(emptyList(), dependencies.toList()).map { it.rule }

    // ---------------------------------------------------------------- shared source

    @Test
    fun `flags the DHIS2 SDK in shared source, because D2 is the Android SDK`() {
        val violations = rulesOf(shared("import org.hisp.dhis.android.core.D2"))

        assertEquals(listOf(PluginConventions.SDK_IN_SHARED_SOURCE), violations)
    }

    @Test
    fun `allows the DHIS2 SDK in androidMain, which is the whole point of the rule`() {
        assertEquals(emptyList<String>(), rulesOf(android("import org.hisp.dhis.android.core.D2")))
    }

    @Test
    fun `flags the SDK in shared test source too, so a test cannot smuggle it in`() {
        val violations = PluginConventions.violations(
            listOf(SourceFile("src/commonTest/T.kt", "commonTest", "org.hisp.dhis.android.core.D2")),
            emptyList(),
        )

        assertEquals(listOf(PluginConventions.SDK_IN_SHARED_SOURCE), violations.map { it.rule })
    }

    @Test
    fun `does not flag a rule quoted in a comment`() {
        // The sample learned this one the hard way: its own architecture rules are cited in the
        // KDoc of the file that enforces them, and a naive grep called that a violation.
        val kdoc = shared(
            """
            /**
             * No `org.hisp.dhis` here, and never a Dhis2PluginContext.
             */
            class Thing
            """.trimIndent(),
        )

        assertEquals(emptyList<String>(), rulesOf(kdoc))
    }

    @Test
    fun `does not flag a rule mentioned in a line comment`() {
        assertEquals(emptyList<String>(), rulesOf(shared("// Dhis2PluginContext belongs in androidMain")))
    }

    @Test
    fun `flags Dhis2PluginContext in shared source, so a Preview can still render`() {
        val violations = rulesOf(shared("fun Card(context: Dhis2PluginContext) = Unit"))

        assertEquals(listOf(PluginConventions.CONTEXT_IN_SHARED_SOURCE), violations)
    }

    @Test
    fun `reports the line the problem is on`() {
        val file = shared("package a\n\nimport b\n\nimport org.hisp.dhis.android.core.D2\n")

        val violation = PluginConventions.violations(listOf(file), emptyList()).single()

        assertEquals("src/commonMain/Thing.kt:5", violation.where)
    }

    // ---------------------------------------------------------------- dependencies

    @Test
    fun `flags a host-provided dependency that is not compileOnly`() {
        val violations = rulesOf(
            DeclaredDependency("commonMain", "implementation", "androidx.compose.material3:material3"),
        )

        assertEquals(listOf(PluginConventions.HOST_DEP_NOT_COMPILE_ONLY), violations)
    }

    @Test
    fun `flags a host-provided dependency on runtimeOnly, which packages it just the same`() {
        val violations = rulesOf(
            DeclaredDependency("androidMain", "runtimeOnly", "io.insert-koin:koin-core"),
        )

        assertEquals(listOf(PluginConventions.HOST_DEP_NOT_COMPILE_ONLY), violations)
    }

    @Test
    fun `accepts a host-provided dependency on compileOnly`() {
        assertEquals(
            emptyList<String>(),
            rulesOf(DeclaredDependency("commonMain", "compileOnly", "org.jetbrains.compose.ui:ui")),
        )
    }

    @Test
    fun `exempts test source sets, which have no host to borrow classes from`() {
        assertEquals(
            emptyList<String>(),
            rulesOf(
                DeclaredDependency("commonTest", "implementation", "io.insert-koin:koin-core"),
                DeclaredDependency("androidHostTest", "implementation", "org.hisp.dhis:android-core"),
            ),
        )
    }

    @Test
    fun `ignores a dependency the host does not provide`() {
        assertEquals(
            emptyList<String>(),
            rulesOf(DeclaredDependency("commonMain", "implementation", "com.example:tiny-util")),
        )
    }

    @Test
    fun `exempts what the Kotlin plugin adds itself, which no author can change`() {
        // kotlin-stdlib lands on every source set's api without anyone asking. Flagging it failed
        // the sample on this rule's first real run, and would have failed every plugin project.
        assertEquals(
            emptyList<String>(),
            rulesOf(DeclaredDependency("commonMain", "api", "org.jetbrains.kotlin:kotlin-stdlib")),
        )
    }

    @Test
    fun `requires compose resources to be implementation, or no Res class is generated`() {
        val violations = rulesOf(
            DeclaredDependency("commonMain", "compileOnly", PluginConventions.COMPOSE_RESOURCES),
        )

        assertEquals(listOf(PluginConventions.RESOURCES_MUST_BE_IMPLEMENTATION), violations)
    }

    @Test
    fun `accepts compose resources on implementation, the one host-provided exception`() {
        assertEquals(
            emptyList<String>(),
            rulesOf(
                DeclaredDependency("commonMain", "implementation", PluginConventions.COMPOSE_RESOURCES),
            ),
        )
    }

    // ---------------------------------------------------------------- cap before enrichment

    @Test
    fun `flags enriching every row and capping afterwards`() {
        // The sample's own expression, before it was fixed.
        val repository = android(
            """
            val recent = enrolled
                .withTrackedEntityAttributeValues()
                .orderByCreated(RepositoryScope.OrderByDirection.DESC)
                .blockingGet()
                .take(LISTED_LIMIT)
            """.trimIndent(),
        )

        assertEquals(listOf(PluginConventions.CAP_BEFORE_ENRICHMENT), rulesOf(repository))
    }

    @Test
    fun `does not flag enrichment without a cap`() {
        assertEquals(
            emptyList<String>(),
            rulesOf(android("val all = enrolled.withTrackedEntityAttributeValues().blockingGet()")),
        )
    }

    @Test
    fun `does not flag a cap with no enrichment, which is the fix`() {
        val fixed = android(
            """
            val uids = enrolled.orderByCreated(DESC).blockingGet().take(3).map { it.uid() }
            val recent = enrolled.byUid().`in`(uids).withTrackedEntityAttributeValues().blockingGet()
            """.trimIndent(),
        )

        assertEquals(emptyList<String>(), rulesOf(fixed))
    }

    @Test
    fun `does not chain the match across unrelated statements`() {
        val unrelated = android(
            "val a = x.withChildren().blockingGet()\n" + "// ...\n".repeat(80) +
                "val b = y.blockingGet().take(3)",
        )

        assertEquals(emptyList<String>(), rulesOf(unrelated))
    }

    @Test
    fun `honours the ignore marker, since the rule is a heuristic`() {
        val suppressed = android(
            """
            // ${PluginConventions.IGNORE_MARKER} ${PluginConventions.CAP_BEFORE_ENRICHMENT}
            val recent = enrolled.withTrackedEntityAttributeValues().blockingGet().take(3)
            """.trimIndent(),
        )

        assertEquals(emptyList<String>(), rulesOf(suppressed))
    }

    @Test
    fun `names the ignore marker in the message, so the escape is discoverable`() {
        val violation = PluginConventions.violations(
            listOf(android("enrolled.withTrackedEntityAttributeValues().blockingGet().take(3)")),
            emptyList(),
        ).single()

        assertTrue(PluginConventions.IGNORE_MARKER in violation.detail)
    }

    @Test
    fun `renders a violation as something a build error can print`() {
        val violation = PluginConventions.violations(
            listOf(shared("import org.hisp.dhis.android.core.D2")),
            emptyList(),
        ).single()

        assertTrue(violation.toString().startsWith("[${PluginConventions.SDK_IN_SHARED_SOURCE}] "))
        assertTrue("src/commonMain/Thing.kt:1" in violation.toString())
    }
}
