package org.dhis2.usescases.uiboost.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.dhis2.usescases.uiboost.data.repository.UBDataStoreRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
private val repository: UBDataStoreRepository
): ViewModel() {

    init {
//        downloadNewDataStoreData()
    }

    fun downloadNewDataStoreData() {
       viewModelScope.launch {
           repository.downloadDataStore()
       }
    }
}