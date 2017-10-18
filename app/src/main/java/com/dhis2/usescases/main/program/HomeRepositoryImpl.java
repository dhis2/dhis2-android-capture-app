package com.dhis2.usescases.main.program;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.reactivex.Observable;

class HomeRepositoryImpl implements HomeRepository {

    /*
     SELECT * FROM
     (SELECT uid,displayName,'TRACKED_ENTITY' AS homeViewModelType FROM TrackedEntity
     UNION SELECT
     uid,displayName,'PROGRAM_NO_REG' AS homeViewModelType FROM Program WHERE Program.programType =
     'WITHOUT_REGISTRATION')
     ORDER BY homeViewModelType DESC
     */
    private final static String SELECT_HOME_VIEW_MODELS = String.format(Locale.US,
            "SELECT * FROM " +
                    "(SELECT %s,%s,'%s' AS %s FROM %s " +
                    "UNION SELECT %s,%s,'%s' AS %s FROM %s WHERE %s.%s = '%s') " +
                    "ORDER BY %s DESC",
            TrackedEntityModel.Columns.UID, TrackedEntityModel.Columns.DISPLAY_NAME,
            HomeViewModel.Type.TRACKED_ENTITY.name(), HomeViewModel.Columns.HOME_VIEW_MODEL_TYPE,
            TrackedEntityModel.TABLE, ProgramModel.Columns.UID, ProgramModel.Columns.DISPLAY_NAME,
            HomeViewModel.Type.PROGRAM.name(), HomeViewModel.Columns.HOME_VIEW_MODEL_TYPE, ProgramModel.TABLE,
            ProgramModel.TABLE, ProgramModel.Columns.PROGRAM_TYPE, ProgramType.WITHOUT_REGISTRATION.name(),
            HomeViewModel.Columns.HOME_VIEW_MODEL_TYPE);

    private static final String[] TABLE_NAMES = new String[]{TrackedEntityModel.TABLE, ProgramModel.TABLE};
    private static final Set<String> TABLE_SET = new HashSet<>(Arrays.asList(TABLE_NAMES));

    private final BriteDatabase briteDatabase;

    HomeRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    @Override
    public Observable<List<HomeViewModel>> homeViewModels() {
        return briteDatabase.createQuery(TABLE_SET, SELECT_HOME_VIEW_MODELS)
                .mapToList(HomeViewModel::fromCursor);
    }
}
