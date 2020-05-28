package org.dhis2.usescases.datasets.dataSetTable;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.jakewharton.rxbinding2.view.RxView;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityDatasetTableBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.validationrules.ValidationRulesBottomDialog;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.period.Period;
import org.hisp.dhis.android.core.validation.engine.ValidationResultViolation;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.Observable;
import kotlin.Unit;

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
    private boolean backPressed;
    private DataSetTableComponent dataSetTableComponent;

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
        orgUnitUid = getIntent().getStringExtra(Constants.ORG_UNIT);
        orgUnitName = getIntent().getStringExtra(Constants.ORG_UNIT_NAME);
        periodTypeName = getIntent().getStringExtra(Constants.PERIOD_TYPE);
        periodId = getIntent().getStringExtra(Constants.PERIOD_ID);
        periodInitialDate = getIntent().getStringExtra(Constants.PERIOD_TYPE_DATE);
        catOptCombo = getIntent().getStringExtra(Constants.CAT_COMB);
        dataSetUid = getIntent().getStringExtra(Constants.DATA_SET_UID);
        accessDataWrite = getIntent().getBooleanExtra(Constants.ACCESS_DATA, true);

        dataSetTableComponent = ((App) getApplicationContext()).userComponent().plus(new DataSetTableModule(this, dataSetUid, periodId, orgUnitUid, catOptCombo));
        dataSetTableComponent.inject(this);
        super.onCreate(savedInstanceState);

        //Orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_table);
        binding.setPresenter(presenter);
        setViewPager();
        observeSaveButtonClicks();
        presenter.init(orgUnitUid, periodTypeName, catOptCombo, periodInitialDate, periodId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDettach();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        viewPagerAdapter.notifyDataSetChanged();
    }

    private void setViewPager() {
        viewPagerAdapter = new DataSetSectionAdapter(this, accessDataWrite, getIntent().getStringExtra(Constants.DATA_SET_UID));
        binding.viewPager.setAdapter(viewPagerAdapter);
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.dataset_overview);
            } else if (position == viewPagerAdapter.getItemCount() - 1) {
                tab.setText(R.string.dashboard_notes);
            } else {
                tab.setText(viewPagerAdapter.getSectionTitle(position));
            }
        }).attach();

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == viewPagerAdapter.getItemCount() -1) {
                    binding.saveButton.hide();
                } else {
                    binding.saveButton.show();
                }
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
        if (sections.get(0) == "NO_SECTION"){
            sections.remove("NO_SECTION");
            sections.add(getString(R.string.tab_tables));
            viewPagerAdapter.swapData(sections);
        }
    }

    public DataSetTableContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public Boolean accessDataWrite() {
        return accessDataWrite;
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
    public void renderDetails(DataSet dataSet, String catComboName, Period period) {
        binding.dataSetName.setText(dataSet.displayName());
        StringBuilder subtitle = new StringBuilder(
                DateUtils.getInstance().getPeriodUIString(period.periodType(), period.startDate(), Locale.getDefault())
        )
                .append("|")
                .append(orgUnitName);
        if (!catComboName.equals("default")) {
            subtitle.append("|")
                    .append(catComboName);
        }
        binding.dataSetSubtitle.setText(subtitle);
    }

    public void update() {
        presenter.init(orgUnitUid, periodTypeName, catOptCombo, periodInitialDate, periodId);
    }

    @Override
    public void back() {
        if (getCurrentFocus() == null || backPressed)
            super.back();
        else {
            backPressed = true;
            binding.getRoot().requestFocus();
            back();
        }
    }

    @Override
    public void onBackPressed() {
        back();
    }

    public boolean isBackPressed() {
        return backPressed;
    }

    public DataSetTableComponent getDataSetTableComponent() {
        return dataSetTableComponent;
    }

    @Override
    public Observable<Object> observeSaveButtonClicks(){
        return RxView.clicks(binding.saveButton);
    }

    @Override
    public void showInfoDialog(boolean isMandatoryFields) {
        String title = getString(R.string.missing_mandatory_fields_title);
        String message;
        if (isMandatoryFields) {
            message = getString(R.string.field_mandatory);
        } else {
            message = getString(R.string.field_required);
        }
        super.showInfoDialog(title, message);
    }

    @Override
    public void showValidationRuleDialog() {
        ValidationRulesBottomDialog.Companion.getInstance()
                .setTitle(getString(R.string.saved))
                .setMessage(getString(R.string.run_validation_rules))
                .setPositiveButton(getString(R.string.yes), () -> {
                    presenter.executeValidationRules();
                    return Unit.INSTANCE;
                })
                .setNegativeButton(getString(R.string.no), null)
                .show(getSupportFragmentManager(), ValidationRulesBottomDialog.class.getSimpleName());
    }

    @Override
    public void showSuccessValidationDialog() {
        ValidationRulesBottomDialog.Companion.getInstance()
                .setTitle(getString(R.string.validation_success_title))
                .setMessage(getString(R.string.mark_dataset_complete))
                .setPositiveButton(getString(R.string.yes), ()-> {
                    presenter.completeDataSet();
                    return Unit.INSTANCE;
                })
                .setNegativeButton(getString(R.string.no), null)
                .show(getSupportFragmentManager(), ValidationRulesBottomDialog.class.getSimpleName());
    }

    @Override
    public void showErrorsValidationDialog(List<ValidationResultViolation> violations) {
        ValidationRulesBottomDialog.Companion.getInstance()
                .setTitle(getString(R.string.error))
                .setPositiveButton(getString(R.string.complete_anyway), () -> {
                    presenter.completeDataSet();
                    return Unit.INSTANCE;
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .setViolations(violations)
                .show(getSupportFragmentManager(), ValidationRulesBottomDialog.class.getSimpleName());

    }

    @Override
    public void showCompleteToast() {
        super.displayMessage(getString(R.string.dataset_completed));
    }
}
