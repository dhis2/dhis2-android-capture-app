package org.dhis2.usescases.searchTrackEntity.searchparameters

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.dhis2.usescases.searchTrackEntity.searchparameters.model.SearchParametersUiState
import java.io.IOException

class SearchParametersViewModel(
    val repository: SearchParametersRepository,
) : ViewModel() {

    var uiState by mutableStateOf(SearchParametersUiState())
        private set

    private var fetchJob: Job? = null

    fun fetchSearchParameters(
        programUid: String?,
        teiTypeUid: String,
    ) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                val searchParameters = repository.searchParameters(programUid, teiTypeUid)
                uiState = uiState.copy(items = searchParameters)
            } catch (ioe: IOException) {
                /*_uiState.update {
                    val messages = getMessagesFromThrowable(ioe)
                    it.copy(userMessages = messages)
                }*/
            }
        }
    }
}
