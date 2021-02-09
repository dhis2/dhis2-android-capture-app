package org.dhis2.usescases.login.auth

import android.content.Intent
import android.net.Uri
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.RegistrationRequest
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.browser.BrowserWhitelist
import net.openid.appauth.browser.VersionedBrowserMatcher


const val RC_AUTH = 2021

class OpenIdHandler(private val onAuthRequestIntent: OnAuthRequestIntent) {

    private var authService: AuthorizationService? = null

    fun logIn(
        authServiceModel: AuthServiceModel,
        authorizationService: AuthorizationService,
        responseCallback: (AuthServiceResponseModel) -> Unit
    ) {
        this.authService = authorizationService
        if (authServiceModel.authType == AuthType.OPENID) {
            doAuthorizationWithOpenId(authServiceModel)
        } else {
            doAuthorizationWithOAuth2(authServiceModel, responseCallback)
        }
    }

    fun onPause() {
        authService?.dispose()
    }

    fun handleAuthRequestResult(
        requestCode: Int, resultCode: Int, data: Intent?,
        responseCallback: (AuthServiceResponseModel) -> Unit
    ) {
        if (requestCode == RC_AUTH && data != null) {
            val response = AuthorizationResponse.fromIntent(data)
            val ex = AuthorizationException.fromIntent(data)
            if (ex != null) {
                responseCallback(
                    AuthServiceResponseModel(response?.authorizationCode, ex.message)
                )
            } else {
                authService?.performTokenRequest(
                    response!!.createTokenExchangeRequest()
                ) { tokenResponse, tokenEx ->
                    responseCallback(
                        AuthServiceResponseModel(tokenResponse?.idToken, tokenEx?.message)
                    )
                }
            }
        }
    }

    private fun doAuthorizationWithOpenId(authServiceModel: AuthServiceModel) {
        authService?.let { authService ->
            requestAuthCode(authServiceModel) {
                onAuthRequestIntent.startIntent(
                    Intent(authService.getAuthorizationRequestIntent(it)),
                    RC_AUTH
                )
            }
        }
    }

    private fun requestAuthCode(
        authServiceModel: AuthServiceModel,
        onAuthRequestReady: (AuthorizationRequest) -> Unit
    ) {
        if (authServiceModel.discoveryUri != null) {
            discoverAuthServiceConfig(authServiceModel,
                { authServiceConfiguration ->
                    onAuthRequestReady(buildRequest(authServiceModel, authServiceConfiguration))
                },
                {

                })
        } else {
            onAuthRequestReady(
                buildRequest(authServiceModel, loadAuthServiceConfig())
            )
        }
    }

    private fun buildRequest(
        authServiceModel: AuthServiceModel,
        authServiceConfiguration: AuthorizationServiceConfiguration
    ): AuthorizationRequest = AuthorizationRequest.Builder(
        authServiceConfiguration,
        authServiceModel.clientId,
        ResponseTypeValues.CODE, //CODE, TOKEN OR ID_TOKEN
        authServiceModel.redirectUri!!
    ).apply {
        authServiceModel.scope?.let { setScope(it) }
    }.build()

    private fun discoverAuthServiceConfig(
        authServiceModel: AuthServiceModel,
        onServiceReady: (AuthorizationServiceConfiguration) -> Unit,
        onServiceError: (Exception) -> Unit
    ) {
        AuthorizationServiceConfiguration
            .fetchFromUrl(authServiceModel.discoveryUri!!) { serviceConfiguration, exception ->
                if (exception != null) {
                    onServiceError(exception)
                } else if (serviceConfiguration != null) {
                    onServiceReady(serviceConfiguration)
                }
            }
    }

    private fun loadAuthServiceConfig(): AuthorizationServiceConfiguration {
        return AuthorizationServiceConfiguration(
            Uri.parse("auth_endpoint"),
            Uri.parse("token_endpoint")
        )
    }

    private fun doAuthorizationWithOAuth2(
        authServiceModel: AuthServiceModel,
        responseCallback: (AuthServiceResponseModel) -> Unit
    ) {
        val request = RegistrationRequest.Builder(
            loadAuthServiceConfig(),
            arrayListOf(authServiceModel.redirectUri)
        ).build()
        authService?.performRegistrationRequest(request) { response, ex ->
            responseCallback(
                AuthServiceResponseModel(response?.registrationAccessToken, ex?.message)
            )
        }
    }

    fun appAuthConfig(): AppAuthConfiguration {
        return AppAuthConfiguration.Builder()
            .setBrowserMatcher(BrowserWhitelist(VersionedBrowserMatcher.CHROME_BROWSER))
            .build()
    }
}