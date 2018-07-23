package com.dhis2.usescases.programStageSelection;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.forms.RulesRepository;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.rules.RuleExpressionEvaluator;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 31/10/2017.
 */
@PerActivity
@Module
public class ProgramStageSelectionModule {

    private final String programUid;
    private final String enrollmentUid;

    public ProgramStageSelectionModule(String programId, String enrollmenId) {
        this.programUid = programId;
        this.enrollmentUid = enrollmenId;
    }

    @Provides
    @PerActivity
    ProgramStageSelectionContract.View providesView(@NonNull ProgramStageSelectionActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    ProgramStageSelectionContract.Presenter providesPresenter(@NonNull ProgramStageSelectionRepository programStageSelectionRepository) {
        return new ProgramStageSelectionPresenter(programStageSelectionRepository);
    }

    @Provides
    @PerActivity
    ProgramStageSelectionRepository providesProgramStageSelectionRepository(@NonNull BriteDatabase briteDatabase,
                                                                            @NonNull RuleExpressionEvaluator evaluator,
                                                                            RulesRepository rulesRepository) {
        return new ProgramStageSelectionRepositoryImpl(briteDatabase, evaluator, rulesRepository, programUid, enrollmentUid);
    }

    @Provides
    @PerActivity
    RulesRepository rulesRepository(BriteDatabase briteDatabase) {
        return new RulesRepository(briteDatabase);
    }
}
