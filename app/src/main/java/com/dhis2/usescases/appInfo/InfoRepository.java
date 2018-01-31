package com.dhis2.usescases.appInfo;

import android.support.annotation.NonNull;

import com.dhis2.domain.responses.TrackedEntityInstance;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 31/01/2018.
 */

public interface InfoRepository {

    @NonNull
    Observable<List<ProgramModel>> programs();

    @NonNull
    Observable<List<EventModel>> events();

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits();

    @NonNull
    Observable<List<TrackedEntityInstanceModel>> trackedEntityInstances();
}
