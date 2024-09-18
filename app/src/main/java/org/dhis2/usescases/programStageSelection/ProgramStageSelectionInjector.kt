package org.dhis2.usescases.programStageSelection

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.resources.D2ErrorUtils
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.data.RulesUtilsProvider
import org.dhis2.usescases.programEventDetail.usecase.CreateEventUseCase
import org.hisp.dhis.android.core.D2

@PerActivity
@Subcomponent(modules = [ProgramStageSelectionModule::class])
interface ProgramStageSelectionInjector {
    fun inject(programStageSelectionActivity: ProgramStageSelectionActivity?)
}

@Module
class ProgramStageSelectionModule(
    private val view: ProgramStageSelectionView,
    private val programUid: String,
    private val enrollmentUid: String,
    private val eventCreationType: String,
) {
    @Provides
    @PerActivity
    fun providesView(activity: ProgramStageSelectionActivity): ProgramStageSelectionView {
        return activity
    }

    @Provides
    @PerActivity
    fun providesPresenter(
        programStageSelectionRepository: ProgramStageSelectionRepository,
        ruleUtils: RulesUtilsProvider,
        metadataIconProvider: MetadataIconProvider,
        schedulerProvider: SchedulerProvider,
        dispatcherProvider: DispatcherProvider,
        createEventUseCase: CreateEventUseCase,
        d2ErrorUtils: D2ErrorUtils,
    ): ProgramStageSelectionPresenter {
        return ProgramStageSelectionPresenter(
            view,
            programStageSelectionRepository,
            ruleUtils,
            metadataIconProvider,
            schedulerProvider,
            dispatcherProvider,
            createEventUseCase,
            d2ErrorUtils,
        )
    }

    @Provides
    @PerActivity
    fun providesProgramStageSelectionRepository(
        d2: D2,
    ): ProgramStageSelectionRepository {
        return ProgramStageSelectionRepositoryImpl(
            programUid,
            enrollmentUid,
            eventCreationType,
            d2,
        )
    }

    @Provides
    @PerActivity
    fun provideCreateEventUseCase(
        dispatcherProvider: DispatcherProvider,
        d2: D2,
        dateUtils: DateUtils,
    ) = CreateEventUseCase(dispatcherProvider, d2, dateUtils)

    @Provides
    @PerActivity
    fun provideD2ErrorUtils() = D2ErrorUtils(view.context, NetworkUtils(view.context))

    @Provides
    @PerActivity
    fun provideDateUitls() = DateUtils.getInstance()
}
