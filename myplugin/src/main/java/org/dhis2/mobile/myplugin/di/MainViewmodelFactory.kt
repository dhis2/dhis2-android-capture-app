package org.dhis2.mobile.myplugin.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.filters.workingLists.WorkingListViewModel
import org.dhis2.mobile.myplugin.ui.theme.MainViewModel
import org.hisp.dhis.android.core.D2


class MainViewmodelFactory(
    //private val d2: D2,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(
           // d2
        ) as T
    }
}
