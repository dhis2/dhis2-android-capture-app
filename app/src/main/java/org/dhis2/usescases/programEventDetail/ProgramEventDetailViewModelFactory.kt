package org.dhis2.usescases.programEventDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.usescases.programEventDetail.usecase.CreateEventUseCase
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator

@Suppress("UNCHECKED_CAST")
class ProgramEventDetailViewModelFactory(
    private val mapStyleConfiguration: MapStyleConfiguration,
    private val eventRepository: ProgramEventDetailRepository,
    private val dispatcher: DispatcherProvider,
    private val createEventUseCase: CreateEventUseCase,
    private val pageConfigurator: NavigationPageConfigurator,
    private val resourceManager: ResourceManager,
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProgramEventDetailViewModel(
            mapStyleConfiguration,
            eventRepository,
            dispatcher,
            createEventUseCase,
            pageConfigurator,
            resourceManager,
        ) as T
    }
}
