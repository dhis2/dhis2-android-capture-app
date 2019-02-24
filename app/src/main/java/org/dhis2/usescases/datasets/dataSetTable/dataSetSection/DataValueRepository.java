package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataInputPeriodModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.dataset.SectionModel;
import org.hisp.dhis.android.core.datavalue.DataValueModel;
import org.hisp.dhis.android.core.period.PeriodModel;

import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;

public interface DataValueRepository {
    Flowable<Long> insertDataValue(List<DataValueModel> dataValues);

    Flowable<DataSetModel> getDataSet();

    Flowable<Map<String, List<DataElementModel>>> getDataElements();

    Flowable<Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>>> getCatOptions();

    Flowable<Map<String, List<CategoryOptionComboModel>>> getCatOptionCombo();

    Flowable<List<DataSetTableModel>> getDataValues(String orgUnitUid, String periodType, String initPeriodType, String catOptionComb);

    Flowable<Map<String, Map<String, List<String>>>> getGreyedFields(List<String> categoryOptionCombo);

    Flowable<Map<String, List<String>>> getMandatoryDataElement(List<String> categoryOptionCombo);

    Flowable<List<SectionModel>> getSectionByDataSet();

    Flowable<String> getNewIDDataValue();

    Flowable<Map<String, List<String>>> getCategoryOptionComboCatOption();

    Flowable<PeriodModel> getPeriod(String periodId);

    Flowable<DataInputPeriodModel> getDataInputPeriod(String periodId);
}
