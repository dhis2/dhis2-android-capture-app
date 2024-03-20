package org.dhis2.mobileProgramRules

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.dhis2.commons.rules.toRuleEngineInstant
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
import org.hisp.dhis.rules.models.Option
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleAction
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
import java.util.Date

fun Date.toRuleEngineInstant() =
    Instant.fromEpochMilliseconds(this.time)

fun Date.toRuleEngineLocalDate() =
    toRuleEngineInstant().toLocalDateTime(TimeZone.currentSystemDefault()).date

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
            RuleAction(
                data = e.message ?: "UNKNOWN",
                type = "error",
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
    return Rule(
        condition = condition() ?: "",
        actions = programRuleActions()?.toRuleActionList() ?: ArrayList(),
        uid = uid(),
        name = name(),
        programStage = programStage()?.uid(),
        priority = priority(),
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
        ProgramRuleActionType.HIDEFIELD ->
            RuleAction(
                data = null,
                type = ProgramRuleActionType.HIDEFIELD.name,
                values = mutableMapOf(
                    Pair("field", field),
                ).also { map ->
                    content()?.let { map["content"] = it }
                },
            )

        ProgramRuleActionType.DISPLAYTEXT ->
            RuleAction(
                data = data(),
                type = ProgramRuleActionType.DISPLAYTEXT.name,
                values = mutableMapOf(
                    Pair("location", location() ?: "indicators"),
                ).also { map ->
                    content()?.let { map["content"] = it }
                },
            )

        ProgramRuleActionType.DISPLAYKEYVALUEPAIR ->
            RuleAction(
                data = data(),
                type = ProgramRuleActionType.DISPLAYKEYVALUEPAIR.name,
                values = mutableMapOf(
                    Pair("location", location()!!),
                ).also { map ->
                    content()?.let { map["content"] = it }
                },
            )

        ProgramRuleActionType.HIDESECTION ->
            programStageSection()?.let {
                RuleAction(
                    data = null,
                    type = ProgramRuleActionType.HIDESECTION.name,
                    values = mutableMapOf(
                        Pair("programStageSection", it.uid()),
                    ),
                )
            } ?: RuleAction(
                "HIDE SECTION RULE IS MISSING PROGRAM STAGE SECTION",
                "unsupported",
            )

        ProgramRuleActionType.HIDEPROGRAMSTAGE ->
            programStage()?.let {
                RuleAction(
                    data = data(),
                    type = ProgramRuleActionType.HIDEPROGRAMSTAGE.name,
                    values = mutableMapOf(
                        Pair("programStage", it.uid()),
                    ),
                )
            } ?: RuleAction(
                "HIDE STAGE RULE IS MISSING PROGRAM STAGE",
                "unsupported",
            )

        ProgramRuleActionType.ASSIGN -> {
            if (field.isEmpty() && content().isNullOrEmpty()) {
                RuleAction(
                    "ASSIGN RULE IS MISSING FIELD TO ASSIGN TO",
                    type = "unsupported",
                )
            } else {
                RuleAction(
                    data = data() ?: "",
                    type = ProgramRuleActionType.ASSIGN.name,
                    values = mutableMapOf(
                        Pair("field", field),
                    ).also { map ->
                        content()?.let { map["content"] = it }
                    },
                )
            }
        }

        ProgramRuleActionType.SHOWWARNING -> RuleAction(
            data = data(),
            type = ProgramRuleActionType.SHOWWARNING.name,
            values = mutableMapOf(
                Pair("field", field),
            ).also { map ->
                content()?.let { map["content"] = it }
            },
        )

        ProgramRuleActionType.WARNINGONCOMPLETE -> RuleAction(
            data = data(),
            type = ProgramRuleActionType.WARNINGONCOMPLETE.name,
            values = mutableMapOf(
                Pair("field", field),
            ).also { map ->
                content()?.let { map["content"] = it }
            },
        )

        ProgramRuleActionType.SHOWERROR ->
            RuleAction(
                data = data(),
                type = ProgramRuleActionType.SHOWERROR.name,
                values = mutableMapOf(
                    Pair("field", field),
                ).also { map ->
                    content()?.let { map["content"] = it }
                },
            )

        ProgramRuleActionType.ERRORONCOMPLETE ->
            RuleAction(
                data = data(),
                type = ProgramRuleActionType.ERRORONCOMPLETE.name,
                values = mutableMapOf(
                    Pair("field", field),
                ).also { map ->
                    content()?.let { map["content"] = it }
                },
            )

        ProgramRuleActionType.CREATEEVENT ->
            programStage()?.uid()?.let { stageUid ->
                RuleAction(
                    data = data(),
                    type = ProgramRuleActionType.CREATEEVENT.name,
                    values = mutableMapOf(
                        Pair("programStage", stageUid),
                    ).also { map ->
                        content()?.let { map["content"] = it }
                    },
                )
            } ?: RuleAction(
                "CREATE EVENT RULE IS MISSING PROGRAM STAGE SECTION",
                "unsupported",
            )

        ProgramRuleActionType.SETMANDATORYFIELD ->
            RuleAction(
                data = data(),
                type = ProgramRuleActionType.SETMANDATORYFIELD.name,
                values = mutableMapOf(
                    Pair("field", field),
                ).also { map ->
                    content()?.let { map["content"] = it }
                },
            )

        ProgramRuleActionType.HIDEOPTION ->
            option()?.uid()?.let { optionUid ->
                RuleAction(
                    data = data(),
                    type = ProgramRuleActionType.HIDEOPTION.name,
                    values = mutableMapOf(
                        Pair("field", field),
                        Pair("option", optionUid),
                    ).also { map ->
                        content()?.let { map["content"] = it }
                    },
                )
            } ?: RuleAction(
                "HIDE OPTION RULE IS MISSING OPTION",
                "unsupported",
            )

        ProgramRuleActionType.SHOWOPTIONGROUP ->
            optionGroup()?.uid()?.let { optionGroupUid ->
                RuleAction(
                    data = data(),
                    type = ProgramRuleActionType.SHOWOPTIONGROUP.name,
                    values = mutableMapOf(
                        Pair("field", field),
                        Pair("optionGroup", optionGroupUid),
                    ).also { map ->
                        content()?.let { map["content"] = it }
                    },
                )
            } ?: RuleAction(
                "SHOW OPTION GROUP RULE IS MISSING OPTION GROUP",
                "unsupported",
            )

        ProgramRuleActionType.HIDEOPTIONGROUP ->
            optionGroup()?.uid()?.let { optionGroupUid ->
                RuleAction(
                    data = data(),
                    type = ProgramRuleActionType.HIDEOPTIONGROUP.name,
                    values = mutableMapOf(
                        Pair("field", field),
                        Pair("optionGroup", optionGroupUid),
                    ).also { map ->
                        content()?.let { map["content"] = it }
                    },
                )
            } ?: RuleAction(
                "HIDE OPTION GROUP RULE IS MISSING OPTION GROUP",
                "unsupported",
            )

        ProgramRuleActionType.SENDMESSAGE, ProgramRuleActionType.SCHEDULEMESSAGE, null ->
            RuleAction(
                "UNSUPPORTED RULE ACTION TYPE",
                "unsupported",
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
        RuleDataValue(
            eventDate = event.eventDate()!!.toRuleEngineInstant(),
            programStage = event.programStage()!!,
            dataElement = it.dataElement()!!,
            value = value!!,
        )
    }.filter { it.value.isNotEmpty() }
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
        RuleAttributeValue(it.trackedEntityAttribute()!!, value!!)
    }.filter { it.value.isNotEmpty() }
}
