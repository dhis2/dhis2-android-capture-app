package org.dhis2.commons.filters.periods.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.dhis2.commons.filters.periods.data.FilterPeriodsRepository
import org.dhis2.commons.filters.periods.data.PeriodTypeLabelProvider
import org.dhis2.commons.filters.periods.domain.GetFilterPeriods
import org.dhis2.commons.filters.periods.ui.FilterPeriodsDialog
import org.dhis2.commons.resources.ResourceManager

class FilterPeriodsDialogViewmodelFactory @AssistedInject constructor(
    private val getFilterPeriods: GetFilterPeriods,
    private val filterPeriodsRepository: FilterPeriodsRepository,
    private val resourceManager: ResourceManager,
    private val filterPeriodTypeLabelProvider: PeriodTypeLabelProvider,
    @Assisted private val launchMode: FilterPeriodsDialog.FilterDialogLaunchMode,

) : ViewModelProvider.Factory {

    @AssistedFactory
    interface Factory {
        fun build(launchMode: FilterPeriodsDialog.FilterDialogLaunchMode): FilterPeriodsDialogViewmodelFactory
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FilterPeriodsDialogViewmodel(
            getFilterPeriods = getFilterPeriods,
            filterPeriodsRepository = filterPeriodsRepository,
            resourceManager = resourceManager,
            periodTypeLabelProvider = filterPeriodTypeLabelProvider,
            launchMode = launchMode,
        ) as T
    }
}
