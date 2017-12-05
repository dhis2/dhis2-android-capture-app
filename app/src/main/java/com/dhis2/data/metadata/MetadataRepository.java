package com.dhis2.data.metadata;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityModel;

import java.util.List;

import io.reactivex.Observable;


/**
 * Created by ppajuelo on 04/12/2017.
 */

public interface MetadataRepository {

    Observable<TrackedEntityModel> getTrackedEntity(String trackedEntityUid);

    Observable<OrganisationUnitModel> getOrganisatuibUnit(String orgUnitUid);

    Observable<List<ProgramTrackedEntityAttributeModel>> getProgramTrackedEntityAttributes(String programUid);

}
