package org.dhis2.usescases.enrollment

import android.content.Context
import dagger.Module
import dagger.Provides
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.Bindings.valueTypeHintMap
import org.dhis2.R
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.dhis2.data.forms.RulesRepository
import org.dhis2.data.forms.dataentry.DataEntryStore
import org.dhis2.data.forms.dataentry.EnrollmentRepository
import org.dhis2.data.forms.dataentry.FormUiModelColorFactoryImpl
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.FormRepositoryPersistenceImpl
import org.dhis2.form.model.RowAction
import org.dhis2.form.ui.style.FormUiColorFactory
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController
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
        onRowActionProcessor: FlowableProcessor<RowAction>,
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
            incidentDateDefaultLabel,
            onRowActionProcessor
        )
    }

    @Provides
    @PerActivity
    fun fieldFactory(
        context: Context,
        colorFactory: FormUiColorFactory
    ): FieldViewModelFactory {
        return FieldViewModelFactoryImpl(context.valueTypeHintMap(), false, colorFactory)
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
        onRowActionProcessor: FlowableProcessor<RowAction>,
        fieldViewModelFactory: FieldViewModelFactory,
        matomoAnalyticsController: MatomoAnalyticsController,
        formRepository: FormRepository
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
            onRowActionProcessor,
            fieldViewModelFactory.sectionProcessor(),
            matomoAnalyticsController,
            formRepository
        )
    }

    @Provides
    @PerActivity
    fun provideOnRowActionProcessor(): FlowableProcessor<RowAction> {
        return PublishProcessor.create()
    }

    @Provides
    @PerActivity
    fun valueStore(d2: D2, enrollmentRepository: EnrollmentObjectRepository): ValueStore {
        return ValueStoreImpl(
            d2,
            enrollmentRepository.blockingGet().trackedEntityInstance()!!,
            DataEntryStore.EntryMode.ATTR,
            DhisEnrollmentUtils(d2)
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
        teiRepository: TrackedEntityInstanceObjectRepository
    ): EnrollmentFormRepository {
        return EnrollmentFormRepositoryImpl(
            d2,
            rulesRepository,
            enrollmentRepository,
            programRepository,
            teiRepository
        )
    }

    @Provides
    @PerActivity
    fun provideEnrollmentFormRepository(
        d2: D2,
        enrollmentRepository: EnrollmentObjectRepository
    ): FormRepository {
        return FormRepositoryPersistenceImpl(
            ValueStoreImpl(
                d2,
                enrollmentRepository.blockingGet().trackedEntityInstance()!!,
                DataEntryStore.EntryMode.ATTR,
                DhisEnrollmentUtils(d2),
                enrollmentRepository
            )
        )
    }
}
