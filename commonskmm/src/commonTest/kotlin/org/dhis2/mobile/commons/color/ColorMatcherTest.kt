package org.dhis2.mobile.commons.color

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ColorMatcherTest {
    // Palette sampled from PaletteThemes covering distinct hue regions
    private val palette =
        listOf(
            PaletteColor.fromHex("#d32f2f"), // Red
            PaletteColor.fromHex("#1976d2"), // Blue
            PaletteColor.fromHex("#2e7d32"), // Green
            PaletteColor.fromHex("#fbc02d"), // Amber/Yellow
            PaletteColor.fromHex("#424242"), // Dark Grey
        )

    @Test
    fun returnsNullWhenPaletteIsEmpty() {
        assertNull(ColorMatcher.findClosest(128, 128, 128, emptyList()))
    }

    @Test
    fun returnsOnlyColorWhenPaletteHasSingleEntry() {
        val single = PaletteColor.fromHex("#1976d2")
        assertEquals(single, ColorMatcher.findClosest(255, 0, 0, listOf(single)))
    }

    @Test
    fun exactColorMatchReturnsItself() {
        // #d32f2f = rgb(211, 47, 47)
        val result = ColorMatcher.findClosest(211, 47, 47, palette)
        assertEquals(PaletteColor.fromHex("#d32f2f"), result)
    }

    @Test
    fun pureRedIsMatchedToRedPaletteEntry() {
        // rgb(255, 0, 0) should be perceptually closest to #d32f2f among the palette
        val result = ColorMatcher.findClosest(255, 0, 0, palette)
        assertEquals(PaletteColor.fromHex("#d32f2f"), result)
    }

    @Test
    fun pureBlueIsMatchedToBluePaletteEntry() {
        // rgb(0, 0, 255) should be perceptually closest to #1976d2 among the palette
        val result = ColorMatcher.findClosest(0, 0, 255, palette)
        assertEquals(PaletteColor.fromHex("#1976d2"), result)
    }

    @Test
    fun pureGreenIsMatchedToGreenPaletteEntry() {
        // rgb(0, 128, 0) should be perceptually closest to #2e7d32 among the palette
        val result = ColorMatcher.findClosest(0, 128, 0, palette)
        assertEquals(PaletteColor.fromHex("#2e7d32"), result)
    }

    @Test
    fun yellowIsMatchedToAmberPaletteEntry() {
        // rgb(255, 200, 0) should be perceptually closest to #fbc02d among the palette
        val result = ColorMatcher.findClosest(255, 200, 0, palette)
        assertEquals(PaletteColor.fromHex("#fbc02d"), result)
    }

    @Test
    fun darkGreyIsMatchedToGreyPaletteEntry() {
        // rgb(64, 64, 64) should be perceptually closest to #424242 among the palette
        val result = ColorMatcher.findClosest(64, 64, 64, palette)
        assertEquals(PaletteColor.fromHex("#424242"), result)
    }
}
