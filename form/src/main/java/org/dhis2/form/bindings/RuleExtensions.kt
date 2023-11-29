/*
 * Copyright (c) 2004 - 2019, University of Oslo
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.dhis2.form.bindings

import org.dhis2.form.model.RuleActionError
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElementCollectionRepository
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.option.OptionCollectionRepository
import org.hisp.dhis.android.core.program.ProgramRule
import org.hisp.dhis.android.core.program.ProgramRuleAction
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.android.core.program.ProgramRuleVariable
import org.hisp.dhis.android.core.program.ProgramRuleVariableCollectionRepository
import org.hisp.dhis.android.core.program.ProgramRuleVariableSourceType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeCollectionRepository
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.hisp.dhis.rules.Option
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleAction
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
import org.hisp.dhis.rules.models.RuleActionUnsupported
import org.hisp.dhis.rules.models.RuleActionWarningOnCompletion
import org.hisp.dhis.rules.models.RuleAttributeValue
import org.hisp.dhis.rules.models.RuleDataValue
import org.hisp.dhis.rules.models.RuleValueType
import org.hisp.dhis.rules.models.RuleVariable
import org.hisp.dhis.rules.models.RuleVariableAttribute
import org.hisp.dhis.rules.models.RuleVariableCalculatedValue
import org.hisp.dhis.rules.models.RuleVariableCurrentEvent
import org.hisp.dhis.rules.models.RuleVariableNewestEvent
import org.hisp.dhis.rules.models.RuleVariableNewestStageEvent
import org.hisp.dhis.rules.models.RuleVariablePreviousEvent
import timber.log.Timber

fun List<ProgramRule>.toRuleList(): List<Rule> {
    return map {
        it.toRuleEngineObject()
    }
}

fun List<ProgramRuleAction>.toRuleActionList(): List<RuleAction> {
    return map {
        try {
            it.toRuleEngineObject()
        } catch (e: Exception) {
            RuleActionError(
                action = it.programRuleActionType().toString(),
                message = e.message ?: "UNKNOWN",
            )
        }
    }
}

fun List<ProgramRuleVariable>.toRuleVariableList(
    attributeRepository: TrackedEntityAttributeCollectionRepository,
    dataElementRepository: DataElementCollectionRepository,
    optionRepository: OptionCollectionRepository,
): List<RuleVariable> {
    return filter {
        when {
            it.dataElement() != null -> {
                dataElementRepository.uid(it.dataElement()?.uid()).blockingExists()
            }

            it.trackedEntityAttribute() != null -> {
                attributeRepository.uid(it.trackedEntityAttribute()?.uid()).blockingExists()
            }

            else -> isCalculatedValue(it)
        }
    }.map {
        it.toRuleVariable(attributeRepository, dataElementRepository, optionRepository)
    }
}

private fun isCalculatedValue(it: ProgramRuleVariable) = it.dataElement() == null &&
    it.trackedEntityAttribute() == null &&
    it.programRuleVariableSourceType() == ProgramRuleVariableSourceType.CALCULATED_VALUE

fun ProgramRule.toRuleEngineObject(): Rule {
    return Rule.create(
        programStage()?.uid(),
        priority(),
        condition() ?: "",
        programRuleActions()?.toRuleActionList() ?: ArrayList(),
        name(),
        uid(),
    )
}

fun ProgramRuleAction.toRuleEngineObject(): RuleAction {
    val field =
        when {
            dataElement() != null -> dataElement()!!.uid()
            trackedEntityAttribute() != null -> trackedEntityAttribute()!!.uid()
            else -> ""
        }

    return when (programRuleActionType()) {
        ProgramRuleActionType.HIDEFIELD -> RuleActionHideField.create(content(), field)
        ProgramRuleActionType.DISPLAYTEXT ->
            if (location() == RuleActionDisplayText.LOCATION_FEEDBACK_WIDGET) {
                RuleActionDisplayText.createForFeedback(
                    content(),
                    data(),
                )
            } else {
                RuleActionDisplayText.createForIndicators(
                    content(),
                    data(),
                )
            }

        ProgramRuleActionType.DISPLAYKEYVALUEPAIR ->
            if (location() == RuleActionDisplayText.LOCATION_FEEDBACK_WIDGET) {
                RuleActionDisplayKeyValuePair.createForFeedback(
                    content(),
                    data(),
                )
            } else {
                RuleActionDisplayKeyValuePair.createForIndicators(
                    content(),
                    data(),
                )
            }

        ProgramRuleActionType.HIDESECTION ->
            programStageSection()?.let {
                RuleActionHideSection.create(it.uid())
            } ?: RuleActionUnsupported.create(
                "HIDE SECTION RULE IS MISSING PROGRAM STAGE SECTION",
                name() ?: uid(),
            )

        ProgramRuleActionType.HIDEPROGRAMSTAGE ->
            programStage()?.let {
                RuleActionHideProgramStage.create(it.uid())
            } ?: RuleActionUnsupported.create(
                "HIDE STAGE RULE IS MISSING PROGRAM STAGE",
                name() ?: uid(),
            )

        ProgramRuleActionType.ASSIGN -> {
            if (field.isEmpty() && content().isNullOrEmpty()) {
                RuleActionUnsupported.create(
                    "ASSIGN RULE IS MISSING FIELD TO ASSIGN TO",
                    name() ?: uid(),
                )
            } else {
                RuleActionAssign.create(content(), data() ?: "", field)
            }
        }

        ProgramRuleActionType.SHOWWARNING -> RuleActionShowWarning.create(content(), data(), field)
        ProgramRuleActionType.WARNINGONCOMPLETE -> RuleActionWarningOnCompletion.create(
            content(),
            data(),
            field,
        )

        ProgramRuleActionType.SHOWERROR -> RuleActionShowError.create(content(), data(), field)
        ProgramRuleActionType.ERRORONCOMPLETE -> RuleActionErrorOnCompletion.create(
            content(),
            data(),
            field,
        )

        ProgramRuleActionType.CREATEEVENT ->
            programStage()?.let {
                RuleActionCreateEvent.create(
                    content(),
                    data(),
                    it.uid(),
                )
            } ?: RuleActionUnsupported.create(
                "CREATE EVENT RULE IS MISSING PROGRAM STAGE SECTION",
                name() ?: uid(),
            )

        ProgramRuleActionType.SETMANDATORYFIELD -> RuleActionSetMandatoryField.create(field)
        ProgramRuleActionType.HIDEOPTION ->
            option()?.let {
                RuleActionHideOption.create(
                    content(),
                    it.uid(),
                    field,
                )
            } ?: RuleActionUnsupported.create(
                "HIDE OPTION RULE IS MISSING OPTION",
                name() ?: uid(),
            )

        ProgramRuleActionType.SHOWOPTIONGROUP ->
            optionGroup()?.let {
                RuleActionShowOptionGroup.create(
                    content(),
                    it.uid(),
                    field,
                )
            } ?: RuleActionUnsupported.create(
                "SHOW OPTION GROUP RULE IS MISSING OPTION GROUP",
                name() ?: uid(),
            )

        ProgramRuleActionType.HIDEOPTIONGROUP ->
            optionGroup()?.let {
                RuleActionHideOptionGroup.create(
                    content(),
                    it.uid(),
                    field,
                )
            } ?: RuleActionUnsupported.create(
                "HIDE OPTION GROUP RULE IS MISSING OPTION GROUP",
                name() ?: uid(),
            )

        ProgramRuleActionType.SENDMESSAGE, ProgramRuleActionType.SCHEDULEMESSAGE, null ->
            RuleActionUnsupported.create(
                "UNSUPPORTED RULE ACTION TYPE",
                name() ?: uid(),
            )
    }
}

fun ProgramRuleVariable.toRuleVariable(
    attributeRepository: TrackedEntityAttributeCollectionRepository,
    dataElementRepository: DataElementCollectionRepository,
    optionRepository: OptionCollectionRepository,
): RuleVariable {
    val valueType = when (programRuleVariableSourceType()) {
        ProgramRuleVariableSourceType.DATAELEMENT_NEWEST_EVENT_PROGRAM_STAGE,
        ProgramRuleVariableSourceType.DATAELEMENT_NEWEST_EVENT_PROGRAM,
        ProgramRuleVariableSourceType.DATAELEMENT_CURRENT_EVENT,
        ProgramRuleVariableSourceType.DATAELEMENT_PREVIOUS_EVENT,
        ->
            dataElement()?.let {
                dataElementRepository.uid(it.uid()).blockingGet()
                    ?.valueType()?.toRuleValueType()
            } ?: RuleValueType.TEXT

        ProgramRuleVariableSourceType.TEI_ATTRIBUTE ->
            trackedEntityAttribute()?.let {
                attributeRepository.uid(it.uid()).blockingGet()
                    ?.valueType()?.toRuleValueType()
            } ?: RuleValueType.TEXT

        ProgramRuleVariableSourceType.CALCULATED_VALUE, null -> RuleValueType.TEXT
    }

    val useCodeForOptionSet = useCodeForOptionSet() ?: false
    val options = getOptions(
        useCodeForOptionSet,
        dataElement()?.uid(),
        trackedEntityAttribute()?.uid(),
        attributeRepository,
        dataElementRepository,
        optionRepository,
    )

    return when (programRuleVariableSourceType()) {
        ProgramRuleVariableSourceType.CALCULATED_VALUE ->
            RuleVariableCalculatedValue.create(
                name() ?: "",
                dataElement()?.uid() ?: trackedEntityAttribute()?.uid() ?: "",
                valueType,
                useCodeForOptionSet,
                options,
            )

        ProgramRuleVariableSourceType.TEI_ATTRIBUTE ->
            RuleVariableAttribute.create(
                name() ?: "",
                trackedEntityAttribute()?.uid() ?: "",
                valueType,
                useCodeForOptionSet,
                options,
            )

        ProgramRuleVariableSourceType.DATAELEMENT_NEWEST_EVENT_PROGRAM_STAGE ->
            RuleVariableNewestStageEvent.create(
                name() ?: "",
                dataElement()?.uid() ?: "",
                programStage()?.uid() ?: "",
                valueType,
                useCodeForOptionSet,
                options,
            )

        ProgramRuleVariableSourceType.DATAELEMENT_NEWEST_EVENT_PROGRAM ->
            RuleVariableNewestEvent.create(
                name() ?: "",
                dataElement()?.uid() ?: "",
                valueType,
                useCodeForOptionSet,
                options,
            )

        ProgramRuleVariableSourceType.DATAELEMENT_CURRENT_EVENT ->
            RuleVariableCurrentEvent.create(
                name() ?: "",
                dataElement()?.uid() ?: "",
                valueType,
                useCodeForOptionSet,
                options,
            )

        ProgramRuleVariableSourceType.DATAELEMENT_PREVIOUS_EVENT ->
            RuleVariablePreviousEvent.create(
                name() ?: "",
                dataElement()?.uid() ?: "",
                valueType,
                useCodeForOptionSet,
                options,
            )

        else -> throw IllegalArgumentException("Unsupported variable ")
    }
}

fun getOptions(
    useCodeForOptionSet: Boolean,
    dataElementUid: String?,
    trackedEntityAttributeUid: String?,
    attributeRepository: TrackedEntityAttributeCollectionRepository,
    dataElementRepository: DataElementCollectionRepository,
    optionRepository: OptionCollectionRepository,
): List<Option> {
    if (useCodeForOptionSet) {
        return emptyList()
    }

    return if (dataElementUid != null) {
        dataElementRepository.uid(dataElementUid).blockingGet()?.optionSet()?.uid()
            ?.let { optionSetUid ->
                optionRepository.byOptionSetUid().eq(optionSetUid).blockingGet()
            }?.map { option -> Option(option.name(), option.code()) } ?: emptyList()
    } else if (trackedEntityAttributeUid != null) {
        attributeRepository.uid(trackedEntityAttributeUid).blockingGet()?.optionSet()?.uid()
            ?.let { optionSetUid ->
                optionRepository.byOptionSetUid().eq(optionSetUid).blockingGet()
            }?.map { option -> Option(option.name(), option.code()) } ?: emptyList()
    } else {
        emptyList()
    }
}

fun ValueType.toRuleValueType(): RuleValueType {
    return when {
        isInteger || isNumeric -> RuleValueType.NUMERIC
        isBoolean -> RuleValueType.BOOLEAN
        else -> RuleValueType.TEXT
    }
}

fun List<TrackedEntityDataValue>.toRuleDataValue(
    event: Event,
    dataElementRepository: DataElementCollectionRepository,
    ruleVariableRepository: ProgramRuleVariableCollectionRepository,
    optionRepository: OptionCollectionRepository,
): List<RuleDataValue> {
    return map {
        var value = if (it.value() != null) it.value() else ""
        val de = dataElementRepository.uid(it.dataElement()).blockingGet()
        if (!de?.optionSetUid().isNullOrEmpty()) {
            if (ruleVariableRepository
                    .byProgramUid().eq(event.program())
                    .byDataElementUid().eq(it.dataElement())
                    .byUseCodeForOptionSet().isTrue
                    .blockingIsEmpty()
            ) {
                value =
                    if (optionRepository
                            .byOptionSetUid().eq(de?.optionSetUid())
                            .byCode().eq(value)
                            .one().blockingExists()
                    ) {
                        optionRepository
                            .byOptionSetUid().eq(de?.optionSetUid())
                            .byCode().eq(value)
                            .one().blockingGet()?.name()
                    } else {
                        ""
                    }
            }
        } else if (de?.valueType()?.isNumeric == true) {
            value = if (value.isNullOrEmpty()) {
                ""
            } else {
                try {
                    value.toFloat().toString()
                } catch (e: Exception) {
                    Timber.e(e)
                    ""
                }
            }
        }
        RuleDataValue.create(
            event.eventDate()!!,
            event.programStage()!!,
            it.dataElement()!!,
            value!!,
        )
    }.filter { it.value().isNotEmpty() }
}

fun List<TrackedEntityAttributeValue>.toRuleAttributeValue(
    d2: D2,
    program: String,
): List<RuleAttributeValue> {
    return map {
        var value = if (it.value() != null) it.value() else ""
        val attr =
            d2.trackedEntityModule().trackedEntityAttributes().uid(it.trackedEntityAttribute())
                .blockingGet()
        if (!attr?.optionSet()?.uid().isNullOrEmpty()) {
            if (d2.programModule().programRuleVariables()
                    .byProgramUid().eq(program)
                    .byTrackedEntityAttributeUid().eq(it.trackedEntityAttribute())
                    .byUseCodeForOptionSet().isTrue
                    .blockingIsEmpty()
            ) {
                value =
                    if (d2.optionModule().options().byOptionSetUid().eq(attr?.optionSet()?.uid())
                            .byCode().eq(value)
                            .one().blockingExists()
                    ) {
                        d2.optionModule().options().byOptionSetUid().eq(attr?.optionSet()?.uid())
                            .byCode().eq(value)
                            .one()
                            .blockingGet()?.name() ?: ""
                    } else {
                        ""
                    }
            }
        } else if (attr?.valueType()?.isNumeric == true) {
            value = try {
                when (attr.valueType()) {
                    ValueType.INTEGER_NEGATIVE,
                    ValueType.INTEGER_ZERO_OR_POSITIVE,
                    ValueType.INTEGER_POSITIVE,
                    ValueType.INTEGER,
                    -> value?.toInt().toString()
                    ValueType.PERCENTAGE,
                    ValueType.UNIT_INTERVAL,
                    ValueType.NUMBER,
                    -> value?.toFloat().toString()
                    else -> value
                }
            } catch (e: Exception) {
                Timber.e(e)
                ""
            }
        }
        RuleAttributeValue.create(it.trackedEntityAttribute()!!, value!!)
    }.filter { it.value().isNotEmpty() }
}
