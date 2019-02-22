package org.dhis2.usescases.datasets.dataSetTable;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ActivityDatasetTableBinding;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSectionFragment;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Flowable;

public class DataSetTableActivity extends ActivityGlobalAbstract implements DataSetTableContract.View {

    String orgUnitUid;
    String orgUnitName;
    String periodTypeName;
    String periodInitialDate;
    String catCombo;
    boolean accessDataWrite;
    int rowTotal;
    int columTotal;
    @Inject
    DataSetTableContract.Presenter presenter;
    private ActivityDatasetTableBinding binding;
    private DataSetSectionAdapter viewPagerAdapter;

    public static Bundle getBundle(@NonNull String dataSetUid,
                                   @NonNull String orgUnitUid,
                                   @NonNull String periodTypeName,
                                   @NonNull String periodInitialDate,
                                   @NonNull String catCombo) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_SET_UID, dataSetUid);
        bundle.putString(Constants.ORG_UNIT, orgUnitUid);
        bundle.putString(Constants.PERIOD_TYPE, periodTypeName);
        bundle.putString(Constants.PERIOD_TYPE_DATE, periodInitialDate);
        bundle.putString(Constants.CAT_COMB, catCombo);
        return bundle;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orgUnitUid = getIntent().getStringExtra(Constants.ORG_UNIT);
        orgUnitName = getIntent().getStringExtra(Constants.ORG_UNIT_NAME);
        periodTypeName = getIntent().getStringExtra(Constants.PERIOD_TYPE);
        periodInitialDate = getIntent().getStringExtra(Constants.PERIOD_TYPE_DATE);
        catCombo = getIntent().getStringExtra(Constants.CAT_COMB);
        String dataSetUid = getIntent().getStringExtra(Constants.DATA_SET_UID);
        accessDataWrite = getIntent().getBooleanExtra(Constants.ACCESS_DATA, true);
        ((App) getApplicationContext()).userComponent().plus(new DataSetTableModule(dataSetUid)).inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_table);
        binding.setPresenter(presenter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this, orgUnitUid, periodTypeName, catCombo, periodInitialDate);
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setDataElements(Map<String, List<DataElementModel>> dataElements, Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> catOptions) {
        viewPagerAdapter = new DataSetSectionAdapter(getSupportFragmentManager(), accessDataWrite, getIntent().getStringExtra(Constants.DATA_SET_UID));
        binding.viewPager.setAdapter(viewPagerAdapter);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        if(dataElements.size()>1)
            dataElements.remove("NO_SECTION");
        else
            binding.tabLayout.setVisibility(View.GONE);

        viewPagerAdapter.swapData(dataElements);
    }

    @Override
    public void setDataSet(DataSetModel data) {
        binding.dataSetName.setText(String.format("%s\n%s", data.displayName(), orgUnitName));
    }

    @Override
    public void setDataValue(List<DataSetTableModel> data) {
    }

    public DataSetTableContract.Presenter getPresenter() {
        return presenter;
    }


    @Override
    public Boolean accessDataWrite() {
        return accessDataWrite;
    }

    @Override
    public Flowable<RowAction> rowActions() {
        return ((DataSetSectionFragment)viewPagerAdapter.getItem(binding.viewPager.getCurrentItem())).rowActions();
    }


}
