package org.dhis2.android.rtsm.ui.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.commons.Constants.INSTANT_DATA_SYNC
import org.dhis2.android.rtsm.data.OperationState
import org.dhis2.android.rtsm.services.SyncManager
import org.dhis2.android.rtsm.services.UserManager
import org.dhis2.android.rtsm.services.preferences.PreferenceProvider
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import org.dhis2.android.rtsm.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val preferenceProvider: PreferenceProvider,
    private val userManager: UserManager,
    private val syncManager: SyncManager
): BaseViewModel(preferenceProvider, schedulerProvider) {
    private val _logoutStatus: MutableLiveData<OperationState<Boolean>> = MutableLiveData()
    val logoutStatus: LiveData<OperationState<Boolean>>
        get() = _logoutStatus

    private val _loggedInUser: MutableLiveData<String> = MutableLiveData()
    val loggedInUser: LiveData<String>
        get() = _loggedInUser

    init {
        disposable.add(
            userManager.userName()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {   username -> username?.let { _loggedInUser.value = it } },
                    { it.printStackTrace() }
                )
        )
    }

    fun logout() {
        _logoutStatus.postValue(OperationState.Loading)

        userManager.logout()?.let {
            disposable.add(
                it.subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        { _logoutStatus.postValue(OperationState.Success<Boolean>(true)) },
                        { error ->
                            error.printStackTrace()
                            _logoutStatus.postValue(OperationState.Error(R.string.logout_error_message))
                        }
                    )
            )
        }
    }

    fun syncData() {
        syncManager.dataSync()
    }

    fun getSyncDataStatus() = syncManager.getSyncStatus(INSTANT_DATA_SYNC)
    fun preferenceDataStore(context: Context) = preferenceProvider.preferenceDataStore(context)
}