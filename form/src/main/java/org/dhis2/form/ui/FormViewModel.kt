package org.dhis2.form.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult

class FormViewModel(
    private val repository: FormRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _items = MutableLiveData<List<FieldUiModel>>()
    val items: LiveData<List<FieldUiModel>> = _items

    private val _savedValue = MutableLiveData<RowAction>()
    val savedValue: LiveData<RowAction> = _savedValue

    private val pendingIntents = MutableSharedFlow<FormIntent>()

    /* fun onItemAction(action: RowAction) = runBlocking {
         processAction(action).collect { result ->
             when (result.valueStoreResult) {
                 ValueStoreResult.VALUE_CHANGED -> {
                     _savedValue.value = action
                 }
                 else -> _items.value = repository.composeList()
             }
         }
     }
 
     private fun processAction(action: RowAction): Flow<StoreResult> = flow {
         emit(repository.processUserAction(action))
     }.flowOn(dispatcher) */

    init {
        viewModelScope.launch {
            pendingIntents
                .map {  }
                .flowOn(dispatcher)
                .collect {

                }
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

    private fun submitRowAction(action: RowAction): Flow<StoreResult> = flow {
        emit(repository.processUserAction(action))
    }.flowOn(dispatcher)


    fun submitIntent(intent: FormIntent) {
        viewModelScope.launch {
            pendingIntents.emit(intent)
        }
    }
}
