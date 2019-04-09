package org.dhis2.utils

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.dhis2.R
import org.dhis2.databinding.ActivityWebviewBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract


class WebViewActivity : ActivityGlobalAbstract() {

    companion object {
        const val WEB_VIEW_URL = "url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityWebviewBinding = DataBindingUtil.setContentView(this, R.layout.activity_webview)

        val url = intent?.extras?.getString(WEB_VIEW_URL)

        url?.let {
            binding.webView.loadUrl(it)
        }
    }
}