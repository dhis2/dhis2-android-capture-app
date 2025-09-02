package org.dhis2.data.service

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.dhis2.BuildConfig
import org.dhis2.bindings.newVersion
import org.hisp.dhis.android.core.D2
import java.io.File

class VersionRepository(
    val d2: D2,
) {
    private val _newAppVersion = MutableSharedFlow<String?>(replay = 1)
    val newAppVersion: SharedFlow<String?> get() = _newAppVersion

    suspend fun downloadLatestVersionInfo() {
        d2.settingModule().latestAppVersion().blockingDownload()
        checkVersionUpdates()
    }

    suspend fun getLatestVersionInfo(): String? =
        d2
            .settingModule()
            .latestAppVersion()
            .blockingGet()
            ?.version()
            .takeIf { it?.newVersion(BuildConfig.VERSION_NAME) ?: false }

    suspend fun checkVersionUpdates() {
        val versionNameOrNull = getLatestVersionInfo()
        _newAppVersion.emit(versionNameOrNull)
    }

    fun download(
        context: Context,
        onDownloadCompleted: (Uri) -> Unit,
        onDownloading: () -> Unit,
    ) {
        val url =
            d2
                .settingModule()
                .latestAppVersion()
                .blockingGet()
                ?.downloadURL()
        val fileName = url?.substringAfterLast("/")

        val destination = "${Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS,
        )}/$fileName"

        val apkFile = File(destination)
        val apkUri = uriFromFile(context, apkFile)
        if (apkFile.exists()) {
            onDownloadCompleted(apkUri)
        } else if (fileName?.endsWith("apk") == true) {
            val request =
                DownloadManager
                    .Request(url.toUri())
                    .setAllowedNetworkTypes(
                        DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE,
                    ).setTitle(fileName)
                    .setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED,
                    ).setAllowedOverMetered(true)
                    .setAllowedOverRoaming(false)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            onDownloading()

            val onComplete: BroadcastReceiver =
                object : BroadcastReceiver() {
                    override fun onReceive(
                        ctxt: Context,
                        intent: Intent?,
                    ) {
                        onDownloadCompleted(apkUri)
                    }
                }
            ContextCompat.registerReceiver(
                context,
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED,
            )
        } else {
            url?.let { onDownloadCompleted(it.toUri()) }
        }
    }

    private fun uriFromFile(
        context: Context,
        file: File,
    ): Uri =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
        } else {
            Uri.fromFile(file)
        }

    fun getUrl(): String? =
        d2
            .settingModule()
            .latestAppVersion()
            .blockingGet()
            ?.downloadURL()

    fun removeVersionInfo() {
        _newAppVersion.tryEmit("")
    }
}
