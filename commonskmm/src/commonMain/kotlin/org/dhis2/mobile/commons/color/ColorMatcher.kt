package org.dhis2.mobile.commons.color

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * CIE L*a*b* color representation for perceptually uniform distance calculations.
 */
private data class Lab(
    val l: Double,
    val a: Double,
    val b: Double,
)

/**
 * Finds the closest color from the palette to the given selected color
 * using the CIEDE2000-simplified (CIE76) Delta E in L*a*b* color space,
 * which is more perceptually accurate than simple RGB euclidean distance.
 */
object ColorMatcher {
    fun findClosest(
        selectedR: Int,
        selectedG: Int,
        selectedB: Int,
        palette: List<PaletteColor>,
    ): PaletteColor? {
        if (palette.isEmpty()) return null
        val selectedLab = rgbToLab(selectedR, selectedG, selectedB)
        return palette.minByOrNull { color ->
            val lab = rgbToLab(color.r, color.g, color.b)
            deltaE(selectedLab, lab)
        }
    }

    /**
     * CIE76 Delta E — Euclidean distance in L*a*b* space.
     */
    private fun deltaE(
        c1: Lab,
        c2: Lab,
    ): Double =
        sqrt(
            (c1.l - c2.l).pow(2) +
                (c1.a - c2.a).pow(2) +
                (c1.b - c2.b).pow(2),
        )

    /**
     * Convert sRGB to CIE L*a*b* via XYZ intermediate.
     */
    private fun rgbToLab(
        r: Int,
        g: Int,
        b: Int,
    ): Lab {
        // sRGB -> linear RGB
        var rr = r / 255.0
        var gg = g / 255.0
        var bb = b / 255.0

        rr = if (rr > 0.04045) ((rr + 0.055) / 1.055).pow(2.4) else rr / 12.92
        gg = if (gg > 0.04045) ((gg + 0.055) / 1.055).pow(2.4) else gg / 12.92
        bb = if (bb > 0.04045) ((bb + 0.055) / 1.055).pow(2.4) else bb / 12.92

        // Linear RGB -> XYZ (D65 illuminant)
        val x = (rr * 0.4124564 + gg * 0.3575761 + bb * 0.1804375) / 0.95047
        val y = (rr * 0.2126729 + gg * 0.7151522 + bb * 0.0721750) / 1.00000
        val z = (rr * 0.0193339 + gg * 0.1191920 + bb * 0.9503041) / 1.08883

        // XYZ -> L*a*b*
        val fx = labF(x)
        val fy = labF(y)
        val fz = labF(z)

        val l = 116.0 * fy - 16.0
        val a = 500.0 * (fx - fy)
        val bLab = 200.0 * (fy - fz)

        return Lab(l, a, bLab)
    }

    private fun labF(t: Double): Double =
        if (t > 0.008856) {
            t.pow(1.0 / 3.0)
        } else {
            (7.787 * t) + (16.0 / 116.0)
        }
}
