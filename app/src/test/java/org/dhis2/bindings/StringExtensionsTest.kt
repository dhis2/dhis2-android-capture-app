package org.dhis2.bindings

import org.dhis2.Bindings.initials
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
}
