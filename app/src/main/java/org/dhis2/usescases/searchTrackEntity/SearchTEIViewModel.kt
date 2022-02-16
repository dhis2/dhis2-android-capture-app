package org.dhis2.usescases.searchTrackEntity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.form.model.RowAction
import org.hisp.dhis.android.core.program.Program

class SearchTEIViewModel(
    private val initialProgramUid:String?,
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val queryData = mutableMapOf<String, String>()
    private val _selectedProgram = MutableLiveData(searchRepository.getProgram(initialProgramUid))
    val selectedProgram: LiveData<Program?> = _selectedProgram

    fun updateQueryData(rowAction: RowAction) {
        if (rowAction.value != null) {
            queryData[rowAction.id] = rowAction.value!!
        } else {
            queryData.remove(rowAction.id)
        }
    }

    fun fetchLocalResults() = searchRepository.searchTrackedEntities(
        SearchParametersModel(
            selectedProgram = _selectedProgram.value,
            queryData = queryData
        ),
        true
    )

    fun fetchOnlineResults() = searchRepository.searchTrackedEntities(
        SearchParametersModel(
            selectedProgram = _selectedProgram.value,
            queryData = queryData
        ),
        true
    )
}