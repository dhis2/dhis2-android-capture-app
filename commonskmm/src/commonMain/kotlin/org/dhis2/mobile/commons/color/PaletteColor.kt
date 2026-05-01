package org.dhis2.mobile.commons.color

/**
 * Represents a color in RGB space, parsed from a hex string.
 */
data class PaletteColor(
    val hex: String,
    val r: Int,
    val g: Int,
    val b: Int,
) {
    companion object {
        fun fromHex(hex: String): PaletteColor {
            val clean = hex.removePrefix("#")
            val r = clean.substring(0, 2).toInt(16)
            val g = clean.substring(2, 4).toInt(16)
            val b = clean.substring(4, 6).toInt(16)
            return PaletteColor(hex = "#$clean", r = r, g = g, b = b)
        }
    }
}
