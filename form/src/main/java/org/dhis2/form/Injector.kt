package org.dhis2.form

import android.content.Context
import org.dhis2.commons.data.EntryMode
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.reporting.CrashReportControllerImpl
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.data.DataEntryRepository
import org.dhis2.form.data.EnrollmentRepository
import org.dhis2.form.data.EnrollmentRuleEngineRepository
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.FormRepositoryImpl
import org.dhis2.form.data.FormValueStore
import org.dhis2.form.data.RulesUtilsProviderImpl
import org.dhis2.form.data.SearchOptionSetOption
import org.dhis2.form.data.metadata.OptionSetConfiguration
import org.dhis2.form.data.metadata.OrgUnitConfiguration
import org.dhis2.form.model.DispatcherProvider
import org.dhis2.form.model.EnrollmentMode
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
        recordUid: String,
        entryMode: EntryMode,
        enrollmentUid: String,
        enrollmentMode: EnrollmentMode?
    ): FormViewModelFactory {
        return FormViewModelFactory(
            provideFormRepository(context, recordUid, entryMode, enrollmentUid, enrollmentMode),
            provideDispatchers()
        )
    }

    private fun provideD2() = D2Manager.getD2()

    fun provideOptionSetDialog(): SearchOptionSetOption {
        return SearchOptionSetOption(
            provideD2().optionModule().options()
        )
    }

    private fun provideDispatchers(): DispatcherProvider {
        return FormDispatcher()
    }

    private fun provideFormRepository(
        context: Context,
        recordUid: String,
        entryMode: EntryMode,
        enrollmentUid: String,
        enrollmentMode: EnrollmentMode?
    ): FormRepository {
        return FormRepositoryImpl(
            formValueStore = provideFormValueStore(
                context = context,
                recordUid = recordUid,
                entryMode = entryMode,
                enrollmentUid = enrollmentUid
            ),
            fieldErrorMessageProvider = provideFieldErrorMessage(context),
            displayNameProvider = provideDisplayNameProvider(),
            dataEntryRepository = provideDataEntryRepository(
                enrollmentUid,
                context,
                enrollmentMode
            ),
            ruleEngineRepository = provideEnrollmentRuleEngineRepository(enrollmentUid),
            rulesUtilsProvider = provideRulesUtilsProvider(),
            legendValueProvider = provideLegendValueProvider(context)
        )
    }

    private fun provideDataEntryRepository(
        enrollmentUid: String,
        context: Context,
        enrollmentMode: EnrollmentMode?
    ): DataEntryRepository {
        // TODO add event and search repositories
        return provideEnrollmentRepository(enrollmentUid, context, enrollmentMode!!)
    }

    private fun provideEnrollmentRepository(
        enrollmentUid: String,
        context: Context,
        enrollmentMode: EnrollmentMode
    ): DataEntryRepository {
        return EnrollmentRepository(
            fieldFactory = provideFieldFactory(context),
            enrollmentUid = enrollmentUid,
            d2 = provideD2(),
            enrollmentMode = enrollmentMode,
            enrollmentFormLabelsProvider = provideEnrollmentFormLabelsProvider(context)
        )
    }

    private fun provideEnrollmentFormLabelsProvider(context: Context) =
        EnrollmentFormLabelsProvider(provideResourcesManager(context))

    private fun provideFieldFactory(context: Context): FieldViewModelFactory =
        FieldViewModelFactoryImpl(
            noMandatoryFields = false, // TODO CHECK If this is always false
            uiStyleProvider = provideUiStyleProvider(context),
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

    private fun provideUiStyleProvider(context: Context): UiStyleProvider =
        UiStyleProviderImpl(
            colorFactory = FormUiModelColorFactoryImpl(context, true), // TODO CHECK TRUE
            longTextColorFactory = LongTextUiColorFactoryImpl(context, true) // TODO CHECK TRUE
        )

    private fun provideFormValueStore(
        context: Context,
        recordUid: String,
        entryMode: EntryMode,
        enrollmentUid: String
    ): FormValueStore {
        return FormValueStore(
            d2 = provideD2(),
            recordUid = recordUid,
            entryMode = entryMode,
            enrollmentRepository = provideEnrollmentObjectRepository(enrollmentUid),
            crashReportController = provideCrashReportController(),
            networkUtils = provideNetworkUtils(context),
            resourceManager = provideResourcesManager(context)
        )
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
        OrgUnitConfiguration(provideD2())
    )

    private fun provideEnrollmentRuleEngineRepository(enrollmentUid: String) =
        EnrollmentRuleEngineRepository(provideD2(), enrollmentUid)

    private fun provideRulesUtilsProvider() = RulesUtilsProviderImpl(provideD2())

    private fun provideLegendValueProvider(context: Context) = LegendValueProviderImpl(
        provideD2(),
        provideResourcesManager(context)
    )
}
