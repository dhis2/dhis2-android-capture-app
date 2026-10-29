package org.dhis2.usescases.about.model

private const val SUPPORTED_FEATURES_URL =
    "https://drive.google.com/open?id=1YHRBk6LSp6uROfs19-wCfQGc-rgl2f5iDrhCDasTR8M"
private const val SOURCE_CODE_URL = "https://github.com/dhis2/dhis2-android-capture-app"

/**
 * Tappable rows of the "About this app" section.
 *
 * [PRIVACY_POLICY] has no [url]: it renders the policy bundled with the app, so it works offline.
 * The rest leave the app for a browser and need a connection.
 */
enum class AboutLink(
    val url: String?,
) {
    SUPPORTED_FEATURES(SUPPORTED_FEATURES_URL),
    PRIVACY_POLICY(null),
    SOURCE_CODE(SOURCE_CODE_URL),
    ;

    val isExternal: Boolean
        get() = url != null
}
