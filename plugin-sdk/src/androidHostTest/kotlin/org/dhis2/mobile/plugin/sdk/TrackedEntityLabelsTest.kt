package org.dhis2.mobile.plugin.sdk

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * The label mapping, on the JVM.
 *
 * No `D2` and no mocks: the mapping takes data, so the test builds that data through the SDK's own
 * builders. The querying that produces [DisplayAttribute]s needs a device and is not tested here —
 * but the ordering and the skipping are where every label defect has actually been.
 */
class TrackedEntityLabelsTest {

    private companion object {
        const val TEI_UID = "tei-uid"

        val FIRST_NAME = DisplayAttribute(uid = "attr-first", label = "First name")
        val LAST_NAME = DisplayAttribute(uid = "attr-last", label = "Last name")
        val GENDER = DisplayAttribute(uid = "attr-gender", label = "Gender")
    }

    // trackedEntityInstance is lateinit on the SDK's builder, so it has to be set even though the
    // mapping never reads it.
    private fun value(attribute: String, value: String?) =
        TrackedEntityAttributeValue.builder()
            .trackedEntityAttribute(attribute)
            .trackedEntityInstance(TEI_UID)
            .value(value)
            .build()

    private fun tei(vararg values: TrackedEntityAttributeValue) =
        TrackedEntityInstance.builder()
            .uid(TEI_UID)
            .trackedEntityAttributeValues(values.toList())
            .build()

    @Test
    fun `labels the values the programme lists, in the programme's order`() {
        // The values arrive last-name-first, as the SDK is free to.
        val tei = tei(
            value("attr-last", "Ryder"),
            value("attr-first", "Filona"),
        )

        val labelled = labelled(tei, listOf(FIRST_NAME, LAST_NAME))

        // The programme's order wins over the order the values came back in. This is the assertion
        // the whole helper exists for.
        assertEquals(
            listOf(
                LabelledAttribute("First name", "Filona"),
                LabelledAttribute("Last name", "Ryder"),
            ),
            labelled,
        )
    }

    @Test
    fun `ignores attributes the programme does not list`() {
        val tei = tei(
            value("attr-first", "Filona"),
            value("attr-gender", "Female"),
        )

        val labelled = labelled(tei, listOf(FIRST_NAME))

        // Rendering every attribute is what produced "Gender: Female / First name: Filona".
        assertEquals(listOf(LabelledAttribute("First name", "Filona")), labelled)
    }

    @Test
    fun `skips a listed attribute the tracked entity has no value for`() {
        val tei = tei(value("attr-first", "Filona"))

        val labelled = labelled(tei, listOf(FIRST_NAME, LAST_NAME))

        assertEquals(listOf(LabelledAttribute("First name", "Filona")), labelled)
    }

    @Test
    fun `treats a null or blank value as absent rather than rendering an empty row`() {
        val tei = tei(
            value("attr-first", null),
            value("attr-last", "   "),
            value("attr-gender", "Female"),
        )

        val labelled = labelled(tei, listOf(FIRST_NAME, LAST_NAME, GENDER))

        assertEquals(listOf(LabelledAttribute("Gender", "Female")), labelled)
    }

    @Test
    fun `returns nothing when the programme lists no attributes`() {
        val tei = tei(value("attr-first", "Filona"))

        // Empty is the caller's signal to fall back — labelsFor() turns this into the org unit name.
        assertEquals(emptyList(), labelled(tei, emptyList()))
    }

    @Test
    fun `returns nothing when the tracked entity has no values at all`() {
        val tei = TrackedEntityInstance.builder().uid(TEI_UID).build()

        assertEquals(emptyList(), labelled(tei, listOf(FIRST_NAME)))
    }

    // The labeller itself, with its fallback injected — no D2 needed, which is the point of taking
    // the fallback as a lambda rather than querying inside the class.

    private val orgUnit = LabelledAttribute("Organisation unit", "Ngelehun CHC")

    private fun labeller(attributes: List<DisplayAttribute>) =
        TrackedEntityLabeller(attributes) { orgUnit }

    @Test
    fun `falls back to something a human recognises when the programme lists nothing`() {
        val tei = tei(value("attr-first", "Filona"))

        // The entity *has* a value; it is just not one the programme lists. Rendering it anyway is
        // the "labelled Female" bug.
        assertEquals(listOf(orgUnit), labeller(emptyList()).labelsFor(tei))
    }

    @Test
    fun `falls back when the entity has no value for any listed attribute`() {
        val tei = tei(value("attr-gender", "Female"))

        assertEquals(listOf(orgUnit), labeller(listOf(FIRST_NAME, LAST_NAME)).labelsFor(tei))
    }

    @Test
    fun `never falls back when a listed attribute does have a value`() {
        val tei = tei(value("attr-last", "Ryder"))

        assertEquals(
            listOf(LabelledAttribute("Last name", "Ryder")),
            labeller(listOf(FIRST_NAME, LAST_NAME)).labelsFor(tei),
        )
    }

    @Test
    fun `renders a name when the caller wants one string`() {
        val tei = tei(
            value("attr-last", "Ryder"),
            value("attr-first", "Filona"),
        )

        // Given name before family name, because that is the order the programme configured.
        assertEquals("Filona Ryder", labeller(listOf(FIRST_NAME, LAST_NAME)).labelFor(tei))
    }

    @Test
    fun `yields an empty name rather than a uid when nothing resolves`() {
        val tei = TrackedEntityInstance.builder().uid(TEI_UID).build()
        val withoutFallback = TrackedEntityLabeller(listOf(FIRST_NAME)) { null }

        val name = withoutFallback.labelFor(tei)

        // The actual defect being prevented: a UID must never reach a human.
        assertEquals("", name)
        assertFalse(TEI_UID in name)
    }
}
