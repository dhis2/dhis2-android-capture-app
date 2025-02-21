package org.dhis2.commons.filters.periods.di

interface FilterPeriodsDialogComponentProvider {
    fun provideFilterPeriodsDialogComponent(module: FilterPeriodsDialogModule): FilterPeriodsDialogComponent?
}
