package com.dhis2.usescases.programEventDetail;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityProgramEventDetailBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.utils.CatComboAdapter;
import com.dhis2.utils.CustomViews.RxDateDialog;
import com.dhis2.utils.DateUtils;
import com.dhis2.utils.OnErrorHandler;
import com.dhis2.utils.Period;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import static com.dhis2.utils.Period.DAILY;
import static com.dhis2.utils.Period.MONTHLY;
import static com.dhis2.utils.Period.WEEKLY;

/**
 * Created by Cristian on 13/02/2018.
 */

public class ProgramEventDetailActivity extends ActivityGlobalAbstract implements ProgramEventDetailContract.View {

    private ActivityProgramEventDetailBinding binding;

    @Inject
    ProgramEventDetailContract.Presenter presenter;

    @Inject
    ProgramEventDetailAdapter adapter;
    private Period currentPeriod = Period.DAILY;
    ProgramModel programModel;

    private Date chosenDateDay = new Date();
    private ArrayList<Date> chosenDateWeek = new ArrayList<>();
    private ArrayList<Date> chosenDateMonth = new ArrayList<>();
    private ArrayList<Date> chosenDateYear = new ArrayList<>();
    SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
    private AndroidTreeView treeView;
    private TreeNode treeNode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new ProgramEventDetailModule()).inject(this);

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_program_event_detail);

        chosenDateWeek.add(new Date());
        chosenDateMonth.add(new Date());
        chosenDateYear.add(new Date());
        //binding.buttonPeriodText.setText(DateUtils.getInstance().formatDate(new Date()));

        String programId = getIntent().getStringExtra("PROGRAM_UID");
        binding.setPresenter(presenter);


        Drawable drawable = null;
        switch (getIntent().getIntExtra("CURRENT_PERIOD", 0)) {
            case R.string.DAILY:
                currentPeriod = Period.DAILY;
                if (getIntent().getSerializableExtra("CHOOSEN_DATE") != null)
                    chosenDateDay = (Date) getIntent().getSerializableExtra("CHOOSEN_DATE");
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_day);
                break;
            case R.string.WEEKLY:
                currentPeriod = Period.WEEKLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_week);
                if (getIntent().getSerializableExtra("CHOOSEN_DATE") != null)
                    chosenDateWeek = (ArrayList<Date>) getIntent().getSerializableExtra("CHOOSEN_DATE");
                break;
            case R.string.MONTHLY:
                currentPeriod = Period.MONTHLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_month);
                if (getIntent().getSerializableExtra("CHOOSEN_DATE") != null)
                    chosenDateMonth = (ArrayList<Date>) getIntent().getSerializableExtra("CHOOSEN_DATE");
                break;
            case R.string.YEARLY:
                currentPeriod = Period.YEARLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_year);
                if (getIntent().getSerializableExtra("CHOOSEN_DATE") != null)
                    chosenDateYear = (ArrayList<Date>) getIntent().getSerializableExtra("CHOOSEN_DATE");
                break;
            default:
                currentPeriod = Period.DAILY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_day);
                break;
        }
        binding.buttonTime.setImageDrawable(drawable);
        presenter.init(this, programId, currentPeriod);
        currentPeriodConfig();

    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setData(List<EventModel> events) {
        if (binding.recycler.getAdapter() == null) {
            binding.recycler.setAdapter(adapter);
        }
        adapter.setEvents(events);
    }

    @Override
    public void setProgram(ProgramModel program) {
        this.programModel = program;
        presenter.setProgram(program);
        binding.setName(program.displayName());
    }

    @Override
    public void openDrawer() {
        if (!binding.drawerLayout.isDrawerOpen(Gravity.END))
            binding.drawerLayout.openDrawer(Gravity.END);
        else
            binding.drawerLayout.closeDrawer(Gravity.END);
    }

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
                    presenter.getProgramEventsWithDates(selectedDates, currentPeriod);
                    //getSelectedPrograms((ArrayList<Date>) selectedDates, currentPeriod, orgUnitFilter.toString());

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
                    presenter.getProgramEventsWithDates(date, currentPeriod);
                    //getSelectedPrograms(date, currentPeriod, orgUnitFilter.toString());

                }
            }, OnErrorHandler.create());
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(chosenDateDay);
            DatePickerDialog pickerDialog;
            pickerDialog = new DatePickerDialog(getContext(), (datePicker, year, monthOfYear, dayOfMonth) -> {
                calendar.set(year, monthOfYear, dayOfMonth);
                Date[] dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), currentPeriod);
                ArrayList<Date> day = new ArrayList<>();
                day.add(dates[0]);
                //getSelectedPrograms(day, currentPeriod, orgUnitFilter.toString());
                presenter.getProgramEventsWithDates(day, currentPeriod);
                binding.buttonPeriodText.setText(DateUtils.getInstance().formatDate(dates[0]));
                chosenDateDay = dates[0];
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            pickerDialog.show();
        }
    }


    @Override
    public void showTimeUnitPicker() {
        Drawable drawable = null;


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
        /*binding.buttonPeriodText.setText(getString(currentPeriod.getNameResouce()));
        Date[] dates = com.dhis2.utils.DateUtils.getInstance().getDateFromPeriod(currentPeriod);
        presenter.getEvents(dates[0], dates[1]);*/

        currentPeriodConfig();
    }

    private void currentPeriodConfig() {
        String textToShow = "";

        switch (currentPeriod) {
            case DAILY:
                ArrayList<Date> datesD = new ArrayList<>();
                datesD.add(chosenDateDay);
                if (!datesD.isEmpty())
                    textToShow = DateUtils.getInstance().formatDate(datesD.get(0));
                if (!datesD.isEmpty() && datesD.size() > 1) textToShow += "... ";
                //getSelectedPrograms(datesD, currentPeriod, orgUnitFilter.toString());
                presenter.getProgramEventsWithDates(datesD, currentPeriod);
                break;
            case WEEKLY:
                if (!chosenDateWeek.isEmpty()) {
                    String week = getString(R.string.week);
                    SimpleDateFormat weeklyFormat = new SimpleDateFormat("'" + week + "' w", Locale.getDefault());
                    textToShow = weeklyFormat.format(chosenDateWeek.get(0)) + ", " + yearFormat.format(chosenDateWeek.get(0));
                }
                if (!chosenDateWeek.isEmpty() && chosenDateWeek.size() > 1) textToShow += "... ";
                //getSelectedPrograms(chosenDateWeek, currentPeriod, orgUnitFilter.toString());
                presenter.getProgramEventsWithDates(chosenDateWeek, currentPeriod);
                break;
            case MONTHLY:
                if (!chosenDateMonth.isEmpty())
                    textToShow = monthFormat.format(chosenDateMonth.get(0));
                if (!chosenDateMonth.isEmpty() && chosenDateMonth.size() > 1) textToShow += "... ";
                //getSelectedPrograms(chosenDateMonth, currentPeriod, orgUnitFilter.toString());
                presenter.getProgramEventsWithDates(chosenDateMonth, currentPeriod);
                break;
            case YEARLY:
                if (!chosenDateYear.isEmpty())
                    textToShow = yearFormat.format(chosenDateYear.get(0));
                if (!chosenDateYear.isEmpty() && chosenDateYear.size() > 1) textToShow += "... ";
                // getSelectedPrograms(chosenDateYear, currentPeriod, orgUnitFilter.toString());
                presenter.getProgramEventsWithDates(chosenDateYear, currentPeriod);
                break;
        }


        binding.buttonPeriodText.setText(textToShow);
    }

    @Override
    public void addTree(TreeNode treeNode) {
        binding.treeViewContainer.removeAllViews();
        this.treeNode = treeNode;

        treeView = new AndroidTreeView(getContext(), treeNode);

        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
        treeView.setSelectionModeEnabled(true);

        binding.treeViewContainer.addView(treeView.getView());
        treeView.expandAll();

        treeView.setDefaultNodeLongClickListener((node, value) -> {
            node.setSelected(!node.isSelected());
            ArrayList<String> childIds = new ArrayList<>();
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
    public void renderError(String message) {
        if (getActivity() != null)
            new AlertDialog.Builder(getActivity())
                    .setPositiveButton(android.R.string.ok, null)
                    .setTitle(getString(R.string.error))
                    .setMessage(message)
                    .show();
    }

    @Override
    public void setCatComboOptions(CategoryComboModel catCombo, List<CategoryOptionComboModel> catComboList) {

        if (catCombo.uid().equals(CategoryComboModel.DEFAULT_UID) || catComboList == null || catComboList.isEmpty()) {
            binding.catCombo.setVisibility(View.GONE);
            binding.catCombo.setVisibility(View.GONE);
        } else {
            binding.catCombo.setVisibility(View.VISIBLE);
            CatComboAdapter adapter = new CatComboAdapter(this,
                    R.layout.spinner_layout,
                    R.id.spinner_text,
                    catComboList,
                    catCombo.displayName(),
                    R.color.white_faf);

            binding.catCombo.setVisibility(View.VISIBLE);
            binding.catCombo.setAdapter(adapter);

            binding.catCombo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        presenter.clearCatComboFilters();
                    } else {
                        presenter.onCatComboSelected(adapter.getItem(position - 1));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

        }
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
    public void showHideFilter() {
        binding.filterLayout.setVisibility(binding.filterLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        checkFilterEnabled();
    }

    private void checkFilterEnabled() {
        if (binding.filterLayout.getVisibility() == View.VISIBLE) {
            binding.filter.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()));
            binding.filter.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        }
        // when filter layout is hidden
        else {
            // not applied period filter
            if (currentPeriod == Period.NONE && areAllOrgUnitsSelected()) {
                binding.filter.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()));
                binding.filter.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            }
            // applied period filter
            else {
                binding.filter.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()));
                binding.filter.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            }
        }
    }

    public boolean areAllOrgUnitsSelected() {
        return treeNode != null && treeNode.getChildren().size() == treeView.getSelected().size();
    }
}