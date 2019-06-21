package org.dhis2.usescases.datasets.dataSetTable;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityDatasetTableBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import static org.dhis2.utils.ConstantsKt.CAT_COMB;
import static org.dhis2.utils.ConstantsKt.DATA_SET_UID;
import static org.dhis2.utils.ConstantsKt.ORG_UNIT;
import static org.dhis2.utils.ConstantsKt.PERIOD_TYPE;
import static org.dhis2.utils.ConstantsKt.PERIOD_TYPE_DATE;

public class DataSetTableActivity extends ActivityGlobalAbstract implements DataSetTableContract.View {

    String orgUnitUid;
    String periodTypeName;
    String periodInitialDate;
    String catCombo;

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
        bundle.putString(DATA_SET_UID, dataSetUid);
        bundle.putString(ORG_UNIT, orgUnitUid);
        bundle.putString(PERIOD_TYPE, periodTypeName);
        bundle.putString(PERIOD_TYPE_DATE, periodInitialDate);
        bundle.putString(CAT_COMB, catCombo);
        return bundle;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orgUnitUid = getIntent().getStringExtra(ORG_UNIT);
        periodTypeName = getIntent().getStringExtra(PERIOD_TYPE);
        periodInitialDate = getIntent().getStringExtra(PERIOD_TYPE_DATE);
        catCombo = getIntent().getStringExtra(CAT_COMB);
        String dataSetUid = getIntent().getStringExtra(DATA_SET_UID);
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
    public void setDataElements(Map<String, List<DataElementModel>> dataElements, Map<String, List<CategoryOptionComboModel>> catOptions) {
        viewPagerAdapter = new DataSetSectionAdapter(getSupportFragmentManager());
        binding.viewPager.setAdapter(viewPagerAdapter);
        binding.tabLayout.setupWithViewPager(binding.viewPager);
        viewPagerAdapter.swapData(dataElements);
    }

    @Override
    public void setDataSet(DataSetModel data) {
        binding.dataSetName.setText(data.displayName());
    }

    public DataSetTableContract.Presenter getPresenter() {
        return presenter;
    }
}
