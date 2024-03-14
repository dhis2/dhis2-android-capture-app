package org.dhis2.bindings

import org.junit.Test

class StringExtensionsTest {

    @Test
    fun `Should return one initial for a string with one word`() {
        val user = "Rick"
        assert(user.initials == "R")
    }

    @Test
    fun `Should return two initials for a string with two words`() {
        val user = "Rick Sanchez"
        assert(user.initials == "RS")
    }

    @Test
    fun `Should return two initials for a string with three words`() {
        val user = "Rick Sanchez Rodriguez"
        assert(user.initials == "RS")
    }

    @Test
    fun `Should return empty for a empty string`() {
        val user = ""
        assert(user.initials == "")
    }

    @Test
    fun `Should return empty for null string`() {
        val user = null
        assert(user.initials == "")
    }

    @Test
    fun `Should parse a string correctly for a wrong string value`() {
        val string = "."
        assert(string.parseToDouble() == "0.0")
    }

    @Test
    fun `Should parse a string correctly for a wrong double value`() {
        val string = "-."
        assert(string.parseToDouble() == "0.0")
    }

    @Test
    fun `Should parse a string correctly for a missing double value`() {
        val string = "-1."
        assert(string.parseToDouble() == "-1.0")
    }

    @Test
    fun `Should parse a string correctly for a incorrectly double value`() {
        val string = "12ccd33"
        assert(string.parseToDouble() == "0.0")
    }

    @Test
    fun `Should return the same value for a correctly inputted double value`() {
        val string = "3.47658"
        assert(string.parseToDouble() == "3.47658")
    }

    @Test
    fun `Should return true for new version`() {
        val new = "1.2.3"
        val old = "1.2.2"
        assert(new.newVersion(old))
    }

    @Test
    fun `Should return true when new have another level`() {
        val new = "1.2.1"
        val old = "1.2"
        assert(new.newVersion(old))
    }

    @Test
    fun `Should return true for new version when new have less levels`() {
        val new = "1.3"
        val old = "1.2.3.4"
        assert(new.newVersion(old))
    }

    @Test
    fun `Should return true when new version has a level with 2 digits`() {
        val new = "1.2.10"
        val old = "1.2.3"
        assert(new.newVersion(old))
    }

    @Test
    fun `Should return false when same version`() {
        val new = "1.2.3"
        val old = "1.2.3"
        assert(!new.newVersion(old))
    }

    @Test
    fun `Should return false when old is bigger`() {
        val new = "1.2.3"
        val old = "1.2.4"
        assert(!new.newVersion(old))
    }

    @Test
    fun `Should return false when version has no integer char`() {
        val new = "1.2.3f"
        val old = "1.2.4"
        assert(!new.newVersion(old))
    }
}
