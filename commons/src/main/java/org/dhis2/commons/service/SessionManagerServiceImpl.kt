package org.dhis2.commons.service

import androidx.lifecycle.LifecycleCoroutineScope
import io.reactivex.Completable
import kotlinx.coroutines.launch
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.Preference.Companion.PIN
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.D2
import timber.log.Timber

class SessionManagerServiceImpl(
    private val d2: D2,
    private val schedulerProvider: SchedulerProvider,
    private val preferences: PreferenceProvider,
    private val featureConfig: FeatureConfigRepository,
) : SessionManagerService {

    override fun onUserInteraction() {
        preferences.setValue(Preference.LAST_USER_INTERACTION.toString(), System.currentTimeMillis())
    }

    override fun checkSessionTimeout(navigateAction: (Int) -> Unit, scope: LifecycleCoroutineScope): Boolean {
        val currentTime = System.currentTimeMillis()
        return if (currentTime - preferences.getLong(Preference.LAST_USER_INTERACTION.toString(), 0L)!! > SESSION_TIMEOUT_DURATION && !preferences.getBoolean(
                Preference.PIN_ENABLED, false,
            ) && featureConfig.isFeatureEnable(
                Feature.AUTO_LOGOUT,
            )
        ) {
            logoutUser(navigateAction, scope)
            true
            // Session timeout reached
        } else {
            false
            // Session timeout not reached
        }
    }

    fun isUserLoggedIn(): Boolean {
        return d2.userModule().isLogged().blockingGet()
    }

    companion object {
        // todo update to have a configurable session timeout with the ASWA
        private const val SESSION_TIMEOUT_DURATION = 60 * 1000 * 15 // 15 minutes
    }

    private fun logoutUser(navigateAction: (Int) -> Unit, scope: LifecycleCoroutineScope) {
        scope.launch {
            Completable.fromCallable {
                FilterManager.getInstance().clearAllFilters()
                preferences.setValue(Preference.SESSION_LOCKED, false)
                d2.dataStoreModule().localDataStore().value(PIN).blockingDeleteIfExist()
            }.andThen(
                d2.userModule().logOut(),
            )
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        navigateAction.invoke(d2.userModule().accountManager().getAccounts().count())
                    },
                    { Timber.e(it) },
                )
        }
    }
}
