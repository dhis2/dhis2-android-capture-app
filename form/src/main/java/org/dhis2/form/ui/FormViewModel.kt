package org.dhis2.form.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.commons.dialogs.bottomsheet.FieldWithIssue
import org.dhis2.commons.periods.model.Period
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.R
import org.dhis2.form.data.DataIntegrityCheckResult
import org.dhis2.form.data.EventRepository.Companion.EVENT_COORDINATE_UID
import org.dhis2.form.data.EventRepository.Companion.EVENT_ORG_UNIT_UID
import org.dhis2.form.data.EventRepository.Companion.EVENT_REPORT_DATE_UID
import org.dhis2.form.data.FieldsWithErrorResult
import org.dhis2.form.data.FieldsWithWarningResult
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.GeometryController
import org.dhis2.form.data.GeometryParserImpl
import org.dhis2.form.data.MissingMandatoryResult
import org.dhis2.form.data.NotSavedResult
import org.dhis2.form.data.RulesUtilsProviderConfigurationError
import org.dhis2.form.data.SuccessfulResult
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldListConfiguration
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.InfoUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.idling.FormCountingIdlingResource
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.provider.FormResultDialogProvider
import org.dhis2.mobile.commons.model.CustomIntentRequestArgumentModel
import org.dhis2.mobile.commons.providers.CustomIntentFailure
import org.dhis2.mobile.commons.validation.validators.FieldMaskValidator
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.valuetype.validation.failures.DateFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.DateTimeFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.TimeFailure
import org.hisp.dhis.android.core.event.EventStatus
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
    private val resultDialogUiProvider: FormResultDialogProvider,
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

    sealed interface FormActions {
        data object OnFinish : FormActions

        data class ShowResultDialog(
            val model: BottomSheetDialogUiModel,
            val allowDiscard: Boolean,
            val fieldsWithIssues: List<FieldWithIssue>,
        ) : FormActions
    }

    private val _actionsChannel = Channel<FormActions>()
    val actionsChannel = _actionsChannel.receiveAsFlow()
    private val _completionPercentage = MutableLiveData<Float>()
    val completionPercentage = _completionPercentage

    private val _calculationLoop = MutableLiveData(false)
    val calculationLoop = _calculationLoop

    private val pendingIntents = MutableSharedFlow<FormIntent>()

    private val fieldListChannel =
        Channel<FieldListConfiguration>(
            capacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    private val handler = Handler(Looper.getMainLooper())

    var filePath: String? = null

    init {

        pendingIntents
            .distinctUntilChanged { old, new ->
                if (old is FormIntent.OnFinish && new is FormIntent.OnFinish) {
                    false
                } else {
                    old == new
                }
            }.onEach { intent ->
                FormCountingIdlingResource.increment()
                val result = createRowActionStore(intent)
                displayResult(result)
                FormCountingIdlingResource.decrement()
            }.flowOn(dispatcher.io())
            .launchIn(viewModelScope)

        viewModelScope.launch(dispatcher.io()) {
            fieldListChannel.consumeEach { fieldListConfiguration ->
                FormCountingIdlingResource.increment()
                val result = repository.composeList(fieldListConfiguration.skipProgramRules)
                _items.postValue(result)
                if (fieldListConfiguration.finish) {
                    runDataIntegrityCheck()
                }
                FormCountingIdlingResource.decrement()
            }
        }

        loadData()
    }

    private fun displayResult(result: Pair<RowAction, StoreResult>) {
        result.second.valueStoreResult?.let {
            when (it) {
                ValueStoreResult.VALUE_CHANGED -> {
                    result.first.let {
                        _savedValue.postValue(it)
                    }
                    processCalculatedItems()
                }

                ValueStoreResult.ERROR_UPDATING_VALUE -> {
                    loading.postValue(false)
                    showToast.postValue(R.string.update_field_error)
                    processCalculatedItems(true)
                }

                ValueStoreResult.UID_IS_NOT_DE_OR_ATTR -> {
                    Timber
                        .tag(TAG)
                        .d("${result.first.id} is not a data element or attribute")
                    processCalculatedItems()
                }

                ValueStoreResult.VALUE_NOT_UNIQUE -> {
                    showInfo.postValue(
                        InfoUiModel(
                            R.string.error,
                            R.string.unique_warning,
                        ),
                    )
                    processCalculatedItems()
                }

                ValueStoreResult.VALUE_HAS_NOT_CHANGED -> {
                    processCalculatedItems(true)
                }

                ValueStoreResult.TEXT_CHANGING -> {
                    result.first.let {
                        Timber.d("${result.first.id} is changing its value")
                        _queryData.postValue(it)
                    }
                    if (repository.hasLegendSet(result.first.id)) {
                        handler.removeCallbacksAndMessages(null)
                        handler.postDelayed({
                            processCalculatedItems(skipProgramRules = true)
                        }, 500L)
                    }
                }

                ValueStoreResult.FINISH -> {
                    processCalculatedItems(finish = true)
                }

                ValueStoreResult.FILE_SAVED -> {
                    // Do nothing
                }
            }
        }
    }

    fun submitIntent(intent: FormIntent) {
        viewModelScope.launch {
            pendingIntents.emit(intent)
        }
    }

    private suspend fun createRowActionStore(it: FormIntent): Pair<RowAction, StoreResult> {
        val rowAction = rowActionFromIntent(it)

        if (rowAction.type == ActionType.ON_FOCUS) {
            focused.postValue(true)
        } else if (rowAction.type == ActionType.ON_SAVE) {
            loading.postValue(true)
        }

        val result = processUserAction(rowAction)
        return Pair(rowAction, result)
    }

    private suspend fun processUserAction(action: RowAction): StoreResult =
        when (action.type) {
            ActionType.ON_SAVE -> handleOnSaveAction(action)
            ActionType.ON_FOCUS, ActionType.ON_NEXT -> handleFocusOrNextAction(action)
            ActionType.ON_TEXT_CHANGE -> handleOnTextChangeAction(action)
            ActionType.ON_SECTION_CHANGE -> handleOnSectionChangeAction(action)
            ActionType.ON_FINISH -> handleOnFinishAction(action)
            ActionType.ON_FIELD_LOADING -> handleOnFieldLoadingAction(action)
            ActionType.ON_FINISH_LOADING ->
                handleOnFieldFinishedLoadingAction(
                    action,
                )

            ActionType.ON_ADD_IMAGE_FINISHED -> handleOnAddImageFinishedAction(action)
            ActionType.ON_STORE_FILE -> handleOnStoreFileAction(action)
            ActionType.ON_FETCH_OPTIONS -> handleFetchOptionsAction(action)
        }

    private fun handleFetchOptionsAction(action: RowAction): StoreResult {
        repository.fetchOptions(action.id, action.extraData!!)
        return StoreResult(
            action.id,
            ValueStoreResult.VALUE_CHANGED,
        )
    }

    private suspend fun handleOnSaveAction(action: RowAction): StoreResult {
        if (action.valueType == ValueType.COORDINATE) {
            repository.setFieldLoading(action.id, false)
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

    private fun handleOnFieldLoadingAction(action: RowAction): StoreResult {
        repository.setFieldLoading(action.id, true)
        return StoreResult(
            action.id,
            ValueStoreResult.VALUE_HAS_NOT_CHANGED,
        )
    }

    private fun handleOnFieldFinishedLoadingAction(action: RowAction): StoreResult {
        repository.setFieldLoading(action.id, false)
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

    private suspend fun handleOnStoreFileAction(action: RowAction): StoreResult {
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

            null ->
                StoreResult(
                    action.id,
                    ValueStoreResult.VALUE_HAS_NOT_CHANGED,
                )

            else -> saveResult
        }
    }

    private fun saveLastFocusedItem(rowAction: RowAction) =
        getLastFocusedTextItem()?.let {
            if (previousActionItem == null) previousActionItem = rowAction
            if (previousActionItem?.value != it.value && previousActionItem?.id == it.uid) {
                val action =
                    rowActionFromIntent(
                        FormIntent.OnSave(
                            it.uid,
                            it.value,
                            it.valueType,
                            it.fieldMask,
                            it.allowFutureDates,
                        ),
                    )
                if (action.error != null) {
                    repository.updateErrorList(action)
                    StoreResult(
                        rowAction.id,
                        ValueStoreResult.VALUE_HAS_NOT_CHANGED,
                    )
                } else {
                    checkAutoCompleteForLastFocusedItem(it)
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
            if (fieldUidModel.renderingType == UiRenderType.AUTOCOMPLETE &&
                !fieldUidModel.value.isNullOrEmpty() &&
                fieldUidModel.value?.trim()?.length != 0
            ) {
                val autoCompleteValues =
                    repository
                        .getListFromPreferences(fieldUidModel.uid)
                        .toMutableList()
                if (!autoCompleteValues.contains(fieldUidModel.value)) {
                    autoCompleteValues.add(fieldUidModel.value.toString())
                    repository.saveListToPreferences(fieldUidModel.uid, autoCompleteValues)
                }
            }
        }

    fun valueTypeIsTextField(
        valueType: ValueType?,
        renderType: UiRenderType? = null,
    ): Boolean =
        if (valueType == null) {
            false
        } else {
            valueType.isNumeric ||
                valueType.isText &&
                renderType?.isPolygon() != true ||
                valueType == ValueType.URL ||
                valueType == ValueType.EMAIL ||
                valueType == ValueType.PHONE_NUMBER
        }

    private fun getLastFocusedTextItem() =
        repository.currentFocusedItem()?.takeIf {
            it.optionSet == null &&
                (
                    valueTypeIsTextField(
                        it.valueType,
                        it.renderingType,
                    ) ||
                        it.valueType == ValueType.AGE ||
                        it.valueType == ValueType.DATETIME ||
                        it.valueType == ValueType.DATE ||
                        it.valueType == ValueType.TIME
                )
        }

    private fun rowActionFromIntent(intent: FormIntent): RowAction =
        when (intent) {
            is FormIntent.ClearValue -> createRowAction(intent.uid, null)
            is FormIntent.SelectLocationFromCoordinates -> {
                val error =
                    checkFieldError(
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

            is FormIntent.SelectLocationFromMap ->
                setCoordinateFieldValue(
                    fieldUid = intent.uid,
                    featureType = intent.featureType,
                    coordinates = intent.coordinates,
                )

            is FormIntent.SaveCurrentLocation -> {
                val error =
                    checkFieldError(
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

            is FormIntent.OnNext ->
                createRowAction(
                    uid = intent.uid,
                    value = intent.value,
                    actionType = ActionType.ON_NEXT,
                )

            is FormIntent.OnSave -> {
                val error =
                    checkFieldError(
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

            is FormIntent.OnSaveCustomIntent -> {
                createRowAction(
                    uid = intent.uid,
                    value = intent.value,
                    error = if (intent.error) CustomIntentFailure.CouldNotRetrieveCustomIntentData else null,
                    valueType = ValueType.TEXT,
                )
            }

            is FormIntent.OnQrCodeScanned -> {
                val error =
                    checkFieldError(
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

            is FormIntent.OnFocus ->
                createRowAction(
                    uid = intent.uid,
                    value = intent.value,
                    actionType = ActionType.ON_FOCUS,
                )

            is FormIntent.OnTextChange ->
                createRowAction(
                    uid = intent.uid,
                    value = intent.value,
                    actionType = ActionType.ON_TEXT_CHANGE,
                    valueType = intent.valueType,
                )

            is FormIntent.OnSection ->
                createRowAction(
                    uid = intent.sectionUid,
                    value = null,
                    actionType = ActionType.ON_SECTION_CHANGE,
                )

            is FormIntent.OnFinish ->
                createRowAction(
                    uid = "",
                    value = null,
                    actionType = ActionType.ON_FINISH,
                )

            is FormIntent.OnFieldLoadingData ->
                createRowAction(
                    uid = intent.uid,
                    value = null,
                    actionType = ActionType.ON_FIELD_LOADING,
                )

            is FormIntent.OnFieldFinishedLoadingData ->
                createRowAction(
                    uid = intent.uid,
                    value = null,
                    actionType = ActionType.ON_FINISH_LOADING,
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
                val error =
                    checkFieldError(
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

            is FormIntent.FetchOptions ->
                createRowAction(
                    uid = intent.uid,
                    value = intent.value,
                    extraData = intent.optionSetUid,
                    actionType = ActionType.ON_FETCH_OPTIONS,
                )
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
            val result =
                when (valueType) {
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
            var error =
                when (result) {
                    is Result.Failure -> result.failure
                    else -> null
                }

            fieldMask?.let { mask ->
                error =
                    when (val validation = FieldMaskValidator(mask).validate(value)) {
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
        val geometryCoordinates =
            coordinates?.let {
                geometryController
                    .generateLocationFromCoordinates(
                        type,
                        coordinates,
                    )?.coordinates()
            }

        val error =
            if (type == FeatureType.POINT) {
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

    private fun processCalculatedItems(
        skipProgramRules: Boolean = false,
        finish: Boolean = false,
    ) {
        viewModelScope.launch {
            fieldListChannel.send(
                FieldListConfiguration(skipProgramRules, finish),
            )
        }
    }

    fun updateConfigurationErrors() {
        confError.value = repository.getConfigurationErrors() ?: emptyList()
    }

    fun runDataIntegrityCheck(backButtonPressed: Boolean? = null) {
        viewModelScope.launch {
            FormCountingIdlingResource.increment()
            val result =
                async(dispatcher.io()) {
                    repository.runDataIntegrityCheck(backPressed = backButtonPressed ?: false)
                }
            try {
                handleDataIntegrityResult(result.await())
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                val list = repository.composeList()
                _items.postValue(list)
                FormCountingIdlingResource.decrement()
            }
        }
    }

    private suspend fun handleDataIntegrityResult(result: DataIntegrityCheckResult) {
        val isEvent = repository.isEvent()
        val action =
            when {
                isEvent && repository.isEventEditable() == false -> FormActions.OnFinish
                (result is SuccessfulResult) and (result.eventResultDetails.eventStatus == null) -> FormActions.OnFinish
                result is NotSavedResult -> FormActions.OnFinish
                else -> showDataEntryResultDialogDeprecated(result)
            }
        action?.let { _actionsChannel.send(it) }
    }

    private suspend fun showDataEntryResultDialogDeprecated(result: DataIntegrityCheckResult): FormActions? =
        when (result.eventResultDetails.eventStatus) {
            EventStatus.ACTIVE, null -> provideShowResultDialog(result)

            EventStatus.COMPLETED -> {
                val resultAction = provideShowResultDialog(result)
                if (resultAction?.fieldsWithIssues?.isEmpty() == true) {
                    FormActions.OnFinish
                }
                resultAction
            }

            EventStatus.SKIPPED -> {
                val resultAction = provideShowResultDialog(result)
                if (resultAction?.fieldsWithIssues?.isEmpty() == true) {
                    activateEvent()
                }
                resultAction
            }

            EventStatus.SCHEDULE,
            EventStatus.VISITED,
            EventStatus.OVERDUE,
            -> FormActions.OnFinish
        }

    private fun provideShowResultDialog(result: DataIntegrityCheckResult): FormActions.ShowResultDialog? =
        when (result) {
            is FieldsWithErrorResult -> {
                resultDialogUiProvider(
                    canComplete = result.canComplete,
                    onCompleteMessage = result.onCompleteMessage,
                    errorFields = result.fieldUidErrorList,
                    emptyMandatoryFields = result.mandatoryFields,
                    warningFields = result.warningFields,
                    eventMode = result.eventResultDetails.eventMode,
                    eventState = result.eventResultDetails.eventStatus,
                    result = result,
                )
            }

            is FieldsWithWarningResult ->
                resultDialogUiProvider(
                    canComplete = result.canComplete,
                    onCompleteMessage = result.onCompleteMessage,
                    errorFields = emptyList(),
                    emptyMandatoryFields = emptyMap(),
                    warningFields = result.fieldUidWarningList,
                    eventMode = result.eventResultDetails.eventMode,
                    eventState = result.eventResultDetails.eventStatus,
                    result = result,
                )

            is MissingMandatoryResult ->
                resultDialogUiProvider(
                    canComplete = result.canComplete,
                    onCompleteMessage = result.onCompleteMessage,
                    errorFields = result.errorFields,
                    emptyMandatoryFields = result.mandatoryFields,
                    warningFields = result.warningFields,
                    eventMode = result.eventResultDetails.eventMode,
                    eventState = result.eventResultDetails.eventStatus,
                    result = result,
                )

            is SuccessfulResult ->
                resultDialogUiProvider(
                    canComplete = result.canComplete,
                    onCompleteMessage = result.onCompleteMessage,
                    errorFields = emptyList(),
                    emptyMandatoryFields = emptyMap(),
                    warningFields = emptyList(),
                    eventMode = result.eventResultDetails.eventMode,
                    eventState = result.eventResultDetails.eventStatus,
                    result = result,
                )

            NotSavedResult -> null
        }?.let { (model, fieldsWithIssues) ->
            FormActions.ShowResultDialog(
                model,
                result.allowDiscard,
                fieldsWithIssues,
            )
        }

    fun calculateCompletedFields() {
        viewModelScope.launch {
            val result =
                async(dispatcher.io()) {
                    repository.completedFieldsPercentage(_items.value ?: emptyList())
                }
            try {
                _completionPercentage.postValue(result.await())
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun completeEvent() {
        viewModelScope.launch {
            try {
                async(dispatcher.io()) {
                    repository.completeEvent()
                }.await()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun activateEvent() {
        viewModelScope.launch(dispatcher.io()) {
            repository.activateEvent()
        }
    }

    fun displayLoopWarningIfNeeded() {
        viewModelScope.launch {
            val result =
                async(dispatcher.io()) {
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
            submitIntent(
                FormIntent.OnSave(
                    it.uid,
                    it.value,
                    it.valueType,
                    it.fieldMask,
                    it.allowFutureDates,
                ),
            )
        }
    }

    fun saveDataEntry() {
        getLastFocusedTextItem()?.let {
            submitIntent(
                FormIntent.OnSave(
                    it.uid,
                    it.value,
                    it.valueType,
                    it.fieldMask,
                    it.allowFutureDates,
                ),
            )
        }
        submitIntent(FormIntent.OnFinish())
    }

    fun loadData() {
        loading.postValue(true)
        viewModelScope.launch(dispatcher.io()) {
            FormCountingIdlingResource.increment()
            val result = repository.fetchFormItems(openErrorLocation)
            dateFormatConfig =
                async {
                    repository.getDateFormatConfiguration()
                }.await()
            try {
                _items.postValue(result)
            } catch (e: Exception) {
                Timber.e(e)
                _items.postValue(emptyList())
            } finally {
                FormCountingIdlingResource.decrement()
            }
        }
    }

    fun clearFocus() {
        repository.clearFocusItem()
    }

    fun getUpdatedData(uiEvent: RecyclerViewUiEvents.OpenChooserIntent): RowAction {
        val currentField = queryData.value
        return when (currentField?.id) {
            uiEvent.uid ->
                currentField.copy(
                    type = ActionType.ON_SAVE,
                    error =
                        checkFieldError(
                            currentField.valueType,
                            currentField.value,
                            null,
                        ),
                )

            else ->
                RowAction(
                    id = uiEvent.uid,
                    value = uiEvent.value,
                    type = ActionType.ON_SAVE,
                )
        }
    }

    fun setFieldLoading(
        fieldUid: String,
        isLoading: Boolean,
        value: String,
    ) {
        repository.setFieldLoading(fieldUid, isLoading)
        repository.updateValueOnList(fieldUid, null, null)
    }

    fun getCustomIntentRequestParams(customIntentUid: String): List<CustomIntentRequestArgumentModel> =
        repository.reEvaluateRequestParams(customIntentUid)

    fun fetchPeriods(): Flow<PagingData<Period>> = repository.fetchPeriods().flowOn(dispatcher.io())

    companion object {
        const val TAG = "FormViewModel"
    }
}
