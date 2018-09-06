package org.dhis2.data.forms.dataentry;

import android.support.annotation.NonNull;


import org.dhis2.data.forms.dataentry.fields.FieldViewModel;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public interface DataEntryRepository {

    @NonNull
    Flowable<List<FieldViewModel>> list();

    Observable<List<OrganisationUnitModel>> getOrgUnits();

    void assign(String field, String content);
}
