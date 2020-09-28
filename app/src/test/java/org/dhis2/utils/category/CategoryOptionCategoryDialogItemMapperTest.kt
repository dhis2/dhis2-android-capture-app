package org.dhis2.utils.category

import org.hisp.dhis.android.core.category.CategoryOption
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryOptionCategoryDialogItemMapperTest {
    @Test
    fun `Should map category option`() {
        val testCategory = CategoryOption.builder()
            .uid("catOptUid")
            .displayName("catOptName")
            .build()
        val result = CategoryOptionCategoryDialogItemMapper().map(testCategory)
        result.apply {
            assertTrue(this.uid == testCategory.uid())
            assertTrue(this.displayName == testCategory.displayName())
        }
    }
}
