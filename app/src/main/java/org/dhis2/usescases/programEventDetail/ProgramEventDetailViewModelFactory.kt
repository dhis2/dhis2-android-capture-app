package org.dhis2.usescases.programEventDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.usecases.MapStyleConfiguration

@Suppress("UNCHECKED_CAST")
class ProgramEventDetailViewModelFactory(
    private val mapStyleConfiguration: MapStyleConfiguration,
    private val eventRepository: ProgramEventDetailRepository,
    private val dispatcher: DispatcherProvider,
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProgramEventDetailViewModel(mapStyleConfiguration, eventRepository, dispatcher) as T
    }
}
