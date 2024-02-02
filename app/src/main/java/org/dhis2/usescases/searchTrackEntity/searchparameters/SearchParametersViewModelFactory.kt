package org.dhis2.usescases.searchTrackEntity.searchparameters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class SearchParametersViewModelFactory(
    private val repository: SearchParametersRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchParametersViewModel(
            repository = repository,
        ) as T
    }
}
