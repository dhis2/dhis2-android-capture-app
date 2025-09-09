package org.dhis2.form.data

import org.dhis2.commons.bindings.formatData
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.ValueStoreResult
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.models.RuleAction
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber

class RulesUtilsProviderImpl(
    val d2: D2,
    private val optionsRepository: OptionsRepository,
) : RulesUtilsProvider {
    var applyForEvent = false
    var canComplete = true
    var messageOnComplete: String? = null
    val fieldsWithErrors = mutableListOf<FieldWithError>()
    val fieldsWithWarnings = mutableListOf<FieldWithError>()
    val unsupportedRuleActions = mutableListOf<String>()
    val optionsToHide = mutableMapOf<String, MutableList<String>>()
    val optionGroupsToHide = mutableMapOf<String, MutableList<String>>()
    val optionGroupsToShow = mutableMapOf<String, MutableList<String>>()
    var fieldsToUpdate = mutableListOf<FieldWithNewValue>()
    var hiddenFields = mutableListOf<String>()
    val configurationErrors = mutableListOf<RulesUtilsProviderConfigurationError>()
    var valueStore: FormValueStore? = null
    var currentRuleUid: String? = null
    val stagesToHide = mutableListOf<String>()
    private val valuesToChange = mutableMapOf<String, String?>()

    @Synchronized
    override fun applyRuleEffects(
        applyForEvent: Boolean,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        calcResult: List<RuleEffect>,
        valueStore: FormValueStore?,
    ): RuleUtilsProviderResult {
        this.applyForEvent = applyForEvent
        canComplete = true
        messageOnComplete = null
        fieldsWithErrors.clear()
        fieldsWithWarnings.clear()
        unsupportedRuleActions.clear()
        optionsToHide.clear()
        optionGroupsToHide.clear()
        optionGroupsToShow.clear()
        fieldsToUpdate.clear()
        hiddenFields.clear()
        configurationErrors.clear()
        valuesToChange.clear()
        this.valueStore = valueStore

        calcResult.forEach {
            currentRuleUid = it.ruleId

            when (ProgramRuleActionType.valueOf(it.ruleAction.type)) {
                ProgramRuleActionType.SHOWWARNING ->
                    showWarning(
                        it.ruleAction,
                        fieldViewModels,
                        it.data ?: "",
                    )

                ProgramRuleActionType.SHOWERROR ->
                    showError(
                        it.ruleAction,
                        fieldViewModels,
                        it.data ?: "",
                    )

                ProgramRuleActionType.HIDEFIELD ->
                    hideField(
                        it.ruleAction,
                        fieldViewModels,
                    )

                ProgramRuleActionType.DISPLAYTEXT ->
                    displayText(
                        it.ruleAction,
                        it,
                        fieldViewModels,
                    )

                ProgramRuleActionType.DISPLAYKEYVALUEPAIR ->
                    displayKeyValuePair(
                        it.ruleAction,
                        it,
                        fieldViewModels,
                    )

                ProgramRuleActionType.HIDESECTION ->
                    hideSection(
                        fieldViewModels,
                        it.ruleAction,
                    )

                ProgramRuleActionType.ASSIGN ->
                    assign(
                        it.ruleAction,
                        it,
                        fieldViewModels,
                    )

                ProgramRuleActionType.CREATEEVENT ->
                    createEvent(
                        it.ruleAction,
                        fieldViewModels,
                    )

                ProgramRuleActionType.SETMANDATORYFIELD ->
                    setMandatory(
                        it.ruleAction,
                        fieldViewModels,
                    )

                ProgramRuleActionType.WARNINGONCOMPLETE ->
                    warningOnCompletion(
                        it.ruleAction,
                        fieldViewModels,
                        it.data ?: "",
                    )

                ProgramRuleActionType.ERRORONCOMPLETE ->
                    errorOnCompletion(
                        it.ruleAction,
                        fieldViewModels,
                        it.data ?: "",
                    )

                ProgramRuleActionType.HIDEPROGRAMSTAGE ->
                    hideProgramStage(
                        it.ruleAction,
                    )

                ProgramRuleActionType.HIDEOPTION ->
                    hideOption(
                        it.ruleAction,
                    )

                ProgramRuleActionType.HIDEOPTIONGROUP ->
                    hideOptionGroup(
                        it.ruleAction,
                    )

                ProgramRuleActionType.SHOWOPTIONGROUP ->
                    showOptionGroup(
                        it.ruleAction,
                    )

                else -> unsupportedRuleActions.add(it.ruleId)
            }
        }

        valuesToChange.entries.forEach {
            if (save(it.key, it.value) == ValueStoreResult.VALUE_CHANGED) {
                fieldsToUpdate.add(FieldWithNewValue(it.key, it.value))
            }
        }

        currentRuleUid = null

        return RuleUtilsProviderResult(
            canComplete = canComplete,
            messageOnComplete = messageOnComplete,
            fieldsWithErrors = fieldsWithErrors,
            fieldsWithWarnings = fieldsWithWarnings,
            unsupportedRules = unsupportedRuleActions,
            fieldsToUpdate = fieldsToUpdate,
            configurationErrors = configurationErrors,
            stagesToHide = stagesToHide,
            optionsToHide = optionsToHide,
            optionGroupsToHide = optionGroupsToHide,
            optionGroupsToShow = optionGroupsToShow,
        )
    }

    override fun applyRuleEffects(
        programStages: MutableMap<String, ProgramStage>,
        calcResult: Result<List<RuleEffect>>,
    ) {
        calcResult
            .getOrNull()
            ?.filter { it.ruleAction.type == ProgramRuleActionType.HIDEPROGRAMSTAGE.name }
            ?.forEach {
                hideProgramStage(programStages, it.ruleAction)
            }
    }

    private fun save(
        uid: String,
        value: String?,
    ): ValueStoreResult =
        if (applyForEvent) {
            saveForEvent(uid, value)
        } else {
            saveForEnrollment(uid, value)
        }

    private fun saveForEvent(
        uid: String,
        value: String?,
    ): ValueStoreResult =
        valueStore?.saveWithTypeCheck(uid, value)?.blockingFirst()?.valueStoreResult
            ?: ValueStoreResult.VALUE_HAS_NOT_CHANGED

    private fun saveForEnrollment(
        uid: String,
        value: String?,
    ): ValueStoreResult {
        try {
            if (d2
                    .dataElementModule()
                    .dataElements()
                    .uid(uid)
                    .blockingExists()
            ) {
                Timber.d("Enrollments rules should not assign values to dataElements")
                return ValueStoreResult.VALUE_HAS_NOT_CHANGED
            } else if (
                d2
                    .trackedEntityModule()
                    .trackedEntityAttributes()
                    .uid(uid)
                    .blockingExists()
            ) {
                return valueStore?.save(uid, value, null)?.valueStoreResult
                    ?: ValueStoreResult.VALUE_HAS_NOT_CHANGED
            }
        } catch (d2Error: D2Error) {
            Timber.e(d2Error.originalException())
            return ValueStoreResult.VALUE_HAS_NOT_CHANGED
        }
        return ValueStoreResult.VALUE_HAS_NOT_CHANGED
    }

    private fun showWarning(
        showWarning: RuleAction,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        data: String,
    ) {
        val field = showWarning.field() ?: ""
        val model = fieldViewModels[field]
        val warningMessage = "${showWarning.content()} $data"
        if (model != null) {
            fieldViewModels[field] = model.setWarning(warningMessage)
            fieldsWithWarnings.add(
                FieldWithError(field, warningMessage),
            )
        }
    }

    private fun showError(
        showError: RuleAction,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        effectData: String,
    ) {
        val field = showError.field() ?: ""
        val model = fieldViewModels[field]
        val errorMessage = "${showError.content()} $effectData"
        if (model != null) {
            fieldViewModels[field] = model.setError(errorMessage)
            canComplete = false
            fieldsWithErrors.add(
                FieldWithError(field, errorMessage),
            )
        }
    }

    private fun hideField(
        hideField: RuleAction,
        fieldViewModels: MutableMap<String, FieldUiModel>,
    ) {
        val field = hideField.field() ?: ""
        if (fieldViewModels[field]?.mandatory != true) {
            fieldViewModels.remove(hideField.field())
            valuesToChange[field] = null
            hiddenFields.add(field)
        }
    }

    private fun displayText(
        displayText: RuleAction,
        ruleEffect: RuleEffect,
        fieldViewModels: MutableMap<String, FieldUiModel>,
    ) {
    }

    private fun displayKeyValuePair(
        displayKeyValuePair: RuleAction,
        ruleEffect: RuleEffect,
        fieldViewModels: MutableMap<String, FieldUiModel>,
    ) {
    }

    private fun hideSection(
        fieldViewModels: MutableMap<String, FieldUiModel>,
        hideSection: RuleAction,
    ) {
        val programStageSection = hideSection.values["programStageSection"]
        fieldViewModels
            .filter {
                it.value.programStageSection == programStageSection &&
                    !it.value.mandatory
            }.keys
            .forEach { fieldViewModels.remove(it) }
    }

    private fun assign(
        assign: RuleAction,
        ruleEffect: RuleEffect,
        fieldViewModels: MutableMap<String, FieldUiModel>,
    ) {
        val fieldUid = assign.field() ?: ""
        fieldViewModels[fieldUid]?.let { field ->
            val value =
                if (field.optionSet != null && field.displayName != null) {
                    val valueOption =
                        optionsRepository.getOptionByDisplayName(
                            optionSet = field.optionSet!!,
                            displayName = field.displayName!!,
                        )
                    if (valueOption == null) {
                        configurationErrors.add(
                            RulesUtilsProviderConfigurationError(
                                currentRuleUid,
                                ActionType.ASSIGN,
                                ConfigurationError.CURRENT_VALUE_NOT_IN_OPTION_SET,
                                listOf(field.label, field.optionSet ?: ""),
                            ),
                        )
                    }
                    valueOption?.code()
                } else {
                    field.value
                }

            if (value == null || value != ruleEffect.data) {
                ruleEffect.data?.formatData(field.valueType)?.let {
                    valuesToChange[fieldUid] = it
                }
            }
            val valueToShow =
                if (field.optionSet != null && ruleEffect.data?.isNotEmpty() == true) {
                    val effectOption =
                        optionsRepository.getOptionByCode(
                            optionSet = field.optionSet!!,
                            code = ruleEffect.data!!,
                        )
                    if (effectOption == null) {
                        configurationErrors.add(
                            RulesUtilsProviderConfigurationError(
                                currentRuleUid,
                                ActionType.ASSIGN,
                                ConfigurationError.VALUE_TO_ASSIGN_NOT_IN_OPTION_SET,
                                listOf(
                                    currentRuleUid ?: "",
                                    ruleEffect.data ?: "",
                                    field.optionSet ?: "",
                                ),
                            ),
                        )
                    }
                    effectOption?.displayName()
                } else {
                    ruleEffect.data
                }

            ruleEffect.data?.formatData(field.valueType)?.let { formattedValue ->
                val updatedField =
                    fieldViewModels[assign.field()]
                        ?.setValue(formattedValue)
                        ?.setDisplayName(valueToShow?.formatData(field.valueType))
                        ?.setEditable(false)

                updatedField?.let {
                    fieldViewModels[fieldUid] = it
                }
            }
        } ?: {
            if (!hiddenFields.contains(assign.field())) {
                valuesToChange[fieldUid] = ruleEffect.data?.formatData()
            }
        }
    }

    private fun createEvent(
        createEvent: RuleAction,
        fieldViewModels: MutableMap<String, FieldUiModel>,
    ) {
        // TODO: Create Event
    }

    private fun setMandatory(
        mandatoryField: RuleAction,
        fieldViewModels: MutableMap<String, FieldUiModel>,
    ) {
        val fieldUid = mandatoryField.field() ?: ""
        val model = fieldViewModels[fieldUid]
        if (model != null) {
            fieldViewModels[fieldUid] = model.setFieldMandatory()
        } else {
            fieldViewModels
                .filterKeys {
                    it.startsWith(fieldUid)
                }.forEach { (key, value) ->
                    fieldViewModels[key] = value.setFieldMandatory()
                }
        }
    }

    private fun warningOnCompletion(
        warningOnCompletion: RuleAction,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        data: String,
    ) {
        val fieldUid = warningOnCompletion.field() ?: ""
        val model = fieldViewModels[fieldUid]
        val message = warningOnCompletion.content() + " " + data
        if (model != null) {
            fieldViewModels[fieldUid] = model.setWarning(message)
            fieldsWithWarnings.add(
                FieldWithError(fieldUid, message),
            )
        }

        messageOnComplete = message
    }

    private fun errorOnCompletion(
        errorOnCompletion: RuleAction,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        data: String,
    ) {
        val fieldUid = errorOnCompletion.field() ?: ""
        val model = fieldViewModels[errorOnCompletion.field()]
        val message = errorOnCompletion.content() + " " + data
        if (model != null) {
            fieldViewModels[fieldUid] = model.setError(message)
            fieldsWithErrors.add(
                FieldWithError(fieldUid, message),
            )
        }

        canComplete = false
        messageOnComplete = message
    }

    private fun hideProgramStage(hideProgramStage: RuleAction) {
        val stage = hideProgramStage.values["programStage"]
        stage?.let { stagesToHide.add(stage) }
    }

    private fun hideProgramStage(
        programStages: MutableMap<String, ProgramStage>,
        hideProgramStage: RuleAction,
    ) {
        val stage = hideProgramStage.values["programStage"]
        stage?.let { programStages.remove(stage) }
    }

    private fun hideOption(hideOption: RuleAction) {
        val fieldUid = hideOption.field() ?: ""
        val option = hideOption.values["option"]
        if (!optionsToHide.containsKey(hideOption.field())) {
            optionsToHide[fieldUid] = mutableListOf()
        }
        option?.let {
            optionsToHide[hideOption.field()]?.add(option)
            valueStore?.let {
                if (it
                        .deleteOptionValueIfSelected(
                            fieldUid,
                            option,
                        ).valueStoreResult == ValueStoreResult.VALUE_CHANGED
                ) {
                    fieldsToUpdate.add(FieldWithNewValue(fieldUid, null))
                }
            }
        }
    }

    private fun hideOptionGroup(hideOptionGroup: RuleAction) {
        val fieldUid = hideOptionGroup.field() ?: ""
        val optionGroup = hideOptionGroup.values["optionGroup"]
        if (!optionGroupsToHide.containsKey(hideOptionGroup.field())) {
            optionGroupsToHide[fieldUid] = mutableListOf()
        }
        optionGroup?.let {
            optionGroupsToHide[hideOptionGroup.field()]?.add(optionGroup)

            valueStore?.let {
                if (it
                        .deleteOptionValueIfSelectedInGroup(
                            fieldUid,
                            optionGroup,
                            true,
                        ).valueStoreResult == ValueStoreResult.VALUE_CHANGED
                ) {
                    fieldsToUpdate.add(FieldWithNewValue(fieldUid, null))
                }
            }
        }
    }

    private fun showOptionGroup(showOptionGroup: RuleAction) {
        val fieldUid: String = showOptionGroup.field() ?: ""
        val optionGroupUid = showOptionGroup.values["optionGroup"]

        if (!optionGroupsToHide.containsKey(fieldUid) ||
            optionGroupUid != null &&
            optionGroupsToHide[fieldUid]?.contains(optionGroupUid) == false
        ) {
            if (optionGroupsToShow[fieldUid] == null) {
                optionGroupsToShow[fieldUid] = mutableListOf(optionGroupUid!!)
            } else {
                optionGroupsToShow[fieldUid]?.add(optionGroupUid!!)
            }
        }
        if (optionGroupUid != null &&
            valueStore
                ?.deleteOptionValueIfSelectedInGroup(
                    fieldUid,
                    optionGroupUid,
                    false,
                )?.valueStoreResult == ValueStoreResult.VALUE_CHANGED
        ) {
            fieldsToUpdate.add(FieldWithNewValue(fieldUid, null))
        }
    }
}
