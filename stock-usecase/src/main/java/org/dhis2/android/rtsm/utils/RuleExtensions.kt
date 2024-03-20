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
package org.dhis2.android.rtsm.utils

import org.dhis2.commons.rules.toRuleEngineInstant
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
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.hisp.dhis.rules.models.Option
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleAction
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
import java.util.Date

fun List<ProgramRule>.toRuleList(): List<Rule> {
    return map {
        it.toRuleEngineObject()
    }
}

fun List<ProgramRuleAction>.toRuleActionList(): List<RuleAction> {
    return map {
        it.toRuleEngineObject()
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
    return Rule(
        condition() ?: "",
        programRuleActions()?.toRuleActionList() ?: ArrayList(),
        uid(),
        name(),
        programStage()?.uid(),
        priority(),
    )
}

fun ProgramRuleAction.toRuleEngineObject(): RuleAction {
    val field =
        when {
            dataElement() != null -> dataElement()!!.uid()
            trackedEntityAttribute() != null -> trackedEntityAttribute()!!.uid()
            else -> ""
        }

    if (programRuleActionType() == ProgramRuleActionType.SHOWERROR) {
        return RuleAction(
            data = data(),
            type = ProgramRuleActionType.SHOWERROR.name,
            values = mutableMapOf(
                Pair("field", field),
            ).also { map ->
                content()?.let { map["content"] = it }
            },
        )
    } else if (programRuleActionType() == ProgramRuleActionType.ERRORONCOMPLETE) {
        return RuleAction(
            data = data(),
            type = ProgramRuleActionType.ERRORONCOMPLETE.name,
            values = mutableMapOf(
                Pair("field", field),
            ).also { map ->
                content()?.let { map["content"] = it }
            },
        )
    } else if (programRuleActionType() == ProgramRuleActionType.ASSIGN) {
        // Temporarily ignore the missing field as you have above until you create
        // an unsupported rule class (RuleActionUnsupported)
        return RuleAction(
            data = data() ?: "",
            type = ProgramRuleActionType.ASSIGN.name,
            values = mutableMapOf(
                Pair("field", field),
            ).also { map ->
                content()?.let { map["content"] = it }
            },
        )
    }

    // Temporarily returns ProgramRuleActionType.HIDEFIELD for the sake of testing
    return RuleAction(
        data = null,
        type = ProgramRuleActionType.HIDEFIELD.name,
        values = mutableMapOf(
            Pair("field", field),
        ).also { map ->
            content()?.let { map["content"] = it }
        },
    )
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
            RuleVariableCalculatedValue(
                name = name() ?: "",
                useCodeForOptionSet = useCodeForOptionSet,
                options = options,
                field = dataElement()?.uid() ?: trackedEntityAttribute()?.uid() ?: "",
                fieldType = valueType,
            )

        ProgramRuleVariableSourceType.TEI_ATTRIBUTE ->
            RuleVariableAttribute(
                name = name() ?: "",
                useCodeForOptionSet = useCodeForOptionSet,
                options = options,
                field = trackedEntityAttribute()?.uid() ?: "",
                fieldType = valueType,
            )

        ProgramRuleVariableSourceType.DATAELEMENT_NEWEST_EVENT_PROGRAM_STAGE ->
            RuleVariableNewestStageEvent(
                name = name() ?: "",
                useCodeForOptionSet = useCodeForOptionSet,
                options = options,
                field = dataElement()?.uid() ?: "",
                fieldType = valueType,
                programStage = programStage()?.uid() ?: "",
            )

        ProgramRuleVariableSourceType.DATAELEMENT_NEWEST_EVENT_PROGRAM ->
            RuleVariableNewestEvent(
                name = name() ?: "",
                useCodeForOptionSet = useCodeForOptionSet,
                options = options,
                field = dataElement()?.uid() ?: "",
                fieldType = valueType,
            )

        ProgramRuleVariableSourceType.DATAELEMENT_CURRENT_EVENT ->
            RuleVariableCurrentEvent(
                name = name() ?: "",
                useCodeForOptionSet = useCodeForOptionSet,
                options = options,
                field = dataElement()?.uid() ?: "",
                fieldType = valueType,
            )

        ProgramRuleVariableSourceType.DATAELEMENT_PREVIOUS_EVENT ->
            RuleVariablePreviousEvent(
                name = name() ?: "",
                useCodeForOptionSet = useCodeForOptionSet,
                options = options,
                field = dataElement()?.uid() ?: "",
                fieldType = valueType,
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
            }?.map { option -> Option(option.name()!!, option.code() ?: "") } ?: emptyList()
    } else if (trackedEntityAttributeUid != null) {
        attributeRepository.uid(trackedEntityAttributeUid).blockingGet()?.optionSet()?.uid()
            ?.let { optionSetUid ->
                optionRepository.byOptionSetUid().eq(optionSetUid).blockingGet()
            }?.map { option -> Option(option.name()!!, option.code() ?: "") } ?: emptyList()
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
            value = try {
                value?.toFloat().toString()
            } catch (e: Exception) {
                Timber.e(e)
                ""
            }
        }
        RuleDataValue(
            eventDate = (event.eventDate() ?: Date()).toRuleEngineInstant(),
            programStage = event.programStage()!!,
            dataElement = it.dataElement()!!,
            value = value!!,
        )
    }.filter { it.value.isNotEmpty() }
}
