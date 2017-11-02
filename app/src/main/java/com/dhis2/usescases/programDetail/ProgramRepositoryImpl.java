package com.dhis2.usescases.programDetail;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class ProgramRepositoryImpl implements ProgramRepository {

    private final BriteDatabase briteDatabase;
    private final String SELECT_PROGRAM_ATTRIBUTES = "SELECT * FROM " + ProgramTrackedEntityAttributeModel.TABLE +
            " WHERE " + ProgramTrackedEntityAttributeModel.TABLE + "." + ProgramTrackedEntityAttributeModel.Columns.PROGRAM + "='%s'";

    ProgramRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }


    @NonNull
    @Override
    public Observable<List<ProgramTrackedEntityAttributeModel>> programAttributes(String programId) {
        return briteDatabase.createQuery(ProgramTrackedEntityAttributeModel.TABLE, String.format(SELECT_PROGRAM_ATTRIBUTES, programId))
                .mapToList(ProgramTrackedEntityAttributeModel::create);
    }
}
