package com.dhis2.usescases.programDetail;

import android.app.DatePickerDialog;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.widget.Toast;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityProgramDetailBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.main.program.HomeViewModel;
import com.dhis2.utils.CustomViews.DateAdapter;
import com.dhis2.utils.CustomViews.DateDialog;
import com.dhis2.utils.EndlessRecyclerViewScrollListener;
import com.dhis2.utils.Period;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 31/10/2017.
 */

public class ProgramDetailActivity extends ActivityGlobalAbstract implements ProgramDetailContractModule.View {
    private final String DAILY = "Daily";
    private final String WEEKLY = "Weekly";
    private final String MONTHLY = "Monthly";
    private final String YEARLY = "Yearly";
    ActivityProgramDetailBinding binding;
    @Inject
    ProgramDetailContractModule.Presenter presenter;
    HomeViewModel homeViewModel;
    @Inject
    ProgramDetailAdapter adapter;
    private Period currentPeriod = Period.DAILY;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).getUserComponent().plus(new ProgramDetailModule()).inject(this);

        super.onCreate(savedInstanceState);
        homeViewModel = (HomeViewModel) getIntent().getSerializableExtra("PROGRAM");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_program_detail);
        binding.setPresenter(presenter);
        presenter.init(this, homeViewModel);

        binding.recycler.addOnScrollListener(new EndlessRecyclerViewScrollListener(binding.recycler.getLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                presenter.nextPageForApi(page + 1);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDettach();
    }

    @Override
    public void swapData(TrackedEntityObject response) {
        if (binding.recycler.getAdapter() == null) {
            adapter.setProgram(homeViewModel);
            binding.recycler.setAdapter(adapter);
        }

        adapter.addItems(response.getTrackedEntityInstances());
    }

    @Override
    public void setAttributeOrder(List<ProgramTrackedEntityAttributeModel> programAttributes) {
        if (binding.recycler.getAdapter() == null) {
            adapter.setProgram(homeViewModel);
            binding.recycler.setAdapter(adapter);
        }

        adapter.setAttributesToShow(programAttributes);
    }

    @Override
    public void setOrgUnitNames(List<OrganisationUnitModel> orgsUnits) {
        adapter.setOrgUnits(orgsUnits);
    }

    @Override
    public void openDrawer() {
        if (!binding.drawerLayout.isDrawerOpen(Gravity.END))
            binding.drawerLayout.openDrawer(Gravity.END);
        else
            binding.drawerLayout.closeDrawer(Gravity.END);
    }

    @Override
    public void showRageDatePicker() {

        Calendar calendar = Calendar.getInstance();
        calendar.setMinimalDaysInFirstWeek(7);
        if (currentPeriod != Period.DAILY) {
            DateDialog dialog = DateDialog.newInstace(currentPeriod);
            dialog.setCancelable(true);
            getActivity().getSupportFragmentManager().beginTransaction().add(dialog, null).commit();
        } else {
            DatePickerDialog pickerDialog = new DatePickerDialog(getContext(), (datePicker, year, monthOfYear, dayOfMonth) -> {

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            pickerDialog.show();
        }
    }

    @Override
    public void showTimeUnitPicker() {
        Drawable drawable = null;
        String period = null;
        switch (currentPeriod) {
            case DAILY:
                currentPeriod = Period.WEEKLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_week);
                period = WEEKLY;
                break;
            case WEEKLY:
                currentPeriod = Period.MONTHLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_month);
                period = MONTHLY;
                break;
            case MONTHLY:
                currentPeriod = Period.YEARLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_year);
                period = YEARLY;
                break;
            case YEARLY:
                currentPeriod = Period.DAILY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_day);
                period = DAILY;
                break;
        }
        binding.buttonTime.setImageDrawable(drawable);
        Toast.makeText(getContext(), String.format("Period filter set to %s", period), Toast.LENGTH_LONG).show();
    }

    @Override
    public void addTree(TreeNode treeNode) {


        binding.treeViewContainer.removeAllViews();

        AndroidTreeView treeView = new AndroidTreeView(getContext(), treeNode);

        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
        treeView.setSelectionModeEnabled(true);

        binding.treeViewContainer.addView(treeView.getView());
        treeView.expandAll();

        treeView.setDefaultNodeLongClickListener((node, value) -> {
            node.setSelected(!node.isSelected());
            ArrayList<String> childIds = new ArrayList<String>();
            childIds.add(((OrganisationUnitModel) value).uid());
            for (TreeNode childNode : node.getChildren()) {
                childIds.add(((OrganisationUnitModel) childNode.getValue()).uid());
                for (TreeNode childNode2 : childNode.getChildren()) {
                    childIds.add(((OrganisationUnitModel) childNode2.getValue()).uid());
                    for (TreeNode childNode3 : childNode2.getChildren()) {
                        childIds.add(((OrganisationUnitModel) childNode3.getValue()).uid());
                    }
                }
            }
            binding.buttonOrgUnit.setText(((OrganisationUnitModel) value).displayShortName());
            binding.drawerLayout.closeDrawers();
            return true;
        });
    }

/*
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {

    }*/
}
