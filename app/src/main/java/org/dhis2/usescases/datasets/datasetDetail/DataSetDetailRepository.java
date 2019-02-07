package org.dhis2.usescases.datasets.datasetDetail;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public interface DataSetDetailRepository {

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits();

    Flowable<List<DataSetDetailModel>> dataSetGroups(String dataSetUid, List<String> selectedOrgUnit, PeriodType selectedPeriodType, int page);
}
