package com.dhis2.usescases.main.program;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.dhis2.R;
import com.dhis2.databinding.FragmentProgramBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import io.reactivex.functions.Consumer;

/**
 * Created by ppajuelo on 18/10/2017.
 */

public class ProgramFragment extends FragmentGlobalAbstract implements ProgramContractModule.View, DatePickerDialog.OnDateSetListener {


    FragmentProgramBinding binding;
    @Inject
    ProgramPresenter presenter;

    private final String DAILY = "Daily";
    private final String WEEKLY = "Weekly";
    private final String MONTHLY = "Monthly";
    private final String YEARLY = "Yearly";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_program, container, false);
        binding.setPresenter(presenter);
        return binding.getRoot();
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
    public void setUpRecycler() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false);
        binding.programRecycler.setLayoutManager(gridLayoutManager);
        binding.programRecycler.setAdapter(new ProgramAdapter(presenter));
        presenter.init();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpRecycler();
    }

    @Override
    public Consumer<List<HomeViewModel>> swapData() {

        return homeEntities -> ((ProgramAdapter) binding.programRecycler.getAdapter()).setData(homeEntities);
    }

    @Override
    public void renderError(String message) {
        new AlertDialog.Builder(getActivity())
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(getString(R.string.error))
                .setMessage(message)
                .show();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {

    }

    @Override
    public void addTree(TreeNode treeNode) {
        binding.treeViewContainer.removeAllViews();

        AndroidTreeView treeView = new AndroidTreeView(getContext(), treeNode);

        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
        treeView.setSelectionModeEnabled(true);

        binding.treeViewContainer.addView(treeView.getView());
        treeView.expandAll();

        treeView.setDefaultNodeLongClickListener(new TreeNode.TreeNodeLongClickListener() {
            @Override
            public boolean onLongClick(TreeNode node, Object value) {
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
                presenter.searchProgramByOrgUnit(childIds);
                return true;
            }
        });

    }

    @Override
    public void setOrgUnits(List<OrganisationUnitModel> orgUnits) {
        binding.linearContainer.removeAllViews();
        for (OrganisationUnitModel orgUnit : orgUnits) {
            TextView textView = new TextView(getContext());
            textView.setText(orgUnit.shortName());
            binding.linearContainer.addView(textView);
        }
    }

    @Override
    public void openDrawer() {
        binding.drawerLayout.openDrawer(Gravity.END);
    }
}
