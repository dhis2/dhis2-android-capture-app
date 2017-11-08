package com.dhis2.usescases.programDetail;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class ProgramRepositoryImpl implements ProgramRepository {

    private final BriteDatabase briteDatabase;
    private final String SELECT_PROGRAM_ATTRIBUTES = "SELECT * FROM " + ProgramTrackedEntityAttributeModel.TABLE +
            " WHERE " + ProgramTrackedEntityAttributeModel.TABLE + "." + ProgramTrackedEntityAttributeModel.Columns.PROGRAM + "='%s'";
    private final String SELECT_PROGRAM_STAGE = "SELECT * FROM " + ProgramStageModel.TABLE + " WHERE " + ProgramStageModel.TABLE + "." + ProgramStageModel.Columns.UID + " = '%s'";

    public ProgramRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }


    @NonNull
    @Override
    public Observable<List<ProgramTrackedEntityAttributeModel>> programAttributes(String programId) {
        return briteDatabase.createQuery(ProgramTrackedEntityAttributeModel.TABLE, String.format(SELECT_PROGRAM_ATTRIBUTES, programId))
                .mapToList(ProgramTrackedEntityAttributeModel::create);
    }

    @NonNull
    @Override
    public Observable<ProgramStageModel> programStage(String programStageId) {
        return briteDatabase.createQuery(ProgramStageModel.TABLE, String.format(SELECT_PROGRAM_STAGE, programStageId))
                .mapToOne(ProgramStageModel::create);
    }
}
