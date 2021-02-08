package org.dhis2.usescases.login.auth

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import net.openid.appauth.AuthorizationService
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.login.LoginActivity

abstract class AuthActivity : ActivityGlobalAbstract() {

    private val openIdHandler = OpenIdHandler(object : OnAuthRequestIntent {
        override fun startIntent(intent: Intent, requestCode: Int) {
            startActivityForResult(intent, requestCode)
        }

        override fun getPendingIntent(): PendingIntent {
            val intent = Intent(this@AuthActivity, LoginActivity::class.java)
            return PendingIntent.getActivity(
                this@AuthActivity,
                RC_AUTH,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    })

    override fun onPause() {
        openIdHandler.onPause()
        super.onPause()
    }

    fun attemptLogin(authServiceModel: AuthServiceModel) {

        val filter = IntentFilter(Intent.ACTION_VIEW).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addCategory(Intent.CATEGORY_BROWSABLE)
            addDataScheme(authServiceModel.redirectUri.toString())
        }
        openIdHandler.logIn(authServiceModel, AuthorizationService(this)) { response ->
            loginWithAuthorization()
        }
    }

    abstract fun loginWithAuthorization()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        openIdHandler.handleAuthRequestResult(requestCode, resultCode, data) { response ->
            loginWithAuthorization()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
