package org.dhis2.mobile.login.main.data

import androidx.core.net.toUri
import coil3.PlatformContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.dhis2.mobile.commons.auth.OpenIdController
import org.dhis2.mobile.commons.biometrics.BiometricActions
import org.dhis2.mobile.commons.biometrics.CryptographicActions
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.providers.BIOMETRIC_CREDENTIALS
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.dhis2.mobile.commons.providers.SECURE_PASS
import org.dhis2.mobile.commons.providers.SECURE_SERVER_URL
import org.dhis2.mobile.commons.reporting.AnalyticActions
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.mobile.commons.resources.D2ErrorMessageProvider
import org.dhis2.mobile.login.main.domain.model.ServerValidationResult
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.openid_invalid_auth_result
import org.dhis2.mobile.login.resources.openid_process_cancelled
import org.dhis2.mobile.login.resources.server_url_error
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig
import org.jetbrains.compose.resources.getString
import java.io.File

private const val PREF_USERS = "PREF_USERS"
const val PREF_URLS = "PREF_URLS"
private const val PREF_SESSION_LOCKED = "SessionLocked"
private const val PIN = "pin"
private const val DATA_STORE_ANALYTICS_PERMISSION_KEY = "analytics_permission"
private const val BIOMETRICS_PERMISSION = "biometrics_permission"
private const val WAS_INITIAL_SYNC_DONE = "WasInitialSyncDone"
const val USER_PROPERTY_SERVER = "serverUrl"
private const val VERSION = "version"

class LoginRepositoryImpl(
    private val d2: D2,
    private val authenticator: BiometricActions,
    private val cryptographyManager: CryptographicActions,
    private val preferences: PreferenceProvider,
    private val d2ErrorMessageProvider: D2ErrorMessageProvider,
    private val crashReportController: CrashReportController,
    private val analyticActions: AnalyticActions,
    private val openIdController: OpenIdController,
    private val dispatcher: Dispatcher,
) : LoginRepository {
    override suspend fun validateServer(
        server: String,
        isNetworkAvailable: Boolean,
    ): ServerValidationResult =
        withContext(dispatcher.io) {
            when (val result = d2.serverModule().blockingCheckServerUrl(server)) {
                is Result.Success -> {
                    if (result.value.isOauthEnabled()) {
                        ServerValidationResult.Oauth
                    } else {
                        val oidcProvider = result.value.oidcProviders.firstOrNull()
                        val serverName =
                            result.value.applicationTitle ?: try {
                                server.substringAfter("://").substringBefore("/")
                            } catch (_: Exception) {
                                server
                            }
                        ServerValidationResult.Legacy(
                            serverName = serverName,
                            serverDescription = result.value.applicationDescription,
                            countryFlag = result.value.countryFlag,
                            allowRecovery = result.value.allowAccountRecovery,
                            oidcIcon = oidcProvider?.icon,
                            oidcLoginText = oidcProvider?.loginText,
                            oidcUrl = oidcProvider?.url,
                        )
                    }
                }

                is Result.Failure -> {
                    val error =
                        d2ErrorMessageProvider.getErrorMessage(
                            throwable = result.failure,
                            isNetworkAvailable = isNetworkAvailable,
                        )
                    ServerValidationResult.Error(error ?: getString(Res.string.server_url_error))
                }
            }
        }

    override suspend fun loginUser(
        serverUrl: String,
        username: String,
        password: String,
        isNetworkAvailable: Boolean,
    ) = withContext(dispatcher.io) {
        try {
            d2.userModule().blockingLogIn(username, password, serverUrl)
            kotlin.Result.success(Unit)
        } catch (e: Exception) {
            kotlin.Result.failure(
                Exception(
                    d2ErrorMessageProvider.getErrorMessage(
                        e,
                        isNetworkAvailable,
                    ),
                ),
            )
        }
    }

    override suspend fun logoutUser() =
        withContext(dispatcher.io) {
            try {
                d2.userModule().blockingLogOut()
                kotlin.Result.success(Unit)
            } catch (e: Exception) {
                kotlin.Result.failure(
                    Exception(
                        e.cause,
                    ),
                )
            }
        }

    override suspend fun getAvailableLoginUsernames(): List<String> =
        withContext(dispatcher.io) {
            preferences.getSet(PREF_USERS, HashSet())?.toList() ?: emptyList()
        }

    override suspend fun unlockSession() =
        withContext(dispatcher.io) {
            preferences.setValue(PREF_SESSION_LOCKED, false)
            d2
                .dataStoreModule()
                .localDataStore()
                .value(PIN)
                .blockingDeleteIfExist()
        }

    override suspend fun updateAvailableUsers(username: String) {
        withContext(dispatcher.io) {
            (preferences.getSet(PREF_USERS, HashSet()) as HashSet).apply {
                if (!contains(username)) {
                    add(username)
                }
                preferences.setValue(PREF_USERS, this)
            }
        }
    }

    override suspend fun updateServerUrls(serverUrl: String) {
        withContext(dispatcher.io) {
            preferences.updateLoginServers(serverUrl)
        }
    }

    override suspend fun displayTrackingMessage(): Boolean =
        withContext(dispatcher.io) {
            d2
                .dataStoreModule()
                .localDataStore()
                .value(DATA_STORE_ANALYTICS_PERMISSION_KEY)
                .blockingGet()
                ?.value() == null
        }

    private fun hasEnabledBiometricsPermission(): Boolean =
        try {
            preferences.getBoolean(BIOMETRICS_PERMISSION, false)
        } catch (_: Exception) {
            false
        }

    override suspend fun initialSyncDone(
        serverUrl: String,
        username: String,
    ): Boolean = withContext(dispatcher.io) { isImportedDatabase(serverUrl, username) or entryExists() }

    override suspend fun canLoginWithBiometrics(serverUrl: String): Boolean =
        withContext(dispatcher.io) {
            val hasBiometrics = authenticator.hasBiometric()
            val biometricPermissionGranted = hasEnabledBiometricsPermission()
            val hasOnlyOneAccount =
                d2
                    .userModule()
                    .accountManager()
                    .getAccounts()
                    .count() == 1
            val isSameServer =
                preferences.getString(SECURE_SERVER_URL)?.let { it == serverUrl } ?: false
            val hasKey = preferences.contains(SECURE_PASS) || cryptographyManager.isKeyReady()
            hasBiometrics && hasOnlyOneAccount && isSameServer && hasKey && biometricPermissionGranted
        }

    override suspend fun displayBiometricMessage(): Boolean =
        withContext(dispatcher.io) {
            val hasBiometrics = authenticator.hasBiometric()
            val credentialsNotSet = preferences.areCredentialsSet().not()
            val hasOnlyOneAccount =
                d2
                    .userModule()
                    .accountManager()
                    .getAccounts()
                    .count() == 1
            hasBiometrics && hasOnlyOneAccount && credentialsNotSet
        }

    override suspend fun hasOtherAccounts(): Boolean =
        withContext(dispatcher.io) {
            d2
                .userModule()
                .accountManager()
                .getAccounts()
                .isNotEmpty()
        }

    override suspend fun numberOfAccounts(): Int =
        withContext(dispatcher.io) {
            d2
                .userModule()
                .accountManager()
                .getAccounts()
                .size
        }

    override suspend fun updateTrackingPermissions(granted: Boolean) {
        withContext(dispatcher.io) {
            d2
                .dataStoreModule()
                .localDataStore()
                .value(DATA_STORE_ANALYTICS_PERMISSION_KEY)
                .blockingSet(granted.toString())
            if (granted) {
                val currentAccount = d2.userModule().accountManager().getCurrentAccount()
                val systemInfo = d2.systemInfoModule().systemInfo().blockingGet()

                analyticActions.trackMatomoEvent(
                    USER_PROPERTY_SERVER,
                    VERSION,
                    systemInfo?.version() ?: "",
                )
                crashReportController.init()
                crashReportController.trackServer(
                    currentAccount?.serverUrl(),
                    systemInfo?.version(),
                )
            }
        }
    }

    override suspend fun updateBiometricsPermissions(granted: Boolean) =
        withContext(dispatcher.io) {
            preferences.setValue(BIOMETRICS_PERMISSION, granted)
        }

    context(context: PlatformContext)
    override suspend fun loginWithBiometric(): kotlin.Result<UserPassword> =
        withContext(dispatcher.main) {
            preferences.getBiometricCredentials()?.let { ciphertextWrapper ->
                cryptographyManager
                    .getInitializedCipherForDecryption(ciphertextWrapper.initializationVector)
                    ?.let { cipher ->
                        suspendCancellableCoroutine { continuation ->
                            authenticator.authenticate(cipher) { cipher ->
                                val pass =
                                    cryptographyManager.decryptData(
                                        ciphertextWrapper.ciphertext,
                                        cipher,
                                    )
                                continuation.resume(value = kotlin.Result.success(pass)) { _, _, _ -> }
                            }
                            continuation.invokeOnCancellation {
                                // If needed perform action on cancellation
                            }
                        }
                    }
            } ?: kotlin.Result.failure(Exception("No biometrics found"))
        }

    override suspend fun deleteBiometricCredentials() {
        withContext(dispatcher.io) {
            preferences.removeValue(BIOMETRIC_CREDENTIALS)
            cryptographyManager.deleteInvalidKey()
        }
    }

    override suspend fun loginWithOpenId(
        serverUrl: String,
        isNetworkAvailable: Boolean,
        clientId: String,
        redirectUri: String,
        discoveryUri: String?,
        authorizationUri: String?,
        tokenUrl: String?,
    ): kotlin.Result<Unit> =
        withContext(dispatcher.io) {
            suspendCancellableCoroutine { continuation ->
                val intent =
                    d2
                        .userModule()
                        .openIdHandler()
                        .blockingLogIn(
                            OpenIDConnectConfig(
                                clientId = clientId,
                                redirectUri = redirectUri.toUri(),
                                discoveryUri = discoveryUri?.toUri(),
                                authorizationUri = authorizationUri,
                                tokenUrl = tokenUrl,
                            ),
                        )
                openIdController.handleIntent(intent) { resultIntent ->
                    val result =
                        when {
                            resultIntent.isFailure -> {
                                kotlin.Result.failure(
                                    resultIntent.exceptionOrNull()
                                        ?: Exception(getString(Res.string.openid_process_cancelled)),
                                )
                            }

                            resultIntent.isSuccess and (resultIntent.getOrNull() !is IntentWithRequestCode) -> {
                                kotlin.Result.failure(Exception(getString(Res.string.openid_invalid_auth_result)))
                            }

                            else -> {
                                try {
                                    val intent = resultIntent.getOrNull() as IntentWithRequestCode
                                    d2
                                        .userModule()
                                        .openIdHandler()
                                        .blockingHandleLogInResponse(
                                            serverUrl = serverUrl,
                                            intent = intent.intent,
                                            requestCode = intent.requestCode,
                                        )

                                    kotlin.Result.success(Unit)
                                } catch (e: Exception) {
                                    kotlin.Result.failure(
                                        Exception(
                                            d2ErrorMessageProvider.getErrorMessage(
                                                e,
                                                isNetworkAvailable,
                                            ),
                                        ),
                                    )
                                }
                            }
                        }

                    continuation.resume(value = result) { _, _, _ -> }
                }
                continuation.invokeOnCancellation {
                    kotlin.Result.failure<Unit>(Exception(""))
                }
            }
        }

    override suspend fun getUsername(): String =
        withContext(dispatcher.io) {
            d2
                .userModule()
                .user()
                .blockingGet()
                ?.username() ?: ""
        }

    private fun isImportedDatabase(
        serverUrl: String,
        username: String,
    ): Boolean =
        d2
            .userModule()
            .accountManager()
            .getCurrentAccount()
            ?.let {
                it.serverUrl() == serverUrl && it.username() == username && it.importDB() != null
            } ?: false

    private fun entryExists() =
        d2
            .dataStoreModule()
            .localDataStore()
            .value(WAS_INITIAL_SYNC_DONE)
            .blockingGet()
            ?.value()
            ?.lowercase()
            ?.toBooleanStrictOrNull() == true

    override suspend fun importDatabase(path: String) =
        withContext(dispatcher.io) {
            try {
                d2
                    .maintenanceModule()
                    .databaseImportExport()
                    .importDatabase(File(path))
                kotlin.Result.success(Unit)
            } catch (e: Exception) {
                kotlin.Result.failure(
                    Exception(
                        d2ErrorMessageProvider.getErrorMessage(
                            e,
                            isNetworkAvailable = true,
                        ),
                    ),
                )
            }
        }
}
