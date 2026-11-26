package org.dhis2.mobile.extension.web.customform

import org.junit.Assert.assertEquals
import org.junit.Test

class CustomFormPageTest {
    @Test
    fun `a form without scripts opens on the plain page`() {
        val html = """<table><tr><td>BCG</td><td><input id="t99PL3gUxIl-HllvX50cXC0-val" /></td></tr></table>"""

        assertEquals(CustomFormPage.PLAIN, CustomFormPage.of(html))
    }

    @Test
    fun `styles and the word on in text do not need the legacy shim`() {
        val html = """<style>td { width: 3em; }</style><p>Report on stock, onion = 3</p>"""

        assertEquals(CustomFormPage.PLAIN, CustomFormPage.of(html))
    }

    @Test
    fun `a form with a script needs the legacy shim`() {
        val html = """<script type="text/javascript">dhis2.util.on('dhis2.de.event.formReady', f);</script>"""

        assertEquals(CustomFormPage.LEGACY, CustomFormPage.of(html))
    }

    @Test
    fun `an inline event handler needs the legacy shim`() {
        val html = """<button class="tablinks" onclick="openCity(event.currentTarget, 'tab1')">Tab</button>"""

        assertEquals(CustomFormPage.LEGACY, CustomFormPage.of(html))
    }

    @Test
    fun `a javascript link needs the legacy shim`() {
        val html = """<a href="javascript:showTab(2)">Tab 2</a>"""

        assertEquals(CustomFormPage.LEGACY, CustomFormPage.of(html))
    }
}
