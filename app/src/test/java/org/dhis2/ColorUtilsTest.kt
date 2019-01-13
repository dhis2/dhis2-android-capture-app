package org.dhis2

import android.graphics.Color
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.dhis2.utils.ColorUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ColorUtilsTest{


    @Test
    fun getColorFrom() {
        val testColor1 = "#fbc02d"
        val testColor2 = "#fbc"
        val testColor3 = "#000000"

        assertEquals(Color.parseColor(testColor1), ColorUtils.getColorFrom( InstrumentationRegistry.getTargetContext(), testColor1))
        assertEquals(Color.parseColor("#ffbbcc"), ColorUtils.getColorFrom(InstrumentationRegistry.getTargetContext(), testColor2))
        assertEquals(Color.parseColor("#BACFFF"), ColorUtils.getColorFrom(InstrumentationRegistry.getTargetContext(), testColor3))
        assertEquals(Color.parseColor("#BACFFF"), ColorUtils.getColorFrom(InstrumentationRegistry.getTargetContext(), null))
    }

}