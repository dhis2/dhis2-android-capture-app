package org.dhis2.usescases.login.auth

import android.content.Intent
import net.openid.appauth.AuthorizationService
import org.dhis2.usescases.general.ActivityGlobalAbstract

abstract class AuthActivity : ActivityGlobalAbstract() {

    var isPerformingLogin = false

    private val openIdHandler = OpenIdHandler(object : OnAuthRequestIntent {
        override fun startIntent(intent: Intent, requestCode: Int) {
            startActivityForResult(intent, requestCode)
        }
    })

    override fun onStop() {
        if (!isPerformingLogin) {
            openIdHandler.onPause()
        }
        super.onStop()
    }

    fun attemptLogin(authServiceModel: AuthServiceModel) {
        isPerformingLogin = true
        openIdHandler.logIn(
            authServiceModel,
            AuthorizationService(this/*, openIdHandler.appAuthConfig()*/)
        ) { response ->
            response.token?.let {
                loginWithAuthorization(it)
            }
        }
    }

    abstract fun loginWithAuthorization(token: String)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        isPerformingLogin = false
        openIdHandler.handleAuthRequestResult(requestCode, resultCode, data) { response ->
            response.token?.let {
                loginWithAuthorization(it)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
