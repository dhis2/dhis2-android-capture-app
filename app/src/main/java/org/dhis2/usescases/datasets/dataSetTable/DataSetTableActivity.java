package org.dhis2.usescases.datasets.dataSetTable;


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.tabs.TabLayout;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ActivityDatasetTableBinding;
import org.dhis2.databinding.ItemCategoryComboBinding;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialModel;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import io.reactivex.functions.Consumer;

public class DataSetTableActivity extends ActivityGlobalAbstract implements DataSetTableContract.View {

    String orgUnitUid;
    String orgUnitName;
    String periodTypeName;
    String periodInitialDate;
    String catOptCombo;
    String dataSetUid;
    String periodId;

    boolean accessDataWrite;
    boolean tableSelectorVisible = false;

    private OrganisationUnitModel selectedOrgUnit;
    private Date selectedPeriod;
    private HashMap<String, CategoryOption> selectedCatOptions;
    private Map<String, List<DataElementModel>> dataElements;

    @Inject
    DataSetTableContract.Presenter presenter;
    private ActivityDatasetTableBinding binding;
    private DataSetSectionAdapter viewPagerAdapter;

    public static Bundle getBundle(@NonNull String dataSetUid,
                                   @NonNull String orgUnitUid,
                                   @NonNull String orgUnitName,
                                   @NonNull String periodTypeName,
                                   @NonNull String periodInitialDate,
                                   @NonNull String periodId,
                                   @NonNull String catOptCombo) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_SET_UID, dataSetUid);
        bundle.putString(Constants.ORG_UNIT, orgUnitUid);
        bundle.putString(Constants.ORG_UNIT_NAME, orgUnitName);
        bundle.putString(Constants.PERIOD_TYPE, periodTypeName);
        bundle.putString(Constants.PERIOD_TYPE_DATE, periodInitialDate);
        bundle.putString(Constants.PERIOD_ID, periodId);
        bundle.putString(Constants.CAT_COMB, catOptCombo);
        return bundle;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        orgUnitUid = getIntent().getStringExtra(Constants.ORG_UNIT);
        orgUnitName = getIntent().getStringExtra(Constants.ORG_UNIT_NAME);
        periodTypeName = getIntent().getStringExtra(Constants.PERIOD_TYPE);
        periodId = getIntent().getStringExtra(Constants.PERIOD_ID);
        periodInitialDate = getIntent().getStringExtra(Constants.PERIOD_TYPE_DATE);
        catOptCombo = getIntent().getStringExtra(Constants.CAT_COMB);
        dataSetUid = getIntent().getStringExtra(Constants.DATA_SET_UID);
        accessDataWrite = getIntent().getBooleanExtra(Constants.ACCESS_DATA, true);
        ((App) getApplicationContext()).userComponent().plus(new DataSetTableModule(dataSetUid, periodId, orgUnitUid, catOptCombo)).inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_table);
        binding.setPresenter(presenter);
        binding.dataSetName.setText(String.format("%s - %s", orgUnitName, periodInitialDate));

    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this, orgUnitUid, periodTypeName, catOptCombo, periodInitialDate, periodId);
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setDataElements(Map<String, List<DataElementModel>> dataElements, Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> catOptions) {
        viewPagerAdapter = new DataSetSectionAdapter(getSupportFragmentManager(), accessDataWrite, getIntent().getStringExtra(Constants.DATA_SET_UID), this);
        binding.viewPager.setAdapter(viewPagerAdapter);
        binding.tabLayout.setupWithViewPager(binding.viewPager);
        binding.tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.selectorLayout.setVisibility(View.GONE);
                tableSelectorVisible = false;
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (viewPagerAdapter.getCurrentItem(binding.tabLayout.getSelectedTabPosition()).currentNumTables().size() > 1)
                    if (tableSelectorVisible)
                        binding.selectorLayout.setVisibility(View.GONE);
                    else {
                        binding.selectorLayout.setVisibility(View.VISIBLE);
                        List<String> tables = new ArrayList<>();
                        tables.addAll(viewPagerAdapter.getCurrentItem(binding.tabLayout.getSelectedTabPosition()).currentNumTables());
                        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
                        layoutManager.setFlexDirection(FlexDirection.ROW);
                        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
                        binding.tableRecycler.setLayoutManager(layoutManager);

                        binding.tableRecycler.setAdapter(new TableCheckboxAdapter(presenter));
                        ((TableCheckboxAdapter) binding.tableRecycler.getAdapter()).swapData(tables);
                    }

                tableSelectorVisible = !tableSelectorVisible;
            }
        });
        this.dataElements = dataElements;
        if (dataElements.containsKey("NO_SECTION") && dataElements.size() > 1)
            dataElements.remove("NO_SECTION");
        viewPagerAdapter.swapData(dataElements);
    }

    public void updateTabLayout(String section, int numTables) {

        if (section.equals("NO_SECTION")) {
            if (numTables > 1) {
                dataElements.put(getString(R.string.tab_tables), dataElements.remove("NO_SECTION"));
                viewPagerAdapter.swapData(dataElements);
            } else
                binding.tabLayout.setVisibility(View.GONE);
        } else {
            if (numTables > 1)
                viewPagerAdapter.swapData(dataElements);
        }
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
    public void showOptions(boolean open) {
        if (open)
            binding.infoContainer.setVisibility(View.VISIBLE);
        else
            binding.infoContainer.setVisibility(View.GONE);
    }

    @Override
    public OrganisationUnitModel getSelectedOrgUnit() {
        return selectedOrgUnit;
    }

    @Override
    public Date getSelectedPeriod() {
        return selectedPeriod;
    }

    @Override
    public String getDataSetUid() {
        return dataSetUid;
    }

    @Override
    public String getOrgUnitName() {
        return orgUnitName;
    }

    @Override
    public void goToTable(int numTable) {
        ((TableCheckboxAdapter) binding.tableRecycler.getAdapter()).setSelectedPosition(numTable);
        viewPagerAdapter.getCurrentItem(binding.tabLayout.getSelectedTabPosition()).goToTable(numTable);
    }

    @Override
    public void setCurrentNumTables(int numTables) {
        //Table Selector
        List<String> tables = new ArrayList<>();
        for (int i = 1; i <= numTables; i++) {
            tables.add(getResources().getString(R.string.table) + i);
        }
        ((TableCheckboxAdapter) binding.tableRecycler.getAdapter()).swapData(tables);
    }

    @Override
    public void renderDetails(DataSetModel dataSetModel, String catComboName) {
        binding.dataSetSubtitle.setText(String.format("%s %s", dataSetModel.displayName(), !catComboName.equals("default") ? "- "+catComboName : ""));
        if (catComboName.equals("default")) {
            binding.catCombo.setVisibility(View.GONE);
            binding.catComboLabel.setVisibility(View.GONE);
        }
        binding.orgUnit.setText(orgUnitName);
        binding.reportPeriod.setText(periodInitialDate);
        binding.catCombo.setText(catComboName);
        binding.datasetDescription.setText(dataSetModel.displayDescription());
    }

    @Override
    public Consumer<Boolean> isDataSetOpen() {
        return dataSetIsOpen -> {
            binding.programLock.setImageResource(!dataSetIsOpen ? R.drawable.ic_lock_open_green : R.drawable.ic_lock_completed);
            binding.programLockText.setText(!dataSetIsOpen ? getString(org.dhis2.R.string.data_set_open) : getString(org.dhis2.R.string.data_set_closed));
        };
    }
}
