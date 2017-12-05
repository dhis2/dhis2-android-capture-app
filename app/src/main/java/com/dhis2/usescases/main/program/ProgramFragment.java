package com.dhis2.usescases.main.program;

import android.app.DatePickerDialog;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dhis2.R;
import com.dhis2.databinding.FragmentProgramBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.utils.CustomViews.DateAdapter;
import com.dhis2.utils.CustomViews.DateDialog;
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

public class ProgramFragment extends FragmentGlobalAbstract implements ProgramContractModule.View {

    public FragmentProgramBinding binding;
    @Inject
    ProgramPresenter presenter;

    private final String DAILY = "daily";
    private final String WEEKLY = "weekly";
    private final String MONTHLY = "monthly";
    private final String YEARLY = "yearly";

    private DateAdapter.Period currentPeriod = DateAdapter.Period.DAILY;


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

        Calendar calendar = Calendar.getInstance();
        calendar.setMinimalDaysInFirstWeek(7);
        if (currentPeriod != DateAdapter.Period.DAILY) {
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
                currentPeriod = DateAdapter.Period.WEEKLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_week);
                period = WEEKLY;
                break;
            case WEEKLY:
                currentPeriod = DateAdapter.Period.MONTHLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_month);
                period = MONTHLY;
                break;
            case MONTHLY:
                currentPeriod = DateAdapter.Period.YEARLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_year);
                period = YEARLY;
                break;
            case YEARLY:
                currentPeriod = DateAdapter.Period.DAILY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_day);
                period = DAILY;
                break;
        }
        binding.buttonTime.setImageDrawable(drawable);
        Toast.makeText(getContext(), String.format("Period filter set to %s", period), Toast.LENGTH_LONG).show();
    }

    @Override
    public void setUpRecycler() {

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

        return homeEntities -> {
            binding.programProgress.setVisibility(View.GONE);
            ((ProgramAdapter) binding.programRecycler.getAdapter()).setData(homeEntities);
        };
    }

    @Override
    public void renderError(String message) {
        if (getActivity() != null)
            new AlertDialog.Builder(getActivity())
                    .setPositiveButton(android.R.string.ok, null)
                    .setTitle(getString(R.string.error))
                    .setMessage(message)
                    .show();
    }
/*
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {

    }*/

    @Override
    public void addTree(TreeNode treeNode) {
        binding.treeViewContainer.removeAllViews();

        AndroidTreeView treeView = new AndroidTreeView(getContext(), treeNode);

        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
        treeView.setSelectionModeEnabled(true);

        binding.treeViewContainer.addView(treeView.getView());
        treeView.expandAll();

        treeView.setDefaultNodeClickListener((node, value) -> {
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
            presenter.searchProgramByOrgUnit(childIds);
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
