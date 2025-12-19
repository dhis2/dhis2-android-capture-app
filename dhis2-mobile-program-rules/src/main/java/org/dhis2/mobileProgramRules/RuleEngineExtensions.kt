package org.dhis2.mobileProgramRules

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toDeprecatedInstant
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElementCollectionRepository
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.option.OptionCollectionRepository
import org.hisp.dhis.android.core.program.ProgramRule
import org.hisp.dhis.android.core.program.ProgramRuleAction
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.android.core.program.ProgramRuleVariable
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
import kotlin.time.ExperimentalTime

fun Date.toRuleEngineInstant() = Instant.fromEpochMilliseconds(this.time)

fun Date.toRuleEngineLocalDate() = toRuleEngineInstant().toLocalDateTime(TimeZone.currentSystemDefault()).date

@OptIn(ExperimentalTime::class)
fun Date.toRuleEngineInstantWithNoTime() =
    LocalDateTime(toRuleEngineLocalDate(), LocalTime(0, 0, 0, 0))
        .toInstant(TimeZone.currentSystemDefault())
        .toDeprecatedInstant()

fun List<Event>.sortForRuleEngine(): List<Event> =
    sortedWith(
        compareBy<Event>(
            { it.eventDate()?.toRuleEngineInstantWithNoTime() },
            { it.created() },
        ).reversed(),
    )

fun List<ProgramRule>.toRuleList(): List<Rule> =
    map {
        it.toRuleEngineObject()
    }

fun List<ProgramRuleAction>.toRuleActionList(): List<RuleAction> =
    map {
        try {
            it.toRuleEngineObject()
        } catch (e: Exception) {
            RuleAction(
                data = e.message ?: "UNKNOWN",
                type = "error",
            )
        }
    }

fun List<ProgramRuleVariable>.toRuleVariableList(
    optionCollectionRepository: OptionCollectionRepository,
    attributeRepository: TrackedEntityAttributeCollectionRepository,
    dataElementRepository: DataElementCollectionRepository,
): List<RuleVariable> =
    mapNotNull {
        val allowVariable =
            when {
                it.dataElement() != null -> {
                    dataElementRepository.uid(it.dataElement()?.uid()).blockingExists()
                }

                it.trackedEntityAttribute() != null -> {
                    attributeRepository.uid(it.trackedEntityAttribute()?.uid()).blockingExists()
                }

                else -> isCalculatedValue(it)
            }
        if (allowVariable) {
            it.toRuleVariable(optionCollectionRepository, attributeRepository, dataElementRepository)
        } else {
            null
        }
    }

private fun isCalculatedValue(it: ProgramRuleVariable) =
    it.dataElement() == null &&
        it.trackedEntityAttribute() == null &&
        it.programRuleVariableSourceType() == ProgramRuleVariableSourceType.CALCULATED_VALUE

fun ProgramRule.toRuleEngineObject(): Rule =
    Rule(
        condition = condition() ?: "",
        actions = programRuleActions()?.toRuleActionList() ?: ArrayList(),
        uid = uid(),
        name = name(),
        programStage = programStage()?.uid(),
        priority = priority(),
    )

fun ProgramRuleAction.toRuleEngineObject(): RuleAction {
    val field =
        when {
            dataElement() != null -> dataElement()!!.uid()
            trackedEntityAttribute() != null -> trackedEntityAttribute()!!.uid()
            else -> ""
        }

    val contentToDisplay = displayContent() ?: content()

    return when (programRuleActionType()) {
        ProgramRuleActionType.HIDEFIELD ->
            RuleAction(
                data = null,
                type = ProgramRuleActionType.HIDEFIELD.name,
                values =
                    mutableMapOf(
                        Pair("field", field),
                    ).also { map ->
                        contentToDisplay?.let { map["content"] = it }
                    },
            )

        ProgramRuleActionType.DISPLAYTEXT ->
            RuleAction(
                data = data(),
                type = ProgramRuleActionType.DISPLAYTEXT.name,
                values =
                    mutableMapOf(
                        Pair("location", location() ?: "indicators"),
                    ).also { map ->
                        contentToDisplay?.let { map["content"] = it }
                    },
            )

        ProgramRuleActionType.DISPLAYKEYVALUEPAIR ->
            RuleAction(
                data = data(),
                type = ProgramRuleActionType.DISPLAYKEYVALUEPAIR.name,
                values =
                    mutableMapOf(
                        Pair("location", location()!!),
                    ).also { map ->
                        contentToDisplay?.let { map["content"] = it }
                    },
            )

        ProgramRuleActionType.HIDESECTION ->
            programStageSection()?.let {
                RuleAction(
                    data = null,
                    type = ProgramRuleActionType.HIDESECTION.name,
                    values =
                        mutableMapOf(
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
                    values =
                        mutableMapOf(
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
                    values =
                        if (field.isNotEmpty()) {
                            mutableMapOf(
                                Pair("field", field),
                            ).also { map ->
                                contentToDisplay?.let { map["content"] = it }
                            }
                        } else {
                            contentToDisplay?.let {
                                mutableMapOf(
                                    Pair("content", it),
                                )
                            } ?: emptyMap()
                        },
                )
            }
        }

        ProgramRuleActionType.SHOWWARNING ->
            RuleAction(
                data = data(),
                type = ProgramRuleActionType.SHOWWARNING.name,
                values =
                    mutableMapOf(
                        Pair("field", field),
                    ).also { map ->
                        contentToDisplay?.let { map["content"] = it }
                    },
            )

        ProgramRuleActionType.WARNINGONCOMPLETE ->
            RuleAction(
                data = data(),
                type = ProgramRuleActionType.WARNINGONCOMPLETE.name,
                values =
                    mutableMapOf(
                        Pair("field", field),
                    ).also { map ->
                        contentToDisplay?.let { map["content"] = it }
                    },
            )

        ProgramRuleActionType.SHOWERROR ->
            RuleAction(
                data = data(),
                type = ProgramRuleActionType.SHOWERROR.name,
                values =
                    mutableMapOf(
                        Pair("field", field),
                    ).also { map ->
                        contentToDisplay?.let { map["content"] = it }
                    },
            )

        ProgramRuleActionType.ERRORONCOMPLETE ->
            RuleAction(
                data = data(),
                type = ProgramRuleActionType.ERRORONCOMPLETE.name,
                values =
                    mutableMapOf(
                        Pair("field", field),
                    ).also { map ->
                        contentToDisplay?.let { map["content"] = it }
                    },
            )

        ProgramRuleActionType.CREATEEVENT ->
            programStage()?.uid()?.let { stageUid ->
                RuleAction(
                    data = data(),
                    type = ProgramRuleActionType.CREATEEVENT.name,
                    values =
                        mutableMapOf(
                            Pair("programStage", stageUid),
                        ).also { map ->
                            contentToDisplay?.let { map["content"] = it }
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
                values =
                    mutableMapOf(
                        Pair("field", field),
                    ).also { map ->
                        contentToDisplay?.let { map["content"] = it }
                    },
            )

        ProgramRuleActionType.HIDEOPTION ->
            option()?.uid()?.let { optionUid ->
                RuleAction(
                    data = data(),
                    type = ProgramRuleActionType.HIDEOPTION.name,
                    values =
                        mutableMapOf(
                            Pair("field", field),
                            Pair("option", optionUid),
                        ).also { map ->
                            contentToDisplay?.let { map["content"] = it }
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
                    values =
                        mutableMapOf(
                            Pair("field", field),
                            Pair("optionGroup", optionGroupUid),
                        ).also { map ->
                            contentToDisplay?.let { map["content"] = it }
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
                    values =
                        mutableMapOf(
                            Pair("field", field),
                            Pair("optionGroup", optionGroupUid),
                        ).also { map ->
                            contentToDisplay?.let { map["content"] = it }
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
    optionCollectionRepository: OptionCollectionRepository,
    attributeRepository: TrackedEntityAttributeCollectionRepository,
    dataElementRepository: DataElementCollectionRepository,
): RuleVariable {
    val valueType =
        when (programRuleVariableSourceType()) {
            ProgramRuleVariableSourceType.DATAELEMENT_NEWEST_EVENT_PROGRAM_STAGE,
            ProgramRuleVariableSourceType.DATAELEMENT_NEWEST_EVENT_PROGRAM,
            ProgramRuleVariableSourceType.DATAELEMENT_CURRENT_EVENT,
            ProgramRuleVariableSourceType.DATAELEMENT_PREVIOUS_EVENT,
            ->
                dataElement()?.let {
                    dataElementRepository
                        .uid(it.uid())
                        .blockingGet()
                        ?.valueType()
                        ?.toRuleValueType()
                } ?: RuleValueType.TEXT

            ProgramRuleVariableSourceType.TEI_ATTRIBUTE ->
                trackedEntityAttribute()?.let {
                    attributeRepository
                        .uid(it.uid())
                        .blockingGet()
                        ?.valueType()
                        ?.toRuleValueType()
                } ?: RuleValueType.TEXT

            ProgramRuleVariableSourceType.CALCULATED_VALUE, null -> RuleValueType.TEXT
        }

    val useCodeForOptionSet = useCodeForOptionSet() ?: false
    val options =
        fetchOptions(
            optionCollectionRepository = optionCollectionRepository,
            dataElementUid = dataElement()?.uid(),
            attributeUid = trackedEntityAttribute()?.uid(),
            dataElementRepository = dataElementRepository,
            attributeRepository = attributeRepository,
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

private fun fetchOptions(
    dataElementUid: String?,
    attributeUid: String?,
    optionCollectionRepository: OptionCollectionRepository,
    dataElementRepository: DataElementCollectionRepository,
    attributeRepository: TrackedEntityAttributeCollectionRepository,
): List<Option> {
    val optionSetUid =
        when {
            dataElementUid != null ->
                dataElementRepository
                    .uid(dataElementUid)
                    .blockingGet()
                    ?.optionSet()
                    ?.uid()
            attributeUid != null ->
                attributeRepository
                    .uid(attributeUid)
                    .blockingGet()
                    ?.optionSet()
                    ?.uid()
            else -> null
        }

    return optionSetUid?.let { optionSetUid ->
        optionCollectionRepository
            .byOptionSetUid()
            .eq(optionSetUid)
            .blockingGet()
            .map {
                Option(
                    name = it.name() ?: "",
                    code = it.code() ?: "",
                )
            }
    } ?: emptyList()
}

fun ValueType.toRuleValueType(): RuleValueType =
    when {
        isInteger || isNumeric -> RuleValueType.NUMERIC
        isBoolean -> RuleValueType.BOOLEAN
        else -> RuleValueType.TEXT
    }

fun List<TrackedEntityDataValue>.toRuleDataValue(): List<RuleDataValue> =
    mapNotNull {
        val dataElement = it.dataElement() ?: return@mapNotNull null
        it.value()?.let { value ->
            RuleDataValue(
                dataElement = dataElement,
                value = value,
            )
        }
    }

fun List<TrackedEntityAttributeValue>.toRuleAttributeValue(d2: D2) =

    mapNotNull {
        val attributeUid = it.trackedEntityAttribute() ?: return@mapNotNull null
        val attr =
            d2
                .trackedEntityModule()
                .trackedEntityAttributes()
                .uid(attributeUid)
                .blockingGet()
        val numericValue =
            if (attr?.valueType()?.isNumeric == true) {
                try {
                    when (attr.valueType()) {
                        ValueType.INTEGER_NEGATIVE,
                        ValueType.INTEGER_ZERO_OR_POSITIVE,
                        ValueType.INTEGER_POSITIVE,
                        ValueType.INTEGER,
                        -> it.value()?.toInt().toString()

                        ValueType.PERCENTAGE,
                        ValueType.UNIT_INTERVAL,
                        ValueType.NUMBER,
                        -> it.value()?.toFloat().toString()

                        else -> it.value()
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                    null
                }
            } else {
                null
            }
        (numericValue ?: it.value())?.let { value ->
            RuleAttributeValue(attributeUid, value)
        }
    }
