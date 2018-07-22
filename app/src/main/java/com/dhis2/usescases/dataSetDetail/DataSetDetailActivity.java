package com.dhis2.usescases.dataSetDetail;

import android.os.Bundle;

import com.dhis2.R;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import org.hisp.dhis.android.core.dataelement.DataElement;

import java.util.List;

public class DataSetDetailActivity extends ActivityGlobalAbstract implements DataSetDetailContract.View {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_set_detail);
    }

    @Override
    public void setData(List<DataElement> aggregates) {

    }
}
