package org.dhis2.commons.filters.di

import org.dhis2.commons.filters.data.FilterPresenter

interface FilterPresenterProvider {
    fun provideFilterPresenter(): FilterPresenter?
}
