package org.dhis2.mobileProgramRules

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.DateUtils
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElementCollectionRepository
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.option.OptionCollectionRepository
import org.hisp.dhis.android.core.program.ProgramRuleAction
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.android.core.program.ProgramRuleVariable
import org.hisp.dhis.android.core.program.ProgramRuleVariableSourceType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeCollectionRepository
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RuleEngineExtensionsTest {
    private val dataElementRepository: DataElementCollectionRepository =
        Mockito.mock(
            DataElementCollectionRepository::class.java,
            RETURNS_DEEP_STUBS,
        )
    private val d2: D2 = Mockito.mock(D2::class.java, RETURNS_DEEP_STUBS)

    private val trackedEntityAttributeCollectionRepository:
        TrackedEntityAttributeCollectionRepository =
        Mockito.mock(
            TrackedEntityAttributeCollectionRepository::class.java,
            RETURNS_DEEP_STUBS,
        )

    private val optionCollectionRepository:
        OptionCollectionRepository =
        Mockito.mock(
            OptionCollectionRepository::class.java,
            RETURNS_DEEP_STUBS,
        )

    @Test
    fun `Should remove the time component`() {
        val date1 = DateUtils.DATE_FORMAT.parse("2025-09-25T11:43:32.431")
        val date2 = DateUtils.DATE_FORMAT.parse("2025-09-25T00:00:00.000")

        assertEquals(date1.toRuleEngineInstantWithNoTime(), date2.toRuleEngineInstantWithNoTime())

        val date3 = DateUtils.DATE_FORMAT.parse("2025-09-25T11:43:32.431")
        val date4 = DateUtils.DATE_FORMAT.parse("2025-09-26T00:00:00.000")

        assertNotEquals(
            date3.toRuleEngineInstantWithNoTime(),
            date4.toRuleEngineInstantWithNoTime(),
        )
    }

    @Test
    fun `Should order events by event date with no time and created`() {
        val event1 =
            Event
                .builder()
                .uid("event1")
                .eventDate(DateUtils.DATE_FORMAT.parse("2025-09-25T11:43:32.431"))
                .created(DateUtils.DATE_FORMAT.parse("2025-09-25T11:50:32.431"))
                .build()

        val event2 =
            Event
                .builder()
                .uid("event2")
                .eventDate(DateUtils.DATE_FORMAT.parse("2025-09-25T00:00:00.000"))
                .created(DateUtils.DATE_FORMAT.parse("2025-09-25T10:10:32.431"))
                .build()

        val event3 =
            Event
                .builder()
                .uid("event3")
                .eventDate(DateUtils.DATE_FORMAT.parse("2025-09-25T00:00:00.000"))
                .created(DateUtils.DATE_FORMAT.parse("2025-09-25T14:30:32.431"))
                .build()

        val events = listOf(event1, event2, event3)
        val sortedEvents = events.sortForRuleEngine()

        assertEquals(listOf(event3, event1, event2), sortedEvents)
    }

    @Test
    fun `Should transform trackedEntityDataValues to ruleDataValues with optionCode value`() {
        val rules = getTrackedEntityDataValues().toRuleDataValue()

        assertTrue(rules.size == 1)
        assertTrue(rules[0].value == "optionCode")
    }

    @Test
    fun `Should transform trackedEntityAttributeValue to ruleDataValues with optionName value`() {
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityAttributes()
                .uid("attrUid")
                .blockingGet(),
        ) doReturn
            TrackedEntityAttribute
                .builder()
                .uid("attrUid")
                .optionSet(ObjectWithUid.create("optionSetUid"))
                .valueType(ValueType.TEXT)
                .build()

        val rules =
            getTrackedEntityAttributeValues()
                .toRuleAttributeValue(d2)

        assertTrue(rules.size == 1)
        assertTrue(rules[0].value == "optionCode")
    }

    @Test
    fun `Should format numeric values`() {
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityAttributes()
                .uid("attrIntegerUid")
                .blockingGet(),
        ) doReturn
            TrackedEntityAttribute
                .builder()
                .uid("attrIntegerUid")
                .valueType(ValueType.INTEGER)
                .build()
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityAttributes()
                .uid("attrNumberUid")
                .blockingGet(),
        ) doReturn
            TrackedEntityAttribute
                .builder()
                .uid("attrNumberUid")
                .valueType(ValueType.NUMBER)
                .build()

        val rules = getTrackedEntityNumericAttributeValues().toRuleAttributeValue(d2)

        assertTrue(rules[0].value == "123")
        assertTrue(rules[1].value == "555.55")
    }

    @Test
    fun `Should transform trackedEntityAttributeValue to ruleDataValues with optionCode value`() {
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityAttributes()
                .uid("attrUid")
                .blockingGet(),
        ) doReturn
            TrackedEntityAttribute
                .builder()
                .uid("dataElementUid")
                .optionSet(ObjectWithUid.create("optionSetUid"))
                .valueType(ValueType.TEXT)
                .build()

        whenever(
            d2
                .programModule()
                .programRuleVariables()
                .byProgramUid()
                .eq("programUid"),
        ) doReturn mock()
        whenever(
            d2
                .programModule()
                .programRuleVariables()
                .byProgramUid()
                .eq("programUid")
                .byTrackedEntityAttributeUid(),
        ) doReturn mock()
        whenever(
            d2
                .programModule()
                .programRuleVariables()
                .byProgramUid()
                .eq("programUid")
                .byTrackedEntityAttributeUid()
                .eq(
                    "attrUid",
                ),
        ) doReturn mock()
        whenever(
            d2
                .programModule()
                .programRuleVariables()
                .byProgramUid()
                .eq("programUid")
                .byTrackedEntityAttributeUid()
                .eq(
                    "attrUid",
                ).byUseCodeForOptionSet(),
        ) doReturn mock()
        whenever(
            d2
                .programModule()
                .programRuleVariables()
                .byProgramUid()
                .eq("programUid")
                .byTrackedEntityAttributeUid()
                .eq(
                    "attrUid",
                ).byUseCodeForOptionSet()
                .isTrue,
        ) doReturn mock()
        whenever(
            d2
                .programModule()
                .programRuleVariables()
                .byProgramUid()
                .eq("programUid")
                .byTrackedEntityAttributeUid()
                .eq(
                    "attrUid",
                ).byUseCodeForOptionSet()
                .isTrue
                .blockingIsEmpty(),
        ) doReturn false

        val rules =
            getTrackedEntityAttributeValues()
                .toRuleAttributeValue(d2)

        assertTrue(rules.size == 1)
        assertTrue(rules[0].value == "optionCode")
    }

    @Test
    fun `Should parse program rule action to unsupported rule action`() {
        val ruleAction =
            ProgramRuleAction
                .builder()
                .uid("uid")
                .content("")
                .data("")
                .build()
        val ruleEngineAction = ruleAction.toRuleEngineObject()
        assertTrue(ruleEngineAction.type == "unsupported")
    }

    @Test
    fun `Should parse program rule action to rule action with calculated value`() {
        val ruleAction =
            ProgramRuleAction
                .builder()
                .uid("uid")
                .data("")
                .content("calculated value")
                .programRuleActionType(ProgramRuleActionType.ASSIGN)
                .build()
        val ruleEngineAction = ruleAction.toRuleEngineObject()
        assertTrue(!ruleEngineAction.values.containsKey("field"))
        assertTrue(ruleEngineAction.values["content"] == "calculated value")
    }

    @Test
    fun `should parse ProgramRuleVariable to RuleVariable`() {
        val programRuleVariable =
            ProgramRuleVariable
                .builder()
                .uid("uid")
                .name("rule")
                .programRuleVariableSourceType(ProgramRuleVariableSourceType.CALCULATED_VALUE)
                .build()
        val ruleVariable =
            programRuleVariable.toRuleVariable(
                optionCollectionRepository = optionCollectionRepository,
                attributeRepository = trackedEntityAttributeCollectionRepository,
                dataElementRepository = dataElementRepository,
            )
        assertEquals(programRuleVariable.name(), ruleVariable.name)
    }

    private fun getTrackedEntityDataValues(): List<TrackedEntityDataValue> =
        arrayListOf(
            TrackedEntityDataValue
                .builder()
                .dataElement("dataElementUid")
                .event("eventUid")
                .value("optionCode")
                .build(),
            TrackedEntityDataValue
                .builder()
                .dataElement("dataElementUid")
                .event("eventUid")
                .build(),
        )

    private fun getTrackedEntityAttributeValues(): List<TrackedEntityAttributeValue> =
        arrayListOf(
            TrackedEntityAttributeValue
                .builder()
                .trackedEntityAttribute("attrUid")
                .trackedEntityInstance("teiUid")
                .value("optionCode")
                .build(),
            TrackedEntityAttributeValue
                .builder()
                .trackedEntityAttribute("attrUid")
                .trackedEntityInstance("teiUid")
                .build(),
        )

    private fun getTrackedEntityNumericAttributeValues(): List<TrackedEntityAttributeValue> =
        arrayListOf(
            TrackedEntityAttributeValue
                .builder()
                .trackedEntityAttribute("attrIntegerUid")
                .trackedEntityInstance("teiIntegerUid")
                .value("123")
                .build(),
            TrackedEntityAttributeValue
                .builder()
                .trackedEntityAttribute("attrNumberUid")
                .trackedEntityInstance("teiNumberUid")
                .value("555.55")
                .build(),
        )
}
