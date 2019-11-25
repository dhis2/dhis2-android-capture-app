package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.dataelement.DataElementOperand;
import org.hisp.dhis.android.core.dataset.DataInputPeriod;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.Section;
import org.hisp.dhis.android.core.period.Period;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface DataValueRepository {
    Completable updateValue(DataSetTableModel dataValue);

    Flowable<DataSet> getDataSet();

    Flowable<List<CategoryCombo>> getCatCombo(String section);

    Flowable<Map<String, List<List<Pair<CategoryOption, Category>>>>> getCatOptions(String section, String catCombo);

    Flowable<List<DataSetTableModel>> getDataValues(String orgUnitUid, String periodType, String initPeriodType, String catOptionComb, String section);

    Flowable<List<DataElementOperand>> getCompulsoryDataElements();

    Flowable<List<DataElementOperand>> getGreyFields(String section);

    Flowable<Section> getSectionByDataSet(String section);

    Flowable<Period> getPeriod(String periodId);

    Flowable<List<DataInputPeriod>> getDataInputPeriod();

    Flowable<Boolean> completeDataSet(String orgUnitUid, String periodInitialDate, String catCombo);

    Flowable<Boolean> reopenDataSet(String orgUnitUid, String periodInitialDate, String catCombo);

    Flowable<Boolean> isCompleted(String orgUnitUid, String periodInitialDate, String catCombo);

    Flowable<Boolean> isApproval(String orgUnit, String period, String attributeOptionCombo);

    Flowable<List<DataElement>> getDataElements(CategoryCombo categoryCombo, String section);

    CategoryOption getCatOptionFromUid(String catOption);

    List<CategoryOption> getCatOptionFromCatOptionCombo(CategoryOptionCombo categoryOptionCombo);

    Flowable<Boolean> canWriteAny();

    @NotNull List<CategoryOptionCombo> getCatOptionComboFrom(@Nullable String catComboUid);
}
