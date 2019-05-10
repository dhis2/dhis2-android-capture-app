package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.hisp.dhis.android.core.category.CategoryCombo;
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
    Flowable<Long> insertDataValue(DataValueModel dataValues);

    Flowable<DataSetModel> getDataSet();

    Flowable<List<DataElementModel>> getDataElements(String section);

    Flowable<List<CategoryCombo>> getCatCombo(String section);

    Flowable<Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>>> getCatOptions(String section);

    Flowable<Map<String, List<CategoryOptionComboModel>>> getCatOptionCombo();

    Flowable<List<DataSetTableModel>> getDataValues(String orgUnitUid, String periodType, String initPeriodType, String catOptionComb, String section);

    Flowable<Map<String, Map<String, List<String>>>> getGreyedFields(List<String> categoryOptionCombo, String section);

    Flowable<Map<String, List<String>>> getMandatoryDataElement(List<String> categoryOptionCombo);

    Flowable<SectionModel> getSectionByDataSet(String section);

    Flowable<Map<String, List<String>>> getCategoryOptionComboCatOption();

    Flowable<PeriodModel> getPeriod(String periodId);

    Flowable<List<DataInputPeriodModel>> getDataInputPeriod();

    Flowable<Boolean> completeDataSet(String orgUnitUid, String periodInitialDate, String catCombo);

    Flowable<Boolean> reopenDataSet(String orgUnitUid, String periodInitialDate, String catCombo);

    Flowable<Boolean> isCompleted(String orgUnitUid, String periodInitialDate, String catCombo);
}
