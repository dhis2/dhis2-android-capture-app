package org.dhis2.usescases.programStageSelection;

import androidx.annotation.NonNull;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.forms.RulesRepository;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.utils.RulesUtilsProvider;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.rules.RuleExpressionEvaluator;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 31/10/2017.
 */
@PerActivity
@Module
public class ProgramStageSelectionModule {

    private final String programUid;
    private final String enrollmentUid;
    private final String eventCreationType;
    private final ProgramStageSelectionContract.View view;

    public ProgramStageSelectionModule(ProgramStageSelectionContract.View view, String programId, String enrollmenId, String eventCreationType) {
        this.view = view;
        this.programUid = programId;
        this.enrollmentUid = enrollmenId;
        this.eventCreationType = eventCreationType;
    }

    @Provides
    @PerActivity
    ProgramStageSelectionContract.View providesView(@NonNull ProgramStageSelectionActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    ProgramStageSelectionContract.Presenter providesPresenter(@NonNull ProgramStageSelectionRepository programStageSelectionRepository,
                                                              @NonNull RulesUtilsProvider ruleUtils,
                                                              SchedulerProvider schedulerProvider) {
        return new ProgramStageSelectionPresenter(view, programStageSelectionRepository, ruleUtils, schedulerProvider);
    }

    @Provides
    @PerActivity
    ProgramStageSelectionRepository providesProgramStageSelectionRepository(@NonNull RuleExpressionEvaluator evaluator,
                                                                            RulesRepository rulesRepository,
                                                                            D2 d2) {
        return new ProgramStageSelectionRepositoryImpl(evaluator, rulesRepository, programUid, enrollmentUid, eventCreationType, d2);
    }

    @Provides
    @PerActivity
    RulesRepository rulesRepository(@NonNull D2 d2) {
        return new RulesRepository(d2);
    }
}
