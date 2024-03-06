package org.dhis2.form.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.data.FormRepository

@Suppress("UNCHECKED_CAST")
class FormViewModelFactory(
    private val repository: FormRepository,
    private val dispatcher: DispatcherProvider,
    private val openErrorLocation: Boolean
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FormViewModel(
            repository = repository,
            dispatcher = dispatcher,
            openErrorLocation = openErrorLocation
        ) as T
    }
}
