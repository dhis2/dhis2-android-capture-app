package org.dhis2.usescases.searchTrackEntity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SearchTeiViewModelFactory(
    val searchRepository: SearchRepository,
    val initialProgramUid: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SearchTEIViewModel(initialProgramUid, searchRepository) as T
    }
}