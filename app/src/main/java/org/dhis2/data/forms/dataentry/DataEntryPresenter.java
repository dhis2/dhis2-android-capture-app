package org.dhis2.data.forms.dataentry;


import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;

import java.util.List;

import io.reactivex.Observable;

interface DataEntryPresenter {
    String getLastFocusItem();

    void clearLastFocusItem();

    @UiThread
    void onAttach(@NonNull DataEntryView view);

    @UiThread
    void onDetach();

    @NonNull
    Observable<List<OrganisationUnit>> getOrgUnits();

    Observable<List<OrganisationUnitLevel>> getLevels();
}
