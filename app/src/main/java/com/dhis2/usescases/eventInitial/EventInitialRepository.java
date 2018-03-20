package com.dhis2.usescases.eventInitial;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Cristian E. on 02/11/2017.
 *
 */

public interface EventInitialRepository {

    @NonNull
    Observable<EventModel> event(String eventId);

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits();

    @NonNull
    Observable<List<CategoryOptionComboModel>> catCombo(String programUid);

    @NonNull
    Observable<List<OrganisationUnitModel>> filteredOrgUnits(String date);
}
