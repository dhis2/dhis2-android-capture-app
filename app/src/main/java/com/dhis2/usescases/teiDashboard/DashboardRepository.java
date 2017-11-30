package com.dhis2.usescases.teiDashboard;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 30/11/2017.
 */

public interface DashboardRepository {

    Observable<ProgramModel> getProgramData(String programUid);

    Observable<List<TrackedEntityAttributeModel>> getAttributes(String programId);

    Observable<OrganisationUnitModel> getOrgUnit(String orgUnitId);

}
