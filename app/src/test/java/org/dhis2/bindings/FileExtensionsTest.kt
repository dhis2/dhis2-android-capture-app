package org.dhis2.bindings

import org.dhis2.Bindings.resizeToMinimum
import org.junit.Assert.assertTrue
import org.junit.Test

class FileExtensionsTest {

    @Test
    fun `Should return defaults if minimum is not provided`() {
        val (width, height) = resizeToMinimum(null, 200, 300)
        assertTrue(width == 200 && height == 300)
    }

    @Test
    fun `Should return minimum height if minimum is provided`() {
        val (width, height) = resizeToMinimum(400, 200, 300)
        assertTrue(width == 266 && height == 400)
    }

    @Test
    fun `Should return minimum width if minimum is provided`() {
        val (width, height) = resizeToMinimum(400, 300, 200)
        assertTrue(width == 400 && height == 266)
    }

    @Test
    fun `Should return defaults if height bigger than minimum`() {
        val (width, height) = resizeToMinimum(100, 200, 300)
        assertTrue(width == 200 && height == 300)
    }

    @Test
    fun `Should return defaults if width bigger than minimum`() {
        val (width, height) = resizeToMinimum(100, 300, 200)
        assertTrue(width == 300 && height == 200)
    }
}
