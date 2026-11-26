package org.dhis2.mobile.extension.host

import org.dhis2.mobile.extension.host.domain.NoFormScripts
import org.dhis2.mobile.extension.host.model.FailureReason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NoFormScriptsTest {
    private val rule = NoFormScripts()

    @Test
    fun `a form without scripts is supported`() {
        val html = """<style>td { width: 3em; }</style><p>Report on stock, onion = 3</p><input id="t99PL3gUxIl-HllvX50cXC0-val" />"""

        assertNull(rule.check(html))
    }

    @Test
    fun `a form with a script is not supported yet`() {
        val html = """<script type="text/javascript">dhis2.util.on('dhis2.de.event.formReady', f);</script>"""

        assertEquals(FailureReason.UNSUPPORTED_FEATURE, rule.check(html))
    }

    @Test
    fun `an inline event handler is not supported yet`() {
        val html = """<button onclick="openCity(event.currentTarget, 'tab1')">Tab</button>"""

        assertEquals(FailureReason.UNSUPPORTED_FEATURE, rule.check(html))
    }

    @Test
    fun `a javascript link is not supported yet`() {
        val html = """<a href="javascript:showTab(2)">Tab 2</a>"""

        assertEquals(FailureReason.UNSUPPORTED_FEATURE, rule.check(html))
    }
}
