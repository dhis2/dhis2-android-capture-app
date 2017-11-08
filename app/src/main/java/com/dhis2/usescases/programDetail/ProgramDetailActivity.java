package com.dhis2.usescases.programDetail;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;

import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityProgramDetailBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.main.program.HomeViewModel;
import com.dhis2.utils.EndlessRecyclerViewScrollListener;
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

public class ProgramDetailActivity extends ActivityGlobalAbstract implements ProgramDetailContractModule.View, DatePickerDialog.OnDateSetListener {
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
//        AndroidInjection.inject(this);
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
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");

        dpd.setOnDateSetListener((view, year, monthOfYear, dayOfMonth, yearEnd, monthOfYearEnd, dayOfMonthEnd) -> {
            String fromDay = dayOfMonth < 10 ? "0" + dayOfMonth : "" + dayOfMonth;
            String fromMonth = monthOfYear + 1 < 10 ? "0" + (monthOfYear + 1) : "" + (monthOfYear + 1);
            String fromYear = String.valueOf(year);
            String toDay = dayOfMonthEnd < 10 ? "0" + dayOfMonthEnd : "" + dayOfMonthEnd;
            String toMonth = monthOfYearEnd + 1 < 10 ? "0" + (monthOfYearEnd + 1) : "" + (monthOfYearEnd + 1);
            String toYear = String.valueOf(yearEnd);

            String from;
            String to;

            if (binding.buttonTime.getText().equals(WEEKLY)) {
                from = "week";
                to = "week";
            } else if (binding.buttonTime.getText().equals(MONTHLY)) {
                from = fromMonth + "/" + fromYear;
                to = toMonth + "/" + toYear;
            } else if (binding.buttonTime.getText().equals(YEARLY)) {
                from = fromYear;
                to = toYear;
            } else {
                from = fromDay + "/" + fromMonth;
                to = toDay + "/" + toMonth;
            }
            binding.buttonDateRange.setText(from + "-" + to);
        });
    }

    @Override
    public void showTimeUnitPicker() {
        PopupMenu popupMenu = new PopupMenu(getContext(), binding.buttonTime);
        popupMenu.getMenu().add(1, 0, Menu.NONE, DAILY);
        popupMenu.getMenu().add(1, 0, Menu.NONE, WEEKLY);
        popupMenu.getMenu().add(1, 0, Menu.NONE, MONTHLY);
        popupMenu.getMenu().add(1, 0, Menu.NONE, YEARLY);
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(item -> {
            binding.buttonTime.setText(item.getTitle());
            return true;
        });
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


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {

    }
}
