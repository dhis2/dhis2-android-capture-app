package org.dhis2.data.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import org.dhis2.R

class BiometricController(
    private val fragmentActivity: FragmentActivity,
) {

    private var biometricPrompt: BiometricPrompt? = null

    fun hasBiometric(): Boolean {
        val biometricManager = BiometricManager.from(fragmentActivity)
        return when (biometricManager.canAuthenticate(BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun authenticate(onSuccess: () -> Unit) {
        if (biometricPrompt == null) {
            biometricPrompt = BiometricPrompt(
                fragmentActivity,
                ContextCompat.getMainExecutor(fragmentActivity),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult,
                    ) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }
                },
            )
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(fragmentActivity.getString(R.string.biometric_title))
            .setNegativeButtonText(fragmentActivity.getString(R.string.use_password))
            .build()

        biometricPrompt?.authenticate(promptInfo)
    }
}
