package org.dhis2.mobile.commons.providers

import kotlin.test.Test
import kotlin.test.assertEquals

class CustomLabelFormatterTest {
    @Test
    fun `capitalizes term at the start of the sentence`() {
        assertEquals("Visit details", "%s details".formatWithTerm("visit"))
    }

    @Test
    fun `capitalizes positional term at the start of the sentence`() {
        assertEquals("Visit cancelled", "%1\$s cancelled".formatWithTerm("visit"))
    }

    @Test
    fun `capitalizes term after leading whitespace`() {
        assertEquals("  Visit details", "  %s details".formatWithTerm("visit"))
    }

    @Test
    fun `capitalizes term starting a new sentence`() {
        assertEquals("Done. Visit next", "Done. %s next".formatWithTerm("visit"))
    }

    @Test
    fun `capitalizes term starting a new line`() {
        assertEquals("Done\nVisit next", "Done\n%s next".formatWithTerm("visit"))
    }

    @Test
    fun `keeps term as configured in the middle of a sentence`() {
        assertEquals("Mark for follow-up", "Mark for %s".formatWithTerm("follow-up"))
    }

    @Test
    fun `keeps mixed case term untouched in the middle of a sentence`() {
        assertEquals("Delete eVisit", "Delete %s".formatWithTerm("eVisit"))
        assertEquals("Delete HIV case", "Delete %s".formatWithTerm("HIV case"))
    }

    @Test
    fun `does not treat an abbreviation dot without space as a sentence end`() {
        assertEquals("e.visit", "e.%s".formatWithTerm("visit"))
    }

    @Test
    fun `keeps term after a quantity as configured`() {
        assertEquals("3 visits", "%1\$d %2\$s".formatWithTerm("visits", quantity = 3))
    }

    @Test
    fun `capitalizes term placed before the quantity`() {
        assertEquals("Visits: 3", "%2\$s: %1\$d".formatWithTerm("visits", quantity = 3))
    }

    @Test
    fun `ignores escaped percent signs`() {
        assertEquals("100% visit", "100%% %s".formatWithTerm("visit"))
    }

    @Test
    fun `formats empty term`() {
        assertEquals(" details", "%s details".formatWithTerm(""))
    }
}
