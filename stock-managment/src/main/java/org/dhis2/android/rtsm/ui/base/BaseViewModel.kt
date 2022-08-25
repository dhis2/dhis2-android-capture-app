package org.dhis2.android.rtsm.ui.base


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.Disposable
import org.dhis2.android.rtsm.BuildConfig
import org.dhis2.android.rtsm.commons.Constants
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.data.RowAction
import org.dhis2.android.rtsm.data.models.Transaction
import org.dhis2.android.rtsm.services.preferences.PreferenceProvider
import org.dhis2.android.rtsm.services.rules.RuleValidationHelper
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import java.util.Date
import javax.inject.Inject

@HiltViewModel
open class BaseViewModel @Inject constructor(
    private val preferenceProvider: PreferenceProvider,
    private val schedulerProvider: BaseSchedulerProvider
) : ViewModel() {
    val lastSyncDate: LiveData<String> = MutableLiveData(
        preferenceProvider.getString(Constants.LAST_DATA_SYNC_DATE)
    )
    private val _showGuide: MutableLiveData<Boolean> = MutableLiveData(false)
    val showGuide: LiveData<Boolean>
        get() = _showGuide

    val appVersion: LiveData<String> = MutableLiveData(getAppVersion())

    /**
     * Evaluates the quantity assigned to the StockItem
     *
     * @param action The row action that comprises the item, adapter position, quantity and
     * callback invoked when the validation completes
     */
    fun evaluate(
        ruleValidationHelper: RuleValidationHelper,
        action: RowAction,
        program: String,
        transaction: Transaction,
        date: Date,
        appConfig: AppConfig
    ): Disposable {

        return ruleValidationHelper.evaluate(
            entry = action.entry,
            eventDate = date,
            program = program,
            transaction = transaction,
            appConfig = appConfig
        )
            .doOnError { it.printStackTrace() }
            .observeOn(schedulerProvider.io())
            .subscribeOn(schedulerProvider.ui())
            .subscribe { ruleEffects ->
                action.callback?.validationCompleted(ruleEffects)
            }
    }

    fun toggleGuideDisplay() {
        _showGuide.value = if (_showGuide.value == null) {
            true
        } else {
            !_showGuide.value!!
        }
    }

    fun isVoiceInputEnabled(prefKey: String) = preferenceProvider.getBoolean(prefKey, false)

    private fun getAppVersion() = "v" + BuildConfig.VERSION_NAME


}