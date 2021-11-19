package org.dhis2.form.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.DispatcherProvider

@Suppress("UNCHECKED_CAST")
class FormViewModelFactory(
    private val repository: FormRepository,
    private val dispatcher: DispatcherProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FormViewModel(
            repository = repository,
            dispatcher = dispatcher
        ) as T
    }
}
