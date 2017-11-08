package com.dhis2.usescases.main.program;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitProgramLinkModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityModel;

import java.util.ArrayList;
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

    private final static String SELECT_PROGRAMS_VIEW_MODELS = String.format(Locale.US,
            "SELECT * FROM " +
                    "(SELECT %s,%s,%s,%s,'%s' AS %s FROM %s) " +
                    "ORDER BY %s DESC",
            ProgramModel.Columns.PROGRAM_TYPE, ProgramModel.Columns.UID, ProgramModel.Columns.DISPLAY_NAME, ProgramModel.Columns.LAST_UPDATED,
            HomeViewModel.Type.PROGRAM.name(), HomeViewModel.Columns.HOME_VIEW_MODEL_TYPE, ProgramModel.TABLE,
            HomeViewModel.Columns.HOME_VIEW_MODEL_TYPE);

    public final static String SELECT_PROGRAMS_VIEW_MODELS_ORG_UNIT =
            "SELECT uid, displayName, lastUpdated, '" + HomeViewModel.Type.PROGRAM.name() + "' AS homeViewModelType FROM " +
                    "Program" +
                    " INNER JOIN OrganisationUnitProgramLink ON Program.uid = OrganisationUnitProgramLink.program" +
                    " WHERE OrganisationUnitProgramLink.organisationUnit IN (%s) GROUP BY Program.uid";


    private final static String SELECT_ORG_UNITS =
            "SELECT * FROM " + OrganisationUnitModel.TABLE;

    private final static String SELECT_TRACK_ENTITIES =
            "SELECT * FROM "+TrackedEntityInstanceModel.TABLE;

    private static final String[] TABLE_NAMES = new String[]{TrackedEntityModel.TABLE, ProgramModel.TABLE};
    private static final String[] TABLE_NAMES_2 = new String[]{ProgramModel.TABLE, OrganisationUnitProgramLinkModel.TABLE};
    private static final Set<String> TABLE_SET = new HashSet<>(Arrays.asList(TABLE_NAMES));
    private static final Set<String> TABLE_SET_2 = new HashSet<>(Arrays.asList(TABLE_NAMES_2));

    private final BriteDatabase briteDatabase;

    HomeRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    @Override
    public Observable<List<HomeViewModel>> homeViewModels() {
        return briteDatabase.createQuery(ProgramModel.TABLE, SELECT_PROGRAMS_VIEW_MODELS)
                .mapToList(HomeViewModel::fromCursor);
    }

    @NonNull
    @Override
    public Observable<List<HomeViewModel>> homeViewModels(ArrayList<String> orgUnitIds) {

        String queary = "";
        for (String id : orgUnitIds) {
            if (queary.isEmpty())
                queary = String.format(Locale.US, "'%s'", id);
            else
                queary = queary.concat(String.format(Locale.US, ", '%s'", id));
        }

        return briteDatabase.createQuery(TABLE_SET_2, String.format(Locale.US, SELECT_PROGRAMS_VIEW_MODELS_ORG_UNIT, queary))
                .mapToList(HomeViewModel::fromCursor);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS)
                .mapToList(OrganisationUnitModel::create);
    }

    @Override
    public Observable<List<TrackedEntityInstanceModel>> trackedEntities() {
        return briteDatabase.createQuery(TrackedEntityInstanceModel.TABLE, SELECT_TRACK_ENTITIES)
                .mapToList(TrackedEntityInstanceModel::create);
    }
}
