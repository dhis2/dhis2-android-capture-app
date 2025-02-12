package org.dhis2.usescases.teiDashboard.dialogs.scheduling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.EventResourcesProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.D2

@Suppress("UNCHECKED_CAST")
class SchedulingViewModelFactory @AssistedInject constructor(
    private val d2: D2,
    private val resourceManager: ResourceManager,
    private val eventResourcesProvider: EventResourcesProvider,
    private val periodUtils: DhisPeriodUtils,
    private val dateUtils: DateUtils,
    private val dispatcherProvider: DispatcherProvider,
    @Assisted private val launchMode: SchedulingDialog.LaunchMode,
) : ViewModelProvider.Factory {

    @AssistedFactory
    interface Factory {
        fun build(launchMode: SchedulingDialog.LaunchMode): SchedulingViewModelFactory
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SchedulingViewModel(
            d2 = d2,
            resourceManager = resourceManager,
            eventResourcesProvider = eventResourcesProvider,
            periodUtils = periodUtils,
            dateUtils = dateUtils,
            dispatchersProvider = dispatcherProvider,
            launchMode = launchMode,
        ) as T
    }
}
