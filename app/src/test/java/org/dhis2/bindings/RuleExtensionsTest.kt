package org.dhis2.bindings

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import java.util.Date
import junit.framework.Assert.assertTrue
import org.dhis2.Bindings.toRuleAttributeValue
import org.dhis2.Bindings.toRuleDataValue
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.dataelement.DataElementCollectionRepository
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.option.OptionCollectionRepository
import org.hisp.dhis.android.core.program.ProgramRuleVariableCollectionRepository
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.junit.Test
import org.mockito.Mockito

class RuleExtensionsTest {

    private val dataElementRepository: DataElementCollectionRepository = Mockito.mock(
        DataElementCollectionRepository::class.java,
        Mockito.RETURNS_DEEP_STUBS
    )

    private val ruleVariableRepository: ProgramRuleVariableCollectionRepository = Mockito.mock(
        ProgramRuleVariableCollectionRepository::class.java,
        Mockito.RETURNS_DEEP_STUBS
    )
    private val optionRepository: OptionCollectionRepository = Mockito.mock(
        OptionCollectionRepository::class.java,
        Mockito.RETURNS_DEEP_STUBS
    )
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Test
    fun `Should transform trackedEntityDataValues to ruleDataValues with optionName value`() {
        whenever(
            dataElementRepository.uid("dataElementUid").blockingGet()
        ) doReturn DataElement.builder()
            .uid("dataElementUid")
            .optionSet(ObjectWithUid.create("optionSetUid"))
            .valueType(ValueType.TEXT)
            .build()

        whenever(ruleVariableRepository.byProgramUid().eq("programUid")) doReturn mock()
        whenever(
            ruleVariableRepository.byProgramUid().eq("programUid")
                .byDataElementUid()
        ) doReturn mock()
        whenever(
            ruleVariableRepository.byProgramUid().eq("programUid")
                .byDataElementUid().eq("dataElementUid")
        ) doReturn mock()
        whenever(
            ruleVariableRepository.byProgramUid().eq("programUid")
                .byDataElementUid().eq("dataElementUid").byUseCodeForOptionSet()
        ) doReturn mock()
        whenever(
            ruleVariableRepository.byProgramUid().eq("programUid")
                .byDataElementUid().eq("dataElementUid")
                .byUseCodeForOptionSet().isTrue
        ) doReturn mock()
        whenever(
            ruleVariableRepository.byProgramUid().eq("programUid")
                .byDataElementUid().eq("dataElementUid")
                .byUseCodeForOptionSet().isTrue.blockingIsEmpty()
        ) doReturn true

        whenever(optionRepository.byOptionSetUid()) doReturn mock()
        whenever(optionRepository.byOptionSetUid().eq("optionSetUid")) doReturn mock()
        whenever(
            optionRepository.byOptionSetUid().eq("optionSetUid")
                .byCode()
        ) doReturn mock()
        whenever(
            optionRepository.byOptionSetUid().eq("optionSetUid")
                .byCode().eq("optionCode")
        ) doReturn mock()
        whenever(
            optionRepository.byOptionSetUid().eq("optionSetUid")
                .byCode().eq("optionCode").one()
        ) doReturn mock()
        whenever(
            optionRepository.byOptionSetUid().eq("optionSetUid")
                .byCode().eq("optionCode").one().blockingExists()
        ) doReturn true

        whenever(
            optionRepository.byOptionSetUid().eq("optionSetUid")
                .byCode().eq("")
        ) doReturn mock()
        whenever(
            optionRepository.byOptionSetUid().eq("optionSetUid")
                .byCode().eq("").one()
        ) doReturn mock()
        whenever(
            optionRepository.byOptionSetUid().eq("optionSetUid")
                .byCode().eq("").one().blockingExists()
        ) doReturn false

        whenever(
            optionRepository.byOptionSetUid().eq("optionSetUid")
                .byCode().eq("optionCode").one().blockingGet()
        ) doReturn Option.builder()
            .uid("optionUid")
            .code("optionCode")
            .name("optionName")
            .build()

        val rules = getTrackedEntityDataValues().toRuleDataValue(
            Event.builder()
                .uid("eventUid")
                .program("programUid")
                .programStage("stageUid")
                .eventDate(Date())
                .build(),
            dataElementRepository,
            ruleVariableRepository,
            optionRepository
        )

        assertTrue(rules.size == 1)
        assertTrue(rules[0].value() == "optionName")
    }

    @Test
    fun `Should transform trackedEntityDataValues to ruleDataValues with optionCode value`() {
        whenever(dataElementRepository.uid("dataElementUid").blockingGet()) doReturn
            DataElement.builder()
                .uid("dataElementUid")
                .optionSet(ObjectWithUid.create("optionSetUid"))
                .valueType(ValueType.TEXT)
                .build()

        whenever(ruleVariableRepository.byProgramUid().eq("programUid")) doReturn mock()
        whenever(
            ruleVariableRepository.byProgramUid().eq("programUid")
                .byDataElementUid()
        ) doReturn mock()
        whenever(
            ruleVariableRepository.byProgramUid().eq("programUid")
                .byDataElementUid().eq("dataElementUid")
        ) doReturn mock()
        whenever(
            ruleVariableRepository.byProgramUid().eq("programUid")
                .byDataElementUid().eq("dataElementUid").byUseCodeForOptionSet()
        ) doReturn mock()
        whenever(
            ruleVariableRepository.byProgramUid().eq("programUid")
                .byDataElementUid().eq("dataElementUid").byUseCodeForOptionSet().isTrue
        ) doReturn mock()
        whenever(
            ruleVariableRepository.byProgramUid().eq("programUid")
                .byDataElementUid().eq("dataElementUid")
                .byUseCodeForOptionSet().isTrue.blockingIsEmpty()
        ) doReturn false

        val rules = getTrackedEntityDataValues().toRuleDataValue(
            Event.builder()
                .uid("eventUid")
                .program("programUid")
                .programStage("stageUid")
                .eventDate(Date())
                .build(),
            dataElementRepository,
            ruleVariableRepository,
            optionRepository
        )

        assertTrue(rules.size == 1)
        assertTrue(rules[0].value() == "optionCode")
    }

    @Test
    fun `Should transform trackedEntityAttributeValue to ruleDataValues with optionName value`() {
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes()
                .uid("attrUid").blockingGet()
        ) doReturn TrackedEntityAttribute.builder()
            .uid("attrUid")
            .optionSet(ObjectWithUid.create("optionSetUid"))
            .valueType(ValueType.TEXT)
            .build()

        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid")
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid").byTrackedEntityAttributeUid()
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid").byTrackedEntityAttributeUid().eq(
                    "attrUid"
                )
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid").byTrackedEntityAttributeUid().eq(
                    "attrUid"
                ).byUseCodeForOptionSet()
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid").byTrackedEntityAttributeUid().eq(
                    "attrUid"
                ).byUseCodeForOptionSet().isTrue
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid").byTrackedEntityAttributeUid().eq(
                    "attrUid"
                ).byUseCodeForOptionSet().isTrue.blockingIsEmpty()
        ) doReturn true

        whenever(
            d2.optionModule().options().byOptionSetUid()
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
                .byCode()
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
                .byCode().eq("optionCode")
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
                .byCode().eq("optionCode").one()
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
                .byCode().eq("optionCode").one().blockingExists()
        ) doReturn true

        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
                .byCode().eq("")
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
                .byCode().eq("").one()
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
                .byCode().eq("").one().blockingExists()
        ) doReturn false

        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
                .byCode().eq("optionCode").one().blockingGet()
        ) doReturn Option.builder()
            .uid("optionUid")
            .code("optionCode")
            .name("optionName")
            .build()

        val rules = getTrackedEntityAttributeValues()
            .toRuleAttributeValue(d2, "programUid")

        assertTrue(rules.size == 1)
        assertTrue(rules[0].value() == "optionName")
    }

    @Test
    fun `Should transform trackedEntityAttributeValue to ruleDataValues with optionCode value`() {
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid("attrUid").blockingGet()
        ) doReturn TrackedEntityAttribute.builder()
            .uid("dataElementUid")
            .optionSet(ObjectWithUid.create("optionSetUid"))
            .valueType(ValueType.TEXT)
            .build()

        whenever(
            d2.programModule().programRuleVariables().byProgramUid().eq("programUid")
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid").byTrackedEntityAttributeUid()
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid").byTrackedEntityAttributeUid().eq(
                    "attrUid"
                )
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid").byTrackedEntityAttributeUid().eq(
                    "attrUid"
                ).byUseCodeForOptionSet()
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid").byTrackedEntityAttributeUid().eq(
                    "attrUid"
                ).byUseCodeForOptionSet().isTrue
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid").byTrackedEntityAttributeUid().eq(
                    "attrUid"
                ).byUseCodeForOptionSet().isTrue.blockingIsEmpty()
        ) doReturn false

        val rules = getTrackedEntityAttributeValues()
            .toRuleAttributeValue(d2, "programUid")

        assertTrue(rules.size == 1)
        assertTrue(rules[0].value() == "optionCode")
    }

    private fun getTrackedEntityDataValues(): List<TrackedEntityDataValue> {
        return arrayListOf(
            TrackedEntityDataValue.builder()
                .dataElement("dataElementUid")
                .event("eventUid")
                .value("optionCode")
                .build(),
            TrackedEntityDataValue.builder()
                .dataElement("dataElementUid")
                .event("eventUid")
                .build()
        )
    }

    private fun getTrackedEntityAttributeValues(): List<TrackedEntityAttributeValue> {
        return arrayListOf(
            TrackedEntityAttributeValue.builder()
                .trackedEntityAttribute("attrUid")
                .trackedEntityInstance("teiUid")
                .value("optionCode")
                .build(),
            TrackedEntityAttributeValue.builder()
                .trackedEntityAttribute("attrUid")
                .trackedEntityInstance("teiUid")
                .build()
        )
    }
}
