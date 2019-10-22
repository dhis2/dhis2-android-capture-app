package org.dhis2.utils

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import org.dhis2.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ColorUtilsTest {

    @Mock
    private lateinit var mockApplicationContext: Context
    @Mock
    private lateinit var mockContextResources: Resources
    @Mock
    private lateinit var attributes: AttributeSet
    @Mock
    private lateinit var typedArray: TypedArray

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun getColorFrom() {
        val testColor1 = "#fbc02d"
        val testColor2 = "#fbc"
        val testColor3 = "#000000"

        assertEquals(Color.parseColor(testColor1), ColorUtils.getColorFrom(testColor1, -14590324))
        assertEquals(Color.parseColor("#ffbbcc"), ColorUtils.getColorFrom(testColor2, -14590324))
        assertEquals(Color.parseColor("#215e8c"), ColorUtils.getColorFrom(testColor3, -14590324))
        assertEquals(Color.parseColor("#215e8c"), ColorUtils.getColorFrom(null, -14590324))
    }

    @Test
    fun getContrastColor() {
        val testBlack = Color.BLACK
        val testWhite = Color.WHITE

        assertEquals(Color.WHITE, ColorUtils.getContrastColor(testBlack))
        assertEquals(Color.BLACK, ColorUtils.getContrastColor(testWhite))
    }

    @Test
    fun getThemeFromColor() {
        val testColor1 = "#ffcdd2"
        val testColor2 = "#e57373"
        val testColor3 = "#d32f2f"
        val testColor4 = "#f06292"
        val testColor5 = "#c2185b"
        val testColor6 = "asdsdasd"

        assertEquals(R.style.colorPrimary_Pink, ColorUtils.getThemeFromColor(testColor1))
        assertEquals(R.style.colorPrimary_e57, ColorUtils.getThemeFromColor(testColor2))
        assertEquals(R.style.colorPrimary_d32, ColorUtils.getThemeFromColor(testColor3))
        assertEquals(R.style.colorPrimary_f06, ColorUtils.getThemeFromColor(testColor4))
        assertEquals(R.style.colorPrimary_c21, ColorUtils.getThemeFromColor(testColor5))
        assertEquals(-1, ColorUtils.getThemeFromColor(testColor6))
        assertEquals(-1, ColorUtils.getThemeFromColor(null))
    }

    @Test
    fun getPrimaryColor() {
        val typedValue = TypedValue()

        `when`(
            mockApplicationContext.obtainStyledAttributes(
                typedValue.data,
                intArrayOf(R.attr.colorPrimary)
            )
        ).thenReturn(typedArray)
        `when`(typedArray.getColor(0, 0)).thenReturn(-14590324)
        assertEquals(
            -14590324,
            ColorUtils.getPrimaryColor(mockApplicationContext, ColorUtils.ColorType.PRIMARY)
        )

        `when`(
            mockApplicationContext.obtainStyledAttributes(
                typedValue.data,
                intArrayOf(R.attr.colorPrimaryLight)
            )
        ).thenReturn(typedArray)
        `when`(typedArray.getColor(0, 0)).thenReturn(-6754663)
        assertEquals(
            -6754663,
            ColorUtils.getPrimaryColor(mockApplicationContext, ColorUtils.ColorType.PRIMARY_LIGHT)
        )

        `when`(
            mockApplicationContext.obtainStyledAttributes(
                typedValue.data,
                intArrayOf(R.attr.colorPrimaryDark)
            )
        ).thenReturn(typedArray)
        `when`(typedArray.getColor(0, 0)).thenReturn(-3895296)
        assertEquals(
            -3895296,
            ColorUtils.getPrimaryColor(mockApplicationContext, ColorUtils.ColorType.PRIMARY_DARK)
        )

        `when`(
            mockApplicationContext.obtainStyledAttributes(
                typedValue.data,
                intArrayOf(R.attr.colorAccent)
            )
        ).thenReturn(typedArray)
        `when`(typedArray.getColor(0, 0)).thenReturn(-6754663)
        assertEquals(
            -6754663,
            ColorUtils.getPrimaryColor(mockApplicationContext, ColorUtils.ColorType.ACCENT)
        )
    }
}
