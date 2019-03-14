package org.dhis2.usescases.main.program;

import android.database.Cursor;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitProgramLinkModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramType;
import org.hisp.dhis.android.core.user.UserOrganisationUnitLinkModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

class HomeRepositoryImpl implements HomeRepository {

    private final static String SELECT_EVENTS = "SELECT Event.* FROM Event " +
            "WHERE %s " +
            "Event.program = ? " +
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";

    private final static String SELECT_TEIS = "SELECT Event.* FROM Event " +
            "JOIN Enrollment ON Enrollment.uid = Event.enrollment " +
            "WHERE %s " +
            "Event.program = ? " +
            "AND Event.state != 'TO_DELETE' " +
            "GROUP BY Enrollment.trackedEntityInstance";

    private final static String TRACKED_ENTITY_TYPE_NAME = "SELECT TrackedEntityType.displayName FROM TrackedEntityType " +
            "WHERE TrackedEntityType.uid = ? LIMIT 1";

    private final static String PROGRAM_MODELS = "SELECT " +
            "Program.uid, " +
            "Program.displayName, " +
            "ObjectStyle.color, " +
            "ObjectStyle.icon," +
            "Program.programType," +
            "Program.trackedEntityType," +
            "Program.description " +
            "FROM Program LEFT JOIN ObjectStyle ON ObjectStyle.uid = Program.uid " +
            "JOIN OrganisationUnitProgramLink ON OrganisationUnitProgramLink.program = Program.uid %s GROUP BY Program.uid ORDER BY Program.displayName" /*+
            "UNION " +
            "SELECT DataSet.uid, " +
            "DataSet.displayName, " +
            "null, " +
            "null, " +
            "'', " +
            "'', " +
            "DataSet.description " +
            "FROM DataSet " +
            "JOIN DataSetOrganisationUnitLink ON DataSetOrganisationUnitLink.dataSet = DataSet.uid GROUP BY DataSet.uid"*/;

    private final static String AGGREGATE_FROM_DATASET = "SELECT * FROM DataSetDataElementLink " +
            "WHERE dataSet = ? ";

    private static final String[] TABLE_NAMES = new String[]{ProgramModel.TABLE, ObjectStyleModel.TABLE, OrganisationUnitProgramLinkModel.TABLE};
    private static final Set<String> TABLE_SET = new HashSet<>(Arrays.asList(TABLE_NAMES));

    private final static String SELECT_ORG_UNITS =
            "SELECT * FROM " + OrganisationUnitModel.TABLE + ", " + UserOrganisationUnitLinkModel.TABLE + " " +
                    "WHERE " + OrganisationUnitModel.TABLE + "." + OrganisationUnitModel.Columns.UID + " = " + UserOrganisationUnitLinkModel.TABLE + "." + UserOrganisationUnitLinkModel.Columns.ORGANISATION_UNIT +
                    " AND " + UserOrganisationUnitLinkModel.TABLE + "." + UserOrganisationUnitLinkModel.Columns.ORGANISATION_UNIT_SCOPE + " = '" + OrganisationUnitModel.Scope.SCOPE_DATA_CAPTURE +
                    "' AND UserOrganisationUnit.root = '1' " +
                    " ORDER BY " + OrganisationUnitModel.TABLE + "." + OrganisationUnitModel.Columns.DISPLAY_NAME + " ASC";

    private final static String SELECT_ORG_UNITS_BY_PARENT =
            "SELECT * FROM " + OrganisationUnitModel.TABLE + ", " + UserOrganisationUnitLinkModel.TABLE + " " +
                    "WHERE " + OrganisationUnitModel.TABLE + "." + OrganisationUnitModel.Columns.UID + " = " + UserOrganisationUnitLinkModel.TABLE + "." + UserOrganisationUnitLinkModel.Columns.ORGANISATION_UNIT +
                    " AND " + UserOrganisationUnitLinkModel.TABLE + "." + UserOrganisationUnitLinkModel.Columns.ORGANISATION_UNIT_SCOPE + " = '" + OrganisationUnitModel.Scope.SCOPE_DATA_CAPTURE +
                    " AND " + OrganisationUnitModel.TABLE + "." + OrganisationUnitModel.Columns.PARENT + " = ? " +
                    "' ORDER BY " + OrganisationUnitModel.TABLE + "." + OrganisationUnitModel.Columns.DISPLAY_NAME + " ASC";

    private final BriteDatabase briteDatabase;

    HomeRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }


    @NonNull
    @Override
    public Flowable<List<ProgramViewModel>> programModels(List<Date> dates, Period period, String orgUnitsId, int orgUnitsSize) {

        int orgUnits = orgUnitsId != null ? orgUnitsId.split(",").length : 0;
        boolean filteringOrgs = orgUnitsId != null && orgUnitsSize != orgUnits;
        //QUERYING Program - orgUnit filter
        String orgQuery = "";
        if (!isEmpty(orgUnitsId))
            orgQuery = String.format("WHERE OrganisationUnitProgramLink.organisationUnit IN (%s)", orgUnitsId);

        String programQuery = PROGRAM_MODELS.replace("%s", orgQuery);

        return briteDatabase.createQuery(TABLE_SET, programQuery)
                .mapToList(cursor -> {
                    String uid = cursor.getString(0);
                    String displayName = cursor.getString(1);
                    String color = cursor.getString(2);
                    String icon = cursor.getString(3);
                    String programType = cursor.getString(4);
                    String teiType = cursor.getString(5);
                    String description = cursor.getString(6);

                    //QUERYING Program EVENTS - dates filter
                    String queryFinal;
                    if (!programType.isEmpty()) {
                        StringBuilder dateQuery = new StringBuilder("");
                        if (dates != null && !dates.isEmpty()) {
                            String queryFormat = "(%s BETWEEN '%s' AND '%s') ";
                            for (int i = 0; i < dates.size(); i++) {
                                Date[] datesToQuery = DateUtils.getInstance().getDateFromDateAndPeriod(dates.get(i), period);
                                dateQuery.append(String.format(queryFormat, "Event.eventDate", DateUtils.databaseDateFormat().format(datesToQuery[0]), DateUtils.databaseDateFormat().format(datesToQuery[1])));
                                if (i < dates.size() - 1)
                                    dateQuery.append("OR ");
                            }
                        }


                        String filter = "";
                        if (!dateQuery.toString().isEmpty())
                            filter = dateQuery.toString() + " AND ";
                        else if (!dateQuery.toString().isEmpty())
                            filter = dateQuery.toString() + " AND ";

                        if (programType.equals(ProgramType.WITH_REGISTRATION.name())) {
                            queryFinal = String.format(SELECT_TEIS, filter);
                        } else {
                            queryFinal = String.format(SELECT_EVENTS, filter);
                        }
                    } else {
                        queryFinal = AGGREGATE_FROM_DATASET;
                    }

                    int count = 0;
                    try (Cursor countCursor = briteDatabase.query(queryFinal, uid)) {
                        if (countCursor != null && countCursor.moveToFirst()) {
                            count = countCursor.getCount();
                        }
                    }

                    //QUERYING Tracker name
                    String typeName = "";
                    if (programType.equals(ProgramType.WITH_REGISTRATION.name())) {
                        try (Cursor typeCursor = briteDatabase.query(TRACKED_ENTITY_TYPE_NAME, teiType)) {
                            if (typeCursor != null && typeCursor.moveToFirst()) {
                                typeName = typeCursor.getString(0);
                            }
                        }
                    } else if (programType.equals(ProgramType.WITHOUT_REGISTRATION.name())) {
                        typeName = "Events";
                    } else {
                        typeName = "DataSets";
                    }

                    return ProgramViewModel.create(uid, displayName, color, icon, count, teiType, typeName, programType, description, true, true);
                }).map(list -> checkCount(list, period)).toFlowable(BackpressureStrategy.LATEST);
    }

    private List<ProgramViewModel> checkCount(List<ProgramViewModel> list, Period period) {
        if (period == null)
            return list;
        else {
            List<ProgramViewModel> models = new ArrayList<>();
            for (ProgramViewModel programViewModel : list)
                if (programViewModel.count() > 0)
                    models.add(programViewModel);
            return models;
        }
    }

    @NonNull
    @Override
    public Observable<Integer> numberOfUserOrgUnits() {
        return briteDatabase.createQuery("sqlite_sequence", "SELECT seq FROM sqlite_sequence WHERE name = 'UserOrganisationUnit'")
                .mapToOne(cursor -> cursor.getInt(0));
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits(String parentUid) {
        String SELECT_ORG_UNITS_BY_PARENT = "SELECT OrganisationUnit.* FROM OrganisationUnit " +
                "JOIN UserOrganisationUnit ON UserOrganisationUnit.organisationUnit = OrganisationUnit.uid " +
                "WHERE OrganisationUnit.parent = ? AND UserOrganisationUnit.organisationUnitScope = 'SCOPE_DATA_CAPTURE' " +
                "ORDER BY OrganisationUnit.displayName ASC";

        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS_BY_PARENT, parentUid)
                .mapToList(OrganisationUnitModel::create);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS)
                .mapToList(OrganisationUnitModel::create);
    }
}
