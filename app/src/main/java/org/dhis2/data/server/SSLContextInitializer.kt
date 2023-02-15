package org.dhis2.data.server

import android.content.Context
import java.security.NoSuchAlgorithmException
import java.security.Security
import javax.net.ssl.SSLContext
import org.conscrypt.Conscrypt
import timber.log.Timber

object SSLContextInitializer {
    fun initializeSSLContext(context: Context?) {
        try {
            SSLContext.getInstance("TLSv1.2")
            Timber.i("%s Initialized", SSLContextInitializer::class.java.simpleName)
        } catch (e: NoSuchAlgorithmException) {
            Timber.e(SSLContextInitializer::class.java.simpleName, e.toString())
        }
        try {
            Security.insertProviderAt(Conscrypt.newProvider(), 1)
        } catch (e: Exception) {
            Timber.e(SSLContextInitializer::class.java.simpleName, e.toString())
        }
    }
}
