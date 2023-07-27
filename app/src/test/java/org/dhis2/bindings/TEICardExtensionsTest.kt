package org.dhis2.bindings

import android.content.Context
import org.dhis2.Bindings.getEnrollmentIconsData
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.program.Program
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TEICardExtensionsTest {

    private val program: Program = mock()
    private val programInfo: List<Program> = listOf(program)
    private val context: Context = mock()

    // TODO finish implementing unit tests for following cases:
    // less than 4 enrollments, all enrollmentdata objects should  be icons.
    // 4 enrollments, all should be icons
    // more than 4 enrollments, should return data list of four elements but last element should
    // not be an icon (enrollmentIconData.isIcon == false).
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

        whenever(ColorUtils.getColorFrom("color", mock())).doReturn(mock())
        whenever(ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY)).doReturn(mock())

        whenever(
            ResourceManager(context).getObjectStyleDrawableResource(program.style().icon(), mock())
        ).doReturn(444)
        whenever(ColorUtils.getColorFrom(mock(), mock())).doReturn(mock())
    }
}
