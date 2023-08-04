package org.dhis2.bindings

import android.content.Context
import org.mockito.kotlin.mock

class TEICardExtensionsTest {

    private val context: Context = mock()
    /*private val colorUtils: ColorUtils = mock()

    // TODO finish implementing unit tests for following cases:
    // less than 4 enrollments, all enrollmentdata objects should  be icons.
    // 4 enrollments, all should be icons
    // more than 4 enrollments, should return data lis]t of four elements but last element should
    // not be an icon (enrollmentIconData.isIcon == false).

    //@Ignore
    @Test
    fun `Should return list of enrollment data icon objects smaller than 4 `() {
        val programs: List<Program> = listOf(
            createProgram("uid_1", "blue", "icon"),
            createProgram("uid_2", "green", "icon"),
            createProgram("uid_3", "red", "icon"),
            createProgram("uid_4", "orange", "icon"),
        )
        mockEnrollmentIconsData(programs)
        val result = programs.getEnrollmentIconsData(context, "uid_1")
        assert(result.size <= 4)
    }

    private fun mockEnrollmentIconsData(programs: List<Program>) {

        //this is where the problem occurs
        //todo migrate ColorUtils file to kotlin to avoid null pointer exception
        //whenever(typedValue).doReturn(mock())
        whenever(ColorUtils.getColorFrom("blue", 1)) doReturn  1
        whenever(ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY)) doReturn 1

        // whenever(ColorUtils.getColorFrom("color", 444)).doReturn(4)
        // whenever(ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY)).doReturn(mock())
        //whenever(ResourceManager(context).getObjectStyleDrawableResource("icon", 1)) doReturn any()
        //whenever(ColorUtils.getColorFrom(mock(), mock())).doReturn(mock())
    }

    private fun createProgram(uid: String, color: String, icon: String) = Program
        .builder()
        .uid(uid)
        .style(ObjectStyle.builder().color(color).icon(icon).build())
        .build()*/
}
