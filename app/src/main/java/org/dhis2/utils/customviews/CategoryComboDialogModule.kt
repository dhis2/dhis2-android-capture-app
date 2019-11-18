package org.dhis2.utils.customviews

import dagger.Module
import dagger.Provides
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryCombo

@Module
class CategoryComboDialogModule(val categoryCombo: CategoryCombo) {

    @Provides
    fun providesPresenter(d2: D2): CategoryComboDialogPresenter {
        return CategoryComboDialogPresenter(d2, categoryCombo.uid())
    }

}
