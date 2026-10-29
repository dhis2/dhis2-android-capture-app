package org.dhis2.usescases.about.model

/**
 * Build, connection and account facts shown in the "This install" section of the About screen.
 *
 * @param version app version name, e.g. `3.5.0`.
 * @param buildHash short commit hash, or null on release builds, where it is not surfaced.
 * @param sdkVersion version of the DHIS2 Android SDK the app was built against.
 * @param serverUrl server the user is signed in to, without the `https://` scheme.
 * @param username username of the signed in user.
 */
data class AboutInfo(
    val version: String,
    val buildHash: String?,
    val sdkVersion: String,
    val serverUrl: String,
    val username: String,
)
