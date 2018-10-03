package org.dhis2.usescases.datasets.dataSetTable;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;

import java.util.List;
import java.util.Map;

public class DataSetTableContract {

    public interface View extends AbstractActivityContracts.View {

        void setDataElements(Map<String, List<DataElementModel>> data, Map<String, List<CategoryOptionComboModel>> stringListMap);

        void setDataSet(DataSetModel data);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void onBackClick();
        void init(View view, String orgUnitUid, String periodTypeName, String periodInitialDate, String catCombo);

        List<DataElementModel> getDataElements(String string);

        List<CategoryOptionComboModel> getCatOptionCombos(String string);
    }
}
