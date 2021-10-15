package org.dhis2.usescases.enrollment

import android.content.Context
import dagger.Module
import dagger.Provides
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.Bindings.valueTypeHintMap
import org.dhis2.R
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.dhis2.data.forms.RulesRepository
import org.dhis2.data.forms.dataentry.DataEntryStore
import org.dhis2.data.forms.dataentry.EnrollmentRepository
import org.dhis2.data.forms.dataentry.FormUiModelColorFactoryImpl
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl
import org.dhis2.data.forms.dataentry.fields.LayoutProviderImpl
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.FormRepositoryImpl
import org.dhis2.form.model.RowAction
import org.dhis2.form.ui.provider.DisplayNameProviderImpl
import org.dhis2.form.ui.provider.HintProviderImpl
import org.dhis2.form.ui.style.FormUiColorFactory
import org.dhis2.form.ui.validation.FieldErrorMessageProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController
import org.dhis2.utils.reporting.CrashReportController
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository

@Module
class EnrollmentModule(
    private val enrollmentView: EnrollmentView,
    val enrollmentUid: String,
    val programUid: String,
    private val enrollmentMode: EnrollmentActivity.EnrollmentMode,
    val activityContext: Context
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
        enrollmentRepository: EnrollmentObjectRepository
    ): TrackedEntityInstanceObjectRepository {
        return d2.trackedEntityModule().trackedEntityInstances()
            .uid(enrollmentRepository.blockingGet().trackedEntityInstance())
    }

    @Provides
    @PerActivity
    fun provideProgramRepository(d2: D2): ReadOnlyOneObjectRepositoryFinalImpl<Program> {
        return d2.programModule().programs().uid(programUid)
    }

    @Provides
    @PerActivity
    fun provideDataEntrytRepository(
        context: Context,
        d2: D2,
        dhisEnrollmentUtils: DhisEnrollmentUtils,
        modelFactory: FieldViewModelFactory
    ): EnrollmentRepository {
        val enrollmentDataSectionLabel = context.getString(R.string.enrollment_data_section_label)
        val singleSectionLabel = context.getString(R.string.enrollment_single_section_label)
        val enrollmentOrgUnitLabel = context.getString(R.string.enrolling_ou)
        val teiCoordinatesLabel = context.getString(R.string.tei_coordinates)
        val enrollmentCoordinatesLabel = context.getString(R.string.enrollment_coordinates)
        val reservedValueWarning = context.getString(R.string.no_reserved_values)
        val enrollmentDateDefaultLabel = context.getString(R.string.enrollmment_date)
        val incidentDateDefaultLabel = context.getString(R.string.incident_date)
        return EnrollmentRepository(
            modelFactory,
            enrollmentUid,
            d2,
            dhisEnrollmentUtils,
            enrollmentMode,
            enrollmentDataSectionLabel,
            singleSectionLabel,
            enrollmentOrgUnitLabel,
            teiCoordinatesLabel,
            enrollmentCoordinatesLabel,
            reservedValueWarning,
            enrollmentDateDefaultLabel,
            incidentDateDefaultLabel
        )
    }

    @Provides
    @PerActivity
    fun fieldFactory(
        context: Context,
        colorFactory: FormUiColorFactory,
        d2: D2
    ): FieldViewModelFactory {
        return FieldViewModelFactoryImpl(
            context.valueTypeHintMap(),
            false,
            colorFactory,
            LayoutProviderImpl(),
            HintProviderImpl(context),
            DisplayNameProviderImpl(d2)
        )
    }

    @Provides
    @PerActivity
    fun provideFormUiColorFactory(): FormUiColorFactory {
        return FormUiModelColorFactoryImpl(activityContext, true)
    }

    @Provides
    @PerActivity
    fun providePresenter(
        context: Context,
        d2: D2,
        enrollmentObjectRepository: EnrollmentObjectRepository,
        dataEntryRepository: EnrollmentRepository,
        teiRepository: TrackedEntityInstanceObjectRepository,
        programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program>,
        schedulerProvider: SchedulerProvider,
        enrollmentFormRepository: EnrollmentFormRepository,
        valueStore: ValueStore,
        analyticsHelper: AnalyticsHelper,
        fieldViewModelFactory: FieldViewModelFactory,
        matomoAnalyticsController: MatomoAnalyticsController
    ): EnrollmentPresenterImpl {
        return EnrollmentPresenterImpl(
            enrollmentView,
            d2,
            enrollmentObjectRepository,
            dataEntryRepository,
            teiRepository,
            programRepository,
            schedulerProvider,
            enrollmentFormRepository,
            valueStore,
            analyticsHelper,
            context.getString(R.string.field_is_mandatory),
            fieldViewModelFactory.sectionProcessor(),
            matomoAnalyticsController
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
        crashReportController: CrashReportController
    ): ValueStore {
        return ValueStoreImpl(
            d2,
            enrollmentRepository.blockingGet().trackedEntityInstance()!!,
            DataEntryStore.EntryMode.ATTR,
            DhisEnrollmentUtils(d2),
            crashReportController
        )
    }

    @Provides
    @PerActivity
    internal fun rulesRepository(d2: D2): RulesRepository {
        return RulesRepository(d2)
    }

    @Provides
    @PerActivity
    fun formRepository(
        d2: D2,
        rulesRepository: RulesRepository,
        enrollmentRepository: EnrollmentObjectRepository,
        programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program>,
        teiRepository: TrackedEntityInstanceObjectRepository,
        enrollmentService: DhisEnrollmentUtils
    ): EnrollmentFormRepository {
        return EnrollmentFormRepositoryImpl(
            d2,
            rulesRepository,
            enrollmentRepository,
            programRepository,
            teiRepository,
            enrollmentService
        )
    }

    @Provides
    @PerActivity
    fun provideEnrollmentFormRepository(
        d2: D2,
        enrollmentRepository: EnrollmentObjectRepository,
        crashReportController: CrashReportController
    ): FormRepository {
        return FormRepositoryImpl(
            ValueStoreImpl(
                d2,
                enrollmentRepository.blockingGet().trackedEntityInstance()!!,
                DataEntryStore.EntryMode.ATTR,
                DhisEnrollmentUtils(d2),
                enrollmentRepository,
                crashReportController
            ),
            FieldErrorMessageProvider(activityContext),
            DisplayNameProviderImpl(d2)
        )
    }
}
