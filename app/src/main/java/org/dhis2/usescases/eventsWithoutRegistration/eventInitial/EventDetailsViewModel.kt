package org.dhis2.usescases.eventsWithoutRegistration.eventInitial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.program.ProgramStage

class EventDetailsViewModel(
    eventUid: String,
    val d2: D2
) : ViewModel() {

    private val _programStage = MutableLiveData<ProgramStage?>()
    val programStage: LiveData<ProgramStage?> = _programStage

    init {
        viewModelScope.launch {
            _programStage.value = withContext(IO) {
                d2.eventModule()
                    .events()
                    .byUid()
                    .eq(eventUid).one().blockingGet().let {
                        d2.programModule()
                            .programStages()
                            .byUid()
                            .eq(it.programStage()).one().blockingGet()
                    }
            }
        }
    }
}