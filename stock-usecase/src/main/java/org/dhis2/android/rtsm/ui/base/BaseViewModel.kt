package org.dhis2.android.rtsm.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.Disposable
import org.dhis2.android.rtsm.data.RowAction
import org.dhis2.android.rtsm.data.models.Transaction
import org.dhis2.android.rtsm.services.rules.RuleValidationHelper
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import org.hisp.dhis.android.core.usecase.stock.StockUseCase
import timber.log.Timber

open class BaseViewModel(
    private val schedulerProvider: BaseSchedulerProvider,
) : ViewModel() {
    private val _showGuide: MutableLiveData<Boolean> = MutableLiveData(false)
    val showGuide: LiveData<Boolean>
        get() = _showGuide

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
        stockUseCase: StockUseCase,
    ): Disposable =
        ruleValidationHelper
            .evaluate(
                entry = action.entry,
                program = program,
                transaction = transaction,
                stockUseCase = stockUseCase,
            ).doOnError { Timber.e(it) }
            .observeOn(schedulerProvider.io())
            .subscribeOn(schedulerProvider.ui())
            .subscribe { ruleEffects ->
                action.callback?.validationCompleted(ruleEffects)
            }

    fun toggleGuideDisplay() {
        _showGuide.value =
            if (_showGuide.value == null) {
                true
            } else {
                !_showGuide.value!!
            }
    }

    fun isVoiceInputEnabled(prefKey: String) = false // TODO check if enabled
}
