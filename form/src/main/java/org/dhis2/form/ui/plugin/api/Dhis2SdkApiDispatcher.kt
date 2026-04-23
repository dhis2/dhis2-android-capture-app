package org.dhis2.form.ui.plugin.api

import android.util.Log
import com.google.gson.Gson
import org.hisp.dhis.android.core.D2Manager

class Dhis2SdkApiDispatcher {
    private val gson = Gson()
    private val d2 get() = D2Manager.getD2()

    fun get(path: String): String? {
        // Strip a leading "{apiVersion}/" segment so /api/43/me and /api/me hit the same branch.
        val normalised = stripApiVersion(path)
        return when {
            normalised.startsWith("system/info") || normalised == "systemInfo" -> systemInfo()
            normalised == "me" || normalised.startsWith("me/") -> me()
            normalised.startsWith("userSettings") -> userSettings()
            normalised.startsWith("schemas") -> "[]"
            else -> {
                Log.w(TAG, "Unhandled GET /api/$path — plugins should log which endpoints they need")
                null
            }
        }
    }

    private fun stripApiVersion(path: String): String {
        val slash = path.indexOf('/')
        if (slash <= 0) return path
        val head = path.substring(0, slash)
        return if (head.all { it.isDigit() }) path.substring(slash + 1) else path
    }

    private fun systemInfo(): String {
        val info = runCatching { d2.systemInfoModule().systemInfo().blockingGet() }
            .onFailure { Log.w(TAG, "systemInfo SDK call failed", it) }
            .getOrNull()
        // Only fields guaranteed to exist on the SDK's SystemInfo are pulled; everything else
        // gets a safe default so @dhis2/app-runtime boot doesn't trip on missing keys.
        return gson.toJson(
            mapOf(
                "version" to (info?.version() ?: "unknown"),
                "contextPath" to (info?.contextPath() ?: ""),
                "dateFormat" to (info?.dateFormat() ?: "yyyy-MM-dd"),
                "revision" to "android-sdk",
                "systemId" to "android-local",
                "systemName" to "DHIS2 Android Capture",
                "serverDate" to "",
            ),
        )
    }

    private fun me(): String {
        val user = runCatching { d2.userModule().user().blockingGet() }
            .onFailure { Log.w(TAG, "user SDK call failed", it) }
            .getOrNull()
        val authorities = runCatching {
            d2.userModule().authorities().blockingGet().mapNotNull { it.name() }
        }
            .onFailure { Log.w(TAG, "authorities SDK call failed", it) }
            .getOrElse { emptyList() }

        return gson.toJson(
            mapOf(
                "id" to (user?.uid() ?: ""),
                "username" to (user?.username() ?: ""),
                "firstName" to (user?.firstName() ?: ""),
                "surname" to (user?.surname() ?: ""),
                "displayName" to (user?.displayName() ?: ""),
                "email" to (user?.email() ?: ""),
                "authorities" to authorities,
                "settings" to emptyMap<String, Any>(),
                "userCredentials" to mapOf(
                    "username" to (user?.username() ?: ""),
                    "userRoles" to emptyList<Map<String, Any>>(),
                ),
            ),
        )
    }

    private fun userSettings(): String = gson.toJson(
        mapOf(
            "keyUiLocale" to "en",
            "keyDbLocale" to "en",
            "keyMessageEmailNotification" to false,
            "keyMessageSmsNotification" to false,
        ),
    )

    companion object {
        private const val TAG = "Dhis2SdkApiDispatcher"
    }
}
