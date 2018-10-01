package org.dhis2.usescases.datasets.dataSetTable;

import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.datavalue.DataValueModel;

import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;

public interface DataSetTableRepository {
    Flowable<Map<String, List<DataElementModel>>> getDataElements();

    Flowable<List<DataValueModel>> getDataValues(String orgUnitUid, String periodType, String initPeriodType, String catOptionComb);
}
