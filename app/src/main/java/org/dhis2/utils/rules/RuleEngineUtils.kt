package org.dhis2.utils.rules

import android.text.TextUtils.isEmpty
import org.dhis2.data.forms.RuleActionUnsupported
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.*
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.hisp.dhis.android.core.user.UserRole
import org.hisp.dhis.rules.models.*
import java.util.*

class RuleEngineUtils {

    companion object {

        fun translateToRules(programRules: List<ProgramRule>): List<Rule> {
            val rules = ArrayList<Rule>()
            for (programRule in programRules) {
                if (programRule.programRuleActions() != null)
                    rules.add(
                            Rule.create(
                                    if (programRule.programStage() != null) programRule.programStage()!!.uid() else null,
                                    programRule.priority(),
                                    if (programRule.condition() != null) programRule.condition()!! else "",
                                    translateToActions(programRule.programRuleActions()!!),
                                    programRule.name())
                    )
            }
            return rules
        }

        fun translateToRules(programRules: List<ProgramRule>, programStage: String): List<Rule> {
            val rules = translateToRules(programRules)
            return rules.filter {
                it.programStage() == null || it.programStage().equals(programStage)
            }
        }

        fun translateToRuleVariable(programRuleVariables: List<ProgramRuleVariable>, d2: D2): List<RuleVariable> {
            val ruleVariables = ArrayList<RuleVariable>()
            for (programRuleVariable in programRuleVariables) {
                ruleVariables.add(
                        translateToRuleVariable(programRuleVariable, d2)
                )
            }
            return ruleVariables
        }

        fun supplementaryData(d2: D2): Map<String, List<String>> {
            val supData = java.util.HashMap<String, List<String>>()

            //ORG UNIT GROUPS
            d2.organisationUnitModule().organisationUnitGroups.blockingGet().forEach {
                if (it.code() != null)
                    supData[it.code()!!] = ArrayList()
            }

            d2.organisationUnitModule().organisationUnits.withOrganisationUnitGroups().blockingGet().forEach {
                if (it.organisationUnitGroups() != null)
                    it.organisationUnitGroups()!!.forEach { ouGroup ->
                        if (supData[ouGroup.code()] != null && !supData[ouGroup.code()]!!.contains(it.uid()))
                            (supData[ouGroup.code()] as ArrayList<String>).add(it.uid())
                    }
            }

            //USER ROLES
            val userRoleUids = UidsHelper.getUidsList<UserRole>(d2.userModule().userRoles.blockingGet())
            supData["USER"] = userRoleUids

            return supData
        }

        fun translateToRuleEnrollment(enrollment: Enrollment,
                                      attributeValues: List<TrackedEntityAttributeValue>,
                                      d2: D2): RuleEnrollment {
            return RuleEnrollment.create(
                    enrollment.uid()!!,
                    enrollment.incidentDate() ?: Calendar.getInstance().time,
                    enrollment.enrollmentDate() ?: Calendar.getInstance().time,
                    RuleEnrollment.Status.valueOf(enrollment.status()!!.name),
                    enrollment.organisationUnit()!!,
                    d2.organisationUnitModule().organisationUnits.uid(enrollment.organisationUnit()).blockingGet().code(),
                    translateToRuleAttributeValue(attributeValues),
                    d2.programModule().programs().uid(enrollment.program()).blockingGet().name()
            )
        }

        fun translateToRuleEvents(events: List<Event>,
                                  d2: D2): List<RuleEvent> {
            return events.map {
                RuleEvent.create(
                        it.uid()!!,
                        it.programStage()!!,
                        RuleEvent.Status.valueOf(it.status()!!.name),
                        it.eventDate()!!,
                        it.dueDate() ?: it.eventDate()!!,
                        it.organisationUnit()!!,
                        d2.organisationUnitModule().organisationUnits.uid(it.organisationUnit()).blockingGet().code(),
                        translateToRuleDataValue(
                                it,
                                d2.trackedEntityModule().trackedEntityDataValues.byEvent().eq(it.uid()).blockingGet()
                        ),
                        d2.programModule().programStages().uid(it.programStage()).blockingGet().name()!!
                )
            }
        }

        private fun translateToActions(actionList: List<ProgramRuleAction>): List<RuleAction> {
            val ruleActions = ArrayList<RuleAction>()
            for (programRuleAction in actionList) {
                val ruleAction: RuleAction
                val dataElement = if (programRuleAction.dataElement() != null)
                    programRuleAction.dataElement()!!.uid()
                else
                    null
                val attribute = if (programRuleAction.trackedEntityAttribute() != null)
                    programRuleAction.trackedEntityAttribute()!!.uid()
                else
                    null
                var field: String? = dataElement ?: attribute
                if (field == null)
                    field = ""

                ruleAction =
                        when (programRuleAction.programRuleActionType()) {
                            ProgramRuleActionType.HIDEFIELD -> RuleActionHideField.create(programRuleAction.content(), field)
                            ProgramRuleActionType.ASSIGN -> RuleActionAssign.create(programRuleAction.content(),
                                    programRuleAction.data()!!,
                                    field)
                            ProgramRuleActionType.SHOWERROR -> RuleActionShowError.create(programRuleAction.content(),
                                    programRuleAction.data(), field)
                            ProgramRuleActionType.HIDEOPTION -> RuleActionHideOption.create(programRuleAction.content(),
                                    programRuleAction.option()!!.uid(), field)
                            ProgramRuleActionType.CREATEEVENT -> RuleActionCreateEvent.create(programRuleAction.content(),
                                    programRuleAction.data(), programRuleAction.programStage()!!.uid())
                            ProgramRuleActionType.DISPLAYTEXT -> RuleActionDisplayText.createForFeedback(programRuleAction.content(),
                                    programRuleAction.data())
                            ProgramRuleActionType.HIDESECTION -> RuleActionHideSection.create(programRuleAction.programStageSection()!!.uid())
                            ProgramRuleActionType.SHOWWARNING -> RuleActionShowWarning.create(programRuleAction.content(),
                                    programRuleAction.data(), field)
                            ProgramRuleActionType.ERRORONCOMPLETE -> RuleActionErrorOnCompletion.create(programRuleAction.content(),
                                    programRuleAction.data(), field)
                            ProgramRuleActionType.HIDEOPTIONGROUP -> RuleActionHideOptionGroup.create(programRuleAction.content(),
                                    programRuleAction.optionGroup()!!.uid())
                            ProgramRuleActionType.HIDEPROGRAMSTAGE -> RuleActionHideProgramStage.create(programRuleAction.programStage()!!.uid())
                            ProgramRuleActionType.SETMANDATORYFIELD -> RuleActionSetMandatoryField.create(field)
                            ProgramRuleActionType.WARNINGONCOMPLETE -> RuleActionWarningOnCompletion.create(programRuleAction.content(),
                                    programRuleAction.data(), field)
                            ProgramRuleActionType.DISPLAYKEYVALUEPAIR -> RuleActionDisplayKeyValuePair.createForIndicators(programRuleAction.content(),
                                    programRuleAction.data())
                            ProgramRuleActionType.SHOWOPTIONGROUP -> RuleActionShowOptionGroup.create(programRuleAction.content(), programRuleAction.optionGroup()!!.uid(), field)
                            ProgramRuleActionType.SENDMESSAGE, ProgramRuleActionType.SCHEDULEMESSAGE -> {
                                val content = if (programRuleAction.content() != null) programRuleAction.content() else "unsupported"
                                val ruleType = programRuleAction.programRuleActionType()!!.name
                                RuleActionUnsupported.create(content!!, ruleType)
                            }
                            else -> {
                                val content = if (programRuleAction.content() != null) programRuleAction.content() else "unsupported"
                                val ruleType = programRuleAction.programRuleActionType()!!.name
                                RuleActionUnsupported.create(content!!, ruleType)
                            }
                        }
                ruleActions.add(ruleAction)
            }
            return ruleActions
        }

        private fun translateToRuleVariable(programRuleVariable: ProgramRuleVariable, d2: D2): RuleVariable {
            val name = programRuleVariable.name()
            var stage: String? = if (programRuleVariable.programStage() != null) programRuleVariable.programStage()!!.uid() else null
            val sourceType = programRuleVariable.programRuleVariableSourceType()!!.name
            val dataElement = if (programRuleVariable.dataElement() != null) programRuleVariable.dataElement()!!.uid() else null
            val attribute = if (programRuleVariable.trackedEntityAttribute() != null) programRuleVariable.trackedEntityAttribute()!!.uid() else null

            // Mime types of the attribute and data element.
            val attributeType: String? = if (attribute != null) d2.trackedEntityModule().trackedEntityAttributes.uid(attribute).blockingGet()!!.valueType()!!.name else null
            val elementType = if (dataElement != null) d2.dataElementModule().dataElements().uid(dataElement).blockingGet()!!.valueType()!!.name else null

            // String representation of value type.
            var mimeType: RuleValueType?

            mimeType = when (ProgramRuleVariableSourceType.valueOf(sourceType)) {
                ProgramRuleVariableSourceType.TEI_ATTRIBUTE -> {
                    if (!isEmpty(attributeType))
                        convertType(attributeType!!)
                    else RuleValueType.TEXT
                }
                ProgramRuleVariableSourceType.DATAELEMENT_CURRENT_EVENT, ProgramRuleVariableSourceType.DATAELEMENT_PREVIOUS_EVENT, ProgramRuleVariableSourceType.DATAELEMENT_NEWEST_EVENT_PROGRAM, ProgramRuleVariableSourceType.DATAELEMENT_NEWEST_EVENT_PROGRAM_STAGE -> {
                    if (!isEmpty(elementType))
                        convertType(elementType!!)
                    else RuleValueType.TEXT
                }
                else -> RuleValueType.TEXT
            }

            return when (ProgramRuleVariableSourceType.valueOf(sourceType)) {
                ProgramRuleVariableSourceType.TEI_ATTRIBUTE ->
                    RuleVariableAttribute.create(name!!, attribute ?: "", mimeType)
                ProgramRuleVariableSourceType.DATAELEMENT_CURRENT_EVENT ->
                    RuleVariableCurrentEvent.create(name!!, dataElement!!, mimeType)
                ProgramRuleVariableSourceType.DATAELEMENT_NEWEST_EVENT_PROGRAM ->
                    RuleVariableNewestEvent.create(name!!, dataElement!!, mimeType)
                ProgramRuleVariableSourceType.DATAELEMENT_NEWEST_EVENT_PROGRAM_STAGE ->
                    RuleVariableNewestStageEvent.create(name!!, dataElement!!, stage
                            ?: "", mimeType)
                ProgramRuleVariableSourceType.DATAELEMENT_PREVIOUS_EVENT ->
                    RuleVariablePreviousEvent.create(name!!, dataElement!!, mimeType)
                ProgramRuleVariableSourceType.CALCULATED_VALUE ->
                    RuleVariableCalculatedValue.create(name!!, (dataElement ?: attribute)
                            ?: "", mimeType)
                else -> throw IllegalArgumentException("Unsupported variable " +
                        "source type: " + sourceType)
            }
        }

        private fun convertType(type: String): RuleValueType {
            val valueType = ValueType.valueOf(type)
            return if (valueType.isInteger || valueType.isNumeric) {
                RuleValueType.NUMERIC
            } else if (valueType.isBoolean) {
                RuleValueType.BOOLEAN
            } else {
                RuleValueType.TEXT
            }
        }

        private fun translateToRuleAttributeValue(attrValues: List<TrackedEntityAttributeValue>): List<RuleAttributeValue> {
            return attrValues.map {
                RuleAttributeValue.create(it.trackedEntityAttribute()!!, it.value()!!)
            }
        }

        private fun translateToRuleDataValue(event: Event, attrValues: List<TrackedEntityDataValue>): List<RuleDataValue> {
            return attrValues.map {
                RuleDataValue.create(event.eventDate()!!, event.programStage()!!, it.dataElement()!!, it.value()!!)
            }
        }

    }
}