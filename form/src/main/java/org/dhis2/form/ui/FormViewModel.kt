package org.dhis2.form.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.ValueStoreResult

class FormViewModel(private val repository: FormRepository) : ViewModel() {

    private val _items = MutableLiveData<List<FieldUiModel>>()
    val items: LiveData<List<FieldUiModel>> = _items

    private val _savedValue = MutableLiveData<String>()
    val savedValue: LiveData<String> = _savedValue

    fun onItemAction(action: RowAction) {
        viewModelScope.launch {
            val storeResult = repository.processUserAction(action)
            when (storeResult.valueStoreResult) {
                ValueStoreResult.VALUE_CHANGED -> {
                    _savedValue.value = storeResult.uid
                }
                else -> _items.value = repository.composeList()
            }
        }
    }
}