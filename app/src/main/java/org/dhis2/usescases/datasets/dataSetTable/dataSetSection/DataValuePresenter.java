package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;


import org.hisp.dhis.android.core.datavalue.DataValueModel;

import java.util.List;

public class DataValuePresenter implements DataValueContract.Presenter{

    private DataValueRepository repository;
    private DataValueContract.View view;

    public DataValuePresenter(DataValueRepository repository){
        this.repository = repository;
    }

    @Override
    public void init(DataValueContract.View view) {
        this.view = view;
    }

    @Override
    public void insertDataValues(List<DataValueModel> dataValues) {
        repository.insertDataValue(dataValues);
    }

    @Override
    public void save() {
        String a = "";
    }

    @Override
    public void onDettach() {

    }

    @Override
    public void displayMessage(String message) {

    }
}
