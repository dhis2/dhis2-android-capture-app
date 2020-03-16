package org.dhis2.usescases.sms

import android.app.Service
import android.content.Intent
import android.os.IBinder

class HeadlessSmsSendService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}