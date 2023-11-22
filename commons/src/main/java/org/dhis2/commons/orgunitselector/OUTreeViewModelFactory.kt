package org.dhis2.commons.orgunitselector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.viewmodel.DispatcherProvider

class OUTreeViewModelFactory(
    private val repository: OUTreeRepository,
    private val dispatchers: DispatcherProvider,
    private val selectedOrgUnits: MutableList<String>,
    private val singleSelection: Boolean,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return OUTreeViewModel(
            repository = repository,
            dispatchers = dispatchers,
            selectedOrgUnits = selectedOrgUnits,
            singleSelection = singleSelection,
        ) as T
    }
}
