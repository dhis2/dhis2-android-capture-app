package com.dhis2.usescases.searchTrackEntity;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class SearchRepositoryImpl implements SearchRepository {

    private final BriteDatabase briteDatabase;
    private final String SELECT_PROGRAM_ATTRIBUTES = "SELECT * FROM " + ProgramTrackedEntityAttributeModel.TABLE +
            " WHERE " + ProgramTrackedEntityAttributeModel.TABLE + "." + ProgramTrackedEntityAttributeModel.Columns.PROGRAM + "='%s'";
    private final String SELECT_ATTRIBUTES = "SELECT * FROM " + TrackedEntityAttributeModel.TABLE;
    private final String SELECT_OPTION_SET = "SELECT * FROM " + OptionModel.TABLE + " WHERE Option.optionSet = ";

    SearchRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }


    @NonNull
    @Override
    public Observable<List<ProgramTrackedEntityAttributeModel>> programAttributes(String programId) {
        return briteDatabase.createQuery(ProgramTrackedEntityAttributeModel.TABLE, String.format(SELECT_PROGRAM_ATTRIBUTES, programId))
                .mapToList(ProgramTrackedEntityAttributeModel::create);
    }

    @Override
    public Observable<List<TrackedEntityAttributeModel>> programAttributes() {
        return briteDatabase.createQuery(TrackedEntityAttributeModel.TABLE, SELECT_ATTRIBUTES)
                .mapToList(TrackedEntityAttributeModel::create);
    }

    @Override
    public Observable<List<OptionModel>> optionSet(String optionSetId) {
        return briteDatabase.createQuery(OptionModel.TABLE, SELECT_OPTION_SET + "'" + optionSetId + "'")
                .mapToList(OptionModel::create);
    }
}
