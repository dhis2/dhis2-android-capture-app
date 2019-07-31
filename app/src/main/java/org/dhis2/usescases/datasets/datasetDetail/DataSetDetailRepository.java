package org.dhis2.usescases.datasets.datasetDetail;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Observable;

public interface DataSetDetailRepository {

    @NonNull
    Observable<List<OrganisationUnit>> orgUnits();

    Flowable<List<DataSetDetailModel>> dataSetGroups(String dataSetUid, List<String> selectedOrgUnit, List<String> periodFilter, int page);
}
