package org.dhis2.form.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.data.FormRepository
import org.dhis2.form.ui.mapper.FormSectionMapper
import org.dhis2.form.ui.provider.FormResultDialogProvider

class FormViewModelFactory(
    private val repository: FormRepository,
    private val dispatcher: DispatcherProvider,
    private val openErrorLocation: Boolean,
    private val resultDialogUiProvider: FormResultDialogProvider,
    private val formSectionMapper: FormSectionMapper,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        FormViewModel(
            repository = repository,
            dispatcher = dispatcher,
            openErrorLocation = openErrorLocation,
            resultDialogUiProvider = resultDialogUiProvider,
            formSectionMapper = formSectionMapper,
        ) as T
}
