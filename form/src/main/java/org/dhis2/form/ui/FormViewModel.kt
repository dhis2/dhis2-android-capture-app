package org.dhis2.form.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.R
import org.dhis2.form.data.DataIntegrityCheckResult
import org.dhis2.form.data.EventRepository.Companion.EVENT_COORDINATE_UID
import org.dhis2.form.data.EventRepository.Companion.EVENT_ORG_UNIT_UID
import org.dhis2.form.data.EventRepository.Companion.EVENT_REPORT_DATE_UID
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.GeometryController
import org.dhis2.form.data.GeometryParserImpl
import org.dhis2.form.data.RulesUtilsProviderConfigurationError
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldListConfiguration
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.InfoUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.binding.getFeatureType
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.validation.validators.FieldMaskValidator
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.valuetype.validation.failures.DateFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.DateTimeFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.TimeFailure
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class FormViewModel(
    private val repository: FormRepository,
    private val dispatcher: DispatcherProvider,
    private val geometryController: GeometryController = GeometryController(GeometryParserImpl()),
    private val openErrorLocation: Boolean = false,
    private val preferenceProvider: PreferenceProvider,
) : ViewModel() {

    val loading = MutableLiveData(true)
    val showToast = MutableLiveData<Int>()
    val focused = MutableLiveData<Boolean>()
    val showInfo = MutableLiveData<InfoUiModel>()
    val confError = MutableLiveData<List<RulesUtilsProviderConfigurationError>>()
    var dateFormatConfig: String = "ddMMyyyy"
    private val _items = MutableLiveData<List<FieldUiModel>>()
    val items: LiveData<List<FieldUiModel>> = _items

    var previousActionItem: RowAction? = null

    private val _savedValue = MutableLiveData<RowAction>()
    val savedValue: LiveData<RowAction> = _savedValue

    private val _queryData = MutableLiveData<RowAction>()
    val queryData = _queryData

    private val _dataIntegrityResult = MutableLiveData<DataIntegrityCheckResult>()
    val dataIntegrityResult = _dataIntegrityResult

    private val _completionPercentage = MutableLiveData<Float>()
    val completionPercentage = _completionPercentage

    private val _calculationLoop = MutableLiveData(false)
    val calculationLoop = _calculationLoop

    private val _pendingIntents = MutableSharedFlow<FormIntent>()

    private val fieldListChannel = Channel<FieldListConfiguration>(
        capacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        viewModelScope.launch {
            _pendingIntents
                .distinctUntilChanged { old, new ->
                    if (old is FormIntent.OnFinish && new is FormIntent.OnFinish) {
                        false
                    } else {
                        old == new
                    }
                }
                .map { intent -> createRowActionStore(intent) }
                .flowOn(dispatcher.io())
                .collect { result -> displayResult(result) }
        }

        viewModelScope.launch(dispatcher.io()) {
            fieldListChannel.consumeEach { fieldListConfiguration ->
                val result = async {
                    repository.composeList(fieldListConfiguration.skipProgramRules)
                }
                _items.postValue(result.await())
                if (fieldListConfiguration.finish) {
                    runDataIntegrityCheck()
                }
            }
        }

        loadData()
    }

    private fun displayResult(result: Pair<RowAction, StoreResult>) {
        result.second.valueStoreResult?.let {
            when (it) {
                ValueStoreResult.VALUE_CHANGED -> {
                    result.first.let {
                        _savedValue.value = it
                    }
                    processCalculatedItems()
                }

                ValueStoreResult.ERROR_UPDATING_VALUE -> {
                    loading.postValue(false)
                    showToast.value = R.string.update_field_error
                    processCalculatedItems(true)
                }

                ValueStoreResult.UID_IS_NOT_DE_OR_ATTR -> {
                    Timber.tag(TAG)
                        .d("${result.first.id} is not a data element or attribute")
                    processCalculatedItems()
                }

                ValueStoreResult.VALUE_NOT_UNIQUE -> {
                    showInfo.value = InfoUiModel(
                        R.string.error,
                        R.string.unique_warning,
                    )
                    processCalculatedItems()
                }

                ValueStoreResult.VALUE_HAS_NOT_CHANGED -> {
                    processCalculatedItems(true)
                }

                ValueStoreResult.TEXT_CHANGING -> {
                    result.first.let {
                        Timber.d("${result.first.id} is changing its value")
                        _queryData.value = it
                    }
                }

                ValueStoreResult.FINISH -> {
                    processCalculatedItems(finish = true)
                }

                ValueStoreResult.FILE_SAVED -> {
                    /*Do nothing*/
                }
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

        val result = processUserAction(rowAction)
        return Pair(rowAction, result)
    }

    private fun processUserAction(action: RowAction): StoreResult {
        return when (action.type) {
            ActionType.ON_SAVE -> handleOnSaveAction(action)
            ActionType.ON_FOCUS, ActionType.ON_NEXT -> handleFocusOrNextAction(action)
            ActionType.ON_TEXT_CHANGE -> handleOnTextChangeAction(action)
            ActionType.ON_SECTION_CHANGE -> handleOnSectionChangeAction(action)
            ActionType.ON_FINISH -> handleOnFinishAction(action)
            ActionType.ON_REQUEST_COORDINATES -> handleOnRequestCoordinatesAction(action)
            ActionType.ON_CANCEL_REQUEST_COORDINATES -> handleOnCancelRequestCoordinatesAction(
                action,
            )

            ActionType.ON_ADD_IMAGE_FINISHED -> handleOnAddImageFinishedAction(action)
            ActionType.ON_STORE_FILE -> handleOnStoreFileAction(action)
        }
    }

    private fun handleOnSaveAction(action: RowAction): StoreResult {
        if (action.valueType == ValueType.COORDINATE) {
            repository.setFieldRequestingCoordinates(action.id, false)
        }

        repository.updateErrorList(action)
        if (action.error != null) {
            return StoreResult(
                action.id,
                ValueStoreResult.VALUE_HAS_NOT_CHANGED,
            )
        }

        val saveResult = repository.save(action.id, action.value, action.extraData)
        if (saveResult?.valueStoreResult != ValueStoreResult.ERROR_UPDATING_VALUE) {
            if (action.isEventDetailsRow) {
                repository.fetchFormItems(openErrorLocation)
            } else {
                repository.updateValueOnList(action.id, action.value, action.valueType)
            }
        } else {
            repository.updateErrorList(
                action.copy(
                    error = Throwable(saveResult.valueStoreResultMessage),
                ),
            )
        }
        return saveResult ?: StoreResult(
            action.id,
            ValueStoreResult.VALUE_CHANGED,
        )
    }

    private fun handleFocusOrNextAction(action: RowAction): StoreResult {
        val storeResult = saveLastFocusedItem(action)
        repository.setFocusedItem(action)
        previousActionItem = action
        return storeResult
    }

    private fun handleOnTextChangeAction(action: RowAction): StoreResult {
        repository.updateValueOnList(action.id, action.value, action.valueType)
        return StoreResult(
            action.id,
            ValueStoreResult.TEXT_CHANGING,
        )
    }

    private fun handleOnSectionChangeAction(action: RowAction): StoreResult {
        repository.updateSectionOpened(action)
        return StoreResult(
            action.id,
            ValueStoreResult.VALUE_HAS_NOT_CHANGED,
        )
    }

    private fun handleOnFinishAction(action: RowAction): StoreResult {
        repository.setFocusedItem(action)
        return StoreResult(
            "",
            ValueStoreResult.FINISH,
        )
    }

    private fun handleOnRequestCoordinatesAction(action: RowAction): StoreResult {
        repository.setFieldRequestingCoordinates(action.id, true)
        return StoreResult(
            action.id,
            ValueStoreResult.VALUE_HAS_NOT_CHANGED,
        )
    }

    private fun handleOnCancelRequestCoordinatesAction(action: RowAction): StoreResult {
        repository.setFieldRequestingCoordinates(action.id, false)
        return StoreResult(
            action.id,
            ValueStoreResult.VALUE_HAS_NOT_CHANGED,
        )
    }

    private fun handleOnAddImageFinishedAction(action: RowAction): StoreResult {
        repository.setFieldAddingImage(action.id, false)
        return StoreResult(
            action.id,
            ValueStoreResult.VALUE_HAS_NOT_CHANGED,
        )
    }

    private fun handleOnStoreFileAction(action: RowAction): StoreResult {
        val saveResult = repository.storeFile(action.id, action.value)
        return when (saveResult?.valueStoreResult) {
            ValueStoreResult.FILE_SAVED -> {
                processUserAction(
                    rowActionFromIntent(
                        FormIntent.OnSave(
                            uid = action.id,
                            value = saveResult.uid,
                            valueType = action.valueType,
                        ),
                    ),
                )
            }

            null -> StoreResult(
                action.id,
                ValueStoreResult.VALUE_HAS_NOT_CHANGED,
            )

            else -> saveResult
        }
    }

    private fun saveLastFocusedItem(rowAction: RowAction) = getLastFocusedTextItem()?.let {
        if (previousActionItem == null) previousActionItem = rowAction
        if (previousActionItem?.value != it.value && previousActionItem?.id == it.uid) {
            val error = checkFieldError(it.valueType, it.value, it.fieldMask)
            if (error != null) {
                val action = rowActionFromIntent(
                    FormIntent.OnSave(it.uid, it.value, it.valueType, it.fieldMask),
                )
                repository.updateErrorList(action)
                StoreResult(
                    rowAction.id,
                    ValueStoreResult.VALUE_HAS_NOT_CHANGED,
                )
            } else {
                checkAutoCompleteForLastFocusedItem(it)
                val intent = getSaveIntent(it)
                val action = rowActionFromIntent(intent)
                val result = repository.save(it.uid, it.value, action.extraData)
                repository.updateValueOnList(it.uid, it.value, it.valueType)
                repository.updateErrorList(action)
                result
            }
        } else {
            StoreResult(
                rowAction.id,
                ValueStoreResult.VALUE_HAS_NOT_CHANGED,
            )
        }
    } ?: StoreResult(
        rowAction.id,
        ValueStoreResult.VALUE_HAS_NOT_CHANGED,
    )

    private fun checkAutoCompleteForLastFocusedItem(fieldUidModel: FieldUiModel) =
        getLastFocusedTextItem()?.let {
            if (fieldUidModel.renderingType == UiRenderType.AUTOCOMPLETE && !fieldUidModel.value.isNullOrEmpty() && fieldUidModel.value?.trim()?.length != 0) {
                val autoCompleteValues =
                    getListFromPreference(fieldUidModel.uid)
                if (!autoCompleteValues.contains(fieldUidModel.value)) {
                    autoCompleteValues.add(fieldUidModel.value.toString())
                    saveListToPreference(fieldUidModel.uid, autoCompleteValues)
                }
            }
        }

    fun valueTypeIsTextField(valueType: ValueType?, renderType: UiRenderType? = null): Boolean {
        return if (valueType == null) {
            false
        } else {
            valueType.isNumeric ||
                valueType.isText && renderType?.isPolygon() != true ||
                valueType == ValueType.URL ||
                valueType == ValueType.EMAIL ||
                valueType == ValueType.PHONE_NUMBER
        }
    }

    private fun getLastFocusedTextItem() = repository.currentFocusedItem()?.takeIf {
        it.optionSet == null && (
            valueTypeIsTextField(
                it.valueType,
                it.renderingType,
            ) || it.valueType == ValueType.AGE ||
                it.valueType == ValueType.DATETIME ||
                it.valueType == ValueType.DATE ||
                it.valueType == ValueType.TIME
            )
    }

    private fun getSaveIntent(field: FieldUiModel) = when (field.valueType) {
        ValueType.COORDINATE -> FormIntent.SaveCurrentLocation(
            field.uid,
            field.value,
            getFeatureType(field.renderingType).name,
        )

        else -> FormIntent.OnSave(field.uid, field.value, field.valueType, field.fieldMask)
    }

    private fun rowActionFromIntent(intent: FormIntent): RowAction {
        return when (intent) {
            is FormIntent.ClearValue -> createRowAction(intent.uid, null)
            is FormIntent.SelectLocationFromCoordinates -> {
                val error = checkFieldError(
                    ValueType.COORDINATE,
                    intent.coordinates,
                    null,
                )
                createRowAction(
                    uid = intent.uid,
                    value = intent.coordinates,
                    extraData = intent.extraData,
                    error = error,
                    valueType = ValueType.COORDINATE,
                )
            }

            is FormIntent.SelectLocationFromMap -> setCoordinateFieldValue(
                fieldUid = intent.uid,
                featureType = intent.featureType,
                coordinates = intent.coordinates,
            )

            is FormIntent.SaveCurrentLocation -> {
                val error = checkFieldError(
                    ValueType.COORDINATE,
                    intent.value,
                    null,
                )
                createRowAction(
                    uid = intent.uid,
                    value = intent.value,
                    extraData = intent.featureType,
                    error = error,
                    valueType = ValueType.COORDINATE,
                )
            }

            is FormIntent.OnNext -> createRowAction(
                uid = intent.uid,
                value = intent.value,
                actionType = ActionType.ON_NEXT,
            )

            is FormIntent.OnSave -> {
                val error = checkFieldError(
                    intent.valueType,
                    intent.value,
                    intent.fieldMask,
                    intent.allowFutureDates,
                )

                createRowAction(
                    uid = intent.uid,
                    value = intent.value,
                    error = error,
                    valueType = intent.valueType,
                )
            }

            is FormIntent.OnQrCodeScanned -> {
                val error = checkFieldError(
                    intent.valueType,
                    intent.value,
                )

                createRowAction(
                    uid = intent.uid,
                    value = intent.value,
                    error = error,
                    valueType = intent.valueType,
                )
            }

            is FormIntent.OnFocus -> createRowAction(
                uid = intent.uid,
                value = intent.value,
                actionType = ActionType.ON_FOCUS,
            )

            is FormIntent.OnTextChange -> createRowAction(
                uid = intent.uid,
                value = intent.value,
                actionType = ActionType.ON_TEXT_CHANGE,
                valueType = intent.valueType,
            )

            is FormIntent.OnSection -> createRowAction(
                uid = intent.sectionUid,
                value = null,
                actionType = ActionType.ON_SECTION_CHANGE,
            )

            is FormIntent.OnFinish -> createRowAction(
                uid = "",
                value = null,
                actionType = ActionType.ON_FINISH,
            )

            is FormIntent.OnRequestCoordinates ->
                createRowAction(
                    uid = intent.uid,
                    value = null,
                    actionType = ActionType.ON_REQUEST_COORDINATES,
                )

            is FormIntent.OnCancelRequestCoordinates ->
                createRowAction(
                    uid = intent.uid,
                    value = null,
                    actionType = ActionType.ON_CANCEL_REQUEST_COORDINATES,
                )

            is FormIntent.OnAddImageFinished ->
                createRowAction(
                    uid = intent.uid,
                    value = null,
                    actionType = ActionType.ON_ADD_IMAGE_FINISHED,
                )

            is FormIntent.OnStoreFile ->
                createRowAction(
                    uid = intent.uid,
                    value = intent.filePath,
                    actionType = ActionType.ON_STORE_FILE,
                    valueType = intent.valueType,
                )

            is FormIntent.OnSaveDate -> {
                val error = checkFieldError(
                    valueType = intent.valueType,
                    fieldValue = intent.value,
                    allowFutureDates = intent.allowFutureDates,
                )

                createRowAction(
                    uid = intent.uid,
                    value = intent.value,
                    error = error,
                    valueType = intent.valueType,
                )
            }
        }
    }

    private fun checkFieldError(
        valueType: ValueType?,
        fieldValue: String?,
        fieldMask: String? = null,
        allowFutureDates: Boolean? = null,
    ): Throwable? {
        if (fieldValue.isNullOrEmpty()) {
            return null
        }

        return fieldValue.let { value ->
            val result = when (valueType) {
                ValueType.DATE -> {
                    validateDateFormats(fieldValue, valueType, allowFutureDates)
                }

                ValueType.TIME -> {
                    validateTimeFormat(fieldValue, valueType)
                }

                ValueType.DATETIME -> {
                    validateDateTimeFormat(fieldValue, valueType, allowFutureDates)
                }

                ValueType.AGE -> {
                    validateDateFormats(fieldValue, valueType, allowFutureDates)
                }

                else -> {
                    valueType?.validator?.validate(value)
                }
            }
            var error = when (result) {
                is Result.Failure -> result.failure
                else -> null
            }

            fieldMask?.let { mask ->
                error = when (val validation = FieldMaskValidator(mask).validate(value)) {
                    is Result.Failure -> validation.failure
                    else -> error
                }
            }
            error
        }
    }

    private fun validateDateTimeFormat(
        dateTimeString: String,
        valueType: ValueType,
        allowFutureDates: Boolean?,
    ): Result<String, Throwable> {
        val regex = Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$")

        if (!regex.matches(dateTimeString)) {
            return Result.Failure(DateTimeFailure.ParseException)
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val isValidDateFormat = isValidDate(dateTimeString.substring(0, 10))
        try {
            val date = LocalDateTime.parse(dateTimeString, formatter)
            if (allowFutureDates == false && date.isAfter(LocalDateTime.now()) || !isValidDateFormat) {
                return Result.Failure(DateFailure.ParseException)
            }
            return valueType.validator.validate(dateTimeString)
        } catch (e: DateTimeParseException) {
            return Result.Failure(DateTimeFailure.ParseException)
        }
    }

    private fun validateTimeFormat(
        timeString: String,
        valueType: ValueType,
    ): Result<String, Throwable> {
        val regex = Regex("([01][0-9]|2[0-3]):[0-5][0-9]")
        return if (regex.matches(timeString)) {
            valueType.validator.validate(timeString)
        } else {
            Result.Failure(TimeFailure.ParseException)
        }
    }

    private fun validateDateFormats(
        dateString: String,
        valueType: ValueType,
        allowFutureDates: Boolean?,
    ): Result<String, Throwable> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        try {
            val date = LocalDate.parse(dateString, formatter)
            val formatDateValid = isValidDate(dateString)
            if (allowFutureDates == false && date.isAfter(LocalDate.now()) || !formatDateValid) {
                return Result.Failure(Throwable())
            }
            return valueType.validator.validate(dateString)
        } catch (e: DateTimeParseException) {
            return Result.Failure(DateFailure.ParseException)
        }
    }

    private fun isValidDate(text: String): Boolean {
        val format = SimpleDateFormat(DateUtils.DATABASE_FORMAT_NO_TIME)
        format.isLenient = false
        return try {
            format.parse(text)
            true
        } catch (e: ParseException) {
            false
        }
    }

    private fun createRowAction(
        uid: String,
        value: String?,
        extraData: String? = null,
        error: Throwable? = null,
        actionType: ActionType = ActionType.ON_SAVE,
        valueType: ValueType? = null,
    ) = RowAction(
        id = uid,
        value = value,
        extraData = extraData,
        error = error,
        type = actionType,
        valueType = valueType,
        isEventDetailsRow = isEventDetailField(uid),
    )

    private fun isEventDetailField(uid: String): Boolean {
        val eventDetailsIds =
            listOf(EVENT_REPORT_DATE_UID, EVENT_ORG_UNIT_UID, EVENT_COORDINATE_UID)
        return eventDetailsIds.contains(uid)
    }

    fun onItemsRendered() {
        loading.value = false
    }

    private fun setCoordinateFieldValue(
        fieldUid: String,
        featureType: String,
        coordinates: String?,
    ): RowAction {
        val type = FeatureType.valueOf(featureType)
        val geometryCoordinates = coordinates?.let {
            geometryController.generateLocationFromCoordinates(
                type,
                coordinates,
            )?.coordinates()
        }

        val error = if (type == FeatureType.POINT) {
            checkFieldError(ValueType.COORDINATE, geometryCoordinates, null)
        } else {
            null
        }

        return createRowAction(
            uid = fieldUid,
            value = geometryCoordinates,
            extraData = featureType,
            error = error,
            valueType = ValueType.COORDINATE,
        )
    }

    fun getFocusedItemUid(): String? {
        return items.value?.firstOrNull { it.focused }?.uid
    }

    private fun processCalculatedItems(skipProgramRules: Boolean = false, finish: Boolean = false) {
        fieldListChannel.trySend(
            FieldListConfiguration(skipProgramRules, finish),
        )
    }

    fun updateConfigurationErrors() {
        confError.value = repository.getConfigurationErrors() ?: emptyList()
    }

    fun runDataIntegrityCheck(backButtonPressed: Boolean? = null) {
        viewModelScope.launch {
            val result = async(dispatcher.io()) {
                repository.runDataIntegrityCheck(allowDiscard = backButtonPressed ?: false)
            }
            try {
                _dataIntegrityResult.postValue(result.await())
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                val list = repository.composeList()
                _items.postValue(list)
            }
        }
    }

    fun calculateCompletedFields() {
        viewModelScope.launch {
            val result = async(dispatcher.io()) {
                repository.completedFieldsPercentage(_items.value ?: emptyList())
            }
            try {
                _completionPercentage.postValue(result.await())
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun displayLoopWarningIfNeeded() {
        viewModelScope.launch {
            val result = async(dispatcher.io()) {
                repository.calculationLoopOverLimit()
            }
            try {
                _calculationLoop.postValue(result.await())
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun discardChanges() {
        repository.backupOfChangedItems().forEach {
            submitIntent(FormIntent.OnSave(it.uid, it.value, it.valueType, it.fieldMask))
        }
    }

    fun saveDataEntry() {
        getLastFocusedTextItem()?.let {
            submitIntent(getSaveIntent(it))
        }
        submitIntent(FormIntent.OnFinish())
    }

    fun loadData() {
        loading.postValue(true)
        viewModelScope.launch(dispatcher.io()) {
            val result = async {
                repository.fetchFormItems(openErrorLocation)
            }
            dateFormatConfig = async {
                repository.getDateFormatConfiguration()
            }.await()
            try {
                _items.postValue(result.await())
            } catch (e: Exception) {
                Timber.e(e)
                _items.postValue(emptyList())
            }
        }
    }

    fun clearFocus() {
        repository.clearFocusItem()
    }

    fun getUpdatedData(uiEvent: RecyclerViewUiEvents.OpenChooserIntent): RowAction {
        val currentField = queryData.value
        return when (currentField?.id) {
            uiEvent.uid -> currentField.copy(
                type = ActionType.ON_SAVE,
                error = checkFieldError(
                    currentField.valueType,
                    currentField.value,
                    null,
                ),
            )

            else -> RowAction(
                id = uiEvent.uid,
                value = uiEvent.value,
                type = ActionType.ON_SAVE,
            )
        }
    }

    private fun getListFromPreference(uid: String): MutableList<String> {
        val gson = Gson()
        val json = preferenceProvider.sharedPreferences().getString(uid, "[]")
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveListToPreference(uid: String, list: List<String>) {
        val gson = Gson()
        val json = gson.toJson(list)
        preferenceProvider.sharedPreferences().edit().putString(uid, json).apply()
    }

    fun areSectionCollapsable(): Boolean {
        return repository.areSectionCollapsable()
    }

    companion object {
        const val TAG = "FormViewModel"
    }
}
