package org.dhis2.form

import org.dhis2.form.data.FormRepository
import org.dhis2.form.ui.FormViewModelFactory

object Injector {

    fun provideFormViewModelFactory(repository: FormRepository): FormViewModelFactory {
        return FormViewModelFactory(repository)
    }
}
