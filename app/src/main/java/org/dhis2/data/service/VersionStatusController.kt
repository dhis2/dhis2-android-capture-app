package org.dhis2.data.service

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File
import org.dhis2.BuildConfig
import org.hisp.dhis.android.core.D2
import timber.log.Timber

class VersionStatusController(val d2: D2) {

    private val _newAppVersion = MutableLiveData(false)
    val newAppVersion: LiveData<Boolean> = _newAppVersion

    fun checkVersionUpdates() {
        val currentVersion = BuildConfig.VERSION_NAME
        if (true /* currentVersion == d2.serverVersion()*/) {
            _newAppVersion.postValue(true)
        }
    }

    fun download(context: Context, onDownloadCompleted: (Uri) -> Unit) {
        var destination: String = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        ).toString()
        destination += "/dhis2-v2.7.1.1.apk"

        val apkFile = File(destination)
        val apkUri = uriFromFile(context, apkFile)
        if (apkFile.exists()) {
            if (apkFile.canRead()) {
                onDownloadCompleted(apkUri)
            } else {
                Timber.e("APK", "Cant read " + apkFile.absoluteFile)
            }
        } else {
            val uri = Uri.parse("file://$destination")
            val url = Uri.parse("https://github.com/dhis2/dhis2-android-capture-app/releases/download/2.7.1.1/dhis2-v2.7.1.1.apk")
            val request = DownloadManager.Request(url).apply {
                setDestinationUri(uri)
                setMimeType("application/vnd.android.package-archive")
                setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
            }

            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadID = manager.enqueue(request)

            val onComplete: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent?) {
                    onDownloadCompleted(apkUri)
                }
            }

            context.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }

    private fun uriFromFile(context: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
        } else {
            Uri.fromFile(file)
        }
    }
}
