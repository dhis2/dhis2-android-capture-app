package org.dhis2.utils.customviews

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.category.CategoryOption

class CategoryComboDialogPresenter(val d2: D2, val uid: String) {

    fun getCatOptionCombo(categoryOptions: List<CategoryOption>): String {
        val catOptCombo = d2.categoryModule().categoryOptionCombos()
            .byCategoryOptions(UidsHelper.getUidsList(categoryOptions)).one().blockingGet()
        return if (catOptCombo != null) {
            catOptCombo.uid()
        } else {
            ""
        }
    }
}
