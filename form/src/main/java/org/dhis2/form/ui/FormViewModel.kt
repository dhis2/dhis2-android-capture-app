package org.dhis2.form.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
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
import org.dhis2.form.model.InfoUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.validation.validators.FieldMaskValidator
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueType
import timber.log.Timber

class FormViewModel(
    private val repository: FormRepository,
    private val dispatcher: DispatcherProvider,
    private val geometryController: GeometryController = GeometryController(GeometryParserImpl())
) : ViewModel() {

    val loading = MutableLiveData(true)
    val showToast = MutableLiveData<Int>()
    val focused = MutableLiveData<Boolean>()
    val showInfo = MutableLiveData<InfoUiModel>()

    private val _items = MutableLiveData<List<FieldUiModel>>()
    val items: LiveData<List<FieldUiModel>> = _items

    private val _savedValue = MutableLiveData<RowAction>()
    val savedValue: LiveData<RowAction> = _savedValue

    private val _queryData = MutableLiveData<RowAction>()
    val queryData = _queryData

    private val _pendingIntents = MutableSharedFlow<FormIntent>()

    init {
        viewModelScope.launch {
            _pendingIntents
                .distinctUntilChanged()
                .map { intent -> createRowActionStore(intent) }
                .flowOn(dispatcher.io())
                .collect { result ->
                    Timber.d("FLOW: new result %s", result.second.valueStoreResult)
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
            ValueStoreResult.UID_IS_NOT_DE_OR_ATTR -> {
                Timber.tag(TAG)
                    .d("${result.first.id} is not a data element or attribute")
                _items.value = repository.composeList()
            }
            ValueStoreResult.VALUE_NOT_UNIQUE -> {
                showInfo.value = InfoUiModel(
                    R.string.error,
                    R.string.unique_warning
                )
                _items.value = repository.composeList()
            }
            ValueStoreResult.VALUE_HAS_NOT_CHANGED -> {
                _items.value = repository.composeList()
            }
            ValueStoreResult.TEXT_CHANGING -> {
                Timber.d("${result.first.id} is changing its value")
                _queryData.value = result.first
            }
        }
    }

    fun submitIntent(intent: FormIntent) {
        viewModelScope.launch {
            _pendingIntents.emit(intent)
        }
    }

    private fun createRowActionStore(it: FormIntent): Pair<RowAction, StoreResult> {
        val rowAction = rowActionFromIntent(it)

        if (rowAction.type == ActionType.ON_FOCUS) {
            focused.postValue(true)
        } else if (rowAction.type == ActionType.ON_SAVE) {
            loading.postValue(true)
        }

        val result = repository.processUserAction(rowAction)
        return Pair(rowAction, result)
    }

    private fun rowActionFromIntent(intent: FormIntent): RowAction {
        return when (intent) {
            is FormIntent.ClearValue -> createRowAction(intent.uid, null)
            is FormIntent.SelectLocationFromCoordinates -> createRowAction(
                uid = intent.uid,
                value = intent.coordinates,
                extraData = intent.extraData,
                valueType = ValueType.COORDINATE
            )
            is FormIntent.SelectLocationFromMap -> setCoordinateFieldValue(
                fieldUid = intent.uid,
                featureType = intent.featureType,
                coordinates = intent.coordinates
            )
            is FormIntent.SaveCurrentLocation -> createRowAction(
                uid = intent.uid,
                value = intent.value,
                extraData = intent.featureType,
                valueType = ValueType.COORDINATE
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
                    error = error,
                    valueType = intent.valueType
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
                actionType = ActionType.ON_TEXT_CHANGE,
                valueType = ValueType.TEXT
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
        actionType: ActionType = ActionType.ON_SAVE,
        valueType: ValueType? = null
    ) = RowAction(
        id = uid,
        value = value,
        extraData = extraData,
        error = error,
        type = actionType,
        valueType = valueType
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
            extraData = featureType,
            valueType = ValueType.COORDINATE
        )
    }

    fun getFocusedItemUid(): String? {
        return items.value?.first { it.focused }?.uid
    }

    fun processCalculatedItems(items: List<FieldUiModel>?) {
        _items.value = repository.composeList(items)
    }

    companion object {
        const val TAG = "FormViewModel"
    }
}
