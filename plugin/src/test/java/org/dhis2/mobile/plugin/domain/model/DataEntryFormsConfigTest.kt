package org.dhis2.mobile.plugin.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DataEntryFormsConfigTest {
    private val config =
        """
        {
          "WZbXY0S00lP": [
            {
              "id": "section-1",
              "name": "Allergies",
              "elements": [
                {
                  "id": "matrix-allergies",
                  "type": "plugin",
                  "pluginSource": "http://localhost:8080/api/apps/matrix-select/plugin.html",
                  "fieldMap": [
                    {"IdFromApp": "QFX1FLWBwtq", "IdFromPlugin": "allergies", "objectType": "TrackedEntityAttribute"},
                    {"IdFromApp": "E6QaDtrQP5e", "IdFromPlugin": "penicillin", "objectType": "TrackedEntityAttribute"},
                    {"IdFromApp": "attr1", "IdFromPlugin": "category", "objectType": "Attribute"}
                  ]
                }
              ]
            },
            {
              "id": "section-2",
              "name": "Visit details",
              "elements": [{"id": "abc", "type": "DataElement"}]
            }
          ],
          "IpHINAT79UW": [
            {"id": "s", "name": "n", "elements": [{"id": "w75KJ2mc4zz", "type": "TrackedEntityAttribute"}]}
          ]
        }
        """.trimIndent()

    @Test
    fun `reads every plugin element with the form it belongs to`() {
        val placements = parseDataEntryForms(config).placements()

        val placement = placements.single()
        assertEquals("WZbXY0S00lP", placement.formUid)
        assertEquals("matrix-allergies", placement.elementId)
        assertEquals("matrix-select", placement.appKey)
    }

    @Test
    fun `keeps the field map order, which is the order the plugin renders fields in`() {
        val fields = parseDataEntryForms(config).placements().single().fields

        assertEquals(listOf("allergies", "penicillin"), fields.map { it.alias })
        assertEquals(listOf("QFX1FLWBwtq", "E6QaDtrQP5e"), fields.map { it.uid })
    }

    @Test
    fun `metadata attributes are not form fields`() {
        val placement = parseDataEntryForms(config).placements().single()

        assertTrue(placement.fields.none { it.alias == "category" })
    }

    @Test
    fun `a missing or malformed configuration is no plugins`() {
        assertEquals(emptyList<FormLayout>(), parseDataEntryForms(""))
        assertEquals(emptyList<FormLayout>(), parseDataEntryForms("{ not json"))
    }

    @Test
    fun `one malformed form does not cost the others`() {
        val placements =
            parseDataEntryForms(
                """
                {
                  "broken": "not an array",
                  "stage": [{"id": "s", "elements": [
                    {"id": "p", "type": "plugin", "pluginSource": "/api/apps/matrix-select/plugin.html", "fieldMap": []}
                  ]}]
                }
                """.trimIndent(),
            )

        assertEquals(listOf("stage"), placements.placements().map { it.formUid })
    }

    @Test
    fun `keeps every form's sections and elements in configured order`() {
        val layout = parseDataEntryForms(config).single { it.formUid == "WZbXY0S00lP" }

        assertEquals(listOf("section-1", "section-2"), layout.sections.map { it.id })
        assertEquals("Allergies", layout.sections.first().name)
        assertEquals("matrix-allergies", (layout.sections[0].elements.single() as FormLayoutElement.Plugin).placement.elementId)
        assertEquals(FormLayoutElement.Field("abc"), layout.sections[1].elements.single())
    }

    @Test
    fun `a section without a name has none, as the Tracker Plugin Configurator writes it`() {
        val layouts =
            parseDataEntryForms("""{"stage": [{"id": "EzhVeTS7vx4", "elements": [{"id": "de", "type": "TrackedEntityAttribute"}]}]}""")

        assertNull(
            layouts
                .single()
                .sections
                .single()
                .name,
        )
    }

    @Test
    fun `a form configured without plugins still has a layout`() {
        // The web Capture app renders such a form from the configuration too.
        assertTrue(parseDataEntryForms(config).any { it.formUid == "IpHINAT79UW" })
    }

    @Test
    fun `the app key comes from the plugin source path, whatever host it names`() {
        assertEquals("matrix-select", pluginAppKey("https://play.dhis2.org/dev/api/apps/matrix-select/plugin.html"))
        assertEquals("growth-chart", pluginAppKey("/api/apps/growth-chart/plugin.html?x=1"))
    }

    @Test
    fun `a plugin served from anywhere but an installed app has no app key`() {
        // A developer's `d2-app-scripts start` URL: there is no installed bundle to download.
        assertNull(pluginAppKey("http://localhost:3001/plugin.html"))
    }
}
