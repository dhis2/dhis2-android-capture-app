package org.dhis2.utils.category

import org.hisp.dhis.android.core.category.CategoryOption

class CategoryOptionCategoryDialogItemMapper {
    fun map(categoryOption: CategoryOption): CategoryDialogItem =
        CategoryDialogItem(
            categoryOption.uid(),
            categoryOption.displayName() ?: "-",
        )
}
