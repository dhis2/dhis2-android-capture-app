package com.dhis2.usescases.datasets.datasetDetail;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.data.service.DataServiceModule;
import com.dhis2.databinding.ActivityDatasetDetailBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.programEventDetail.ProgramEventDetailContract;
import com.dhis2.usescases.programEventDetail.ProgramEventDetailModule;
import com.dhis2.utils.Period;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class DataSetDetailActivity extends ActivityGlobalAbstract implements DataSetDetailContract.View{

    private ActivityDatasetDetailBinding binding;
    private ArrayList<Date> chosenDateWeek = new ArrayList<>();
    private ArrayList<Date> chosenDateMonth = new ArrayList<>();
    private ArrayList<Date> chosenDateYear = new ArrayList<>();
    private String programId;
    private Period currentPeriod = Period.NONE;
    private StringBuilder orgUnitFilter = new StringBuilder();

    @Inject
    DataSetDetailContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) getApplicationContext()).userComponent().plus(new DataSetDetailModule()).inject(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_detail);

        chosenDateWeek.add(new Date());
        chosenDateMonth.add(new Date());
        chosenDateYear.add(new Date());

        programId = getIntent().getStringExtra("PROGRAM_UID");
        binding.setPresenter(presenter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this, programId, currentPeriod);

        presenter.getDataSetWithDates(null,null, currentPeriod, orgUnitFilter.toString());
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
        binding.treeViewContainer.removeAllViews();
    }

    @Override
    public void setData(List<DataSetModel> events) {

    }

    @Override
    public void addTree(TreeNode treeNode) {

    }

    @Override
    public void openDrawer() {

    }

    @Override
    public void showTimeUnitPicker() {

    }

    @Override
    public void showRageDatePicker() {

    }

    @Override
    public void setProgram(ProgramModel programModel) {

    }

    @Override
    public void setCatComboOptions(CategoryComboModel catCombo, List<CategoryOptionComboModel> catComboList) {

    }

    @Override
    public void setOrgUnitFilter(StringBuilder orgUnitFilter) {

    }

    @Override
    public void showHideFilter() {

    }

    @Override
    public void apply() {

    }

    @Override
    public void setWritePermission(Boolean aBoolean) {

    }
}
