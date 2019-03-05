package org.dhis2.usescases.datasets.dataSetTable;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.datavalue.DataValue;

import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;

public interface DataSetTableRepository {
    Flowable<DataSetModel> getDataSet();

    Flowable<Map<String, List<DataElement>>> getDataElements();

    Flowable<Map<String, List<CategoryOptionComboModel>>> getCatOptions();

    Flowable<List<DataValue>> getDataValues(String orgUnitUid, String periodType, String initPeriodType, String catOptionComb);
}
