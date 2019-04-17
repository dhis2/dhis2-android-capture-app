package org.dhis2.usescases.datasets.dataSetTable;

import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;

import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;

public interface DataSetTableRepository {
    Flowable<DataSetModel> getDataSet();

    Flowable<Map<String, List<DataElementModel>>> getDataElements();

    Flowable<Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>>> getCatOptions();

    Flowable<Map<String, List<CategoryOptionComboModel>>> getCatOptionCombo();

    Flowable<Boolean> dataSetStatus();
    Flowable<String> getCatComboName(String catcomboUid);

}
