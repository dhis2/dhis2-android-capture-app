package org.dhis2.usescases.eventsWithoutRegistration.eventInitial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.hisp.dhis.android.core.D2

@Suppress("UNCHECKED_CAST")
class EventDetailsViewModelFactory(
    private val eventUid: String,
    private val d2: D2
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EventDetailsViewModel(eventUid, d2) as T
    }
}