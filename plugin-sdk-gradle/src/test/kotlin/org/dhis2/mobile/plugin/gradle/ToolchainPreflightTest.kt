package org.dhis2.mobile.plugin.gradle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolchainPreflightTest {
    private val compatible =
        ToolchainState(
            kotlinVersion = HostToolchain.KOTLIN,
            composeVersion = HostToolchain.COMPOSE,
            compileSdk = HostToolchain.COMPILE_SDK,
            minSdk = HostToolchain.MIN_SDK_FLOOR,
            jvmTarget = HostToolchain.JVM_TARGET.toString(),
        )

    @Test
    fun `a project matching the host has no problems`() {
        assertTrue(ToolchainPreflight.problems(compatible).isEmpty())
    }

    @Test
    fun `undeterminable values are skipped rather than guessed`() {
        assertTrue(ToolchainPreflight.problems(ToolchainState()).isEmpty())
    }

    @Test
    fun `a different Kotlin version is rejected`() {
        val problems = ToolchainPreflight.problems(compatible.copy(kotlinVersion = "2.3.0"))

        assertEquals(1, problems.size)
        assertTrue(problems.single().contains("incompatible version of Kotlin"))
    }

    @Test
    fun `a different Compose version is rejected`() {
        val problems = ToolchainPreflight.problems(compatible.copy(composeVersion = "1.9.0"))

        assertEquals(1, problems.size)
        assertTrue(problems.single().contains("NoSuchMethodError"))
    }

    @Test
    fun `compileSdk below the host is rejected but above it is fine`() {
        assertEquals(
            1,
            ToolchainPreflight.problems(compatible.copy(compileSdk = HostToolchain.COMPILE_SDK - 1)).size,
        )
        assertTrue(
            ToolchainPreflight.problems(compatible.copy(compileSdk = HostToolchain.COMPILE_SDK + 1)).isEmpty(),
        )
    }

    @Test
    fun `minSdk below the class loader's floor is rejected`() {
        val problems = ToolchainPreflight.problems(compatible.copy(minSdk = 24))

        assertEquals(1, problems.size)
        assertTrue(problems.single().contains("InMemoryDexClassLoader"))
    }

    @Test
    fun `a lower JVM target is safe and a higher one is not`() {
        // A plugin built for JVM 11 loads fine on a JVM 17 host: a lower class-file version is
        // accepted, a higher one is not. §5.1 of the docs used to call this an equality requirement.
        assertTrue(ToolchainPreflight.problems(compatible.copy(jvmTarget = "11")).isEmpty())
        assertTrue(ToolchainPreflight.problems(compatible.copy(jvmTarget = "1.8")).isEmpty())
        assertEquals(
            1,
            ToolchainPreflight
                .problems(
                    compatible.copy(jvmTarget = (HostToolchain.JVM_TARGET + 4).toString()),
                ).size,
        )
    }

    @Test
    fun `the legacy android library plugin is rejected`() {
        val problems = ToolchainPreflight.problems(compatible.copy(usesLegacyAndroidLibraryPlugin = true))

        assertEquals(1, problems.size)
        assertTrue(problems.single().contains("com.android.kotlin.multiplatform.library"))
    }

    @Test
    fun `every problem is reported at once`() {
        val problems =
            ToolchainPreflight.problems(
                compatible.copy(kotlinVersion = "2.3.0", minSdk = 23, compileSdk = 30),
            )

        assertEquals(3, problems.size)
    }

    @Test
    fun `jvm target strings are normalised`() {
        assertEquals(17, ToolchainPreflight.majorJvmVersion("17"))
        assertEquals(17, ToolchainPreflight.majorJvmVersion("JVM_17"))
        assertEquals(8, ToolchainPreflight.majorJvmVersion("1.8"))
        assertNull(ToolchainPreflight.majorJvmVersion(null))
    }

    @Test
    fun `dependencies that will not be packaged are warned about`() {
        assertNull(ToolchainPreflight.runtimeDependencyWarning(emptyList()))
        assertTrue(
            ToolchainPreflight
                .runtimeDependencyWarning(listOf("com.squareup.okhttp3:okhttp"))
                .orEmpty()
                .contains("NoClassDefFoundError"),
        )
    }
}
