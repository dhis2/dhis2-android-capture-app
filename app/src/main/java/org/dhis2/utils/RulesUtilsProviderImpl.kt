package org.dhis2.utils

import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel
import org.dhis2.utils.rules.RuleEffectResult
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
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

/**
 * QUADRAM. Created by ppajuelo on 13/06/2018.
 */

class RulesUtilsProviderImpl(private val codeGenerator: CodeGenerator) : RulesUtilsProvider {

    private var currentFieldViewModels: HashMap<String, FieldViewModel>? = null

    override fun applyRuleEffects(
        fieldViewModels: MutableMap<String, FieldViewModel>,
        calcResult: Result<RuleEffect>,
        rulesActionCallbacks: RulesActionCallbacks
    ) {
        calcResult.items().forEach {
            when (it.ruleAction()) {
                is RuleActionShowWarning -> showWarning(
                    it.ruleAction() as RuleActionShowWarning,
                    fieldViewModels,
                    it.data()
                )
                is RuleActionShowError -> showError(
                    it.ruleAction() as RuleActionShowError,
                    fieldViewModels,
                    rulesActionCallbacks
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
                    it.ruleAction() as RuleActionHideSection,
                    fieldViewModels,
                    rulesActionCallbacks
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
                    it.data()
                )
                is RuleActionErrorOnCompletion -> errorOnCompletion(
                    it.ruleAction() as RuleActionErrorOnCompletion,
                    rulesActionCallbacks,
                    fieldViewModels,
                    it.data()
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
        fieldViewModels: MutableMap<String, FieldViewModel>,
        data: String
    ) {
        val model = fieldViewModels[showWarning.field()]
        if (model != null) {
            fieldViewModels[showWarning.field()] = model.withWarning(showWarning.content() + data)
        }
    }

    private fun showError(
        showError: RuleActionShowError,
        fieldViewModels: MutableMap<String, FieldViewModel>,
        rulesActionCallbacks: RulesActionCallbacks
    ) {
        val model = fieldViewModels[showError.field()]

        if (model != null) {
            fieldViewModels[showError.field()] = model.withError(showError.content())
        }

        rulesActionCallbacks.setShowError(showError, model)
    }

    private fun hideField(
        hideField: RuleActionHideField,
        fieldViewModels: MutableMap<String, FieldViewModel>,
        rulesActionCallbacks: RulesActionCallbacks
    ) {
        fieldViewModels.remove(hideField.field())
        rulesActionCallbacks.save(hideField.field(), null)
    }

    private fun displayText(
        displayText: RuleActionDisplayText,
        ruleEffect: RuleEffect,
        fieldViewModels: MutableMap<String, FieldViewModel>
    ) {
        val uid = displayText.content()

        val displayViewModel = DisplayViewModel.create(
            uid, "",
            displayText.content() + ruleEffect.data(), "Display"
        )
        fieldViewModels[uid] = displayViewModel
    }

    private fun displayKeyValuePair(
        displayKeyValuePair: RuleActionDisplayKeyValuePair,
        ruleEffect: RuleEffect,
        fieldViewModels: MutableMap<String, FieldViewModel>,
        rulesActionCallbacks: RulesActionCallbacks
    ) {
        val uid = displayKeyValuePair.content()

        val displayViewModel = DisplayViewModel.create(
            uid, displayKeyValuePair.content(),
            ruleEffect.data(), "Display"
        )
        fieldViewModels[uid] = displayViewModel
        rulesActionCallbacks.setDisplayKeyValue(displayKeyValuePair.content(), ruleEffect.data())
    }

    private fun hideSection(
        hideSection: RuleActionHideSection,
        fieldViewModels: MutableMap<String, FieldViewModel>,
        rulesActionCallbacks: RulesActionCallbacks
    ) {
        rulesActionCallbacks.setHideSection(hideSection.programStageSection())
        for (field in fieldViewModels.values) {
            if (field.programStageSection() == hideSection.programStageSection() &&
                field.value() != null
            ) {
                val uid =
                    if (field.uid().contains(".")) {
                        field.uid()
                            .split("\\.".toRegex())
                            .dropLastWhile { it.isEmpty() }
                            .toTypedArray()[0]
                    } else {
                        field.uid()
                    }
                rulesActionCallbacks.save(uid, null)
            }
        }
    }

    private fun assign(
        assign: RuleActionAssign,
        ruleEffect: RuleEffect,
        fieldViewModels: MutableMap<String, FieldViewModel>,
        rulesActionCallbacks: RulesActionCallbacks
    ) {
        if (fieldViewModels[assign.field()] == null) {
            rulesActionCallbacks.setCalculatedValue(assign.content(), ruleEffect.data())
        } else {
            val value = fieldViewModels[assign.field()]!!.value()

            if (value == null || value != ruleEffect.data()) {
                rulesActionCallbacks.save(assign.field(), ruleEffect.data())
            }

            fieldViewModels.put(
                assign.field(),
                fieldViewModels[assign.field()]!!.withValue(ruleEffect.data())
            )!!.withEditMode(false)
        }
    }

    private fun createEvent(
        createEvent: RuleActionCreateEvent,
        fieldViewModels: MutableMap<String, FieldViewModel>,
        rulesActionCallbacks: RulesActionCallbacks
    ) {
        // TODO: Create Event
    }

    private fun setMandatory(
        mandatoryField: RuleActionSetMandatoryField,
        fieldViewModels: MutableMap<String, FieldViewModel>
    ) {
        val model = fieldViewModels[mandatoryField.field()]
        if (model != null) {
            fieldViewModels[mandatoryField.field()] = model.setMandatory()
        }
    }

    private fun warningOnCompletion(
        warningOnCompletion: RuleActionWarningOnCompletion,
        rulesActionCallbacks: RulesActionCallbacks,
        fieldViewModels: MutableMap<String, FieldViewModel>,
        data: String
    ) {
        val model = fieldViewModels[warningOnCompletion.field()]
        if (model != null) {
            fieldViewModels[warningOnCompletion.field()] =
                model.withWarning(warningOnCompletion.content() + data)
        }

        rulesActionCallbacks.setMessageOnComplete(warningOnCompletion.content(), true)
    }

    private fun errorOnCompletion(
        errorOnCompletion: RuleActionErrorOnCompletion,
        rulesActionCallbacks: RulesActionCallbacks,
        fieldViewModels: MutableMap<String, FieldViewModel>,
        data: String
    ) {
        val model = fieldViewModels[errorOnCompletion.field()]
        if (model != null) {
            fieldViewModels[errorOnCompletion.field()] =
                model.withWarning(errorOnCompletion.content() + data)
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
        rulesActionCallbacks.setOptionToHide(hideOption.option())
    }

    private fun hideOptionGroup(
        hideOptionGroup: RuleActionHideOptionGroup,
        rulesActionCallbacks: RulesActionCallbacks
    ) {
        rulesActionCallbacks.setOptionGroupToHide(hideOptionGroup.optionGroup(), true)
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

    /*region NEW METHOD*/

    var eventUid: String? = null
    var teiUid: String? = null

    /**
     *
     * @param fields List of all uids
     * @param calcResult All rule effects to apply for the given fields
     * */
    override fun applyRuleEffects(
        fields: List<String>,
        calcResult: Result<RuleEffect>
    ): RuleEffectResult {
        val ruleEffectResult = RuleEffectResult()

        calcResult.items().forEach {
            when (it.ruleAction()) {
                is RuleActionShowWarning -> setShowWarning(
                    it.ruleAction() as RuleActionShowWarning,
                    it.data(),
                    ruleEffectResult.warnings
                )
                is RuleActionShowError -> setShowError(
                    it.ruleAction() as RuleActionShowError,
                    it.data(),
                    ruleEffectResult.errors
                )
                is RuleActionHideField -> setHideField(
                    it.ruleAction() as RuleActionHideField,
                    fields
                )
                is RuleActionDisplayText -> setDisplayText(
                    it.ruleAction() as RuleActionDisplayText,
                    it.data(),
                    ruleEffectResult.displayTextList
                )
                is RuleActionDisplayKeyValuePair -> setDisplayKeyValue(
                    it.ruleAction() as RuleActionDisplayKeyValuePair,
                    it.data(),
                    ruleEffectResult.displayKeyValue
                )
                is RuleActionHideSection -> setHideSection(
                    it.ruleAction() as RuleActionHideSection,
                    fields,
                    ruleEffectResult.sectionsToHide
                )
                is RuleActionAssign -> setAssign(it.ruleAction() as RuleActionAssign)
                is RuleActionSetMandatoryField -> setMandatoryField(
                    it.ruleAction() as RuleActionSetMandatoryField,
                    ruleEffectResult.mandatoryFields
                )
                is RuleActionWarningOnCompletion -> setWarningOnCompletion(
                    it.ruleAction() as RuleActionWarningOnCompletion,
                    it.data(),
                    ruleEffectResult.warningOnCompletions
                )
                is RuleActionErrorOnCompletion -> setErrorOnCompletion(
                    it.ruleAction() as RuleActionErrorOnCompletion,
                    it.data(),
                    ruleEffectResult.errorOnCompletions
                )
                is RuleActionHideProgramStage -> {
                    ruleEffectResult
                        .stagesToHide
                        .add((it.ruleAction() as RuleActionHideProgramStage).programStage())
                }
                is RuleActionHideOption -> {
                    ruleEffectResult
                        .optionsToHide
                        .add((it.ruleAction() as RuleActionHideOption).option())
                }
                is RuleActionHideOptionGroup -> {
                    ruleEffectResult
                        .optionGroupsToHide
                        .add((it.ruleAction() as RuleActionHideOptionGroup).optionGroup())
                }
                is RuleActionShowOptionGroup -> {
                    ruleEffectResult.showOptionGroup
                        .add((it.ruleAction() as RuleActionShowOptionGroup).optionGroup())
                }
                else -> ruleEffectResult.unsupportedRules.add("unsupported")
            }
        }
        return ruleEffectResult
    }

    private fun setShowWarning(
        action: RuleActionShowWarning,
        data: String,
        warnings: HashMap<String, String>
    ) {
        warnings[action.field()] = action.content() + " " + data
    }

    private fun setShowError(
        action: RuleActionShowError,
        data: String,
        errors: HashMap<String, String>
    ) {
        errors[action.field()] = action.content() + " " + data
    }

    private fun setHideField(
        action: RuleActionHideField,
        fields: List<String>
    ) { // TODO: CHECK IF ACTION FIELD IS DE OR ATTR
        (fields as ArrayList).remove(action.field())

        if (eventUid != null && D2Manager.getD2().trackedEntityModule().trackedEntityDataValues()
            .value(eventUid, action.field()).blockingExists()
        ) {
            D2Manager.getD2().trackedEntityModule().trackedEntityDataValues()
                .value(eventUid, action.field()).blockingDelete()
        } else if (teiUid != null &&
            D2Manager.getD2().trackedEntityModule().trackedEntityAttributeValues()
                .value(action.field(), teiUid).blockingExists()
        ) {
            D2Manager.getD2().trackedEntityModule().trackedEntityAttributeValues()
                .value(action.field(), teiUid).blockingDelete()
        }
    }

    private fun setDisplayText(
        action: RuleActionDisplayText,
        data: String,
        displayTextList: ArrayList<String>
    ) {
        displayTextList.add(action.content() + " " + data)
    }

    private fun setDisplayKeyValue(
        action: RuleActionDisplayKeyValuePair,
        data: String,
        displayKeyValues: HashMap<String, String>
    ) {
        displayKeyValues[action.content()] = data
    }

    private fun setHideSection(
        action: RuleActionHideSection,
        fields: List<String>,
        sectionsToHide: ArrayList<String>
    ) {
        sectionsToHide.add(action.programStageSection())
        val sectionDataElements = UidsHelper.getUidsList(
            D2Manager
                .getD2()
                .programModule()
                .programStageSections().uid(action.programStageSection())
                .blockingGet().dataElements()
        )
        sectionDataElements.forEach {
            if (fields.contains(it)) {
                (fields as ArrayList).remove(it)
                if (eventUid != null &&
                    D2Manager.getD2().trackedEntityModule().trackedEntityDataValues()
                        .value(eventUid, it).blockingExists()
                ) {
                    D2Manager.getD2().trackedEntityModule().trackedEntityDataValues()
                        .value(eventUid, it).blockingDelete()
                } else if (teiUid != null &&
                    D2Manager.getD2().trackedEntityModule().trackedEntityAttributeValues()
                        .value(it, teiUid).blockingExists()
                ) {
                    D2Manager.getD2().trackedEntityModule().trackedEntityAttributeValues()
                        .value(it, teiUid).blockingDelete()
                }
            }
        }
    }

    private fun setAssign(action: RuleActionAssign) { // TODO: CHECK IF ACTION FIELD IS DE OR ATTR
        if (eventUid != null) {
            D2Manager.getD2().trackedEntityModule().trackedEntityDataValues()
                .value(eventUid, action.field()).blockingSet(action.content())
        } else if (teiUid != null) {
            D2Manager.getD2().trackedEntityModule().trackedEntityAttributeValues()
                .value(action.field(), teiUid).blockingSet(action.content())
        }
    }

    private fun setMandatoryField(
        action: RuleActionSetMandatoryField,
        mandatoryFields: ArrayList<String>
    ) {
        mandatoryFields.add(action.field())
    }

    private fun setWarningOnCompletion(
        action: RuleActionWarningOnCompletion,
        data: String,
        warningOnCompletions: HashMap<String, String>
    ) {
        warningOnCompletions[action.field()] = action.content() + " " + data
    }

    private fun setErrorOnCompletion(
        action: RuleActionErrorOnCompletion,
        data: String,
        errorOnCompletions: HashMap<String, String>
    ) {
        errorOnCompletions[action.field()] = action.content() + " " + data
    }

    /*endregion*/
}
