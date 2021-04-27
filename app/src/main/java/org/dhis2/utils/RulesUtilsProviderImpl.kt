package org.dhis2.utils

import org.dhis2.form.model.FieldUiModel
import org.hisp.dhis.android.core.D2
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

class RulesUtilsProviderImpl(val d2: D2) : RulesUtilsProvider {

    private var currentFieldViewModels: HashMap<String, FieldUiModel>? = null

    override fun applyRuleEffects(
        fieldViewModels: MutableMap<String, FieldUiModel>,
        calcResult: Result<RuleEffect>,
        rulesActionCallbacks: RulesActionCallbacks
    ) {
        calcResult.items().forEach {
            when (it.ruleAction()) {
                is RuleActionShowWarning -> showWarning(
                    it.ruleAction() as RuleActionShowWarning,
                    fieldViewModels,
                    it.data() ?: ""
                )
                is RuleActionShowError -> showError(
                    it.ruleAction() as RuleActionShowError,
                    fieldViewModels,
                    rulesActionCallbacks,
                    it.data() ?: ""
                )
                is RuleActionHideField -> hideField(
                    it.ruleAction() as RuleActionHideField,
                    fieldViewModels,
                    rulesActionCallbacks
                )
                is RuleActionDisplayText -> displayText(
                    it.ruleAction() as RuleActionDisplayText,
                    it,
                    fieldViewModels
                )
                is RuleActionDisplayKeyValuePair -> displayKeyValuePair(
                    it.ruleAction() as RuleActionDisplayKeyValuePair,
                    it,
                    fieldViewModels,
                    rulesActionCallbacks
                )
                is RuleActionHideSection -> hideSection(
                    fieldViewModels,
                    it.ruleAction() as RuleActionHideSection
                )
                is RuleActionAssign -> assign(
                    it.ruleAction() as RuleActionAssign,
                    it,
                    fieldViewModels,
                    rulesActionCallbacks
                )
                is RuleActionCreateEvent -> createEvent(
                    it.ruleAction() as RuleActionCreateEvent,
                    fieldViewModels,
                    rulesActionCallbacks
                )
                is RuleActionSetMandatoryField -> setMandatory(
                    it.ruleAction() as RuleActionSetMandatoryField,
                    fieldViewModels
                )
                is RuleActionWarningOnCompletion -> warningOnCompletion(
                    it.ruleAction() as RuleActionWarningOnCompletion,
                    rulesActionCallbacks,
                    fieldViewModels,
                    it.data() ?: ""
                )
                is RuleActionErrorOnCompletion -> errorOnCompletion(
                    it.ruleAction() as RuleActionErrorOnCompletion,
                    rulesActionCallbacks,
                    fieldViewModels,
                    it.data() ?: ""
                )
                is RuleActionHideProgramStage -> hideProgramStage(
                    it.ruleAction() as RuleActionHideProgramStage,
                    rulesActionCallbacks
                )
                is RuleActionHideOption -> hideOption(
                    it.ruleAction() as RuleActionHideOption,
                    rulesActionCallbacks
                )
                is RuleActionHideOptionGroup -> hideOptionGroup(
                    it.ruleAction() as RuleActionHideOptionGroup,
                    rulesActionCallbacks
                )
                is RuleActionShowOptionGroup -> showOptionGroup(
                    it.ruleAction() as RuleActionShowOptionGroup,
                    rulesActionCallbacks
                )
                else -> rulesActionCallbacks.unsupportedRuleAction()
            }
        }

        if (currentFieldViewModels == null) {
            currentFieldViewModels = HashMap()
        }
        currentFieldViewModels!!.clear()
        currentFieldViewModels!!.putAll(fieldViewModels)
    }

    override fun applyRuleEffects(
        programStages: MutableMap<String, ProgramStage>,
        calcResult: Result<RuleEffect>
    ) {
        calcResult.items().filter { it.ruleAction() is RuleActionHideProgramStage }.forEach {
            hideProgramStage(programStages, it.ruleAction() as RuleActionHideProgramStage)
        }
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
        rulesActionCallbacks: RulesActionCallbacks,
        effectData: String
    ) {
        val model = fieldViewModels[showError.field()]

        if (model != null) {
            fieldViewModels[showError.field()] =
                model.setError("${showError.content()} $effectData")
        }

        rulesActionCallbacks.setShowError(showError, model)
    }

    private fun hideField(
        hideField: RuleActionHideField,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        rulesActionCallbacks: RulesActionCallbacks
    ) {
        if (fieldViewModels[hideField.field()]?.isMandatory() != true) {
            fieldViewModels.remove(hideField.field())
            rulesActionCallbacks.save(hideField.field(), null)
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
        fieldViewModels: MutableMap<String, FieldUiModel>,
        rulesActionCallbacks: RulesActionCallbacks
    ) {
    }

    private fun hideSection(
        fieldViewModels: MutableMap<String, FieldUiModel>,
        hideSection: RuleActionHideSection
    ) {
        fieldViewModels.filter {
            it.value.getProgramStageSection() == hideSection.programStageSection() &&
                !it.value.isMandatory()
        }.keys.forEach { fieldViewModels.remove(it) }
    }

    private fun assign(
        assign: RuleActionAssign,
        ruleEffect: RuleEffect,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        rulesActionCallbacks: RulesActionCallbacks
    ) {
        if (fieldViewModels[assign.field()] != null) {
            val field = fieldViewModels[assign.field()]!!

            val value =
                if (field.getOptionSet() != null && field.getValue() != null) {
                    d2.optionModule().options().byOptionSetUid().eq(field.getOptionSet())
                        .byDisplayName().eq(field.getValue())
                        .one().blockingGet().code()
                } else {
                    field.getValue()
                }

            if (value == null || value != ruleEffect.data()) {
                rulesActionCallbacks.save(assign.field(), ruleEffect.data())
            }

            val valueToShow =
                if (field.getOptionSet() != null && ruleEffect.data()?.isNotEmpty() == true) {
                    d2.optionModule().options().byOptionSetUid().eq(field.getOptionSet())
                        .byCode().eq(ruleEffect.data())
                        .one().blockingGet().displayName()
                } else {
                    ruleEffect.data()
                }

            fieldViewModels[assign.field()] =
                fieldViewModels[assign.field()]!!
                    .setValue(valueToShow)
                    .setEditMode(false)
        }
    }

    private fun createEvent(
        createEvent: RuleActionCreateEvent,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        rulesActionCallbacks: RulesActionCallbacks
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
        rulesActionCallbacks: RulesActionCallbacks,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        data: String
    ) {
        val model = fieldViewModels[warningOnCompletion.field()]
        if (model != null) {
            fieldViewModels[warningOnCompletion.field()] =
                model.setWarning(warningOnCompletion.content() + " " + data)
        }

        rulesActionCallbacks.setMessageOnComplete(warningOnCompletion.content(), true)
    }

    private fun errorOnCompletion(
        errorOnCompletion: RuleActionErrorOnCompletion,
        rulesActionCallbacks: RulesActionCallbacks,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        data: String
    ) {
        val model = fieldViewModels[errorOnCompletion.field()]
        if (model != null) {
            fieldViewModels[errorOnCompletion.field()] =
                model.setWarning(errorOnCompletion.content() + " " + data)
        }

        rulesActionCallbacks.setMessageOnComplete(errorOnCompletion.content(), false)
    }

    private fun hideProgramStage(
        hideProgramStage: RuleActionHideProgramStage,
        rulesActionCallbacks: RulesActionCallbacks
    ) {
        rulesActionCallbacks.setHideProgramStage(hideProgramStage.programStage())
    }

    private fun hideProgramStage(
        programStages: MutableMap<String, ProgramStage>,
        hideProgramStage: RuleActionHideProgramStage
    ) {
        programStages.remove(hideProgramStage.programStage())
    }

    private fun hideOption(
        hideOption: RuleActionHideOption,
        rulesActionCallbacks: RulesActionCallbacks
    ) {
        rulesActionCallbacks.setOptionToHide(hideOption.option(), hideOption.field())
    }

    private fun hideOptionGroup(
        hideOptionGroup: RuleActionHideOptionGroup,
        rulesActionCallbacks: RulesActionCallbacks
    ) {
        rulesActionCallbacks.setOptionGroupToHide(
            hideOptionGroup.optionGroup(),
            true,
            hideOptionGroup.field()
        )
    }

    private fun showOptionGroup(
        showOptionGroup: RuleActionShowOptionGroup,
        rulesActionCallbacks: RulesActionCallbacks
    ) {
        rulesActionCallbacks.setOptionGroupToHide(
            showOptionGroup.optionGroup(),
            false,
            showOptionGroup.field()
        )
    }
}
