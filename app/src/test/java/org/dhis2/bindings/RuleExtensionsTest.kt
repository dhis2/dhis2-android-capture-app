package org.dhis2.bindings

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.Bindings.toRuleVariableList
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.dataelement.DataElementCollectionRepository
import org.hisp.dhis.android.core.program.ProgramRuleVariable
import org.hisp.dhis.android.core.program.ProgramRuleVariableSourceType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeCollectionRepository
import org.junit.Test

class RuleExtensionsTest {
    private val dataElementRepository: DataElementCollectionRepository = mock()
    private val attributeRepository: TrackedEntityAttributeCollectionRepository = mock()

    @Test
    fun `Should remove variables with data elements or attributes that do not exist`() {
        whenever(dataElementRepository.uid("dataElement1")) doReturn mock()
        whenever(dataElementRepository.uid("dataElement2")) doReturn mock()
        whenever(dataElementRepository.uid("dataElement3")) doReturn mock()
        whenever(attributeRepository.uid("attr1")) doReturn mock()
        whenever(attributeRepository.uid("attr2")) doReturn mock()

        whenever(dataElementRepository.uid("dataElement1").blockingExists()) doReturn true
        whenever(dataElementRepository.uid("dataElement2").blockingExists()) doReturn false
        whenever(dataElementRepository.uid("dataElement3").blockingExists()) doReturn true
        whenever(attributeRepository.uid("attr1").blockingExists()) doReturn true
        whenever(attributeRepository.uid("attr2").blockingExists()) doReturn false

        whenever(dataElementRepository.uid("dataElement1").blockingGet()) doReturn mockedDataElement()
        whenever(dataElementRepository.uid("dataElement3").blockingGet()) doReturn mockedDataElement()
        whenever(attributeRepository.uid("attr2").blockingGet()) doReturn mockedAttribute()

        val variables =
            getVariableList().toRuleVariableList(attributeRepository, dataElementRepository)

        assert(variables.size == 3)
    }

    private fun getObject(uid: String): ObjectWithUid {
        return ObjectWithUid.create(uid)
    }

    private fun mockedDataElement(): DataElement {
        return DataElement.builder()
            .uid("dataElement")
            .valueType(ValueType.TEXT)
            .build()
    }

    private fun mockedAttribute(): TrackedEntityAttribute {
        return TrackedEntityAttribute.builder()
            .uid("attr")
            .valueType(ValueType.TEXT)
            .build()
    }

    private fun getVariableList(): List<ProgramRuleVariable> {
        return arrayListOf(
            ProgramRuleVariable.builder()
                .dataElement(getObject("dataElement1"))
                .program(getObject("program"))
                .programRuleVariableSourceType(ProgramRuleVariableSourceType.DATAELEMENT_CURRENT_EVENT)
                .programStage(getObject("programStage"))
                .trackedEntityAttribute(null)
                .useCodeForOptionSet(false)
                .uid("variable1")
                .displayName("variable1")
                .build(),
            ProgramRuleVariable.builder()
                .dataElement(getObject("dataElement2"))
                .program(getObject("program"))
                .programRuleVariableSourceType(ProgramRuleVariableSourceType.DATAELEMENT_CURRENT_EVENT)
                .programStage(getObject("programStage"))
                .trackedEntityAttribute(null)
                .useCodeForOptionSet(false)
                .uid("variable2")
                .displayName("variable2")
                .build(),
            ProgramRuleVariable.builder()
                .dataElement(getObject("dataElement3"))
                .program(getObject("program"))
                .programRuleVariableSourceType(ProgramRuleVariableSourceType.DATAELEMENT_CURRENT_EVENT)
                .programStage(getObject("programStage"))
                .trackedEntityAttribute(null)
                .useCodeForOptionSet(false)
                .uid("variable3")
                .displayName("variable3")
                .build(),
            ProgramRuleVariable.builder()
                .dataElement(null)
                .program(getObject("program"))
                .programRuleVariableSourceType(ProgramRuleVariableSourceType.DATAELEMENT_CURRENT_EVENT)
                .programStage(getObject("programStage"))
                .trackedEntityAttribute(getObject("attr1"))
                .useCodeForOptionSet(false)
                .uid("variable1")
                .displayName("variable1")
                .build(),
            ProgramRuleVariable.builder()
                .dataElement(null)
                .program(getObject("program"))
                .programRuleVariableSourceType(ProgramRuleVariableSourceType.DATAELEMENT_CURRENT_EVENT)
                .programStage(getObject("programStage"))
                .trackedEntityAttribute(getObject("attr2"))
                .useCodeForOptionSet(false)
                .uid("variable1")
                .displayName("variable1")
                .build()
        )
    }
}