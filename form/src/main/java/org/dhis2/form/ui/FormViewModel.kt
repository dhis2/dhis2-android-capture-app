package org.dhis2.form.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
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

    fun onItemAction(action: RowAction) = runBlocking {
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
    }.flowOn(dispatcher)
}
