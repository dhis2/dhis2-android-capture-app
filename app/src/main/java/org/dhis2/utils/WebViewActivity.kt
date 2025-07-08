package org.dhis2.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import org.dhis2.R
import org.dhis2.databinding.ActivityWebviewBinding
import timber.log.Timber

class WebViewActivity : Activity() {

    companion object {
        const val WEB_VIEW_URL = "url"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityWebviewBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_webview)

        val url = intent?.extras?.getString(WEB_VIEW_URL)

        url?.let {
            // Avoid the WebView to automatically redirect to a browser
            binding.webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest,
                ): Boolean {
                    Timber.tag("WEBVIEW_CAPTURE").d("new url: ${request.url}")
                    return super.shouldOverrideUrlLoading(view, request)
                }

                // Compatibility with APIs below 24
                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    return super.shouldOverrideUrlLoading(view, url)
                }
            }

            binding.webView.settings.javaScriptEnabled = true
            binding.webView.settings.domStorageEnabled = true
            binding.webView.loadUrl(it)
        }
    }

    fun backToLogin(view: View) {
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }
}
