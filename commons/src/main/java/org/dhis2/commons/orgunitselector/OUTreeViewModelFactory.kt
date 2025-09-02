package org.dhis2.commons.orgunitselector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.viewmodel.DispatcherProvider

class OUTreeViewModelFactory(
    private val repository: OUTreeRepository,
    private val dispatchers: DispatcherProvider,
    private val selectedOrgUnits: MutableList<String>,
    private val singleSelection: Boolean,
    private val model: OUTreeModel,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        OUTreeViewModel(
            repository = repository,
            dispatchers = dispatchers,
            selectedOrgUnits = selectedOrgUnits,
            singleSelection = singleSelection,
            model = model,
        ) as T
}
