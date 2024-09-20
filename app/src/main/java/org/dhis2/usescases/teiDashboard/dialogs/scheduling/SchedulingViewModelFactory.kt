package org.dhis2.usescases.teiDashboard.dialogs.scheduling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.EventResourcesProvider
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2

@Suppress("UNCHECKED_CAST")
class SchedulingViewModelFactory @AssistedInject constructor(
    private val d2: D2,
    private val resourceManager: ResourceManager,
    val eventResourcesProvider: EventResourcesProvider,
    private val periodUtils: DhisPeriodUtils,
    @Assisted private val launchMode: SchedulingDialog.LaunchMode,
) : ViewModelProvider.Factory {

    @AssistedFactory
    interface Factory {
        fun build(launchMode: SchedulingDialog.LaunchMode): SchedulingViewModelFactory
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SchedulingViewModel(
            d2,
            resourceManager,
            eventResourcesProvider,
            periodUtils,
            launchMode,
        ) as T
    }
}
