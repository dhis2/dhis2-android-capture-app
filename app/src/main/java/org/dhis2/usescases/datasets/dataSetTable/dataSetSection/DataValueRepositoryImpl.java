package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.datavalue.DataValueModel;

import java.util.List;

public class DataValueRepositoryImpl implements DataValueRepository {

    private BriteDatabase briteDatabase;

    private static String INSERT_DATAVALUE = "INSERT INTO DataValue ( " +
            "dataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo, value, storeBy, created, lastUpdated, followUp, state" +
            ") VALUES (?, ?, ?, ?,?, ?, ?,?,?, ?, ? );";

    public DataValueRepositoryImpl(BriteDatabase briteDatabase){
        this.briteDatabase = briteDatabase;
    }

    public void insertDataValue(List<DataValueModel> dataValues){
        for(DataValueModel dataValueModel: dataValues)
            briteDatabase.insert(DataValueModel.TABLE, dataValueModel.toContentValues());
    }
}
