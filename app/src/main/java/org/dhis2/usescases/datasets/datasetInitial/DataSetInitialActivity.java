package org.dhis2.usescases.datasets.datasetInitial;

import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.textfield.TextInputEditText;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityDatasetInitialBinding;
import org.dhis2.databinding.ItemCategoryComboBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.custom_views.OrgUnitDialog;
import org.dhis2.utils.custom_views.PeriodDialog;
import org.dhis2.utils.custom_views.PeriodDialogInputPeriod;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

public class DataSetInitialActivity extends ActivityGlobalAbstract implements DataSetInitialContract.View {

    private ActivityDatasetInitialBinding binding;
    View selectedView;
    @Inject
    DataSetInitialContract.Presenter presenter;

    private HashMap<String, CategoryOption> selectedCatOptions;
    private OrganisationUnit selectedOrgUnit;
    private Date selectedPeriod;
    private String dataSetUid;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataSetUid = getIntent().getStringExtra(Constants.DATA_SET_UID);
        ((App) getApplicationContext()).userComponent().plus(new DataSetInitialModule(dataSetUid)).inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_initial);
        binding.setPresenter(presenter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setAccessDataWrite(Boolean canWrite) {

    }

    @Override
    public void setData(DataSetInitialModel dataSetInitialModel) {
        binding.setDataSetModel(dataSetInitialModel);
        binding.catComboContainer.removeAllViews();
        selectedCatOptions = new HashMap<>();
        if (!dataSetInitialModel.categoryComboName().equals("default"))
            for (Category categories : dataSetInitialModel.getCategories()) {
                selectedCatOptions.put(categories.uid(), null);
                ItemCategoryComboBinding categoryComboBinding = ItemCategoryComboBinding.inflate(getLayoutInflater(), binding.catComboContainer, false);
                categoryComboBinding.inputLayout.setHint(categories.displayName());
                categoryComboBinding.inputEditText.setOnClickListener(view -> {
                    selectedView = view;
                    presenter.onCatOptionClick(categories.uid());
                });
                binding.catComboContainer.addView(categoryComboBinding.getRoot());
            }
        else
            presenter.onCatOptionClick(dataSetInitialModel.getCategories().get(0).uid());
        checkActionVisivbility();
    }

    /**
     * When changing orgUnit, date must be cleared
     */
    @Override
    public void showOrgUnitDialog(List<OrganisationUnit> data) {
        OrgUnitDialog orgUnitDialog = OrgUnitDialog.getInstace();
        orgUnitDialog
                .setMultiSelection(false)
                .setOrgUnits(data)
                .setProgram(dataSetUid)
                .setTitle(getString(R.string.org_unit))
                .setPossitiveListener(v -> {
                    if (orgUnitDialog.getSelectedOrgUnit() != null && !orgUnitDialog.getSelectedOrgUnit().isEmpty()) {
                        selectedOrgUnit = orgUnitDialog.getSelectedOrgUnitModel();
                        if (selectedOrgUnit == null)
                            orgUnitDialog.dismiss();
                        binding.dataSetOrgUnitEditText.setText(selectedOrgUnit.displayName());
                        binding.dataSetPeriodEditText.setText("");
                    }
                    checkActionVisivbility();
                    orgUnitDialog.dismiss();
                })
                .setNegativeListener(v -> orgUnitDialog.dismiss())
                .show(getSupportFragmentManager(), OrgUnitDialog.class.getSimpleName());
    }

    @Override
    public void showPeriodSelector(PeriodType periodType, List<DateRangeInputPeriodModel> periods, Integer openFuturePeriods) {
        new PeriodDialogInputPeriod()
                .setInputPeriod(periods)
                .setOpenFuturePeriods(openFuturePeriods)
                .setPeriod(periodType)
                .setPossitiveListener(selectedDate -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(selectedDate);
                    this.selectedPeriod = calendar.getTime();
                    binding.dataSetPeriodEditText.setText(DateUtils.getInstance().getPeriodUIString(periodType, selectedDate, Locale.getDefault()));
                    checkActionVisivbility();
                })
                .show(getSupportFragmentManager(), PeriodDialog.class.getSimpleName());
    }

    @Override
    public void showCatComboSelector(String catOptionUid, List<CategoryOption> data) {
        if (data.size() == 1 && data.get(0).name().equals("default")) {
            if (selectedCatOptions == null)
                selectedCatOptions = new HashMap<>();
            selectedCatOptions.put(catOptionUid, data.get(0));
        } else {

            PopupMenu menu = new PopupMenu(this, selectedView, Gravity.BOTTOM);
            for (CategoryOption option : data)
                menu.getMenu().add(Menu.NONE, Menu.NONE, data.indexOf(option), option.displayName());

            menu.setOnDismissListener(menu1 -> selectedView = null);
            menu.setOnMenuItemClickListener(item -> {
                if (selectedCatOptions == null)
                    selectedCatOptions = new HashMap<>();
                selectedCatOptions.put(catOptionUid, data.get(item.getOrder()));
                ((TextInputEditText) selectedView).setText(data.get(item.getOrder()).displayName());
                checkActionVisivbility();
                return false;
            });
            menu.show();
        }
    }

    @Override
    public String getDataSetUid() {
        return dataSetUid;
    }

    @Override
    public OrganisationUnit getSelectedOrgUnit() {
        return selectedOrgUnit;
    }

    @Override
    public Date getSelectedPeriod() {
        return selectedPeriod;
    }

    @Override
    public List<String> getSelectedCatOptions() {
        List<String> selectedCatOption = new ArrayList<>();
        for (int i = 0; i < selectedCatOptions.keySet().size(); i++) {
            CategoryOption catOpt = selectedCatOptions.get(selectedCatOptions.keySet().toArray()[i]);
            selectedCatOption.add(catOpt.uid());
        }
        return selectedCatOption;
    }

    @Override
    public String getPeriodType() {
        return binding.getDataSetModel().periodType().name();
    }

    private void checkActionVisivbility() {
        boolean visible = true;
        if (selectedOrgUnit == null)
            visible = false;
        if (selectedPeriod == null)
            visible = false;
        for (String key : selectedCatOptions.keySet()) {
            if (selectedCatOptions.get(key) == null)
                visible = false;
        }

        binding.actionButton.setVisibility(visible ? View.VISIBLE : View.GONE);

    }
}
