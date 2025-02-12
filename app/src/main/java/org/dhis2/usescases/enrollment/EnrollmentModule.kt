package org.dhis2.usescases.enrollment

import android.content.Context
import dagger.Module
import dagger.Provides
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.commons.data.EntryMode
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.PreferenceProviderImpl
import org.dhis2.commons.reporting.CrashReportController
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.EventResourcesProvider
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.dhis2.data.forms.dataentry.SearchTEIRepository
import org.dhis2.data.forms.dataentry.SearchTEIRepositoryImpl
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.form.data.EnrollmentRepository
import org.dhis2.form.data.metadata.EnrollmentConfiguration
import org.dhis2.form.data.metadata.FileResourceConfiguration
import org.dhis2.form.data.metadata.OptionSetConfiguration
import org.dhis2.form.data.metadata.OrgUnitConfiguration
import org.dhis2.form.model.EnrollmentMode
import org.dhis2.form.model.RowAction
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.form.ui.FieldViewModelFactoryImpl
import org.dhis2.form.ui.provider.AutoCompleteProviderImpl
import org.dhis2.form.ui.provider.DisplayNameProviderImpl
import org.dhis2.form.ui.provider.EnrollmentFormLabelsProvider
import org.dhis2.form.ui.provider.FormResultDialogProvider
import org.dhis2.form.ui.provider.FormResultDialogResourcesProvider
import org.dhis2.form.ui.provider.HintProviderImpl
import org.dhis2.form.ui.provider.KeyboardActionProviderImpl
import org.dhis2.form.ui.provider.LegendValueProviderImpl
import org.dhis2.form.ui.provider.UiEventTypesProviderImpl
import org.dhis2.form.ui.validation.FieldErrorMessageProvider
import org.dhis2.usescases.teiDashboard.TeiAttributesProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository

@Module
class EnrollmentModule(
    private val enrollmentView: EnrollmentView,
    val enrollmentUid: String,
    val programUid: String,
    private val enrollmentMode: EnrollmentActivity.EnrollmentMode,
    private val activityContext: Context,
) {

    @Provides
    @PerActivity
    fun provideEnrollmentRepository(d2: D2): EnrollmentObjectRepository {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid)
    }

    @Provides
    @PerActivity
    fun provideTeiRepository(
        d2: D2,
        enrollmentRepository: EnrollmentObjectRepository,
    ): TrackedEntityInstanceObjectRepository {
        return d2.trackedEntityModule().trackedEntityInstances()
            .uid(enrollmentRepository.blockingGet()?.trackedEntityInstance())
    }

    @Provides
    @PerActivity
    fun provideProgramRepository(d2: D2): ReadOnlyOneObjectRepositoryFinalImpl<Program> {
        return d2.programModule().programs().uid(programUid)
    }

    @Provides
    @PerActivity
    fun provideEnrollmentConfiguration(
        d2: D2,
    ) = EnrollmentConfiguration(d2, enrollmentUid)

    @Provides
    @PerActivity
    fun provideDataEntryRepository(
        modelFactory: FieldViewModelFactory,
        enrollmentFormLabelsProvider: EnrollmentFormLabelsProvider,
        enrollmentConfiguration: EnrollmentConfiguration,
        metadataIconProvider: MetadataIconProvider,
    ): EnrollmentRepository {
        return EnrollmentRepository(
            fieldFactory = modelFactory,
            conf = enrollmentConfiguration,
            enrollmentMode = EnrollmentMode.valueOf(enrollmentMode.name),
            enrollmentFormLabelsProvider = enrollmentFormLabelsProvider,
            metadataIconProvider = metadataIconProvider,
        )
    }

    @Provides
    @PerActivity
    fun provideEnrollmentFormLabelsProvider(resourceManager: ResourceManager) =
        EnrollmentFormLabelsProvider(resourceManager)

    @Provides
    @PerActivity
    fun provideEventRepository(d2: D2): EventCollectionRepository {
        return d2.eventModule().events()
    }

    @Provides
    @PerActivity
    fun fieldFactory(
        context: Context,
        d2: D2,
        resourceManager: ResourceManager,
        periodUtils: DhisPeriodUtils,
    ): FieldViewModelFactory {
        return FieldViewModelFactoryImpl(
            HintProviderImpl(context),
            DisplayNameProviderImpl(
                OptionSetConfiguration(d2),
                OrgUnitConfiguration(d2),
                FileResourceConfiguration(d2),
                periodUtils,
            ),
            UiEventTypesProviderImpl(),
            KeyboardActionProviderImpl(),
            LegendValueProviderImpl(d2, resourceManager),
            AutoCompleteProviderImpl(PreferenceProviderImpl(context)),
        )
    }

    @Provides
    @PerActivity
    fun provideDateEditionWarningHandler(
        enrollmentConfiguration: EnrollmentConfiguration,
        eventResourcesProvider: EventResourcesProvider,
    ) = DateEditionWarningHandler(
        enrollmentConfiguration,
        eventResourcesProvider,
    )

    @Provides
    @PerActivity
    fun providePresenter(
        d2: D2,
        enrollmentObjectRepository: EnrollmentObjectRepository,
        teiRepository: TrackedEntityInstanceObjectRepository,
        programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program>,
        schedulerProvider: SchedulerProvider,
        enrollmentFormRepository: EnrollmentFormRepository,
        analyticsHelper: AnalyticsHelper,
        matomoAnalyticsController: MatomoAnalyticsController,
        eventCollectionRepository: EventCollectionRepository,
        teiAttributesProvider: TeiAttributesProvider,
        dateEditionWarningHandler: DateEditionWarningHandler,
    ): EnrollmentPresenterImpl {
        return EnrollmentPresenterImpl(
            enrollmentView,
            d2,
            enrollmentObjectRepository,
            teiRepository,
            programRepository,
            schedulerProvider,
            enrollmentFormRepository,
            analyticsHelper,
            matomoAnalyticsController,
            eventCollectionRepository,
            teiAttributesProvider,
            dateEditionWarningHandler,
        )
    }

    @Provides
    @PerActivity
    fun provideOnRowActionProcessor(): FlowableProcessor<RowAction> {
        return PublishProcessor.create()
    }

    @Provides
    @PerActivity
    fun valueStore(
        d2: D2,
        enrollmentRepository: EnrollmentObjectRepository,
        crashReportController: CrashReportController,
        networkUtils: NetworkUtils,
        searchTEIRepository: SearchTEIRepository,
        resourceManager: ResourceManager,
    ): ValueStore {
        val fieldErrorMessageProvider = FieldErrorMessageProvider(activityContext)
        return ValueStoreImpl(
            d2,
            enrollmentRepository.blockingGet()?.trackedEntityInstance()!!,
            EntryMode.ATTR,
            DhisEnrollmentUtils(d2),
            crashReportController,
            networkUtils,
            searchTEIRepository,
            fieldErrorMessageProvider,
            resourceManager,
        )
    }

    @Provides
    @PerActivity
    internal fun searchRepository(d2: D2): SearchTEIRepository {
        return SearchTEIRepositoryImpl(d2, DhisEnrollmentUtils(d2))
    }

    @Provides
    @PerActivity
    fun formRepository(
        d2: D2,
        enrollmentRepository: EnrollmentObjectRepository,
        programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program>,
        teiRepository: TrackedEntityInstanceObjectRepository,
        enrollmentService: DhisEnrollmentUtils,
    ): EnrollmentFormRepository {
        return EnrollmentFormRepositoryImpl(
            d2,
            enrollmentRepository,
            programRepository,
            teiRepository,
            enrollmentService,
        )
    }

    @Provides
    @PerActivity
    fun provideResultDialogProvider(
        resourceManager: ResourceManager,
    ): FormResultDialogProvider {
        return FormResultDialogProvider(
            FormResultDialogResourcesProvider(resourceManager),
        )
    }

    @Provides
    @PerActivity
    fun provideDialogResourcesProvider(
        resourceManager: ResourceManager,
    ): FormResultDialogResourcesProvider {
        return FormResultDialogResourcesProvider(resourceManager)
    }

    @Provides
    @PerActivity
    fun providesTeiAttributesProvider(d2: D2): TeiAttributesProvider {
        return TeiAttributesProvider(d2)
    }
}
