package org.dhis2.usescases.login.auth

import android.app.PendingIntent
import android.content.Intent

interface OnAuthRequestIntent {
    fun startIntent(intent: Intent, requestCode: Int)
}