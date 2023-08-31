package org.dhis2.form.di

import android.content.Context
import org.dhis2.commons.data.EntryMode
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.reporting.CrashReportControllerImpl
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.data.DataEntryRepository
import org.dhis2.form.data.EnrollmentRepository
import org.dhis2.form.data.EnrollmentRuleEngineRepository
import org.dhis2.form.data.EventRepository
import org.dhis2.form.data.EventRuleEngineRepository
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.FormRepositoryImpl
import org.dhis2.form.data.FormValueStore
import org.dhis2.form.data.OptionsRepository
import org.dhis2.form.data.RuleEngineRepository
import org.dhis2.form.data.RulesUtilsProviderImpl
import org.dhis2.form.data.SearchOptionSetOption
import org.dhis2.form.data.SearchRepository
import org.dhis2.form.data.metadata.FileResourceConfiguration
import org.dhis2.form.data.metadata.OptionSetConfiguration
import org.dhis2.form.data.metadata.OrgUnitConfiguration
import org.dhis2.form.model.EnrollmentRecords
import org.dhis2.form.model.EventRecords
import org.dhis2.form.model.FormRepositoryRecords
import org.dhis2.form.model.SearchRecords
import org.dhis2.form.model.coroutine.FormDispatcher
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.form.ui.FieldViewModelFactoryImpl
import org.dhis2.form.ui.FormViewModelFactory
import org.dhis2.form.ui.LayoutProviderImpl
import org.dhis2.form.ui.provider.DisplayNameProviderImpl
import org.dhis2.form.ui.provider.EnrollmentFormLabelsProvider
import org.dhis2.form.ui.provider.HintProviderImpl
import org.dhis2.form.ui.provider.KeyboardActionProviderImpl
import org.dhis2.form.ui.provider.LegendValueProviderImpl
import org.dhis2.form.ui.provider.UiEventTypesProviderImpl
import org.dhis2.form.ui.provider.UiStyleProvider
import org.dhis2.form.ui.provider.UiStyleProviderImpl
import org.dhis2.form.ui.style.FormUiModelColorFactoryImpl
import org.dhis2.form.ui.style.LongTextUiColorFactoryImpl
import org.dhis2.form.ui.validation.FieldErrorMessageProvider
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository

object Injector {
    fun provideFormViewModelFactory(
        context: Context,
        repositoryRecords: FormRepositoryRecords,
        openErrorLocation: Boolean
    ): FormViewModelFactory {
        return FormViewModelFactory(
            provideFormRepository(
                context,
                repositoryRecords
            ),
            provideDispatchers(),
            openErrorLocation
        )
    }

    private fun provideD2() = D2Manager.getD2()

    fun provideOptionSetDialog(): SearchOptionSetOption {
        return SearchOptionSetOption(
            provideD2().optionModule().options()
        )
    }

    fun provideDispatchers(): DispatcherProvider {
        return FormDispatcher()
    }

    private fun provideFormRepository(
        context: Context,
        repositoryRecords: FormRepositoryRecords
    ): FormRepository {
        return FormRepositoryImpl(
            formValueStore = provideFormValueStore(
                context = context,
                recordUid = repositoryRecords.recordUid,
                entryMode = repositoryRecords.entryMode
            ),
            fieldErrorMessageProvider = provideFieldErrorMessage(context),
            displayNameProvider = provideDisplayNameProvider(),
            dataEntryRepository = provideDataEntryRepository(
                entryMode = repositoryRecords.entryMode,
                context = context,
                repositoryRecords = repositoryRecords
            ),
            ruleEngineRepository = provideRuleEngineRepository(
                repositoryRecords.entryMode,
                repositoryRecords.recordUid
            ),
            rulesUtilsProvider = provideRulesUtilsProvider(),
            legendValueProvider = provideLegendValueProvider(context)
        )
    }

    private fun provideDataEntryRepository(
        entryMode: EntryMode?,
        context: Context,
        repositoryRecords: FormRepositoryRecords
    ): DataEntryRepository {
        return when (entryMode) {
            EntryMode.ATTR -> provideEnrollmentRepository(
                context,
                repositoryRecords as EnrollmentRecords
            )

            EntryMode.DE -> provideEventRepository(
                context,
                repositoryRecords as EventRecords
            )

            else -> provideSearchRepository(
                context,
                repositoryRecords as SearchRecords
            )
        }
    }

    private fun provideSearchRepository(
        context: Context,
        searchRecords: SearchRecords
    ): DataEntryRepository {
        return SearchRepository(
            d2 = provideD2(),
            fieldViewModelFactory = provideFieldFactory(
                context,
                searchRecords.allowMandatoryFields,
                searchRecords.isBackgroundTransparent
            ),
            programUid = searchRecords.programUid,
            teiTypeUid = searchRecords.teiTypeUid,
            currentSearchValues = searchRecords.currentSearchValues
        )
    }

    private fun provideEnrollmentRepository(
        context: Context,
        enrollmentRecords: EnrollmentRecords
    ): DataEntryRepository {
        return EnrollmentRepository(
            fieldFactory = provideFieldFactory(
                context,
                enrollmentRecords.allowMandatoryFields,
                enrollmentRecords.isBackgroundTransparent
            ),
            enrollmentUid = enrollmentRecords.enrollmentUid,
            d2 = provideD2(),
            enrollmentMode = enrollmentRecords.enrollmentMode,
            enrollmentFormLabelsProvider = provideEnrollmentFormLabelsProvider(context)
        )
    }

    private fun provideEventRepository(
        context: Context,
        eventRecords: EventRecords
    ): DataEntryRepository {
        return EventRepository(
            fieldFactory = provideFieldFactory(
                context,
                eventRecords.allowMandatoryFields,
                eventRecords.isBackgroundTransparent
            ),
            eventUid = eventRecords.eventUid,
            d2 = provideD2()
        )
    }

    private fun provideEnrollmentFormLabelsProvider(context: Context) =
        EnrollmentFormLabelsProvider(provideResourcesManager(context))

    private fun provideFieldFactory(
        context: Context,
        allowMandatoryFields: Boolean,
        isBackgroundTransparent: Boolean
    ): FieldViewModelFactory = FieldViewModelFactoryImpl(
        noMandatoryFields = !allowMandatoryFields,
        uiStyleProvider = provideUiStyleProvider(context, isBackgroundTransparent),
        layoutProvider = provideLayoutProvider(),
        hintProvider = provideHintProvider(context),
        displayNameProvider = provideDisplayNameProvider(),
        uiEventTypesProvider = provideUiEventTypesProvider(),
        keyboardActionProvider = provideKeyBoardActionProvider(),
        legendValueProvider = provideLegendValueProvider(context)
    )

    private fun provideKeyBoardActionProvider() = KeyboardActionProviderImpl()

    private fun provideUiEventTypesProvider() = UiEventTypesProviderImpl()

    private fun provideHintProvider(context: Context) = HintProviderImpl(context)

    private fun provideLayoutProvider() = LayoutProviderImpl()

    private fun provideUiStyleProvider(
        context: Context,
        isBackgroundTransparent: Boolean
    ): UiStyleProvider = UiStyleProviderImpl(
        colorFactory = FormUiModelColorFactoryImpl(context, isBackgroundTransparent),
        longTextColorFactory = LongTextUiColorFactoryImpl(context, isBackgroundTransparent),
        actionIconClickable = isBackgroundTransparent
    )

    private fun provideFormValueStore(
        context: Context,
        recordUid: String?,
        entryMode: EntryMode?
    ): FormValueStore? {
        return entryMode?.let { it ->
            val enrollmentObjectRepository = if (it == EntryMode.ATTR) {
                provideEnrollmentObjectRepository(recordUid!!)
            } else {
                null
            }
            FormValueStore(
                d2 = provideD2(),
                recordUid = enrollmentObjectRepository?.blockingGet()?.trackedEntityInstance()
                    ?: recordUid!!,
                entryMode = it,
                enrollmentRepository = enrollmentObjectRepository,
                crashReportController = provideCrashReportController(),
                networkUtils = provideNetworkUtils(context),
                resourceManager = provideResourcesManager(context)
            )
        }
    }

    private fun provideEnrollmentObjectRepository(
        enrollmentUid: String
    ): EnrollmentObjectRepository {
        return provideD2().enrollmentModule().enrollments().uid(enrollmentUid)
    }

    private fun provideCrashReportController() = CrashReportControllerImpl()

    private fun provideNetworkUtils(context: Context) = NetworkUtils(context)

    private fun provideResourcesManager(context: Context) = ResourceManager(context)

    private fun provideFieldErrorMessage(context: Context) = FieldErrorMessageProvider(context)

    private fun provideDisplayNameProvider() = DisplayNameProviderImpl(
        OptionSetConfiguration(provideD2()),
        OrgUnitConfiguration(provideD2()),
        FileResourceConfiguration(provideD2())
    )

    private fun provideRuleEngineRepository(
        entryMode: EntryMode?,
        recordUid: String?
    ): RuleEngineRepository? {
        return when (entryMode) {
            EntryMode.ATTR -> provideEnrollmentRuleEngineRepository(recordUid!!)
            EntryMode.DE -> provideEventRuleEngineRepository(recordUid!!)
            else -> null
        }
    }

    private fun provideEnrollmentRuleEngineRepository(enrollmentUid: String) =
        EnrollmentRuleEngineRepository(provideD2(), enrollmentUid)

    private fun provideEventRuleEngineRepository(eventUid: String) =
        EventRuleEngineRepository(provideD2(), eventUid)

    private fun provideRulesUtilsProvider() = RulesUtilsProviderImpl(
        provideD2(),
        provideOptionsRepository()
    )

    private fun provideOptionsRepository() = OptionsRepository(provideD2())

    private fun provideLegendValueProvider(context: Context) = LegendValueProviderImpl(
        provideD2(),
        provideResourcesManager(context)
    )
}
