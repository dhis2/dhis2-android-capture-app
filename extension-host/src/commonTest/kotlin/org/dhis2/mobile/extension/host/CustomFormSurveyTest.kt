package org.dhis2.mobile.extension.host

import org.dhis2.mobile.extension.host.domain.surveyCustomForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CustomFormSurveyTest {
    @Test
    fun `plain html form has fields and nothing else`() {
        val survey =
            surveyCustomForm(
                """<table><tr><td><input id="t99PL3gUxIl-HllvX50cXC0-val" /></td>
                <td><input id="s46m5MS0hxu-HllvX50cXC0-val" /></td></tr></table>""",
            )

        assertEquals(2, survey.fields)
        assertFalse(survey.hasScript)
        assertTrue(survey.legacyCalls.isEmpty())
        assertTrue(survey.apiPaths.isEmpty())
        assertNull(survey.widestFixedWidth)
    }

    @Test
    fun `counts totals and indicators`() {
        val survey =
            surveyCustomForm(
                """<input id="totalt99PL3gUxIl" name="total" dataelementid="t99PL3gUxIl" />
                <input id="indicatorUvn6LCg7dVU" indicatorid="Uvn6LCg7dVU" />""",
            )

        assertEquals(1, survey.totals)
        assertEquals(1, survey.indicators)
    }

    @Test
    fun `lists the legacy calls and which are not supported`() {
        val survey =
            surveyCustomForm(
                """<script>
                dhis2.util.on(dhis2.de.event.formReady, function () {});
                dhis2.de.loadForm();
                </script>""",
            )

        assertEquals(1, survey.inlineScripts)
        assertEquals(setOf("dhis2.util.on", "dhis2.de.event.formReady", "dhis2.de.loadForm"), survey.legacyCalls)
        assertEquals(setOf("dhis2.de.loadForm"), survey.unsupportedCalls)
    }

    @Test
    fun `lists external scripts and api paths`() {
        val survey =
            surveyCustomForm(
                """<script src="https://cdn.example.org/chart.js"></script>
                <script>$.getJSON('../api/dataValueSets.json?dataSet=x', draw);</script>""",
            )

        assertEquals(setOf("https://cdn.example.org/chart.js"), survey.externalScripts)
        assertEquals(1, survey.inlineScripts)
        assertEquals(setOf("api/dataValueSets.json"), survey.apiPaths)
    }

    @Test
    fun `finds the widest fixed width`() {
        val survey = surveyCustomForm("""<table width="800"><tr><td style="width: 1100px">x</td></tr></table>""")

        assertEquals(1100, survey.widestFixedWidth)
    }
}
