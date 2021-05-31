package org.dhis2.form.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.intent.FormIntent

class FormViewModel(
    private val repository: FormRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _items = MutableLiveData<List<FieldUiModel>>()
    val items: LiveData<List<FieldUiModel>> = _items

    private val _savedValue = MutableLiveData<RowAction>()
    val savedValue: LiveData<RowAction> = _savedValue

    private val _pendingIntents = MutableSharedFlow<FormIntent>()
    private val pendingIntents = _pendingIntents

    init {
        viewModelScope.launch {
            _pendingIntents
                .map { intent -> createRowActionStore(intent) }
                .flowOn(dispatcher)
                .collect { result ->
                    when (result.second.valueStoreResult) {
                        ValueStoreResult.VALUE_CHANGED -> {
                            _savedValue.value = result.first
                        }
                        else -> _items.value = repository.composeList()
                    }
                }
        }
    }

    private fun createRowActionStore(it: FormIntent): Pair<RowAction, StoreResult> {
        val rowAction = rowActionFromIntent(it)
        val result = repository.processUserAction(rowAction)
        return Pair(rowAction, result)
    }

    private fun rowActionFromIntent(intent: FormIntent): RowAction {
        return when (intent) {
            is FormIntent.SelectDateFromAgeCalendar -> createRowAction(
                intent.uid,
                intent.date
            )
            is FormIntent.ClearDateFromAgeCalendar -> createRowAction(intent.uid, null)
        }
    }

    private fun createRowAction(uid: String, date: String?): RowAction {
        return RowAction(
            uid,
            date,
            false,
            null,
            null,
            null,
            null,
            ActionType.ON_SAVE
        )
    }

    fun submitIntent(intent: FormIntent) {
        viewModelScope.launch {
            _pendingIntents.emit(intent)
        }
    }

    @Deprecated("Use for legacy only")
    fun onItemAction(action: RowAction) {
        viewModelScope.launch {
            submitRowAction(action).collect {
                when (it.valueStoreResult) {
                    ValueStoreResult.VALUE_CHANGED -> {
                        _savedValue.value = action
                    }
                    else -> _items.value = repository.composeList()
                }
            }
        }
    }

    @Deprecated("Use for legacy only")
    private fun submitRowAction(action: RowAction): Flow<StoreResult> = flow {
        emit(repository.processUserAction(action))
    }.flowOn(dispatcher)
}
