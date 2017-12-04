package com.dhis2.usescases.searchTrackEntity;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class SearchRepositoryImpl implements SearchRepository {

    private final BriteDatabase briteDatabase;
    private final String SELECT_PROGRAM_WITH_REGISTRATION = "SELECT * FROM " + ProgramModel.TABLE + " WHERE Program.programType='WITH_REGISTRATION' AND Program.trackedEntity = ";
    private final String SELECT_PROGRAM_ATTRIBUTES = "SELECT TrackedEntityAttribute.* FROM " + TrackedEntityAttributeModel.TABLE +
            " INNER JOIN " + ProgramTrackedEntityAttributeModel.TABLE +
            " ON " + TrackedEntityAttributeModel.TABLE + "." + TrackedEntityAttributeModel.Columns.UID +" = " + ProgramTrackedEntityAttributeModel.TABLE + "." + ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE +
            " WHERE " + ProgramTrackedEntityAttributeModel.TABLE + "." + ProgramTrackedEntityAttributeModel.Columns.PROGRAM + " = ";
    private final String SELECT_ATTRIBUTES = "SELECT * FROM " + TrackedEntityAttributeModel.TABLE;
    private final String SELECT_OPTION_SET = "SELECT * FROM " + OptionModel.TABLE + " WHERE Option.optionSet = ";

    private static final String[] TABLE_NAMES = new String[]{TrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE};
    private static final Set<String> TABLE_SET = new HashSet<>(Arrays.asList(TABLE_NAMES));


    SearchRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }


    @NonNull
    @Override
    public Observable<List<TrackedEntityAttributeModel>> programAttributes(String programId) {
        return briteDatabase.createQuery(TABLE_SET, SELECT_PROGRAM_ATTRIBUTES + "'" + programId + "'")
                .mapToList(TrackedEntityAttributeModel::create);
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

    @Override
    public Observable<List<ProgramModel>> programsWithRegistration(String programTypeId) {
        return briteDatabase.createQuery(ProgramModel.TABLE, SELECT_PROGRAM_WITH_REGISTRATION+"'"+programTypeId+"'")
                .mapToList(ProgramModel::create);
    }
}
