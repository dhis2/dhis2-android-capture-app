package org.dhis2.usescases.teiDashboard

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeAttribute
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TeiAttributesProviderTest {

    lateinit var attributesProvider: TeiAttributesProvider
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Before
    fun setup() {
        attributesProvider = TeiAttributesProvider(d2)
    }

    @Test
    fun `Should return attributes values from TrackedEntityTypeAttributes`() {
        val teType = "teType"
        val teiUid = "teiUid"
        val expectedResult = arrayListOf("attrValue1", "attrValue2", "attrValue3")

        mockTrackedEntityTypeAttributes(teType)
        whenever(
            d2.trackedEntityModule().trackedEntityTypeAttributes()
                .byTrackedEntityTypeUid().eq(teType)
                .byDisplayInList().isTrue
                .blockingGet(),
        ) doReturn trackedEntityTypeAttributes()
        mockTrackedEntityAttributeValues(teType, teiUid)
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq(anyString())
                .byTrackedEntityAttribute().eq(anyString())
                .one().blockingGet(),
        ) doReturnConsecutively trackedEntityAttributeValues()

        val result = attributesProvider.getValuesFromTrackedEntityTypeAttributes(teType, teiUid)

        assert(result.size == 3)
        result.forEachIndexed { index, attributeValue ->
            assert(attributeValue.value() == expectedResult[index])
        }
    }

    @Test
    fun `Should return attributes values from ProgramTrackedEntityAttributes`() {
        val teType = "teType"
        val teiUid = "teiUid"
        val expectedResult = arrayListOf("attrValue1", "attrValue2", "attrValue3")

        mockProgramTrackedEntityAttributes(teType)
        whenever(
            d2.programModule().programTrackedEntityAttributes()
                .byProgram().eq(anyString())
                .byDisplayInList().isTrue
                .blockingGet(),
        ) doReturn programAttributeValues()
        mockTrackedEntityAttributeValues(teType, teiUid)
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq(anyString())
                .byTrackedEntityAttribute().eq(anyString())
                .one().blockingGet(),
        ) doReturnConsecutively trackedEntityAttributeValues()

        val result = attributesProvider.getValuesFromProgramTrackedEntityAttributes(teType, teiUid)

        assert(result.size == 3)
        result.forEachIndexed { index, attributeValue ->
            assert(attributeValue.value() == expectedResult[index])
        }
    }

    @Test
    fun `Should return attributes values from ProgramTrackedEntityAttributes using program Uid`() {
        val program = "program"
        val teiUid = "teiUid"
        val expectedResult = arrayListOf("attrValue1", "attrValue2", "attrValue3")

        mockProgramTrackedEntityAttributes(program)
        whenever(
            d2.programModule().programTrackedEntityAttributes()
                .byProgram().eq(anyString())
                .byDisplayInList().isTrue
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC),
        ) doReturn mock()
        whenever(
            d2.programModule().programTrackedEntityAttributes()
                .byProgram().eq(anyString())
                .byDisplayInList().isTrue
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                .blockingGet(),
        ) doReturn programAttributeValues()
        mockTrackedEntityAttributeValues(teiUid, program)
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq(anyString())
                .byTrackedEntityAttribute().eq(anyString())
                .one().blockingGet(),
        ) doReturnConsecutively trackedEntityAttributeValues()

        val testObserver =
            attributesProvider.getValuesFromProgramTrackedEntityAttributesByProgram(program, teiUid)
                .test()

        testObserver
            .assertNoErrors()
            .assertValue {
                it[0].value() == expectedResult[0] &&
                    it[1].value() == expectedResult[1] &&
                    it[2].value() == expectedResult[2]
            }
    }

    @Test
    fun `Should get list of attributeValues from ProgramTrackedEntityAttributes by programUid`() {
        val program = "program"
        val teiUid = "teiUid"
        val expectedResult = arrayListOf("attrValue1", "attrValue2", "attrValue3")

        mockProgramTrackedEntityAttributes(program)
        whenever(
            d2.programModule().programTrackedEntityAttributes()
                .byProgram().eq(anyString())
                .byDisplayInList().isTrue
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC),
        ) doReturn mock()
        whenever(
            d2.programModule().programTrackedEntityAttributes()
                .byProgram().eq(anyString())
                .byDisplayInList().isTrue
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                .blockingGet(),
        ) doReturn programAttributeValues()
        mockTrackedEntityAttributeValues(teiUid, program)
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq(anyString())
                .byTrackedEntityAttribute().eq(anyString())
                .one().blockingGet(),
        ) doReturnConsecutively trackedEntityAttributeValues()

        val attributes =
            attributesProvider
                .getListOfValuesFromProgramTrackedEntityAttributesByProgram(program, teiUid)

        assert(
            attributes[0].value() == expectedResult[0] &&
                attributes[1].value() == expectedResult[1] &&
                attributes[2].value() == expectedResult[2],
        )
    }

    private fun mockTrackedEntityTypeAttributes(teType: String) {
        whenever(d2.trackedEntityModule().trackedEntityTypeAttributes()) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypeAttributes().byTrackedEntityTypeUid(),
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypeAttributes()
                .byTrackedEntityTypeUid().eq(teType),
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypeAttributes()
                .byTrackedEntityTypeUid().eq(teType)
                .byDisplayInList(),
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypeAttributes()
                .byTrackedEntityTypeUid().eq(teType)
                .byDisplayInList().isTrue,
        ) doReturn mock()
    }

    private fun mockProgramTrackedEntityAttributes(teType: String, program: String? = "program") {
        whenever(d2.programModule().programs()) doReturn mock()
        whenever(d2.programModule().programs().byTrackedEntityTypeUid()) doReturn mock()
        whenever(d2.programModule().programs().byTrackedEntityTypeUid().eq(teType)) doReturn mock()
        whenever(
            d2.programModule().programs().byTrackedEntityTypeUid().eq(teType).blockingGet(),
        ) doReturn mock()
        whenever(
            d2.programModule().programs().byTrackedEntityTypeUid().eq(teType).blockingGet()[0],
        ) doReturn Program.builder().uid(program).build()

        whenever(d2.programModule().programTrackedEntityAttributes()) doReturn mock()
        whenever(d2.programModule().programTrackedEntityAttributes().byProgram()) doReturn mock()
        whenever(
            d2.programModule().programTrackedEntityAttributes().byProgram().eq(anyString()),
        ) doReturn mock()
        whenever(
            d2.programModule().programTrackedEntityAttributes()
                .byProgram().eq(anyString()).byDisplayInList(),
        ) doReturn mock()
        whenever(
            d2.programModule().programTrackedEntityAttributes()
                .byProgram().eq(anyString())
                .byDisplayInList().isTrue,
        ) doReturn mock()
    }

    private fun mockTrackedEntityAttributeValues(teiUid: String, teAttribute: String) {
        whenever(d2.trackedEntityModule().trackedEntityAttributeValues()) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance(),
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq(anyString()),
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq(anyString())
                .byTrackedEntityAttribute(),
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq(anyString())
                .byTrackedEntityAttribute().eq(anyString()),
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq(anyString())
                .byTrackedEntityAttribute().eq(anyString())
                .one(),
        ) doReturn mock()
    }

    private fun trackedEntityTypeAttributes(): List<TrackedEntityTypeAttribute> {
        return arrayListOf(
            TrackedEntityTypeAttribute.builder()
                .id(1)
                .displayInList(true)
                .searchable(true)
                .trackedEntityType(ObjectWithUid.create("teType"))
                .trackedEntityAttribute(ObjectWithUid.create("attr1"))
                .build(),
            TrackedEntityTypeAttribute.builder()
                .id(2)
                .displayInList(true)
                .searchable(true)
                .trackedEntityType(ObjectWithUid.create("teType"))
                .trackedEntityAttribute(ObjectWithUid.create("attr2"))
                .build(),
            TrackedEntityTypeAttribute.builder()
                .id(4)
                .searchable(true)
                .displayInList(true)
                .trackedEntityType(ObjectWithUid.create("teType"))
                .trackedEntityAttribute(ObjectWithUid.create("attr3"))
                .build(),
        )
    }

    private fun trackedEntityAttributeValues(): List<TrackedEntityAttributeValue> {
        return arrayListOf(
            TrackedEntityAttributeValue.builder()
                .id(1)
                .value("attrValue1")
                .trackedEntityAttribute("attr1")
                .build(),
            TrackedEntityAttributeValue.builder()
                .id(2)
                .value("attrValue2")
                .trackedEntityAttribute("attr2")
                .build(),
            TrackedEntityAttributeValue.builder()
                .id(3)
                .value("attrValue3")
                .trackedEntityAttribute("attr3")
                .build(),
        )
    }

    private fun programAttributeValues(): List<ProgramTrackedEntityAttribute> {
        return arrayListOf(
            ProgramTrackedEntityAttribute.builder()
                .uid("programAttr1")
                .trackedEntityAttribute(ObjectWithUid.create(("attr1")))
                .build(),
            ProgramTrackedEntityAttribute.builder()
                .uid("programAttr2")
                .trackedEntityAttribute(ObjectWithUid.create(("attr2")))
                .build(),
            ProgramTrackedEntityAttribute.builder()
                .uid("programAttr3")
                .trackedEntityAttribute(ObjectWithUid.create(("attr3")))
                .build(),
        )
    }
}
