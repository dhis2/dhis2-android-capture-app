package org.dhis2.mobile.extensibility.model

enum class FailureReason {
    INSECURE_SERVER,
    WEBVIEW_NOT_SUPPORTED,
    UNSUPPORTED_FEATURE,
    EMPTY_CONTENT,
    CONTENT_UNREADABLE,
    RENDER_TIMEOUT,
    RENDERER_CRASHED,
    SCRIPT_ERROR,
}
