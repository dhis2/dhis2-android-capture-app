package org.dhis2.usescases.datasets.dataSetTable;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.tabs.TabLayout;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityDatasetTableBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.sms.InputArguments;
import org.dhis2.usescases.sms.SmsSubmitActivity;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.dataset.DataSet;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

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
    private List<String> sections;

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
        ((App) getApplicationContext()).userComponent().plus(new DataSetTableModule(dataSetUid, periodId, orgUnitUid, catOptCombo)).inject(this);
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

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_table);
        binding.setPresenter(presenter);
        binding.dataSetName.setText(String.format("%s - %s", orgUnitName, periodInitialDate));

        setViewPager();

        presenter.init(this, orgUnitUid, periodTypeName, catOptCombo, periodInitialDate, periodId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDettach();
    }

    private void setViewPager() {
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
                if (viewPagerAdapter.getCurrentItem(binding.tabLayout.getSelectedTabPosition()).currentNumTables() > 1)
                    if (tableSelectorVisible)
                        binding.selectorLayout.setVisibility(View.GONE);
                    else {
                        binding.selectorLayout.setVisibility(View.VISIBLE);
                        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
                        layoutManager.setFlexDirection(FlexDirection.ROW);
                        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
                        binding.tableRecycler.setLayoutManager(layoutManager);

                        binding.tableRecycler.setAdapter(new TableCheckboxAdapter(presenter, getContext()));
                        ((TableCheckboxAdapter) binding.tableRecycler.getAdapter()).swapData(viewPagerAdapter.getCurrentItem(binding.tabLayout.getSelectedTabPosition()).currentNumTables());
                    }

                tableSelectorVisible = !tableSelectorVisible;
            }
        });
    }

    @Override
    public void setSections(List<String> sections) {
        this.sections = sections;
        if (sections.contains("NO_SECTION") && sections.size() > 1)
            sections.remove("NO_SECTION");
        viewPagerAdapter.swapData(sections);
    }

    public void updateTabLayout(String section, int numTables) {

        if (section.equals("NO_SECTION")) {
            if (numTables > 1) {
                sections.remove("NO_SECTION");
                sections.add(getString(R.string.tab_tables));
                viewPagerAdapter.swapData(sections);
            } else
                binding.tabLayout.setVisibility(View.GONE);
        } else {
            if (numTables > 1)
                viewPagerAdapter.swapData(sections);
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
    public void renderDetails(DataSet dataSet, String catComboName) {
        binding.dataSetSubtitle.setText(String.format("%s %s", dataSet.displayName(), !catComboName.equals("default") ? "- " + catComboName : ""));
        if (catComboName.equals("default")) {
            binding.catCombo.setVisibility(View.GONE);
            binding.catComboLabel.setVisibility(View.GONE);
        }
        binding.orgUnit.setText(orgUnitName);
        binding.reportPeriod.setText(periodInitialDate);
        binding.catCombo.setText(catComboName);
        binding.datasetDescription.setText(dataSet.displayDescription());
    }

    @Override
    public void isDataSetOpen(boolean dataSetIsOpen) {
        binding.programLock.setImageResource(!dataSetIsOpen ? R.drawable.ic_lock_open_green : R.drawable.ic_lock_completed);
        binding.programLockText.setText(!dataSetIsOpen ? getString(org.dhis2.R.string.data_set_open) : getString(org.dhis2.R.string.data_set_closed));
        binding.programLockText.setTextColor(!dataSetIsOpen ? getResources().getColor(R.color.green_7ed) : getResources().getColor(R.color.gray_666));
    }

    @Override
    public void isDataSetSynced(boolean dataSetIsSynced) {
        binding.syncState.setImageResource(dataSetIsSynced ? R.drawable.ic_sync_green : R.drawable.ic_sync_problem_grey);
    }

    @Override
    public void runSmsSubmission() {
        if (!getResources().getBoolean(R.bool.sms_enabled)) {
            return;
        }
        Intent intent = new Intent(this, SmsSubmitActivity.class);
        Bundle args = new Bundle();
        InputArguments.setDataSet(args, dataSetUid, orgUnitUid, periodId, catOptCombo);
        intent.putExtras(args);
        startActivity(intent);
    }

    public void update() {
        presenter.init(this, orgUnitUid, periodTypeName, catOptCombo, periodInitialDate, periodId);
    }
}
