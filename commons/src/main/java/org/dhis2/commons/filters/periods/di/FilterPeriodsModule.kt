package org.dhis2.commons.filters.periods.di

import org.dhis2.commons.filters.periods.data.FilterPeriodsRepository
import org.dhis2.commons.filters.periods.data.PeriodTypeLabelProvider
import org.dhis2.commons.filters.periods.domain.GetFilterPeriodTypes
import org.dhis2.commons.filters.periods.domain.GetFilterPeriods
import org.dhis2.commons.filters.periods.ui.viewmodel.FilterPeriodsDialogViewmodel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val filterPeriodsModule =
    module {

        factory {
            GetFilterPeriods(
                filterPeriodRepository = get(),
            )
        }
        factory {
            GetFilterPeriodTypes(
                filterPeriodRepository = get(),
            )
        }

        single {
            FilterPeriodsRepository(get())
        }

        singleOf(::PeriodTypeLabelProvider)

        viewModel { params ->
            FilterPeriodsDialogViewmodel(
                getFilterPeriods = get(),
                getFilterPeriodTypes = get(),
                resourceManager = get { parametersOf(params.get()) },
                periodTypeLabelProvider = get(),
                launchMode = params.get(),
            )
        }
    }
