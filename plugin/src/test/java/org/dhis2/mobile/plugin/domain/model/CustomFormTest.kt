package org.dhis2.mobile.plugin.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CustomFormTest {
    @Test
    fun `keeps the html of the data sets whose form type is custom`() {
        val response =
            """
            {"dataSets":[
              {"id":"TuL8IOPzpHh","formType":"CUSTOM","dataEntryForm":{"htmlCode":"<h2>EPI Stock</h2>"}},
              {"id":"BfMAe6Itzgt","formType":"SECTION","dataEntryForm":{"htmlCode":"<p>stale custom form</p>"}},
              {"id":"QX4ZTUbOt3a","formType":"DEFAULT"},
              {"id":"eZDhcZi6FLP","formType":"CUSTOM","dataEntryForm":{"htmlCode":"  "}}
            ]}
            """.trimIndent()

        assertEquals(mapOf("TuL8IOPzpHh" to "<h2>EPI Stock</h2>"), parseCustomForms(response))
    }

    @Test
    fun `a field is keyed as the form's input ids are, without the -val suffix`() {
        assertEquals("t99PL3gUxIl-HllvX50cXC0", CustomFormFieldId("t99PL3gUxIl", "HllvX50cXC0").key)
    }
}
