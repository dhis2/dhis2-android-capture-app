package org.dhis2.usescases.programStageSelection

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.form.data.RulesRepository
import org.dhis2.form.data.RulesUtilsProvider
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
        schedulerProvider: SchedulerProvider,
    ): ProgramStageSelectionPresenter {
        return ProgramStageSelectionPresenter(
            view,
            programStageSelectionRepository,
            ruleUtils,
            schedulerProvider,
        )
    }

    @Provides
    @PerActivity
    fun providesProgramStageSelectionRepository(
        rulesRepository: RulesRepository,
        d2: D2,
    ): ProgramStageSelectionRepository {
        return ProgramStageSelectionRepositoryImpl(
            rulesRepository,
            programUid,
            enrollmentUid,
            eventCreationType,
            d2,
        )
    }

    @Provides
    @PerActivity
    fun rulesRepository(d2: D2): RulesRepository {
        return RulesRepository(d2)
    }
}
