package org.dhis2.mobile.commons.logging

import android.util.Log

actual fun logDebug(
    tag: String,
    message: String,
) {
    Log.d(tag, message)
}
