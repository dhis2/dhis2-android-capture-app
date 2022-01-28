package org.dhis2.usescases.eventsWithoutRegistration.eventInitial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.program.ProgramStage

class EventDetailsViewModel(
    eventUid: String,
    val d2: D2
) : ViewModel() {

    private val _programStage: MutableStateFlow<ProgramStage?> = MutableStateFlow(null)
    val programStage: StateFlow<ProgramStage?> get() = _programStage

    private val _objectStyle: MutableStateFlow<ObjectStyle?> = MutableStateFlow(null)
    val objectStyle: StateFlow<ObjectStyle?> get() = _objectStyle

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

            programStage.collect { programStage ->
                _objectStyle.value = d2.programModule()
                    .programs()
                    .uid(programStage?.program()?.uid())
                    .blockingGet()
                    .style()
            }
        }
    }
}
