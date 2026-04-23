package org.dhis2.form.ui.plugin

import android.util.Log
import android.webkit.JavascriptInterface
import com.google.gson.Gson

class PluginBridge(
    private val setFieldValue: (SetFieldValueParams) -> Unit,
    private val setContextFieldValue: (SetContextFieldValueParams) -> Unit,
) {
    private val gson = Gson()

    @JavascriptInterface
    fun onSetFieldValue(paramsJson: String) {
        Log.d(TAG, "onSetFieldValue $paramsJson")
        runCatching { gson.fromJson(paramsJson, SetFieldValueParams::class.java) }
            .onSuccess(setFieldValue)
            .onFailure { Log.e(TAG, "onSetFieldValue parse failed", it) }
    }

    @JavascriptInterface
    fun onSetContextFieldValue(paramsJson: String) {
        Log.d(TAG, "onSetContextFieldValue $paramsJson")
        runCatching { gson.fromJson(paramsJson, SetContextFieldValueParams::class.java) }
            .onSuccess(setContextFieldValue)
            .onFailure { Log.e(TAG, "onSetContextFieldValue parse failed", it) }
    }

    companion object {
        const val JS_NAME = "Android"
        private const val TAG = "PluginBridge"
    }
}
