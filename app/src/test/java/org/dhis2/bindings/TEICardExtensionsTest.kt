package org.dhis2.bindings

import android.content.Context
import android.content.res.TypedArray
import android.util.TypedValue
import org.dhis2.Bindings.getEnrollmentIconsData
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.program.Program
import org.junit.Ignore
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever


class TEICardExtensionsTest {

    private val program: Program = mock()
    private val programInfo: List<Program> = listOf(program,program, program, program)
    private val context: Context = mock()
    private val  typedValue : TypedValue = mock();
    private val intArray: IntArray = intArrayOf(4)
    private  val typedIntArray: TypedArray = mock()
    private val typedArray: TypedArray = mock()

    // TODO finish implementing unit tests for following cases:
    // less than 4 enrollments, all enrollmentdata objects should  be icons.
    // 4 enrollments, all should be icons
    // more than 4 enrollments, should return data lis]t of four elements but last element should
    // not be an icon (enrollmentIconData.isIcon == false).

    @Ignore
    @Test
    fun `Should return list of enrollment data icon objects smaller than 4 `() {
        mockEnrollmentIconsData(context, "programUID1")
        val result = programInfo.getEnrollmentIconsData(context, "programUID1")
        assert(result.size <= 4)
    }

    private fun mockEnrollmentIconsData(context: Context, currentProgram: String?) {
        whenever(program.uid()).doReturn("programUID2")
        whenever(program.style()).doReturn(mock())
        whenever(program.style().color()).doReturn("color")
        whenever(program.style().icon()).doReturn("icon")
        whenever(context.obtainStyledAttributes(null, intArray )).doReturn(typedArray)
        whenever(typedArray.getColor(0,0) ).doReturn(777)

        //this is where the problem occurs
        //todo migrate ColorUtils file to kotlin to avoid null pointer exception
        //whenever(typedValue).doReturn(mock())
        whenever(ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY)).doReturn(4)

        // whenever(ColorUtils.getColorFrom("color", 444)).doReturn(4)
        // whenever(ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY)).doReturn(mock())
        whenever(ResourceManager(context).getObjectStyleDrawableResource(program.style().icon(), mock()) ).doReturn(444)
        //whenever(ColorUtils.getColorFrom(mock(), mock())).doReturn(mock())
    }
}
