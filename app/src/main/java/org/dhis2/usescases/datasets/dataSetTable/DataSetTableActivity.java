package org.dhis2.usescases.datasets.dataSetTable;

import android.os.Bundle;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityDatasetTableBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.dataset.DataSetModel;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class DataSetTableActivity extends ActivityGlobalAbstract implements DataSetTableContract.DataSetTableView {

    String orgUnitUid;
    String periodTypeName;
    String periodInitialDate;
    String catCombo;

    @Inject
    DataSetTableContract.DataSetTablePresenter presenter;
    private ActivityDatasetTableBinding binding;

    public static Bundle getBundle(@NonNull String dataSetUid,
                                   @NonNull String orgUnitUid,
                                   @NonNull String periodTypeName,
                                   @NonNull String periodInitialDate,
                                   @NonNull String catCombo) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_SET_UID, dataSetUid);
        bundle.putString(Constants.EXTRA_ORG_UNIT, orgUnitUid);
        bundle.putString(Constants.PERIOD_TYPE, periodTypeName);
        bundle.putString(Constants.PERIOD_TYPE_DATE, periodInitialDate);
        bundle.putString(Constants.CAT_COMB, catCombo);
        return bundle;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orgUnitUid = getIntent().getStringExtra(Constants.EXTRA_ORG_UNIT);
        periodTypeName = getIntent().getStringExtra(Constants.PERIOD_TYPE);
        periodInitialDate = getIntent().getStringExtra(Constants.PERIOD_TYPE_DATE);
        catCombo = getIntent().getStringExtra(Constants.CAT_COMB);
        String dataSetUid = getIntent().getStringExtra(Constants.DATA_SET_UID);
        ((App) getApplicationContext()).userComponent().plus(new DataSetTableModule(dataSetUid)).inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_table);
        binding.setPresenter(presenter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this, orgUnitUid, periodTypeName, periodInitialDate, catCombo);
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setDataElements(Map<String, List<DataElement>> dataElements, Map<String, List<CategoryOptionComboModel>> catOptions) {
        DataSetSectionAdapter viewPagerAdapter = new DataSetSectionAdapter(getSupportFragmentManager());
        binding.viewPager.setAdapter(viewPagerAdapter);
        binding.tabLayout.setupWithViewPager(binding.viewPager);
        viewPagerAdapter.swapData(dataElements);
    }

    @Override
    public void setDataSet(DataSetModel data) {
        binding.dataSetName.setText(data.displayName());
    }

    public DataSetTableContract.DataSetTablePresenter getPresenter() {
        return presenter;
    }
}
