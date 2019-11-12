package org.dhis2.usescases.programStageSelection

import com.squareup.sqlbrite2.BriteDatabase
import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.forms.RulesRepository
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.utils.RulesUtilsProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.rules.RuleExpressionEvaluator

/**
 * QUADRAM. Created by ppajuelo on 31/10/2017.
 */
@PerActivity
@Module
class ProgramStageSelectionModule(
    private val view: ProgramStageSelectionView,
    private val programUid: String,
    private val enrollmentUid: String,
    private val eventCreationType: String
) {

    @Provides
    @PerActivity
    internal fun providesView(activity: ProgramStageSelectionActivity): ProgramStageSelectionView {
        return activity
    }

    @Provides
    @PerActivity
    internal fun providesPresenter(
        programStageSelectionRepository: ProgramStageSelectionRepository,
        ruleUtils: RulesUtilsProvider,
        schedulerProvider: SchedulerProvider
    ): ProgramStageSelectionPresenter {
        return ProgramStageSelectionPresenter(
            view,
            programStageSelectionRepository,
            ruleUtils,
            schedulerProvider
        )
    }

    @Provides
    @PerActivity
    internal fun providesProgramStageSelectionRepository(
        briteDatabase: BriteDatabase,
        evaluator: RuleExpressionEvaluator,
        rulesRepository: RulesRepository,
        d2: D2
    ): ProgramStageSelectionRepository {
        return ProgramStageSelectionRepositoryImpl(
            briteDatabase,
            evaluator,
            rulesRepository,
            programUid,
            enrollmentUid,
            eventCreationType,
            d2
        )
    }

    @Provides
    @PerActivity
    internal fun rulesRepository(d2: D2): RulesRepository {
        return RulesRepository(d2)
    }
}
