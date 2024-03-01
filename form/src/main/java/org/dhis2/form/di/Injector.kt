package org.dhis2.form.di

import android.content.Context
import org.dhis2.commons.data.EntryMode
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.PreferenceProviderImpl
import org.dhis2.commons.reporting.CrashReportControllerImpl
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.data.DataEntryRepository
import org.dhis2.form.data.EnrollmentRepository
import org.dhis2.form.data.EventRepository
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.FormRepositoryImpl
import org.dhis2.form.data.FormValueStore
import org.dhis2.form.data.OptionsRepository
import org.dhis2.form.data.RulesUtilsProviderImpl
import org.dhis2.form.data.SearchOptionSetOption
import org.dhis2.form.data.metadata.EnrollmentConfiguration
import org.dhis2.form.data.metadata.FileResourceConfiguration
import org.dhis2.form.data.metadata.OptionSetConfiguration
import org.dhis2.form.data.metadata.OrgUnitConfiguration
import org.dhis2.form.model.EnrollmentRecords
import org.dhis2.form.model.EventRecords
import org.dhis2.form.model.FormRepositoryRecords
import org.dhis2.form.model.coroutine.FormDispatcher
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.form.ui.FieldViewModelFactoryImpl
import org.dhis2.form.ui.FormViewModelFactory
import org.dhis2.form.ui.LayoutProviderImpl
import org.dhis2.form.ui.provider.AutoCompleteProviderImpl
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
import org.dhis2.mobileProgramRules.EvaluationType
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.dhis2.mobileProgramRules.RulesRepository
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository

object Injector {
    fun provideFormViewModelFactory(
        context: Context,
        repositoryRecords: FormRepositoryRecords,
        openErrorLocation: Boolean,
        useCompose: Boolean,
    ): FormViewModelFactory {
        return FormViewModelFactory(
            provideFormRepository(
                context,
                repositoryRecords,
                useCompose,
            ),
            provideDispatchers(),
            openErrorLocation,
            providePreferenceProvider(context),
        )
    }

    private fun provideD2() = D2Manager.getD2()

    fun provideOptionSetDialog(): SearchOptionSetOption {
        return SearchOptionSetOption(
            provideD2().optionModule().options(),
        )
    }

    private fun provideMetadataIconProvider(context: Context) =
        MetadataIconProvider(provideD2(), provideResourcesManager(context))

    fun provideDispatchers(): DispatcherProvider {
        return FormDispatcher()
    }

    private fun provideFormRepository(
        context: Context,
        repositoryRecords: FormRepositoryRecords,
        useCompose: Boolean,
    ): FormRepository {
        return FormRepositoryImpl(
            formValueStore = provideFormValueStore(
                context = context,
                recordUid = repositoryRecords.recordUid,
                entryMode = repositoryRecords.entryMode,
            ),
            fieldErrorMessageProvider = provideFieldErrorMessage(context),
            displayNameProvider = provideDisplayNameProvider(),
            dataEntryRepository = provideDataEntryRepository(
                entryMode = repositoryRecords.entryMode,
                context = context,
                repositoryRecords = repositoryRecords,
                metadataIconProvider = provideMetadataIconProvider(context),
            ),
            ruleEngineRepository = provideRuleEngineRepository(
                repositoryRecords.entryMode,
                repositoryRecords.recordUid,
            ),
            rulesUtilsProvider = provideRulesUtilsProvider(),
            legendValueProvider = provideLegendValueProvider(context),
            useCompose = useCompose,
        )
    }

    private fun provideDataEntryRepository(
        entryMode: EntryMode?,
        context: Context,
        repositoryRecords: FormRepositoryRecords,
        metadataIconProvider: MetadataIconProvider,
    ): DataEntryRepository {
        return when (entryMode) {
            EntryMode.ATTR -> provideEnrollmentRepository(
                context,
                repositoryRecords as EnrollmentRecords,
                metadataIconProvider,
            )

            else -> provideEventRepository(
                context,
                repositoryRecords as EventRecords,
            )
        }
    }

    private fun provideEnrollmentRepository(
        context: Context,
        enrollmentRecords: EnrollmentRecords,
        metadataIconProvider: MetadataIconProvider,
    ): DataEntryRepository {
        return EnrollmentRepository(
            fieldFactory = provideFieldFactory(context),
            conf = EnrollmentConfiguration(provideD2(), enrollmentRecords.enrollmentUid, metadataIconProvider),
            enrollmentMode = enrollmentRecords.enrollmentMode,
            enrollmentFormLabelsProvider = provideEnrollmentFormLabelsProvider(context),
        )
    }

    private fun provideEventRepository(
        context: Context,
        eventRecords: EventRecords,
    ): DataEntryRepository {
        return EventRepository(
            fieldFactory = provideFieldFactory(context),
            eventUid = eventRecords.eventUid,
            d2 = provideD2(),
            metadataIconProvider = provideMetadataIconProvider(context),
        )
    }

    private fun provideEnrollmentFormLabelsProvider(context: Context) =
        EnrollmentFormLabelsProvider(provideResourcesManager(context))

    private fun provideFieldFactory(
        context: Context,
    ): FieldViewModelFactory = FieldViewModelFactoryImpl(
        uiStyleProvider = provideUiStyleProvider(context),
        layoutProvider = provideLayoutProvider(),
        hintProvider = provideHintProvider(context),
        displayNameProvider = provideDisplayNameProvider(),
        uiEventTypesProvider = provideUiEventTypesProvider(),
        keyboardActionProvider = provideKeyBoardActionProvider(),
        legendValueProvider = provideLegendValueProvider(context),
        autoCompleteProvider = provideAutoCompleteProvider(context),
    )

    private fun provideKeyBoardActionProvider() = KeyboardActionProviderImpl()

    private fun provideUiEventTypesProvider() = UiEventTypesProviderImpl()

    private fun provideHintProvider(context: Context) = HintProviderImpl(context)

    private fun provideLayoutProvider() = LayoutProviderImpl()

    private fun provideUiStyleProvider(
        context: Context,
    ): UiStyleProvider = UiStyleProviderImpl(
        colorFactory = FormUiModelColorFactoryImpl(
            context,
            provideColorUtils(),
        ),
        longTextColorFactory = LongTextUiColorFactoryImpl(
            context,
            provideColorUtils(),
        ),
        actionIconClickable = true,
    )

    private fun provideFormValueStore(
        context: Context,
        recordUid: String?,
        entryMode: EntryMode?,
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
                resourceManager = provideResourcesManager(context),
            )
        }
    }

    private fun provideEnrollmentObjectRepository(
        enrollmentUid: String,
    ): EnrollmentObjectRepository {
        return provideD2().enrollmentModule().enrollments().uid(enrollmentUid)
    }

    private fun provideCrashReportController() = CrashReportControllerImpl()

    private fun provideNetworkUtils(context: Context) = NetworkUtils(context)

    fun provideResourcesManager(context: Context) = ResourceManager(
        context,
        provideColorUtils(),
    )

    private fun provideFieldErrorMessage(context: Context) = FieldErrorMessageProvider(context)

    private fun provideDisplayNameProvider() = DisplayNameProviderImpl(
        OptionSetConfiguration(provideD2()),
        OrgUnitConfiguration(provideD2()),
        FileResourceConfiguration(provideD2()),
    )

    private fun providePreferenceProvider(context: Context) = PreferenceProviderImpl(context)

    private fun provideRuleEngineRepository(
        entryMode: EntryMode?,
        recordUid: String?,
    ): RuleEngineHelper? {
        return recordUid?.let {
            RuleEngineHelper(
                when (entryMode) {
                    EntryMode.DE -> EvaluationType.Event(recordUid)
                    EntryMode.ATTR -> EvaluationType.Enrollment(recordUid)
                    else -> throw IllegalArgumentException()
                },
                RulesRepository(provideD2()),
            )
        }
    }

    private fun provideRulesUtilsProvider() = RulesUtilsProviderImpl(
        provideD2(),
        provideOptionsRepository(),
    )

    private fun provideOptionsRepository() = OptionsRepository(provideD2())

    private fun provideLegendValueProvider(context: Context) = LegendValueProviderImpl(
        provideD2(),
        provideResourcesManager(context),
    )

    private fun provideAutoCompleteProvider(context: Context) = AutoCompleteProviderImpl(
        providePreferenceProvider(context),
    )

    private fun provideColorUtils() = ColorUtils()
}
