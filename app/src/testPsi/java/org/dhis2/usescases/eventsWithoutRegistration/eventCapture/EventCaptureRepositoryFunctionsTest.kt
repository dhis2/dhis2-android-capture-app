package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import org.dhis2.utils.extension.invoke
import org.hisp.dhis.android.core.option.Option
import org.junit.Assert
import org.junit.Test

class EventCaptureRepositoryFunctionsTest {
    private val selectedOption = Option.builder()
        .uid("2")
        .code("code2")
        .name("name2")
        .displayName("displayName2").build()

    private val names = mapOf(
        "DE_UID_1" to "value1",
        "DE_UID_2" to selectedOption,
        "DE_UID_3" to "value3"
    )

    private val getDataElementValue = { uid: String, prop: String ->
        val value = names[uid] ?: ""

        if (value is Option) {
            value.invoke(prop) as String
        } else {
            value as String
        }
    }

    @Test
    fun `should replace data element uid token with displayName of selected option by default if DE is of option set type`() {
        val attValue = "Name2: {{DE_UID_2}}"

        val result = getProgramStageNameByAttributeValue(attValue, getDataElementValue)

        Assert.assertEquals("Name2: displayName2", result)
    }

    @Test
    fun `should replace data element uid token with name of selected option if DE is of option set type`() {
        val attValue = "Name2: {{DE_UID_2.name}}"

        val result = getProgramStageNameByAttributeValue(attValue, getDataElementValue)

        Assert.assertEquals("Name2: name2", result)
    }

    @Test
    fun `should replace data element uid token with code of selected option if DE is of option set type`() {
        val attValue = "Name2: {{DE_UID_2.code}}"

        val result = getProgramStageNameByAttributeValue(attValue, getDataElementValue)

        Assert.assertEquals("Name2: code2", result)
    }


    @Test
    fun `should replace data element uid token with its value`() {
        val attValue = "Name: {{DE_UID_1}}"

        val result = getProgramStageNameByAttributeValue(attValue, getDataElementValue)

        Assert.assertEquals("Name: value1", result)
    }

    @Test
    fun `Should return expected program stage name replacing data element uid tokens for two data elements`() {
        val attValue = "Name: {{DE_UID_1}} Name3: {{DE_UID_3}}"

        val result = getProgramStageNameByAttributeValue(attValue, getDataElementValue)

        Assert.assertEquals("Name: value1 Name3: value3", result)
    }

    @Test
    fun `Should return expected program stage name replacing data element uid tokens for three data elements`() {
        val attValue = "Name: {{DE_UID_1}} Name2: {{DE_UID_2}} Name3: {{DE_UID_3}}"

        val result = getProgramStageNameByAttributeValue(attValue, getDataElementValue)

        Assert.assertEquals("Name: value1 Name2: displayName2 Name3: value3", result)
    }

    @Test
    fun `Should return expected program stage name replacing data element uid tokens for two equal data elements`() {
        val attValue = "Name: {{DE_UID_1}} Name2: {{DE_UID_2}}"

        val result = getProgramStageNameByAttributeValue(attValue, getDataElementValue)

        Assert.assertEquals("Name: value1 Name2: displayName2", result)
    }
}
