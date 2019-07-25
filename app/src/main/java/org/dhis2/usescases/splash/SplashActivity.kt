package org.dhis2.usescases.splash

import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.util.Base64
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.gms.safetynet.SafetyNet
import org.dhis2.App
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.databinding.ActivitySplashBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import javax.inject.Inject
import javax.inject.Named
import org.json.JSONObject
import java.security.SecureRandom


class SplashActivity : ActivityGlobalAbstract(), SplashContracts.View {
    companion object {
        const val FLAG = "FLAG"
    }

    lateinit var binding: ActivitySplashBinding

    @Inject
    lateinit var presenter: SplashContracts.Presenter

    @Inject
    @field:Named(FLAG)
    lateinit var flag: String


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SplashTheme)
        val appComponent = (applicationContext as App).appComponent()
        val serverComponent = (applicationContext as App).serverComponent()
        appComponent.plus(SplashModule(serverComponent)).inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        renderFlag(flag)
        SafetyNet.getClient(this).attest(generateNonce(), BuildConfig.GOOGLE_VERIFICATION_API_KEY)
                .addOnSuccessListener(this) { result ->
                    val json = JSONObject(decodeJws(result.jwsResult))
                    if (json.has("basicIntegrity") &&
                            json["basicIntegrity"] as Boolean) {
                        presenter.init(this)
                    } else {
                        showToast("Phone with basic compromised integrity.")
                    }
                }
                .addOnFailureListener(this) { e ->
                    showToast("Phone with basic compromised integrity.")
                }
    }

    private fun decodeJws(jwsResult: String?): String? {
        if (jwsResult == null) {
            return null
        }
        val jwtParts = jwsResult.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (jwtParts.size == 3) {
            String(Base64.decode(jwtParts[1], Base64.DEFAULT))
        } else {
            null
        }
    }

    private fun generateNonce(): ByteArray {
        val nonce = ByteArray(16)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(nonce)
        return nonce
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        presenter.destroy()
        super.onPause()
    }

    override fun renderFlag(flagName: String) {
        val resource = if (!isEmpty(flagName))
            resources.getIdentifier(flagName, "drawable", packageName)
        else
            -1
        if (resource != -1) {
            binding.flag.setImageResource(resource)
            binding.logo.visibility = View.GONE
            binding.flag.visibility = View.VISIBLE
        }
    }


}