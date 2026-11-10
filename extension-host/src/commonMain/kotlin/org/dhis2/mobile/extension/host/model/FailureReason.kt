package org.dhis2.mobile.extension.host.model

/**
 * Why an extension is not shown. The user gets the native form, and the reason is sent to
 * Sentry each time it happens.
 */
enum class FailureReason {
    // Found before the form is shown
    INSECURE_SERVER,
    WEBVIEW_NOT_SUPPORTED,
    UNSUPPORTED_FEATURE,
    EMPTY_CONTENT,
    CONTENT_UNREADABLE,

    // The form does not finish loading, or the WebView crashes: switch to the native form
    RENDER_TIMEOUT,
    RENDERER_CRASHED,

    // A script error after the form has loaded: reported only, the form stays
    SCRIPT_ERROR,
}
