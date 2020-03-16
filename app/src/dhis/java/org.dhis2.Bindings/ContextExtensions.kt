package org.dhis2.Bindings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import hu.supercluster.paperwork.Paperwork
import org.dhis2.BuildConfig

fun Context.buildInfo(): String {
    val paperWork = Paperwork(this)
    return if (BuildConfig.BUILD_TYPE == "release") {
        "v${BuildConfig.VERSION_NAME}"
    } else {
        "v${BuildConfig.VERSION_NAME} : ${paperWork.get("buildTime")} : ${paperWork.get("gitSha")} "
    }
}

fun Fragment.checkSMSPermission(requestPermission: Boolean, request: Int): Boolean {
    val smsPermissions = arrayOf(
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    )

    if (context?.hasPermissions(smsPermissions) != true) {
        if (requestPermission) {
            requestPermissions(smsPermissions, request)
        }
        return false
    }
    return true
}

fun Context.hasPermissions(permissions: Array<String>): Boolean {
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
    }
    return true
}

fun Context.showSMS(): Boolean {
    return BuildConfig.FLAVOR == "dhis"
}

fun Context.defaultSMSHandler(): String {
    return Telephony.Sms.getDefaultSmsPackage(this)
}

fun Context.isDefaultSMSHandler(): Boolean {
    return packageName == defaultSMSHandler()
}

fun Fragment.setDefaultSMSHandler(defaultSMSHandlerPackage: String? = null, reqID: Int) {
    val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
    intent.putExtra(
        Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
        defaultSMSHandlerPackage ?: context?.packageName
    )
    startActivityForResult(intent, reqID)
}