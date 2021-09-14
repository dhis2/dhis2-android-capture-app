package org.dhis2.form

import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.DispatcherProvider
import org.dhis2.form.ui.FormViewModelFactory

object Injector {
    fun provideFormViewModelFactory(
        repository: FormRepository,
        dispatcher: DispatcherProvider
    ): FormViewModelFactory {
        return FormViewModelFactory(repository, dispatcher)
    }
}
