package org.dhis2.tracker.searchparameters.di

import org.dhis2.mobile.commons.input.UiActionHandler
import org.dhis2.tracker.searchparameters.ui.viewmodel.SearchParametersViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val searchParametersModule = module {

    // ViewModels
    viewModel { params ->
        val uiActionHandler = params.get<UiActionHandler>()

        SearchParametersViewModel(
            uiActionHandler = uiActionHandler,
        )
    }
}
