package org.dhis2.usescases.datasets.datasetDetail;

import android.support.annotation.NonNull;

import org.dhis2.utils.Period;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

public interface DataSetDetailRepository {

    @NonNull
    Observable<List<DataSetDetailModel>> filteredDataSet(String uidDataset, String fromDate, String toDate, CategoryOptionComboModel categoryOptionComboModel);

    @NonNull
    Observable<List<DataSetDetailModel>> filteredDataSet(List<Date> dates, Period period, CategoryOptionComboModel categoryOptionComboModel);

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits();

    @NonNull
    Observable<List<TrackedEntityDataValueModel>> dataSetDataValues(DataSetModel eventModel);

    Observable<List<String>> dataSetValuesNew(DataSetDetailModel eventModel);

    Observable<Boolean> writePermission(String programId);

}
