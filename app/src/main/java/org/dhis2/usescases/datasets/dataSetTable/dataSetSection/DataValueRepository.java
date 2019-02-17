package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import org.hisp.dhis.android.core.datavalue.DataValueModel;

import java.util.List;

public interface DataValueRepository {
    void insertDataValue(List<DataValueModel> dataValues);
}
