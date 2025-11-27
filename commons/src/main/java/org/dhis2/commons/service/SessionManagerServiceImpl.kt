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

    override fun checkSessionTimeout(
        navigateAction: (Int) -> Unit,
        scope: LifecycleCoroutineScope,
    ): Boolean {
        val currentTime = System.currentTimeMillis()
        val hasPinProtection = preferences.getBoolean(Preference.SESSION_LOCKED, false)

        return if (currentTime - preferences.getLong(Preference.LAST_USER_INTERACTION.toString(), 0L)!! > SESSION_TIMEOUT_DURATION &&
            !hasPinProtection &&
            featureConfig.isFeatureEnable(
                Feature.AUTO_LOGOUT,
            )
        ) {
            logoutUser(navigateAction, scope)
            true
        } else {
            false
        }
    }

    fun isUserLoggedIn(): Boolean = d2.userModule().isLogged().blockingGet()

    companion object {
        // todo request configured session time out duration when ready for production
        private const val SESSION_TIMEOUT_DURATION = 10 * 1000 // 10 seconds
    }

    private fun logoutUser(
        navigateAction: (Int) -> Unit,
        scope: LifecycleCoroutineScope,
    ) {
        scope.launch {
            Completable
                .fromCallable {
                    FilterManager.getInstance().clearAllFilters()
                    preferences.setValue(Preference.SESSION_LOCKED, false)
                    d2
                        .dataStoreModule()
                        .localDataStore()
                        .value(PIN)
                        .blockingDeleteIfExist()
                }.andThen(
                    d2.userModule().logOut(),
                ).subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        navigateAction.invoke(
                            d2
                                .userModule()
                                .accountManager()
                                .getAccounts()
                                .count(),
                        )
                    },
                    { Timber.e(it) },
                )
        }
    }
}
