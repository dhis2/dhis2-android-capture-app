package org.dhis2.usecases.eventsWithoutRegistration.eventCapture

import org.junit.Assert
import org.junit.Test

class EventCaptureRepositoryFunctionsTest {
    private val names = mapOf("1" to "name1", "2" to "name2", "3" to "name3")
    private val getDataElementDisplayName = { uid: String -> names[uid] ?: "" }

    @Test
    fun `Should return expected program stage name replacing data element uid tokens for one data element`() {
        val attValue = "Name: {{1}}"

        val result = getProgramStageNameByAttributeValue(attValue, getDataElementDisplayName)

        Assert.assertEquals("Name: name1", result)
    }

    @Test
    fun `Should return expected program stage name replacing data element uid tokens for two data elements`() {
        val attValue = "Name: {{1}} Name2: {{2}}"

        val result = getProgramStageNameByAttributeValue(attValue, getDataElementDisplayName)

        Assert.assertEquals("Name: name1 Name2: name2", result)
    }

    @Test
    fun `Should return expected program stage name replacing data element uid tokens for three data elements`() {
        val attValue = "Name: {{1}} Name2: {{2}} Name3: {{3}}"

        val result = getProgramStageNameByAttributeValue(attValue, getDataElementDisplayName)

        Assert.assertEquals("Name: name1 Name2: name2 Name3: name3", result)
    }

    @Test
    fun `Should return expected program stage name replacing data element uid tokens for two equal data elements`() {
        val attValue = "Name: {{1}} Name2: {{1}}"

        val result = getProgramStageNameByAttributeValue(attValue, getDataElementDisplayName)

        Assert.assertEquals("Name: name1 Name2: name1", result)
    }

}
