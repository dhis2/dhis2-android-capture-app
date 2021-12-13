package org.dhis2.utils

import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.fields.optionset.OptionSetViewModel
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel
import org.dhis2.data.forms.dataentry.fields.visualOptionSet.MatrixOptionSetModel
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.ValueStoreResult
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.models.RuleActionAssign
import org.hisp.dhis.rules.models.RuleActionCreateEvent
import org.hisp.dhis.rules.models.RuleActionDisplayKeyValuePair
import org.hisp.dhis.rules.models.RuleActionDisplayText
import org.hisp.dhis.rules.models.RuleActionErrorOnCompletion
import org.hisp.dhis.rules.models.RuleActionHideField
import org.hisp.dhis.rules.models.RuleActionHideOption
import org.hisp.dhis.rules.models.RuleActionHideOptionGroup
import org.hisp.dhis.rules.models.RuleActionHideProgramStage
import org.hisp.dhis.rules.models.RuleActionHideSection
import org.hisp.dhis.rules.models.RuleActionSetMandatoryField
import org.hisp.dhis.rules.models.RuleActionShowError
import org.hisp.dhis.rules.models.RuleActionShowOptionGroup
import org.hisp.dhis.rules.models.RuleActionShowWarning
import org.hisp.dhis.rules.models.RuleActionWarningOnCompletion
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber

class RulesUtilsProviderImpl(val d2: D2) : RulesUtilsProvider {

    var applyForEvent = false
    var canComplete = true
    var messageOnComplete: String? = null
    val fieldsWithErrors = mutableListOf<FieldWithError>()
    val unsupportedRuleActions = mutableListOf<String>()
    val optionsToHide = mutableMapOf<String, MutableList<String>>()
    val optionGroupsToHide = mutableMapOf<String, MutableList<String>>()
    val optionGroupsToShow = mutableMapOf<String, MutableList<String>>()
    var fieldsToUpdate = mutableListOf<String>()
    val configurationErrors = mutableListOf<RulesUtilsProviderConfigurationError>()
    var valueStore: ValueStore? = null
    var currentRuleUid: String? = null
    val stagesToHide = mutableListOf<String>()

    override fun applyRuleEffects(
        applyForEvent: Boolean,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        calcResult: Result<RuleEffect>,
        valueStore: ValueStore?
    ): RuleUtilsProviderResult {
        this.applyForEvent = applyForEvent
        canComplete = true
        messageOnComplete = null
        fieldsWithErrors.clear()
        unsupportedRuleActions.clear()
        optionsToHide.clear()
        optionGroupsToHide.clear()
        optionGroupsToShow.clear()
        fieldsToUpdate.clear()
        configurationErrors.clear()
        this.valueStore = valueStore

        calcResult.items().forEach {
            currentRuleUid = it.ruleId()
            when (it.ruleAction()) {
                is RuleActionShowWarning -> showWarning(
                    it.ruleAction() as RuleActionShowWarning,
                    fieldViewModels,
                    it.data() ?: ""
                )
                is RuleActionShowError -> showError(
                    it.ruleAction() as RuleActionShowError,
                    fieldViewModels,
                    it.data() ?: ""
                )
                is RuleActionHideField -> hideField(
                    it.ruleAction() as RuleActionHideField,
                    fieldViewModels
                )
                is RuleActionDisplayText -> displayText(
                    it.ruleAction() as RuleActionDisplayText,
                    it,
                    fieldViewModels
                )
                is RuleActionDisplayKeyValuePair -> displayKeyValuePair(
                    it.ruleAction() as RuleActionDisplayKeyValuePair,
                    it,
                    fieldViewModels
                )
                is RuleActionHideSection -> hideSection(
                    fieldViewModels,
                    it.ruleAction() as RuleActionHideSection
                )
                is RuleActionAssign -> assign(
                    it.ruleAction() as RuleActionAssign,
                    it,
                    fieldViewModels
                )
                is RuleActionCreateEvent -> createEvent(
                    it.ruleAction() as RuleActionCreateEvent,
                    fieldViewModels
                )
                is RuleActionSetMandatoryField -> setMandatory(
                    it.ruleAction() as RuleActionSetMandatoryField,
                    fieldViewModels
                )
                is RuleActionWarningOnCompletion -> warningOnCompletion(
                    it.ruleAction() as RuleActionWarningOnCompletion,
                    fieldViewModels,
                    it.data() ?: ""
                )
                is RuleActionErrorOnCompletion -> errorOnCompletion(
                    it.ruleAction() as RuleActionErrorOnCompletion,
                    fieldViewModels,
                    it.data() ?: ""
                )
                is RuleActionHideProgramStage -> hideProgramStage(
                    it.ruleAction() as RuleActionHideProgramStage
                )
                is RuleActionHideOption -> hideOption(
                    it.ruleAction() as RuleActionHideOption
                )
                is RuleActionHideOptionGroup -> hideOptionGroup(
                    it.ruleAction() as RuleActionHideOptionGroup
                )
                is RuleActionShowOptionGroup -> showOptionGroup(
                    it.ruleAction() as RuleActionShowOptionGroup
                )
                else -> it.ruleId()?.let { ruleUid -> unsupportedRuleActions.add(ruleUid) }
            }
        }

        currentRuleUid = null
        setOptionsInFields(fieldViewModels)

        return RuleUtilsProviderResult(
            canComplete = canComplete,
            messageOnComplete = messageOnComplete,
            fieldsWithErrors = fieldsWithErrors,
            unsupportedRules = unsupportedRuleActions,
            fieldsToUpdate = fieldsToUpdate,
            configurationErrors = configurationErrors,
            stagesToHide = stagesToHide
        )
    }

    override fun applyRuleEffects(
        programStages: MutableMap<String, ProgramStage>,
        calcResult: Result<RuleEffect>
    ) {
        calcResult.items().filter { it.ruleAction() is RuleActionHideProgramStage }.forEach {
            hideProgramStage(programStages, it.ruleAction() as RuleActionHideProgramStage)
        }
    }

    private fun setOptionsInFields(
        fieldViewModels: MutableMap<String, FieldUiModel>
    ) {
        fieldViewModels.forEach {
            val fieldUid = it.key
            val optionsToHide = optionsToHide[fieldUid] ?: mutableListOf()
            val optionsInGroupsToHide = optionsFromGroups(
                optionGroupsToHide[fieldUid] ?: mutableListOf()
            )
            val optionsInGroupsToShow = optionsFromGroups(
                optionGroupsToShow[fieldUid] ?: mutableListOf()
            )
            when (val fieldViewModel = it.value) {
                is MatrixOptionSetModel -> {
                    val hiddenMatrixModel = fieldViewModel.setOptionsToHide(
                        optionsToHide,
                        optionsInGroupsToHide,
                        optionsInGroupsToShow
                    )
                    fieldViewModels[fieldUid] = hiddenMatrixModel
                }
                is SpinnerViewModel -> {
                    var mappedSpinnerModel = fieldViewModel.setOptionsToHide(
                        optionsToHide,
                        optionsInGroupsToHide
                    ) as SpinnerViewModel
                    mappedSpinnerModel = mappedSpinnerModel.setOptionGroupsToShow(
                        optionsInGroupsToShow
                    ) as SpinnerViewModel
                    fieldViewModels[fieldUid] = mappedSpinnerModel
                }
                is OptionSetViewModel -> {
                    var mappedOptionSetModel = fieldViewModel.setOptionsToHide(
                        listOf(
                            optionsToHide,
                            optionsInGroupsToHide
                        ).flatten()
                    ) as OptionSetViewModel
                    mappedOptionSetModel =
                        mappedOptionSetModel.setOptionsToShow(optionsInGroupsToShow)
                    fieldViewModels[fieldUid] = mappedOptionSetModel
                }
                else -> {
                }
            }
        }
    }

    private fun save(uid: String, value: String?): ValueStoreResult {
        return if (applyForEvent) {
            saveForEvent(uid, value)
        } else {
            saveForEnrollment(uid, value)
        }
    }

    private fun saveForEvent(uid: String, value: String?): ValueStoreResult {
        return valueStore?.saveWithTypeCheck(uid, value)?.blockingFirst()?.valueStoreResult
            ?: ValueStoreResult.VALUE_HAS_NOT_CHANGED
    }

    private fun saveForEnrollment(uid: String, value: String?): ValueStoreResult {
        try {
            if (d2.dataElementModule().dataElements().uid(uid).blockingExists()) {
                Timber.d("Enrollments rules should not assign values to dataElements")
                return ValueStoreResult.VALUE_HAS_NOT_CHANGED
            } else if (
                d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingExists()
            ) {
                return valueStore?.save(uid, value)?.blockingFirst()?.valueStoreResult
                    ?: ValueStoreResult.VALUE_HAS_NOT_CHANGED
            }
        } catch (d2Error: D2Error) {
            Timber.e(d2Error.originalException())
            return ValueStoreResult.VALUE_HAS_NOT_CHANGED
        }
        return ValueStoreResult.VALUE_HAS_NOT_CHANGED
    }

    private fun showWarning(
        showWarning: RuleActionShowWarning,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        data: String
    ) {
        val model = fieldViewModels[showWarning.field()]
        if (model != null) {
            fieldViewModels[showWarning.field()] =
                model.setWarning(showWarning.content() + " " + data)
        }
    }

    private fun showError(
        showError: RuleActionShowError,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        effectData: String
    ) {
        val model = fieldViewModels[showError.field()]
        val errorMessage = "${showError.content()} $effectData"
        if (model != null) {
            fieldViewModels[showError.field()] = model.setError(errorMessage)
            canComplete = false
            fieldsWithErrors.add(
                FieldWithError(showError.field(), errorMessage)
            )
            valueStore?.saveWithTypeCheck(showError.field(), null)?.blockingFirst()
        }
    }

    private fun hideField(
        hideField: RuleActionHideField,
        fieldViewModels: MutableMap<String, FieldUiModel>
    ) {
        if (fieldViewModels[hideField.field()]?.mandatory != true) {
            fieldViewModels.remove(hideField.field())
            if (save(hideField.field(), null) == ValueStoreResult.VALUE_CHANGED) {
                fieldsToUpdate.add(hideField.field())
            }
        }
    }

    private fun displayText(
        displayText: RuleActionDisplayText,
        ruleEffect: RuleEffect,
        fieldViewModels: MutableMap<String, FieldUiModel>
    ) {
    }

    private fun displayKeyValuePair(
        displayKeyValuePair: RuleActionDisplayKeyValuePair,
        ruleEffect: RuleEffect,
        fieldViewModels: MutableMap<String, FieldUiModel>
    ) {
    }

    private fun hideSection(
        fieldViewModels: MutableMap<String, FieldUiModel>,
        hideSection: RuleActionHideSection
    ) {
        fieldViewModels.filter {
            it.value.programStageSection == hideSection.programStageSection() &&
                !it.value.mandatory
        }.keys.forEach { fieldViewModels.remove(it) }
    }

    private fun assign(
        assign: RuleActionAssign,
        ruleEffect: RuleEffect,
        fieldViewModels: MutableMap<String, FieldUiModel>
    ) {
        if (fieldViewModels[assign.field()] != null) {
            val field = fieldViewModels[assign.field()]!!

            val value =
                if (field.getOptionSet() != null && field.value != null) {
                    val valueOption =
                        d2.optionModule().options().byOptionSetUid().eq(field.getOptionSet())
                            .byDisplayName().eq(field.value)
                            .one().blockingGet()
                    if (valueOption == null) {
                        configurationErrors.add(
                            RulesUtilsProviderConfigurationError(
                                currentRuleUid,
                                ActionType.ASSIGN,
                                ConfigurationError.CURRENT_VALUE_NOT_IN_OPTION_SET,
                                listOf(field.label, field.getOptionSet() ?: "")
                            )
                        )
                    }
                    valueOption?.code()
                } else {
                    field.value
                }

            if ((value == null || value != ruleEffect.data()) && save(
                assign.field(),
                ruleEffect.data()
            ) == ValueStoreResult.VALUE_CHANGED
            ) {
                fieldsToUpdate.add(assign.field())
            }
            val valueToShow =
                if (field.getOptionSet() != null && ruleEffect.data()?.isNotEmpty() == true) {
                    val effectOption =
                        d2.optionModule().options().byOptionSetUid().eq(field.getOptionSet())
                            .byCode().eq(ruleEffect.data())
                            .one().blockingGet()
                    if (effectOption == null) {
                        configurationErrors.add(
                            RulesUtilsProviderConfigurationError(
                                currentRuleUid,
                                ActionType.ASSIGN,
                                ConfigurationError.VALUE_TO_ASSIGN_NOT_IN_OPTION_SET,
                                listOf(
                                    currentRuleUid ?: "",
                                    ruleEffect.data() ?: "",
                                    field.getOptionSet() ?: ""
                                )
                            )
                        )
                    }
                    effectOption?.displayName()
                } else {
                    ruleEffect.data()
                }

            fieldViewModels[assign.field()] =
                fieldViewModels[assign.field()]!!
                    .setValue(valueToShow)
                    .setEditable(false)
        } else {
            save(assign.field(), ruleEffect.data())
        }
    }

    private fun createEvent(
        createEvent: RuleActionCreateEvent,
        fieldViewModels: MutableMap<String, FieldUiModel>
    ) {
        // TODO: Create Event
    }

    private fun setMandatory(
        mandatoryField: RuleActionSetMandatoryField,
        fieldViewModels: MutableMap<String, FieldUiModel>
    ) {
        val model = fieldViewModels[mandatoryField.field()]
        if (model != null) {
            fieldViewModels[mandatoryField.field()] = model.setFieldMandatory()
        } else {
            fieldViewModels.filterKeys {
                it.startsWith(mandatoryField.field())
            }.forEach { (key, value) ->
                fieldViewModels[key] = value.setFieldMandatory()
            }
        }
    }

    private fun warningOnCompletion(
        warningOnCompletion: RuleActionWarningOnCompletion,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        data: String
    ) {
        val model = fieldViewModels[warningOnCompletion.field()]
        val message = warningOnCompletion.content() + " " + data
        if (model != null) {
            fieldViewModels[warningOnCompletion.field()] = model.setWarning(message)
        }

        messageOnComplete = message
    }

    private fun errorOnCompletion(
        errorOnCompletion: RuleActionErrorOnCompletion,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        data: String
    ) {
        val model = fieldViewModels[errorOnCompletion.field()]
        val message = errorOnCompletion.content() + " " + data
        if (model != null) {
            fieldViewModels[errorOnCompletion.field()] = model.setError(message)
            fieldsWithErrors.add(
                FieldWithError(errorOnCompletion.field(), message)
            )
        }

        canComplete = false
        messageOnComplete = message
    }

    private fun hideProgramStage(
        hideProgramStage: RuleActionHideProgramStage
    ) {
        stagesToHide.add(hideProgramStage.programStage())
    }

    private fun hideProgramStage(
        programStages: MutableMap<String, ProgramStage>,
        hideProgramStage: RuleActionHideProgramStage
    ) {
        programStages.remove(hideProgramStage.programStage())
    }

    private fun hideOption(
        hideOption: RuleActionHideOption
    ) {
        if (!optionsToHide.containsKey(hideOption.field())) {
            optionsToHide[hideOption.field()] = mutableListOf()
        }
        optionsToHide[hideOption.field()]?.add(hideOption.option())

        valueStore?.let {
            if (it.deleteOptionValueIfSelected(
                hideOption.field(),
                hideOption.option()
            ).valueStoreResult == ValueStoreResult.VALUE_CHANGED
            ) {
                fieldsToUpdate.add(hideOption.field())
            }
        }
    }

    private fun hideOptionGroup(
        hideOptionGroup: RuleActionHideOptionGroup
    ) {
        if (!optionGroupsToHide.containsKey(hideOptionGroup.field())) {
            optionGroupsToHide[hideOptionGroup.field()] = mutableListOf()
        }
        optionGroupsToHide[hideOptionGroup.field()]?.add(hideOptionGroup.optionGroup())

        valueStore?.let {
            if (it.deleteOptionValueIfSelectedInGroup(
                hideOptionGroup.field(),
                hideOptionGroup.optionGroup(),
                true
            ).valueStoreResult == ValueStoreResult.VALUE_CHANGED
            ) {
                fieldsToUpdate.add(hideOptionGroup.field())
            }
        }
    }

    private fun showOptionGroup(
        showOptionGroup: RuleActionShowOptionGroup
    ) {
        val fieldUid: String = showOptionGroup.field()
        val optionGroupUid: String = showOptionGroup.optionGroup()

        if (!optionGroupsToHide.containsKey(fieldUid) ||
            optionGroupsToHide[fieldUid]?.contains(optionGroupUid) == false
        ) {
            if (optionGroupsToShow[fieldUid] == null) {
                optionGroupsToShow[fieldUid] = mutableListOf(optionGroupUid)
            } else {
                optionGroupsToShow[fieldUid]!!.add(optionGroupUid)
            }
        }
        if (valueStore?.deleteOptionValueIfSelectedInGroup(
            fieldUid,
            optionGroupUid,
            false
        )?.valueStoreResult == ValueStoreResult.VALUE_CHANGED
        ) {
            fieldsToUpdate.add(fieldUid)
        }
    }

    fun optionsFromGroups(optionGroupUids: List<String>): List<String> {
        if (optionGroupUids.isEmpty()) return emptyList()
        val optionsFromGroups = arrayListOf<String>()
        val optionGroups = d2.optionModule().optionGroups()
            .withOptions()
            .byUid().`in`(optionGroupUids)
            .blockingGet()
        for (optionGroup in optionGroups) {
            for (option in optionGroup.options()!!) {
                if (!optionsFromGroups.contains(option.uid())) {
                    optionsFromGroups.add(option.uid())
                }
            }
        }
        return optionsFromGroups
    }
}
