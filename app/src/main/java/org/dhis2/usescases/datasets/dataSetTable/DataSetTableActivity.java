package org.dhis2.usescases.datasets.dataSetTable;


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ActivityDatasetTableBinding;
import org.dhis2.databinding.ItemCategoryComboBinding;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSectionFragment;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialModel;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.custom_views.OrgUnitDialog;
import org.dhis2.utils.custom_views.PeriodDialog;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import io.reactivex.Flowable;

public class DataSetTableActivity extends ActivityGlobalAbstract implements DataSetTableContract.View {

    String orgUnitUid;
    String orgUnitName;
    String periodTypeName;
    String periodInitialDate;
    String catCombo;
    String dataSetUid;
    String periodId;

    boolean accessDataWrite;
    boolean tableSelectorVisible = false;

    private OrganisationUnitModel selectedOrgUnit;
    private Date selectedPeriod;
    private HashMap<String, CategoryOption> selectedCatOptions;
    private Map<String, List<DataElementModel>> dataElements;
    View selectedView;

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
                                   @NonNull String catCombo) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_SET_UID, dataSetUid);
        bundle.putString(Constants.ORG_UNIT, orgUnitUid);
        bundle.putString(Constants.ORG_UNIT_NAME, orgUnitName);
        bundle.putString(Constants.PERIOD_TYPE, periodTypeName);
        bundle.putString(Constants.PERIOD_TYPE_DATE, periodInitialDate);
        bundle.putString(Constants.PERIOD_ID, periodId);
        bundle.putString(Constants.CAT_COMB, catCombo);
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
        catCombo = getIntent().getStringExtra(Constants.CAT_COMB);
        dataSetUid = getIntent().getStringExtra(Constants.DATA_SET_UID);
        accessDataWrite = getIntent().getBooleanExtra(Constants.ACCESS_DATA, true);
        ((App) getApplicationContext()).userComponent().plus(new DataSetTableModule(dataSetUid)).inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_table);
        binding.setPresenter(presenter);
        binding.dataSetName.setText(String.format("%s - %s", orgUnitName, periodInitialDate));

        //DataSet Selector
        binding.dataSetOrgUnitEditText.setText(orgUnitName);
        binding.dataSetPeriodEditText.setText(periodInitialDate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this, orgUnitUid, periodTypeName, catCombo, periodInitialDate, periodId);
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
                if(viewPagerAdapter.getCurrentItem(binding.tabLayout.getSelectedTabPosition()).currentNumTables()>1)
                if (tableSelectorVisible)
                    binding.selectorLayout.setVisibility(View.GONE);
                else {
                    binding.selectorLayout.setVisibility(View.VISIBLE);
                    List<String> tables = new ArrayList<>();
                    for(int i =1; i<= viewPagerAdapter.getCurrentItem(binding.tabLayout.getSelectedTabPosition()).currentNumTables() ; i++){
                        tables.add(getResources().getString(R.string.table) + " " + i);
                    }
                    FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
                    layoutManager.setFlexDirection(FlexDirection.ROW);
                    layoutManager.setJustifyContent(JustifyContent.FLEX_START);
                    binding.tableRecycler.setLayoutManager(layoutManager);

                    binding.tableRecycler.setAdapter(new TableCheckboxAdapter(presenter));
                    ((TableCheckboxAdapter)binding.tableRecycler.getAdapter()).swapData(tables);
                }

                tableSelectorVisible = !tableSelectorVisible;
            }
        });
        this.dataElements = dataElements;
        if(dataElements.containsKey("NO_SECTION") && dataElements.size() > 1)
                dataElements.remove("NO_SECTION");
        viewPagerAdapter.swapData(dataElements);
    }

    public void updateTabLayout(String section, int numTables){

        if(section.equals("NO_SECTION")) {
            if (numTables > 1) {
                dataElements.put(getString(R.string.tab_tables), dataElements.remove("NO_SECTION"));
                viewPagerAdapter.swapData(dataElements);
            } else
                binding.tabLayout.setVisibility(View.GONE);
        }else {
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
    public void showOrgUnitDialog(List<OrganisationUnitModel> data) {
        OrgUnitDialog orgUnitDialog = OrgUnitDialog.getInstace().setMultiSelection(false);
        orgUnitDialog.setOrgUnits(data);
        orgUnitDialog.setTitle(getString(R.string.org_unit))
                .setPossitiveListener(v -> {
                    if (orgUnitDialog.getSelectedOrgUnit() != null) {
                        selectedOrgUnit = orgUnitDialog.getSelectedOrgUnitModel();
                        binding.dataSetOrgUnitEditText.setText(selectedOrgUnit.displayName());
                    }
                    orgUnitDialog.dismiss();
                })
                .setNegativeListener(v -> orgUnitDialog.dismiss());
        orgUnitDialog.show(getSupportFragmentManager(), OrgUnitDialog.class.getSimpleName());
    }

    @Override
    public void showPeriodSelector(PeriodType periodType) {
        new PeriodDialog()
                .setPeriod(periodType)
                .setMaxDate(DateUtils.getInstance().getCalendar().getTime())
                .setPossitiveListener(selectedDate -> {
                    this.selectedPeriod = selectedDate;
                    binding.dataSetPeriodEditText.setText(DateUtils.getInstance().getPeriodUIString(periodType, selectedDate, Locale.getDefault()));
                })
                .show(getSupportFragmentManager(), PeriodDialog.class.getSimpleName());
    }

    @Override
    public void setData(DataSetInitialModel dataSetInitialModel) {
        binding.catComboContainer.removeAllViews();
        selectedCatOptions = new HashMap<>();
        if (!dataSetInitialModel.categoryComboName().equals("default"))
            for (Category categories : dataSetInitialModel.categories()) {
                selectedCatOptions.put(categories.uid(), null);
                ItemCategoryComboBinding categoryComboBinding = ItemCategoryComboBinding.inflate(getLayoutInflater(), binding.catComboContainer, false);
                categoryComboBinding.inputLayout.setHint(categories.displayName());
                categoryComboBinding.inputEditText.setOnClickListener(view -> {
                    selectedView = view;
                    presenter.onCatOptionClick(categories.uid());
                });
                binding.catComboContainer.addView(categoryComboBinding.getRoot());
            }
    }

    @Override
    public void showCatComboSelector(String catOptionUid, List<CategoryOption> data) {
        PopupMenu menu = new PopupMenu(this, selectedView, Gravity.BOTTOM);
        for (CategoryOption option : data)
            menu.getMenu().add(Menu.NONE, Menu.NONE, data.indexOf(option), option.displayName());

        menu.setOnDismissListener(menu1 -> selectedView = null);
        menu.setOnMenuItemClickListener(item -> {
            if (selectedCatOptions == null)
                selectedCatOptions = new HashMap<>();
            selectedCatOptions.put(catOptionUid, data.get(item.getOrder()));
            ((TextInputEditText) selectedView).setText(data.get(item.getOrder()).displayName());
            ((TextInputEditText) selectedView).setTextColor(getResources().getColor(R.color.white));
            return false;
        });
        menu.show();
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
        ((TableCheckboxAdapter)binding.tableRecycler.getAdapter()).setSelectedPosition(numTable);
        viewPagerAdapter.getCurrentItem(binding.tabLayout.getSelectedTabPosition()).goToTable(numTable);
    }

    @Override
    public void setCurrentNumTables(int numTables) {
        //Table Selector
        List<String> tables = new ArrayList<>();
        for(int i =1; i<= numTables ; i++){
            tables.add(getResources().getString(R.string.table)+ i);
        }
        ((TableCheckboxAdapter)binding.tableRecycler.getAdapter()).swapData(tables);
    }

    @Override
    public void renderDetails(DataSetInitialModel dataSetInitialModel) {
        binding.dataSetSubtitle.setText(dataSetInitialModel.displayName());
    }
}
