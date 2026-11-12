package org.dhis2.form.ui.mapper

import org.dhis2.form.data.EventRepository.Companion.EVENT_DETAILS_SECTION_UID
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FormSection
import org.dhis2.form.model.SectionPlugin
import org.dhis2.mobile.plugin.domain.model.FormFieldPluginPlacement
import org.dhis2.mobile.plugin.domain.model.FormLayout
import org.dhis2.mobile.plugin.domain.model.FormLayoutElement
import org.dhis2.mobile.plugin.domain.model.FormLayoutSection
import org.dhis2.mobile.plugin.domain.model.PluginField
import org.hisp.dhis.mobile.ui.designsystem.component.SectionState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class FormLayoutMapperTest {
    private fun field(uid: String): FieldUiModel = mock { on { this.uid } doReturn uid }

    private fun section(
        uid: String,
        vararg fieldUids: String,
    ) = FormSection(uid = uid, title = uid, state = SectionState.OPEN, fields = fieldUids.map(::field))

    private val eventDetails = section(EVENT_DETAILS_SECTION_UID, "EVENT_REPORT_DATE_UID")
    private val conditions = section("conditions", "chronic", "allergies", "penicillin", "medication")
    private val visit = section("visit", "weight", "height")
    private val sections = listOf(eventDetails, conditions, visit)

    private fun plugin(
        id: String,
        vararg mapped: String,
    ) = FormLayoutElement.Plugin(
        FormFieldPluginPlacement("stage", id, "/api/apps/matrix-select/plugin.html", mapped.map { PluginField(it, "alias-$it") }),
    )

    private fun fields(vararg uids: String) = uids.map { FormLayoutElement.Field(it) }.toTypedArray()

    private fun layoutOf(vararg sections: FormLayoutSection) = FormLayout("stage", sections.toList())

    private fun uids(section: FormSection) = section.fields.map { it.uid }

    @Test
    fun `sections and fields keep the order the SDK gives them`() {
        // The configuration is a snapshot of the maintenance order; the SDK has the current one.
        val result =
            sections.withLayout(
                layoutOf(
                    FormLayoutSection("visit", null, fields("height", "weight").toList()),
                    FormLayoutSection("conditions", null, fields("medication", "chronic", "allergies", "penicillin").toList()),
                ),
            )

        assertEquals(listOf(EVENT_DETAILS_SECTION_UID, "conditions", "visit"), result.map { it.uid })
        assertEquals(listOf("chronic", "allergies", "penicillin", "medication"), uids(result[1]))
    }

    @Test
    fun `a plugin is placed before the field that follows it in the configuration`() {
        val result =
            sections.withLayout(
                layoutOf(
                    FormLayoutSection(
                        "conditions",
                        null,
                        listOf(FormLayoutElement.Field("chronic"), plugin("matrix"), *fields("allergies", "penicillin", "medication")),
                    ),
                ),
            )

        assertEquals(listOf(SectionPlugin("matrix", position = 1)), result[1].plugins)
    }

    @Test
    fun `a plugin stays after the field before it when program rules hide the fields after it`() {
        // Configured: chronic, plugin, diabetes, medication. The form hides diabetes until chronic is
        // Yes, and shows allergy fields the configuration does not list before medication.
        val result =
            sections.withLayout(
                layoutOf(
                    FormLayoutSection(
                        "conditions",
                        null,
                        listOf(FormLayoutElement.Field("chronic"), plugin("matrix"), *fields("diabetes", "medication")),
                    ),
                ),
            )

        assertEquals(listOf(SectionPlugin("matrix", position = 1)), result[1].plugins)
    }

    @Test
    fun `a plugin first in its section goes before the first field`() {
        val result =
            sections.withLayout(
                layoutOf(FormLayoutSection("conditions", null, listOf(plugin("matrix"), *fields("chronic", "allergies")))),
            )

        assertEquals(listOf(SectionPlugin("matrix", position = 0)), result[1].plugins)
    }

    @Test
    fun `a plugin with no field after it goes at the end of its section`() {
        val result = sections.withLayout(layoutOf(FormLayoutSection("visit", null, listOf(*fields("weight", "height"), plugin("matrix")))))

        assertEquals(listOf(SectionPlugin("matrix", position = 2)), result[2].plugins)
    }

    @Test
    fun `no field is hidden, whatever the configuration lists`() {
        // The Tracker Plugin Configurator only places plugins; every native field stays, as on the web.
        val result =
            sections.withLayout(
                layoutOf(
                    FormLayoutSection(
                        "conditions",
                        null,
                        listOf(plugin("matrix", "allergies", "penicillin"), *fields("chronic", "medication")),
                    ),
                ),
            )

        assertEquals(listOf("chronic", "allergies", "penicillin", "medication"), uids(result[1]))
        assertEquals(listOf(SectionPlugin("matrix", position = 0)), result[1].plugins)
    }

    @Test
    fun `a plugin in a section the form does not have goes where its next field is`() {
        val result =
            sections.withLayout(
                layoutOf(FormLayoutSection("custom-section", "Allergies", listOf(plugin("matrix"), FormLayoutElement.Field("height")))),
            )

        assertEquals(listOf(SectionPlugin("matrix", position = 1)), result[2].plugins)
    }

    @Test
    fun `sections the app adds itself are left untouched`() {
        val result = sections.withLayout(layoutOf(FormLayoutSection("conditions", null, listOf(plugin("matrix")))))

        assertEquals(eventDetails, result.first())
    }
}
