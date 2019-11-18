package org.dhis2.utils.customviews

import dagger.Subcomponent

@Subcomponent(modules = [CategoryComboDialogModule::class])
interface CategoryComboDialogComponent {
    fun inject(categoryOptionComboDialog: CategoryComboDialog)
}
