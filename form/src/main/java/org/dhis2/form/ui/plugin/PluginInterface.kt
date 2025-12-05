package org.dhis2.form.ui.plugin

import android.webkit.JavascriptInterface

class PluginInterface(
    private val onUpdate: (String) -> Unit
) {
    @JavascriptInterface
    fun updateValue(value: String) {
        onUpdate(value)
    }
}
