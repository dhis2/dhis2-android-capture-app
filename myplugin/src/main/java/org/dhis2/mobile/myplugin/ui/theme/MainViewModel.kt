package org.dhis2.mobile.myplugin.ui.theme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import javax.inject.Inject

@HiltViewModel
open class MainViewModel @Inject constructor(
    private val d2: D2,
) : ViewModel() {
    private val _programList = MutableLiveData<List<ProgramItem>>()
    val programList: LiveData<List<ProgramItem>>
        get() = _programList
    init {
        loadPrograms()
    }

    private fun loadPrograms() {
        viewModelScope.launch {
            _programList.value = getProgramsInCaptureOrgUnits()
        }
    }

    private fun getProgramsInCaptureOrgUnits(): List<ProgramItem> {
        return d2.programModule().programs()
            .withTrackedEntityType()
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
            .blockingGet().map { sdkProgram ->
                ProgramItem(
                    program = sdkProgram
                )
            }
    }
}

data class ProgramItem(
    val program: Program,
)
