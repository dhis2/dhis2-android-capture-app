package org.dhis2.bindings

import android.content.Context
import android.content.res.Resources
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.program.Program
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TEICardExtensionsTest {

    private val context: Context = mock()
    private val resourceManager: ResourceManager = mock()
    private val resources: Resources = mock()
    private val colorUtils: ColorUtils = mock()
    private val metadataIconData: MetadataIconData = MetadataIconData.Resource(1, 1)

    @Test
    fun `Should return list of enrollment data icons that is smaller than four and all icons`() {
        val programs: MutableList<Program> = mutableListOf()
        for (index in 1..4)
            programs.add(createProgram("uid_$index"))

        mockEnrollmentIconsData()
        val result = programs.getEnrollmentIconsData("uid_1", metadataIconData)
        assert(result.size == 3)
        result.forEach {
            assert(it.isIcon)
        }
    }

    @Test
    fun `Should return list of enrollment data icons equal to four and all icons`() {
        val programs: MutableList<Program> = mutableListOf()
        for (index in 1..5)
            programs.add(createProgram("uid_$index"))

        mockEnrollmentIconsData()
        val result = programs.getEnrollmentIconsData("uid_1", metadataIconData)
        assert(result.size == 4)
        result.forEach {
            assert(it.isIcon)
        }
    }

    @Test
    fun `Should return list of enrollment data icons equal to four, three icons and an integer`() {
        val programs: MutableList<Program> = mutableListOf()
        for (index in 1..7)
            programs.add(createProgram("uid_$index"))

        mockEnrollmentIconsData()
        val result = programs.getEnrollmentIconsData("uid_5", metadataIconData)
        assert(result.size == 4)
        result.forEachIndexed { index, item ->
            if (index in 0..result.size - 2) {
                assert(item.isIcon)
            }
            if (index == result.size - 1) {
                assert(!item.isIcon)
                assert(item.remainingEnrollments == 3)
            }
        }
    }

    @Test
    fun `Should return list when current program is null`() {
        val programs: MutableList<Program> = mutableListOf()
        for (index in 1..6)
            programs.add(createProgram("uid_$index"))

        mockEnrollmentIconsData()
        val result = programs.getEnrollmentIconsData(null, metadataIconData)
        assert(result.size == 4)
        result.forEachIndexed { index, item ->
            if (index in 0..result.size - 2) {
                assert(item.isIcon)
            }
            if (index == result.size - 1) {
                assert(!item.isIcon)
                assert(item.remainingEnrollments == 3)
            }
        }
    }

    @Test
    fun `Should return max icons number`() {
        val programs: MutableList<Program> = mutableListOf()
        for (index in 0..150)
            programs.add(createProgram("uid_$index"))

        mockEnrollmentIconsData()
        val result = programs.getEnrollmentIconsData("uid_100", metadataIconData)
        assert(result.size == 4)
        assert(!result.last().isIcon)
        assert(result.last().remainingEnrollments == 99)
    }

    private fun mockEnrollmentIconsData() {
        whenever(colorUtils.getColorFrom("blue", 1)) doReturn 1
        whenever(colorUtils.getPrimaryColor(context, ColorType.PRIMARY)) doReturn 1
        whenever(resourceManager.getWrapperContext()) doAnswer { context }
        whenever(resourceManager.getObjectStyleDrawableResource("icon", 1)) doAnswer { 1 }
        whenever(context.resources) doAnswer { resources }
        whenever(resources.getIdentifier(any(), any(), any())) doAnswer { 1 }
    }

    private fun createProgram(uid: String) = Program
        .builder()
        .uid(uid)
        .style(ObjectStyle.builder().color("color").icon("icon").build())
        .build()
}
