package com.dhis2.usescases.main.program;

import android.annotation.SuppressLint;
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

import com.dhis2.Components;
import com.dhis2.R;
import com.dhis2.data.tuples.Pair;
import com.dhis2.databinding.FragmentProgramBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.utils.CustomViews.OrgUnitButton;
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

import static com.dhis2.utils.Period.DAILY;
import static com.dhis2.utils.Period.MONTHLY;
import static com.dhis2.utils.Period.WEEKLY;
import static com.dhis2.utils.Period.YEARLY;

/**
 * Created by ppajuelo on 18/10/2017.f
 */

public class ProgramFragment extends FragmentGlobalAbstract implements ProgramContract.View, OrgUnitInterface {

    public FragmentProgramBinding binding;
    @Inject
    ProgramContract.Presenter presenter;

    private Period currentPeriod = DAILY;
    private StringBuilder orgUnitFilter = new StringBuilder();

    private AndroidTreeView treeView;

    private Date chosenDateDay = new Date();
    private ArrayList<Date> chosenDateWeek = new ArrayList<>();
    private ArrayList<Date> chosenDateMonth = new ArrayList<>();
    private ArrayList<Date> chosenDateYear = new ArrayList<>();
    SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

    //-------------------------------------------
    //region LIFECYCLE

    public void setOrgUnitFilter(StringBuilder orgUnitFilter) {
        this.orgUnitFilter = orgUnitFilter;
    }

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
        chosenDateWeek.add(new Date());
        chosenDateMonth.add(new Date());
        chosenDateYear.add(new Date());
        binding.buttonPeriodText.setText(DateUtils.getInstance().formatDate(new Date()));
        setUpRecycler();

        return binding.getRoot();
    }

    //endregion

    @SuppressLint({"CheckResult", "RxLeakedSubscription", "RxSubscribeOnError"})
    @Override
    public void showRageDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setMinimalDaysInFirstWeek(7);

        String week = getString(R.string.week);
        SimpleDateFormat weeklyFormat = new SimpleDateFormat("'" + week + "' w", Locale.getDefault());

        if (currentPeriod != DAILY) {
            new RxDateDialog(getAbstractActivity(), currentPeriod).create().show().subscribe(selectedDates -> {
                if (!selectedDates.isEmpty()) {
                    String textToShow;
                    if (currentPeriod == WEEKLY) {
                        textToShow = weeklyFormat.format(selectedDates.get(0)) + ", " + yearFormat.format(selectedDates.get(0));
                        chosenDateWeek = (ArrayList<Date>) selectedDates;
                        if (selectedDates.size() > 1)
                            textToShow += "... " /*+ weeklyFormat.format(selectedDates.get(1))*/;
                    } else if (currentPeriod == MONTHLY) {
                        textToShow = monthFormat.format(selectedDates.get(0));
                        chosenDateMonth = (ArrayList<Date>) selectedDates;
                        if (selectedDates.size() > 1)
                            textToShow += "... " /*+ monthFormat.format(selectedDates.get(1))*/;
                    } else {
                        textToShow = yearFormat.format(selectedDates.get(0));
                        chosenDateYear = (ArrayList<Date>) selectedDates;
                        if (selectedDates.size() > 1)
                            textToShow += "... " /*+ yearFormat.format(selectedDates.get(1))*/;

                    }
                    binding.buttonPeriodText.setText(textToShow);
                    getSelectedPrograms((ArrayList<Date>) selectedDates, currentPeriod, orgUnitFilter.toString());

                } else {
                    ArrayList<Date> date = new ArrayList<>();
                    date.add(new Date());

                    String text = "";

                    switch (currentPeriod) {
                        case WEEKLY:
                            text = weeklyFormat.format(date.get(0)) + ", " + yearFormat.format(date.get(0));
                            chosenDateWeek = date;
                            break;
                        case MONTHLY:
                            text = monthFormat.format(date.get(0));
                            chosenDateMonth = date;
                            break;
                        case YEARLY:
                            text = yearFormat.format(date.get(0));
                            chosenDateYear = date;
                            break;
                    }
                    binding.buttonPeriodText.setText(text);
                    getSelectedPrograms(date, currentPeriod, orgUnitFilter.toString());

                }
            });
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(chosenDateDay);
            DatePickerDialog pickerDialog;
            pickerDialog = new DatePickerDialog(getContext(), (datePicker, year, monthOfYear, dayOfMonth) -> {
                calendar.set(year, monthOfYear, dayOfMonth);
                Date[] dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), currentPeriod);
                ArrayList<Date> day = new ArrayList<>();
                day.add(dates[0]);
                getSelectedPrograms(day, currentPeriod, orgUnitFilter.toString());
                binding.buttonPeriodText.setText(DateUtils.getInstance().formatDate(dates[0]));
                chosenDateDay = dates[0];
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            pickerDialog.show();
        }
    }

    @Override
    public void showTimeUnitPicker() {

        Drawable drawable = null;
        String textToShow = "";

        switch (currentPeriod) {
            case DAILY:
                currentPeriod = WEEKLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_week);
                break;
            case WEEKLY:
                currentPeriod = MONTHLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_month);
                break;
            case MONTHLY:
                currentPeriod = YEARLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_year);
                break;
            case YEARLY:
                currentPeriod = DAILY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_day);
                break;
        }
        ((ProgramAdapter) binding.programRecycler.getAdapter()).setCurrentPeriod(currentPeriod);
        binding.buttonTime.setImageDrawable(drawable);

        switch (currentPeriod) {
            case DAILY:
                ArrayList<Date> datesD = new ArrayList<>();
                datesD.add(chosenDateDay);
                if (!datesD.isEmpty())
                    textToShow = DateUtils.getInstance().formatDate(datesD.get(0));
                if (!datesD.isEmpty() && datesD.size() > 1) textToShow += "... ";
                getSelectedPrograms(datesD, currentPeriod, orgUnitFilter.toString());
                break;
            case WEEKLY:
                if (!chosenDateWeek.isEmpty()) {
                    String week = getString(R.string.week);
                    SimpleDateFormat weeklyFormat = new SimpleDateFormat("'" + week + "' w", Locale.getDefault());
                    textToShow = weeklyFormat.format(chosenDateWeek.get(0)) + ", " + yearFormat.format(chosenDateWeek.get(0));
                }
                if (!chosenDateWeek.isEmpty() && chosenDateWeek.size() > 1) textToShow += "... ";
                getSelectedPrograms(chosenDateWeek, currentPeriod, orgUnitFilter.toString());
                break;
            case MONTHLY:
                if (!chosenDateMonth.isEmpty())
                    textToShow = monthFormat.format(chosenDateMonth.get(0));
                if (!chosenDateMonth.isEmpty() && chosenDateMonth.size() > 1) textToShow += "... ";
                getSelectedPrograms(chosenDateMonth, currentPeriod, orgUnitFilter.toString());
                break;
            case YEARLY:
                if (!chosenDateYear.isEmpty())
                    textToShow = yearFormat.format(chosenDateYear.get(0));
                if (!chosenDateYear.isEmpty() && chosenDateYear.size() > 1) textToShow += "... ";
                getSelectedPrograms(chosenDateYear, currentPeriod, orgUnitFilter.toString());
                break;
        }


        binding.buttonPeriodText.setText(textToShow);
    }

    @Override
    public void setUpRecycler() {
        binding.programRecycler.setAdapter(new ProgramAdapter(presenter,currentPeriod));
        presenter.init(this);
    }

    @Override
    public void getSelectedPrograms(ArrayList<Date> dates, Period period, String orgUnitQuery) {
        if (orgUnitQuery.isEmpty())
            presenter.getProgramsWithDates(dates, period);
        else
            presenter.getProgramsOrgUnit(dates, period, orgUnitQuery);
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
        binding.orgUnitApply.setOnClickListener(view -> apply());
        treeView = new AndroidTreeView(getContext(), treeNode);

        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
        treeView.setSelectionModeEnabled(true);

        binding.treeViewContainer.addView(treeView.getView());
        treeView.expandAll();

        treeView.setDefaultNodeClickListener((node, value) -> {
            if (treeView.getSelected().size() == 1 && !node.isSelected()) {
                ((OrgUnitHolder) node.getViewHolder()).update();
                binding.buttonOrgUnit.setText(String.format("(%s) Org Unit", treeView.getSelected().size()));
            } else if (treeView.getSelected().size() > 1) {
                ((OrgUnitHolder) node.getViewHolder()).update();
                binding.buttonOrgUnit.setText(String.format("(%s) Org Unit", treeView.getSelected().size()));
            }
        });

        binding.buttonOrgUnit.setText(String.format("(%s) Org Unit", treeView.getSelected().size()));
    }


    @Override
    public void openDrawer() {
        binding.drawerLayout.openDrawer(Gravity.END);
    }

    @Override
    public ArrayList<Date> getChosenDateWeek() {
        return chosenDateWeek;
    }

    @Override
    public ArrayList<Date> getChosenDateMonth() {
        return chosenDateMonth;
    }

    @Override
    public ArrayList<Date> getChosenDateYear() {
        return chosenDateYear;
    }

    @Override
    public Date getChosenDateDay() {
        return chosenDateDay;
    }


    @Override
    public void apply() {
        binding.drawerLayout.closeDrawers();
        orgUnitFilter = new StringBuilder();
        for (int i = 0; i < treeView.getSelected().size(); i++) {
            orgUnitFilter.append("'");
            orgUnitFilter.append(((OrganisationUnitModel) treeView.getSelected().get(i).getValue()).uid());
            orgUnitFilter.append("'");
            if (i < treeView.getSelected().size() - 1)
                orgUnitFilter.append(", ");
        }


        switch (currentPeriod) {
            case DAILY:
                ArrayList<Date> datesD = new ArrayList<>();
                datesD.add(chosenDateDay);
                getSelectedPrograms(datesD, currentPeriod, orgUnitFilter.toString());
                break;
            case WEEKLY:
                getSelectedPrograms(chosenDateWeek, currentPeriod, orgUnitFilter.toString());
                break;
            case MONTHLY:
                getSelectedPrograms(chosenDateMonth, currentPeriod, orgUnitFilter.toString());
                break;
            case YEARLY:
                getSelectedPrograms(chosenDateYear, currentPeriod, orgUnitFilter.toString());
                break;
        }
        ((ProgramAdapter) binding.programRecycler.getAdapter()).setCurrentPeriod(currentPeriod);
    }
}
