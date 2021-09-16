package org.dhis2.form.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.dhis2.form.R
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.GeometryController
import org.dhis2.form.data.GeometryParserImpl
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.DispatcherProvider
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.validation.validators.FieldMaskValidator
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueType

class FormViewModel(
    private val repository: FormRepository,
    private val dispatcher: DispatcherProvider,
    private val geometryController: GeometryController = GeometryController(GeometryParserImpl())
) : ViewModel() {

    val loading = MutableLiveData<Boolean>()
    val showToast = MutableLiveData<Int>()
    val focused = MutableLiveData<Boolean>()

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
                .flowOn(dispatcher.io())
                .collect { result ->
                    displayResult(result)
                }
        }
    }

    private fun displayResult(result: Pair<RowAction, StoreResult>) {
        when (result.second.valueStoreResult) {
            ValueStoreResult.VALUE_CHANGED -> {
                _savedValue.value = result.first
            }
            ValueStoreResult.ERROR_UPDATING_VALUE -> {
                showToast.value = R.string.update_field_error
            }
            else -> _items.value = repository.composeList()
        }
    }

    fun submitIntent(intent: FormIntent) {
        viewModelScope.launch {
            _pendingIntents.emit(intent)
        }
    }

    private fun createRowActionStore(it: FormIntent): Pair<RowAction, StoreResult> {
        val rowAction = rowActionFromIntent(it)
        when (rowAction.type) {
            ActionType.ON_FOCUS -> focused.postValue(true)
            ActionType.ON_SAVE -> loading.postValue(true)
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
            is FormIntent.ClearValue -> createRowAction(intent.uid, null)
            is FormIntent.SelectLocationFromCoordinates -> createRowAction(
                intent.uid,
                intent.coordinates,
                intent.extraData
            )
            is FormIntent.SelectLocationFromMap -> setCoordinateFieldValue(
                intent.uid,
                intent.featureType,
                intent.coordinates
            )
            is FormIntent.SaveCurrentLocation -> createRowAction(
                uid = intent.uid,
                value = intent.value,
                extraData = intent.featureType
            )
            is FormIntent.OnNext -> createRowAction(
                uid = intent.uid,
                value = intent.value,
                actionType = ActionType.ON_NEXT
            )
            is FormIntent.OnSave -> {
                val error = checkFieldError(
                    intent.valueType,
                    intent.value,
                    intent.fieldMask
                )

                createRowAction(
                    uid = intent.uid,
                    value = intent.value,
                    error = error
                )
            }
            is FormIntent.OnFocus -> createRowAction(
                uid = intent.uid,
                value = intent.value,
                actionType = ActionType.ON_FOCUS
            )

            is FormIntent.OnTextChange -> createRowAction(
                uid = intent.uid,
                value = intent.value,
                actionType = ActionType.ON_TEXT_CHANGE
            )

        }
    }

    private fun checkFieldError(
        valueType: ValueType?,
        fieldValue: String?,
        fieldMask: String?
    ): Throwable? {
        return fieldValue?.let { value ->
            var error =
                when (val result = valueType?.validator?.validate(value)) {
                    is Result.Failure -> result.failure
                    else -> null
                }

            fieldMask?.let { mask ->
                error = when (val result = FieldMaskValidator(mask).validate(value)) {
                    is Result.Failure -> result.failure
                    else -> error
                }
            }
            error
        }
    }

    private fun createRowAction(
        uid: String,
        value: String?,
        extraData: String? = null,
        error: Throwable? = null,
        actionType: ActionType = ActionType.ON_SAVE
    ) = RowAction(
        id = uid,
        value = value,
        extraData = extraData,
        error = error,
        type = actionType
    )

    fun onItemsRendered() {
        loading.value = false
    }

    private fun setCoordinateFieldValue(
        fieldUid: String,
        featureType: String,
        coordinates: String?
    ): RowAction {
        val geometryCoordinates = coordinates?.let {
            geometryController.generateLocationFromCoordinates(
                FeatureType.valueOf(featureType),
                coordinates
            )?.coordinates()
        }
        return createRowAction(
            uid = fieldUid,
            value = geometryCoordinates,
            extraData = featureType
        )
    }

    fun getFocusedItemUid(): String? {
        return items.value?.first { it.focused }?.uid
    }
}
