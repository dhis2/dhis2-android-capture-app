package org.dhis2.usescases.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.dhis2.R
import org.dhis2.databinding.ActivityAboutPolicyBinding

class PolicyView : AppCompatActivity() {

    private lateinit var binding: ActivityAboutPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about_policy)
        initToolbar()
        binding.webviewPolicy.loadUrl(policyAssets)
    }

    private fun initToolbar() {
        binding.menu.setOnClickListener { onBackPressed() }
        binding.toolbarText.text = getString(R.string.privacy_policy_title)
    }

    companion object {
        const val policyAssets = "file:///android_asset/policy.html"
    }
}
