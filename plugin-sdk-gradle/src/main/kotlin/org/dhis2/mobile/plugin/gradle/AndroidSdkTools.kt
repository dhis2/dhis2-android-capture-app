package org.dhis2.mobile.plugin.gradle

import org.gradle.api.GradleException
import java.io.File

/**
 * Locates the two external tools a bundle is built with, both from the Android SDK's build-tools:
 * `d8` to dex the plugin's classes, and `apksigner` to sign the zip.
 */
internal object AndroidSdkTools {
    /** Dexer. */
    fun d8(sdkDirectory: File): File = buildTool(sdkDirectory, "d8", "pluginBundle.d8Executable")

    /**
     * Signer.
     *
     * `apksigner` rather than the JDK's `jarsigner`, even though the bundle is a plain signed zip:
     * `jarsigner` embeds a `signingTime` attribute in the PKCS#7 block, and because that attribute is
     * itself signed, two builds of identical content never produce the same bytes — which means a new
     * SHA-256 to paste into the server dataStore after every rebuild. `apksigner`'s v1 (JAR) signature
     * carries no timestamp, so the bundle is reproducible. Both produce signatures the host accepts.
     */
    fun apksigner(sdkDirectory: File): File = buildTool(sdkDirectory, "apksigner", "pluginBundle.apksignerExecutable")

    /**
     * [name] from the newest installed build-tools. AGP resolves the SDK location itself, so only the
     * build-tools component can realistically be missing — hence the install hint.
     */
    private fun buildTool(
        sdkDirectory: File,
        name: String,
        overrideProperty: String,
    ): File {
        val buildTools = File(sdkDirectory, "build-tools")
        val newest =
            buildTools
                .listFiles()
                ?.filter { it.isDirectory }
                ?.maxByOrNull { it.name }
                ?: throw GradleException(
                    "No Android build-tools installed under $buildTools. Install them through the SDK " +
                        "Manager, or set $overrideProperty.",
                )

        val tool = File(newest, name)
        if (!tool.exists()) {
            throw GradleException(
                "$name not found at $tool. Install build-tools ${newest.name} through the SDK Manager, " +
                    "or set $overrideProperty.",
            )
        }
        return tool
    }
}
