package org.dhis2.utils.category

import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryOptionComboCategoryDialogItemMapperTest {
    @Test
    fun `Should map category option combo`() {
        val testCategory = CategoryOptionCombo.builder()
            .uid("catOptCombUid")
            .displayName("catOptCombName")
            .build()
        val result = CategoryOptionComboCategoryDialogItemMapper().map(testCategory)
        result.apply {
            assertTrue(this.uid == testCategory.uid())
            assertTrue(this.displayName == testCategory.displayName())
        }
    }
}
