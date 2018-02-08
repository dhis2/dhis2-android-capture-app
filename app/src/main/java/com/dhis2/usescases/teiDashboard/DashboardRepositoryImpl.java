package com.dhis2.usescases.teiDashboard;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 30/11/2017.
 *
 */

public class DashboardRepositoryImpl implements DashboardRepository {

    private final String PROGRAM_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            ProgramModel.TABLE, ProgramModel.TABLE, ProgramModel.Columns.UID);

    private final String ATTRIBUTES_QUERY = String.format("SELECT %s.* FROM %s INNER JOIN %s ON %s.%s = %s.%s WHERE %s.%s = ",
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE,
            ProgramTrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.PROGRAM);

    private final String ORG_UNIT_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            OrganisationUnitModel.TABLE, OrganisationUnitModel.TABLE, OrganisationUnitModel.Columns.UID
    );

    private final String PROGRAM_STAGE_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            ProgramStageModel.TABLE, ProgramStageModel.TABLE, ProgramStageModel.Columns.PROGRAM);

    private static final String[] ATTRUBUTE_TABLES = new String[]{TrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE};
    private static final Set<String> ATTRIBUTE_TABLE_SET = new HashSet<>(Arrays.asList(ATTRUBUTE_TABLES));


    private final BriteDatabase briteDatabase;

    public DashboardRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @Override
    public Observable<ProgramModel> getProgramData(String programUid) {
        return briteDatabase.createQuery(ProgramModel.TABLE, PROGRAM_QUERY + "'" + programUid + "'")
                .mapToOne(ProgramModel::create);
    }

    @Override
    public Observable<List<TrackedEntityAttributeModel>> getAttributes(String programId) {
        return briteDatabase.createQuery(ATTRIBUTE_TABLE_SET, ATTRIBUTES_QUERY + "'" + programId + "'")
                .mapToList(TrackedEntityAttributeModel::create);
    }

    @Override
    public Observable<OrganisationUnitModel> getOrgUnit(String orgUnitId) {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, ORG_UNIT_QUERY + "'" + orgUnitId + "'")
                .mapToOne(OrganisationUnitModel::create);
    }

    @Override
    public Observable<List<ProgramStageModel>> getProgramStages(String programUid) {
        return briteDatabase.createQuery(ProgramStageModel.TABLE, PROGRAM_STAGE_QUERY + "'" + programUid + "'")
                .mapToList(ProgramStageModel::create);
    }
}
