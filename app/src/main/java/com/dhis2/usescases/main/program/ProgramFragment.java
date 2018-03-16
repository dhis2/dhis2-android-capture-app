package com.dhis2.usescases.main.program;

import android.app.DatePickerDialog;
import android.content.Context;
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

import com.dhis2.Components;
import com.dhis2.R;
import com.dhis2.databinding.FragmentProgramBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.utils.CustomViews.RxDateDialog;
import com.dhis2.utils.DateUtils;
import com.dhis2.utils.Period;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

/**
 * Created by ppajuelo on 18/10/2017.f
 */

public class ProgramFragment extends FragmentGlobalAbstract implements ProgramContract.View, OrgUnitInterface {

    public FragmentProgramBinding binding;
    @Inject
    ProgramContract.Presenter presenter;

    private Period currentPeriod = Period.DAILY;

    private AndroidTreeView treeView;

    //-------------------------------------------
    //region LIFECYCLE


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((Components) getActivity().getApplicationContext()).userComponent()
                .plus(new ProgramModule()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_program, container, false);
        binding.setPresenter(presenter);
        setUpRecycler();

        return binding.getRoot();
    }

    //endregion

    @Override
    public void showRageDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setMinimalDaysInFirstWeek(7);
        if (currentPeriod != Period.DAILY) {
            new RxDateDialog(getAbstractActivity(), currentPeriod).create().show().subscribe(selectedDates -> {
                if (!selectedDates.isEmpty()) {
                    String textToShow = DateUtils.getInstance().formatDate(selectedDates.get(0));
                    if (selectedDates.size() > 1)
                        textToShow += " " + DateUtils.getInstance().formatDate(selectedDates.get(1));
                    binding.buttonPeriodText.setText(textToShow);
                    presenter.getProgramsWithDates(selectedDates, currentPeriod);
                } else {
                    binding.buttonPeriodText.setText(getString(currentPeriod.getNameResouce()));
                    Date[] dates = DateUtils.getInstance().getDateFromPeriod(currentPeriod);
                    presenter.getPrograms(dates[0], dates[1]);
                }
            });
        } else {
            DatePickerDialog pickerDialog = new DatePickerDialog(getContext(), (datePicker, year, monthOfYear, dayOfMonth) -> {
                calendar.set(year, monthOfYear, dayOfMonth);
                Date[] dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), currentPeriod);
                presenter.getPrograms(dates[0], dates[1]);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            pickerDialog.show();
        }
    }

    @Override
    public void showTimeUnitPicker() {

        Drawable drawable = null;
        String textToShow = "";
        SimpleDateFormat weeklyFormat = new SimpleDateFormat("'Week' w", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

        switch (currentPeriod) {
            case DAILY:
                currentPeriod = Period.WEEKLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_week);
                break;
            case WEEKLY:
                currentPeriod = Period.MONTHLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_month);
                break;
            case MONTHLY:
                currentPeriod = Period.YEARLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_year);
                break;
            case YEARLY:
                currentPeriod = Period.DAILY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_day);
                break;
        }
        binding.buttonTime.setImageDrawable(drawable);
        Date[] dates = com.dhis2.utils.DateUtils.getInstance().getDateFromPeriod(currentPeriod);
        if (dates.length!=0) {

            switch (currentPeriod) {
                case DAILY:
                    textToShow = DateUtils.getInstance().formatDate(dates[0]);
                    break;
                case WEEKLY:
                    textToShow = weeklyFormat.format(dates[0]);
                    break;
                case MONTHLY:
                    textToShow = monthFormat.format(dates[0]);
                    break;
                case YEARLY:
                    textToShow = yearFormat.format(dates[0]);
                    break;
            }
        }

            binding.buttonPeriodText.setText(textToShow);

        //binding.buttonPeriodText.setText(getString(currentPeriod.getNameResouce()));
       presenter.getPrograms(dates[0], dates[1]);
    }

    @Override
    public void setUpRecycler() {
        binding.programRecycler.setAdapter(new ProgramAdapter(presenter));
        presenter.init(this);
    }

    @Override
    public Consumer<List<ProgramModel>> swapProgramData() {
        return programs -> {
            binding.programProgress.setVisibility(View.GONE);
            ((ProgramAdapter) binding.programRecycler.getAdapter()).setData(programs);
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

    @Override
    public void addTree(TreeNode treeNode) {
        binding.treeViewContainer.removeAllViews();
        binding.orgUnitApply.setOnClickListener((View.OnClickListener) view -> apply());
        treeView = new AndroidTreeView(getContext(), treeNode);

        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
        treeView.setSelectionModeEnabled(true);

        binding.treeViewContainer.addView(treeView.getView());
        treeView.expandAll();

        treeView.setDefaultNodeClickListener((node, value) -> {
            ArrayList<String> selectedUids = new ArrayList<>();
            for (TreeNode selectedOrgsTreeNode : treeView.getSelected()) {
                selectedUids.add(((OrganisationUnitModel) selectedOrgsTreeNode.getValue()).uid());
            }
            binding.buttonOrgUnit.setText(String.format("(%s) Org Unit", treeView.getSelected()));
        });
    }


    @Override
    public void openDrawer() {
        binding.drawerLayout.openDrawer(Gravity.END);
    }

    @Override
    public void apply() {
        binding.drawerLayout.closeDrawers();
        StringBuilder orgUnitFilter = new StringBuilder();
        for (int i = 0; i < treeView.getSelected().size(); i++) {
            orgUnitFilter.append("'");
            orgUnitFilter.append(((OrganisationUnitModel) treeView.getSelected().get(i).getValue()).uid());
            orgUnitFilter.append("'");
            if (i < treeView.getSelected().size() - 1)
                orgUnitFilter.append(", ");
        }

        presenter.getProgramsOrgUnit(orgUnitFilter.toString());

    }
}
