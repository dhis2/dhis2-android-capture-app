package org.dhis2.usescases.eventsWithoutRegistration.eventInitial

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.data.DataEntryRepository
import org.dhis2.form.data.EventDetailRepository
import org.dhis2.form.data.EventDetailStore
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.FormRepositoryImpl
import org.dhis2.form.ui.FieldViewModelFactoryImpl
import org.dhis2.form.ui.LayoutProviderImpl
import org.dhis2.form.ui.provider.DisplayNameProviderImpl
import org.dhis2.form.ui.provider.HintProviderImpl
import org.dhis2.form.ui.provider.KeyboardActionProviderImpl
import org.dhis2.form.ui.provider.UiEventTypesProviderImpl
import org.dhis2.form.ui.provider.UiStyleProviderImpl
import org.dhis2.form.ui.style.FormUiModelColorFactoryImpl
import org.dhis2.form.ui.style.LongTextUiColorFactoryImpl
import org.dhis2.form.ui.validation.FieldErrorMessageProvider
import org.dhis2.utils.reporting.CrashReportController
import org.hisp.dhis.android.core.D2

@Module
class EventDetailsModule(
    val eventUid: String,
    val context: Context,
    val eventCreationType: String?
) {

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
        resourceManager: ResourceManager
    ): FormRepository = FormRepositoryImpl(
        formValueStore = EventDetailStore(),
        fieldErrorMessageProvider = FieldErrorMessageProvider(context),
        displayNameProvider = DisplayNameProviderImpl(d2),
        dataEntryRepository = provideEventDetailRepository(d2, resourceManager),
        ruleEngineRepository = null,
        rulesUtilsProvider = null
    )

    private fun provideEventDetailRepository(
        d2: D2,
        resourceManager: ResourceManager
    ): DataEntryRepository =
        EventDetailRepository(
            FieldViewModelFactoryImpl(
                noMandatoryFields = true,
                uiStyleProvider = UiStyleProviderImpl(
                    colorFactory = FormUiModelColorFactoryImpl(context, true),
                    longTextColorFactory = LongTextUiColorFactoryImpl(context, true)
                ),
                layoutProvider = LayoutProviderImpl(),
                hintProvider = HintProviderImpl(context),
                displayNameProvider = DisplayNameProviderImpl(d2),
                uiEventTypesProvider = UiEventTypesProviderImpl(),
                keyboardActionProvider = KeyboardActionProviderImpl()
            ),
            eventUid,
            d2,
            resourceManager,
            eventCreationType
        )
}
