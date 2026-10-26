package org.dhis2.mobile.commons.logging

actual fun logDebug(
    tag: String,
    message: String,
) {
    println("DEBUG | $tag: $message")
}
