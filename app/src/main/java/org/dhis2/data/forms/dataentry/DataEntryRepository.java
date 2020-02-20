package org.dhis2.data.forms.dataentry;

import androidx.annotation.NonNull;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public interface DataEntryRepository {

    @NonNull
    Flowable<List<FieldViewModel>> list();

    Observable<List<OrganisationUnit>> getOrgUnits();
}
