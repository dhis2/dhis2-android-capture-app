package org.dhis2.usescases.eventsWithoutRegistration.eventInitial

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.dhis2.data.forms.dataentry.DataEntryStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.FormRepositoryImpl
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.form.ui.provider.DisplayNameProviderImpl
import org.dhis2.form.ui.validation.FieldErrorMessageProvider
import org.dhis2.utils.reporting.CrashReportController
import org.hisp.dhis.android.core.D2

@Module
class EventDetailsModule(val eventUid: String, val context: Context) {

    @Provides
    @PerFragment
    fun eventDetailsViewModelFactory(d2: D2): EventDetailsViewModelFactory {
        return EventDetailsViewModelFactory(eventUid, d2)
    }

    @Provides
    @PerFragment
    fun provideEventDetailsFormRepository(
        d2: D2,
        crashReportController: CrashReportController,
        fieldViewModelFactory: FieldViewModelFactory,
        resourceManager: ResourceManager
    ): FormRepository = FormRepositoryImpl(
        formValueStore = ValueStoreImpl(
            d2,
            eventUid,
            DataEntryStore.EntryMode.DE,
            DhisEnrollmentUtils(d2),
            crashReportController
        ),
        fieldErrorMessageProvider = FieldErrorMessageProvider(context),
        displayNameProvider = DisplayNameProviderImpl(d2),
        dataEntryRepository = org.dhis2.form.data.EventInitialRepository(
            fieldViewModelFactory,
            eventUid,
            d2,
            resourceManager
        ),
        ruleEngineRepository = null,
        rulesUtilsProvider = null
    )
}
