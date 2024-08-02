package org.dhis2.usescases.teiDashboard.dialogs.scheduling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2

@Suppress("UNCHECKED_CAST")
class SchedulingViewModelFactory(
    private val d2: D2,
    private val resourceManager: ResourceManager,
    private val periodUtils: DhisPeriodUtils,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SchedulingViewModel(
            d2,
            resourceManager,
            periodUtils,
        ) as T
    }
}
