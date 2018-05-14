package com.dhis2.usescases.programStageSelection;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class ProgramStageSelectionRepositoryImpl implements ProgramStageSelectionRepository {

    private final String PROGRAM_STAGE_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ? ORDER BY %s.%s ASC",
            ProgramStageModel.TABLE, ProgramStageModel.TABLE, ProgramStageModel.Columns.PROGRAM,
            ProgramStageModel.TABLE, ProgramStageModel.Columns.SORT_ORDER);

    private final String ENROLLMENT_PROGRAM_STAGES = "SELECT ProgramStage.* FROM ProgramStage " +
            "JOIN Program ON Program.uid = ProgramStage.program " +
            "JOIN Enrollment ON Enrollment.program = Program.uid " +
            "WHERE Enrollment.uid = ?";

    private final BriteDatabase briteDatabase;

    ProgramStageSelectionRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    @Override
    public Observable<List<ProgramStageModel>> getProgramStages(String programUid) {
        return briteDatabase.createQuery(ProgramStageModel.TABLE, PROGRAM_STAGE_QUERY, programUid)
                .mapToList(ProgramStageModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ProgramStageModel>> enrollmentProgramStages(String programId, String enrollmentUid) {
        List<ProgramStageModel> enrollmentStages = new ArrayList<>();
        List<ProgramStageModel> selectableStages = new ArrayList<>();
        return briteDatabase.createQuery(ProgramStageModel.TABLE, ENROLLMENT_PROGRAM_STAGES, enrollmentUid)
                .mapToList(ProgramStageModel::create)
                .flatMap(data -> {
                    enrollmentStages.addAll(data);
                    return briteDatabase.createQuery(ProgramStageModel.TABLE, PROGRAM_STAGE_QUERY, programId)
                            .mapToList(ProgramStageModel::create);
                })
                .map(data -> {
                    boolean isSelectable;
                    for (ProgramStageModel programStage : data) {
                        isSelectable = true;
                        for (ProgramStageModel enrollmentStage : enrollmentStages) {
                            if (enrollmentStage.uid().equals(programStage.uid())) {
                                //TODO: DISCUSS THIS WITH THE TEAM
                                isSelectable = programStage.repeatable() == true ? true : true;
                            }
                        }

                        if (isSelectable)
                            selectableStages.add(programStage);
                    }
                    return selectableStages;
                });
    }
}
