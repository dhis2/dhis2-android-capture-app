package org.dhis2.usescases.datasets.dataSetTable;

import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.DataSetInstance;
import org.hisp.dhis.android.core.period.Period;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

public interface DataSetTableRepository {
    Flowable<DataSet> getDataSet();

    Flowable<DataSetInstance> dataSetInstance();

    Flowable<DataSetInstance> defaultDataSetInstance();

    Flowable<List<String>> getSections();

    Flowable<Boolean> dataSetStatus();

    Flowable<State> dataSetState();

    Flowable<String> getCatComboName(String catcomboUid);

    String getCatOptComboFromOptionList(List<String> catOpts);

    Single<String> getDataSetCatComboName();

    Flowable<Period> getPeriod();

    Flowable<Boolean> completeDataSetInstance();

    boolean hasToRunValidationRules();

    boolean isValidationRuleOptional();

    boolean executeValidationRules();
}
