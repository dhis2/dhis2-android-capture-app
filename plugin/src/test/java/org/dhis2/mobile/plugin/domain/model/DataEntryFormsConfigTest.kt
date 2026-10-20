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
        val placements = parseDataEntryForms(config)

        val placement = placements.single()
        assertEquals("WZbXY0S00lP", placement.formUid)
        assertEquals("matrix-allergies", placement.elementId)
        assertEquals("matrix-select", placement.appKey)
    }

    @Test
    fun `keeps the field map order, which is the order the plugin renders fields in`() {
        val fields = parseDataEntryForms(config).single().fields

        assertEquals(listOf("allergies", "penicillin"), fields.map { it.alias })
        assertEquals(listOf("QFX1FLWBwtq", "E6QaDtrQP5e"), fields.map { it.uid })
    }

    @Test
    fun `metadata attributes are not form fields`() {
        val placement = parseDataEntryForms(config).single()

        assertTrue(placement.fields.none { it.alias == "category" })
    }

    @Test
    fun `a missing or malformed configuration is no plugins`() {
        assertEquals(emptyList<FormFieldPluginPlacement>(), parseDataEntryForms(""))
        assertEquals(emptyList<FormFieldPluginPlacement>(), parseDataEntryForms("{ not json"))
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

        assertEquals(listOf("stage"), placements.map { it.formUid })
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
