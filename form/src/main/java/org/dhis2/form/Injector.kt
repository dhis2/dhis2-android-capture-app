package org.dhis2.form

import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.SearchOptionSetOption
import org.dhis2.form.model.DispatcherProvider
import org.dhis2.form.ui.FormViewModelFactory
import org.hisp.dhis.android.core.D2Manager

object Injector {
    fun provideFormViewModelFactory(
        repository: FormRepository,
        dispatcher: DispatcherProvider
    ): FormViewModelFactory {
        return FormViewModelFactory(repository, dispatcher)
    }

    fun provideD2() = D2Manager.getD2()

    fun provideOptionSetDialog(): SearchOptionSetOption {
        return SearchOptionSetOption(
            provideD2().optionModule().options()
        )
    }
}
