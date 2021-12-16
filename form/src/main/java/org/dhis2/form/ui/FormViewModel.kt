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
import org.dhis2.form.data.GeometryController
import org.dhis2.form.data.GeometryParserImpl
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.FeatureType

class FormViewModel(
    private val repository: FormRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val geometryController: GeometryController = GeometryController(GeometryParserImpl())
) : ViewModel() {

    val loading = MutableLiveData<Boolean>()
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
        if (rowAction.type == ActionType.ON_SAVE) {
            loading.postValue(true)
        }
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

    @Deprecated("Use for legacy only. Do not use this for refactor views")
    fun onItemAction(action: RowAction) {
        if (action.type == ActionType.ON_SAVE) {
            loading.value = true
        }
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

    @Deprecated("Use for legacy only. Do not use this for refactor views")
    private fun submitRowAction(action: RowAction): Flow<StoreResult> = flow {
        emit(repository.processUserAction(action))
    }.flowOn(dispatcher)

    fun onItemsRendered() {
        loading.value = false
    }

    fun setCoordinateFieldValue(fieldUid: String?, featureType: String?, coordinates: String?) {
        if (fieldUid != null && featureType != null) {
            val geometryCoordinates = coordinates?.let {
                geometryController.generateLocationFromCoordinates(
                    FeatureType.valueOf(featureType),
                    coordinates
                )?.coordinates()
            }
            onItemAction(
                RowAction(
                    id = fieldUid,
                    value = geometryCoordinates,
                    type = ActionType.ON_SAVE,
                    extraData = featureType
                )
            )
        }
    }

    fun getFocusedItemUid(): String? {
        return items.value?.first { it.focused }?.uid
    }
}
