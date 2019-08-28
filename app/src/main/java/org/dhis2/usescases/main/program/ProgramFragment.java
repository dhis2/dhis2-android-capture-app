package org.dhis2.usescases.main.program;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.dhis2.Components;
import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.FragmentProgramBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.usescases.main.MainContracts;
import org.dhis2.usescases.org_unit_selector.OUTreeActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DatePickerUtils;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.Period;
import org.dhis2.utils.custom_views.RxDateDialog;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;
import timber.log.Timber;

import static org.dhis2.utils.Period.DAILY;
import static org.dhis2.utils.Period.MONTHLY;
import static org.dhis2.utils.Period.NONE;
import static org.dhis2.utils.Period.WEEKLY;
import static org.dhis2.utils.Period.YEARLY;

/**
 * Created by ppajuelo on 18/10/2017.f
 */

public class ProgramFragment extends FragmentGlobalAbstract implements ProgramContract.View, OrgUnitInterface {

    private FragmentProgramBinding binding;
    @Inject
    ProgramContract.Presenter presenter;

    private Period currentPeriod = NONE;

    private AndroidTreeView treeView;

    private Date chosenDateDay = new Date();
    private ArrayList<Date> chosenDateWeek = new ArrayList<>();
    private ArrayList<Date> chosenDateMonth = new ArrayList<>();
    private ArrayList<Date> chosenDateYear = new ArrayList<>();
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMM-yyyy", Locale.getDefault());
    private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
    private TreeNode treeNode;
    private Context context;

    public FragmentProgramBinding getBinding() {
        return binding;
    }
    //-------------------------------------------
    //region LIFECYCLE


    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
        if (getActivity() != null)
            ((Components) getActivity().getApplicationContext()).userComponent()
                    .plus(new ProgramModule()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_program, container, false);
        binding.setPresenter(presenter);
        chosenDateWeek.add(new Date());
        chosenDateMonth.add(new Date());
        chosenDateYear.add(new Date());
        binding.programRecycler.setAdapter(new ProgramModelAdapter(presenter, currentPeriod));
        binding.programRecycler.addItemDecoration(new DividerItemDecoration(getAbstracContext(), DividerItemDecoration.VERTICAL));
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        binding.orgUnitApply.setOnClickListener(view -> apply());
        binding.orgUnitCancel.setOnClickListener(view -> {
            binding.drawerLayout.closeDrawer(GravityCompat.END);
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        });
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.init(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.dispose();
        binding.treeViewContainer.removeAllViews();
        treeView = null;
    }

    //endregion

    @SuppressLint({"CheckResult", "RxLeakedSubscription"})
    @Override
    public void showRageDatePicker() {
        if (isAdded() && getContext() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setMinimalDaysInFirstWeek(7);

            String week = getString(R.string.week);
            SimpleDateFormat weeklyFormat = new SimpleDateFormat("'" + week + "' w", Locale.getDefault());

            if (currentPeriod != DAILY && currentPeriod != NONE) {
                new RxDateDialog(getAbstractActivity(), currentPeriod).create().show().subscribe(selectedDates -> {
                    if (!selectedDates.val1().isEmpty()) {
                        String textToShow;
                        if (currentPeriod == WEEKLY) {
                            textToShow = weeklyFormat.format(selectedDates.val1().get(0)) + ", " + yearFormat.format(selectedDates.val1().get(0));
                            chosenDateWeek = (ArrayList<Date>) selectedDates.val1();
                            if (selectedDates.val1().size() > 1)
                                textToShow += "... " /*+ weeklyFormat.format(selectedDates.get(1))*/;
                        } else if (currentPeriod == MONTHLY) {
                            String dateFormatted = monthFormat.format(selectedDates.val1().get(0));
                            textToShow = dateFormatted.substring(0, 1).toUpperCase() + dateFormatted.substring(1);
                            chosenDateMonth = (ArrayList<Date>) selectedDates.val1();
                            if (selectedDates.val1().size() > 1)
                                textToShow += "... " /*+ monthFormat.format(selectedDates.get(1))*/;
                        } else {
                            textToShow = yearFormat.format(selectedDates.val1().get(0));
                            chosenDateYear = (ArrayList<Date>) selectedDates.val1();
                            if (selectedDates.val1().size() > 1)
                                textToShow += "... " /*+ yearFormat.format(selectedDates.get(1))*/;

                        }
                        binding.buttonPeriodText.setText(textToShow);
                        presenter.updateDateFilter(DateUtils.getInstance().getDatePeriodListFor(selectedDates.val1(), currentPeriod));

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
                                String dateFormatted = monthFormat.format(date.get(0));
                                text = dateFormatted.substring(0, 1).toUpperCase() + dateFormatted.substring(1);
                                chosenDateMonth = date;
                                break;
                            case YEARLY:
                                text = yearFormat.format(date.get(0));
                                chosenDateYear = date;
                                break;
                        }
                        binding.buttonPeriodText.setText(text);
                        presenter.updateDateFilter(DateUtils.getInstance().getDatePeriodListFor(selectedDates.val1(), currentPeriod));
                    }
                }, Timber::d);
            } else if (currentPeriod == DAILY) {
                showCustomCalendar(calendar);
            }
        }
    }

    private void showCustomCalendar(Calendar calendar) {

        DatePickerUtils.getDatePickerDialog(context, new DatePickerUtils.OnDatePickerClickListener() {
            @Override
            public void onNegativeClick() {

            }

            @Override
            public void onPositiveClick(DatePicker datePicker) {
                calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                Date[] dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), currentPeriod);
                ArrayList<Date> selectedDates = new ArrayList<>();
                selectedDates.add(dates[0]);
                presenter.updateDateFilter(DateUtils.getInstance().getDatePeriodListFor(selectedDates, currentPeriod));
                binding.buttonPeriodText.setText(DateUtils.getInstance().formatDate(dates[0]));
                chosenDateDay = dates[0];
            }
        }).show();
    }

    public Period getCurrentPeriod() {
        return currentPeriod;
    }

    public boolean areFiltersApplied() {
        return presenter.areFiltersApplied();
    }

    @Override
    public void showTimeUnitPicker() {

        if (isAdded() && getContext() != null) {
            Drawable drawable = null;
            String textToShow = "";

            switch (currentPeriod) {
                case NONE:
                    currentPeriod = DAILY;
                    drawable = AppCompatResources.getDrawable(context, R.drawable.ic_view_day);
                    break;
                case DAILY:
                    currentPeriod = WEEKLY;
                    drawable = AppCompatResources.getDrawable(context, R.drawable.ic_view_week);
                    break;
                case WEEKLY:
                    currentPeriod = MONTHLY;
                    drawable = AppCompatResources.getDrawable(context, R.drawable.ic_view_month);
                    break;
                case MONTHLY:
                    currentPeriod = YEARLY;
                    drawable = AppCompatResources.getDrawable(context, R.drawable.ic_view_year);
                    break;
                case YEARLY:
                    currentPeriod = NONE;
                    drawable = AppCompatResources.getDrawable(context, R.drawable.ic_view_none);
                    break;
            }
            if (binding.programRecycler.getAdapter() != null) {
                ((ProgramModelAdapter) binding.programRecycler.getAdapter()).setCurrentPeriod(currentPeriod);
            }
            binding.buttonTime.setImageDrawable(drawable);

            switch (currentPeriod) {
                case NONE:
                    presenter.updateDateFilter(new ArrayList<>());
                    textToShow = getString(R.string.period);
                    break;
                case DAILY:
                    ArrayList<Date> datesD = new ArrayList<>();
                    datesD.add(chosenDateDay);
                    if (!datesD.isEmpty())
                        textToShow = DateUtils.getInstance().formatDate(datesD.get(0));
                    if (!datesD.isEmpty() && datesD.size() > 1) textToShow += "... ";
                    presenter.updateDateFilter(DateUtils.getInstance().getDatePeriodListFor(datesD, currentPeriod));
                    break;
                case WEEKLY:
                    if (!chosenDateWeek.isEmpty()) {
                        String week = getString(R.string.week);
                        SimpleDateFormat weeklyFormat = new SimpleDateFormat("'" + week + "' w", Locale.getDefault());
                        textToShow = weeklyFormat.format(chosenDateWeek.get(0)) + ", " + yearFormat.format(chosenDateWeek.get(0));
                    }
                    if (!chosenDateWeek.isEmpty() && chosenDateWeek.size() > 1)
                        textToShow += "... ";
                    presenter.updateDateFilter(DateUtils.getInstance().getDatePeriodListFor(chosenDateWeek, currentPeriod));
                    break;
                case MONTHLY:
                    if (!chosenDateMonth.isEmpty()) {
                        String dateFormatted = monthFormat.format(chosenDateMonth.get(0));
                        textToShow = dateFormatted.substring(0, 1).toUpperCase() + dateFormatted.substring(1);
                    }
                    if (!chosenDateMonth.isEmpty() && chosenDateMonth.size() > 1)
                        textToShow += "... ";
                    presenter.updateDateFilter(DateUtils.getInstance().getDatePeriodListFor(chosenDateMonth, currentPeriod));
                    break;
                case YEARLY:
                    if (!chosenDateYear.isEmpty())
                        textToShow = yearFormat.format(chosenDateYear.get(0));
                    if (!chosenDateYear.isEmpty() && chosenDateYear.size() > 1)
                        textToShow += "... ";
                    presenter.updateDateFilter(DateUtils.getInstance().getDatePeriodListFor(chosenDateYear, currentPeriod));
                    break;
            }

            binding.buttonPeriodText.setText(textToShow);
        }
    }

    @Override
    public Consumer<List<ProgramViewModel>> swapProgramModelData() {
        return programs -> {
            binding.programProgress.setVisibility(View.GONE);
            binding.emptyView.setVisibility(programs.isEmpty() ? View.VISIBLE : View.GONE);
            ((ProgramModelAdapter) binding.programRecycler.getAdapter()).setData(programs);
        };
    }

    @Override
    public void renderError(String message) {
        if (isAdded() && getActivity() != null)
            new AlertDialog.Builder(getActivity())
                    .setPositiveButton(android.R.string.ok, null)
                    .setTitle(getString(R.string.error))
                    .setMessage(message)
                    .show();
    }

    @Override
    public void addTree(TreeNode treeNode) {
        if (isAdded() && getContext() != null) {
            this.treeNode = treeNode;
            binding.treeViewContainer.removeAllViews();

            binding.orgUnitAll.setOnClickListener(view -> {
                if (treeView != null) {
                    treeView.selectAll(false);
                    for (TreeNode node : treeView.getSelected()) {
                        ((OrgUnitHolder_2) node.getViewHolder()).check();
                    }
                }
            });

            binding.orgUnitUnselectAll.setOnClickListener(view -> {
                if (treeView != null) {
                    for (TreeNode node : treeView.getSelected()) {
                        ((OrgUnitHolder_2) node.getViewHolder()).uncheck();
                        ((OrgUnitHolder_2) node.getViewHolder()).update();
                    }
                    treeView.deselectAll();
                }
            });
            treeView = new AndroidTreeView(context, treeNode);

            treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
            treeView.setSelectionModeEnabled(true);
            treeView.setUseAutoToggle(false);

            binding.treeViewContainer.addView(treeView.getView());
            if (presenter.getOrgUnits().size() < 25)
                treeView.expandAll();

            treeView.setDefaultNodeClickListener((node, value) -> {
                if (isAdded()) {
                    if (treeView != null) {
                        if ((treeView.getSelected().size() == 1 && !node.isSelected()) || treeView.getSelected().size() > 1) {
                            binding.buttonOrgUnit.setText(String.format(getString(R.string.org_unit_filter), treeView.getSelected().size()));
                        }
                    }
                    if (node.getChildren().isEmpty())
                        presenter.onExpandOrgUnitNode(node, ((OrganisationUnit) node.getValue()).uid());
                    else
                        node.setExpanded(node.isExpanded());
                }
            });

            binding.buttonOrgUnit.setText(String.format(getString(R.string.org_unit_filter), treeView.getSelected().size()));
        }
    }


    public boolean areAllOrgUnitsSelected() {
        return treeNode != null && treeView != null && presenter.getOrgUnits().size() == treeView.getSelected().size();
    }

    @Override
    public void openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.END);
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
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
    public void orgUnitProgress(boolean showProgress) {
        binding.orgUnitProgress.setVisibility(showProgress ? View.VISIBLE : View.GONE);
    }

    @Override
    public Consumer<Pair<TreeNode, List<TreeNode>>> addNodeToTree() {
        return node -> {
            for (TreeNode childNode : node.val1())
                treeView.addNode(node.val0(), childNode);
            treeView.expandAll();
        };
    }

    @Override
    public void openOrgUnitTreeSelector() {
        Intent ouTreeIntent = new Intent(context, OUTreeActivity.class);
        ((MainActivity)context).startActivityForResult(ouTreeIntent, FilterManager.OU_TREE);
    }


    @Override
    public void apply() {
        if (isAdded() && getContext() != null) {
            if (treeView != null && !treeView.getSelected().isEmpty()) {
                binding.drawerLayout.closeDrawers();
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

                List<String> orgUnitsUids = new ArrayList<>();
                for (TreeNode treeNode : treeView.getSelected()) {
                    orgUnitsUids.add(((OrganisationUnit) treeNode.getValue()).uid());
                }

                if (treeView.getSelected().size() >= 1) {
                    binding.buttonOrgUnit.setText(String.format(getString(R.string.org_unit_filter), treeView.getSelected().size()));
                }

                presenter.updateOrgUnitFilter(orgUnitsUids);
                ((ProgramModelAdapter) binding.programRecycler.getAdapter()).setCurrentPeriod(currentPeriod);
            } else {
                displayMessage(getString(R.string.org_unit_selection_warning));
            }
        }
    }

    @Override
    public void setTutorial() {
        try {
            if (getContext() != null && isAdded()) {
                new Handler().postDelayed(() -> {
                    if (getAbstractActivity() != null) {
                        SparseBooleanArray stepCondition = new SparseBooleanArray();
                        stepCondition.put(7, binding.programRecycler.getAdapter().getItemCount() > 0);
                        HelpManager.getInstance().show(getAbstractActivity(), HelpManager.TutorialName.PROGRAM_FRAGMENT, stepCondition);
                    }

                }, 500);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public void openFilter(boolean open) {
        binding.filter.setVisibility(open ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showHideFilter(){
        ((MainActivity)getActivity()).showHideFilter();
    }

    @Override
    public void clearFilters() {
        ((MainActivity)getActivity()).getAdapter().notifyDataSetChanged();
    }
}