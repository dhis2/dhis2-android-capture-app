package org.dhis2.usescases.login

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.commons.prefs.Preference.Companion.PIN
import org.dhis2.commons.prefs.Preference.Companion.SESSION_LOCKED
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.server.UserManager
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig
import timber.log.Timber
import java.io.File

const val VERSION = "version"

class LoginViewModel(
    private val view: LoginContracts.View,
    private val preferenceProvider: PreferenceProvider,
    private val resourceManager: ResourceManager,
    private val schedulers: SchedulerProvider,
    private var userManager: UserManager?,
) : ViewModel() {
    var disposable: CompositeDisposable = CompositeDisposable()

    fun openIdLogin(config: OpenIDConnectConfig) {
        try {
            disposable.add(
                Observable
                    .just(view.initLogin())
                    .flatMap { userManager ->
                        this.userManager = userManager
                        userManager.logIn(config)
                    }.subscribeOn(schedulers.io())
                    .observeOn(schedulers.ui())
                    .subscribe(
                        {
                            view.openOpenIDActivity(it)
                        },
                        {
                            Timber.e(it)
                        },
                    ),
            )
        } catch (throwable: Throwable) {
            Timber.e(throwable)
        }
    }

    fun handleAuthResponseData(
        serverUrl: String,
        data: Intent,
        requestCode: Int,
    ) {
        userManager?.let { userManager ->
            disposable.add(
                userManager
                    .handleAuthData(serverUrl, data, requestCode)
                    .map {
                        run {
                            with(preferenceProvider) {
                                setValue(SESSION_LOCKED, false)
                            }
                            deletePin()
                            Result.success(null)
                        }
                    }.subscribeOn(schedulers.io())
                    .observeOn(schedulers.ui())
                    .subscribe(
                        {},
                        { t -> },
                    ),
            )
        }
    }

    fun onDestroy() {
        disposable.clear()
    }

    fun logOut() {
        userManager?.let {
            disposable.add(
                it.d2
                    .userModule()
                    .logOut()
                    .subscribeOn(schedulers.io())
                    .observeOn(schedulers.ui())
                    .subscribe(
                        {
                            preferenceProvider.setValue(SESSION_LOCKED, false)
                            view.handleLogout()
                        },
                        { view.handleLogout() },
                    ),
            )
        }
    }

    private fun deletePin() {
        userManager
            ?.d2
            ?.dataStoreModule()
            ?.localDataStore()
            ?.value(PIN)
            ?.blockingDeleteIfExist()
    }

    fun onImportDataBase(file: File) {
        userManager?.let {
            viewModelScope.launch {
                val resultJob =
                    async {
                        try {
                            val importedMetadata =
                                it.d2
                                    .maintenanceModule()
                                    .databaseImportExport()
                                    .importDatabase(file)
                            Result.success(importedMetadata)
                        } catch (e: Exception) {
                            Result.failure(e)
                        }
                    }

                val result = resultJob.await()

                result.fold(
                    onSuccess = {
                        view.displayMessage(resourceManager.getString(R.string.importing_successful))
                    },
                    onFailure = {
                        view.displayMessage(resourceManager.parseD2Error(it))
                    },
                )
            }
        }
    }
}
