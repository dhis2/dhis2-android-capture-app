package org.dhis2.usescases.main.program;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.dhis2.BuildConfig;
import org.dhis2.Components;
import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.FragmentProgramBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.Period;
import org.dhis2.utils.custom_views.RxDateDialog;
import org.hisp.dhis.android.core.constant.Constant;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;
import me.toptas.fancyshowcase.DismissListener;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;
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
                    if (!selectedDates.isEmpty()) {
                        String textToShow;
                        if (currentPeriod == WEEKLY) {
                            textToShow = weeklyFormat.format(selectedDates.get(0)) + ", " + yearFormat.format(selectedDates.get(0));
                            chosenDateWeek = (ArrayList<Date>) selectedDates;
                            if (selectedDates.size() > 1)
                                textToShow += "... " /*+ weeklyFormat.format(selectedDates.get(1))*/;
                        } else if (currentPeriod == MONTHLY) {
                            String dateFormatted = monthFormat.format(selectedDates.get(0));
                            textToShow = dateFormatted.substring(0, 1).toUpperCase() + dateFormatted.substring(1);
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
                        presenter.updateDateFilter(DateUtils.getInstance().getDatePeriodListFor(selectedDates, currentPeriod));

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
                        presenter.updateDateFilter(DateUtils.getInstance().getDatePeriodListFor(selectedDates, currentPeriod));
                    }
                }, Timber::d);
            } else if (currentPeriod == DAILY) {
                showCustomCalendar(calendar);
            }
        }
    }

    private void showNativeCalendar(Calendar calendar) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(chosenDateDay);
        DatePickerDialog pickerDialog;
        pickerDialog = new DatePickerDialog(getContext(), (datePicker, year, monthOfYear, dayOfMonth) -> {
            calendar.set(year, monthOfYear, dayOfMonth);
            Date[] dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), currentPeriod);
            ArrayList<Date> selectedDates = new ArrayList<>();
            selectedDates.add(dates[0]);
            presenter.updateDateFilter(DateUtils.getInstance().getDatePeriodListFor(selectedDates, currentPeriod));
            binding.buttonPeriodText.setText(DateUtils.getInstance().formatDate(dates[0]));
            chosenDateDay = dates[0];
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            pickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> {
                pickerDialog.dismiss();
                showCustomCalendar(calendar);
            });
        }

        pickerDialog.show();
    }

    private void showCustomCalendar(Calendar calendar) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View datePickerView = layoutInflater.inflate(R.layout.widget_datepicker, null);
        final DatePicker datePicker = datePickerView.findViewById(R.id.widget_datepicker);

        Calendar c = Calendar.getInstance();
        datePicker.updateDate(
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), R.style.DatePickerTheme)
                .setPositiveButton(R.string.action_accept, (dialog, which) -> {
                    calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                    Date[] dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), currentPeriod);
                    ArrayList<Date> selectedDates = new ArrayList<>();
                    selectedDates.add(dates[0]);
                    presenter.updateDateFilter(DateUtils.getInstance().getDatePeriodListFor(selectedDates, currentPeriod));
                    binding.buttonPeriodText.setText(DateUtils.getInstance().formatDate(dates[0]));
                    chosenDateDay = dates[0];
                })
                .setNeutralButton(getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> showNativeCalendar(calendar));

        alertDialog.setView(datePickerView);
        Dialog dialog = alertDialog.create();
        dialog.show();
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
                    drawable = ContextCompat.getDrawable(context, R.drawable.ic_view_day);
                    break;
                case DAILY:
                    currentPeriod = WEEKLY;
                    drawable = ContextCompat.getDrawable(context, R.drawable.ic_view_week);
                    break;
                case WEEKLY:
                    currentPeriod = MONTHLY;
                    drawable = ContextCompat.getDrawable(context, R.drawable.ic_view_month);
                    break;
                case MONTHLY:
                    currentPeriod = YEARLY;
                    drawable = ContextCompat.getDrawable(context, R.drawable.ic_view_year);
                    break;
                case YEARLY:
                    currentPeriod = NONE;
                    drawable = ContextCompat.getDrawable(context, R.drawable.ic_view_none);
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

            setTutorial();
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
        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        try {

            if (getContext() != null && isAdded() && getAbstractActivity() != null) {
                new Handler().postDelayed(() -> {
                    FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(getAbstractActivity())
                            .title(getString(R.string.tuto_main_1))
                            .closeOnTouch(true)
                            .build();
                    FancyShowCaseView tuto2 = new FancyShowCaseView.Builder(getAbstractActivity())
                            .title(getString(R.string.tuto_main_2))
                            .closeOnTouch(true)
                            .build();

                    FancyShowCaseView tuto3 = new FancyShowCaseView.Builder(getAbstractActivity())
                            .title(getString(R.string.tuto_main_3))
                            .focusOn(getAbstractActivity().findViewById(R.id.filter))
                            .closeOnTouch(true)
                            .dismissListener(new DismissListener() {
                                @Override
                                public void onDismiss(String id) {
                                    if (getAbstractActivity() != null &&
                                            getAbstractActivity().findViewById(R.id.filter_layout) != null &&
                                            getAbstractActivity().findViewById(R.id.filter_layout).getVisibility() == View.GONE)
                                        getAbstractActivity().findViewById(R.id.filter).performClick();
                                }

                                @Override
                                public void onSkipped(String id) {
                                    // do nothing
                                }
                            })
                            .build();

                    FancyShowCaseView tuto4 = new FancyShowCaseView.Builder(getAbstractActivity())
                            .title(getString(R.string.tuto_main_4))
                            .focusOn(binding.periodLayout)
                            .focusShape(FocusShape.ROUNDED_RECTANGLE)
                            .closeOnTouch(true)
                            .build();

                    FancyShowCaseView tuto5 = new FancyShowCaseView.Builder(getAbstractActivity())
                            .title(getString(R.string.tuto_main_5))
                            .focusOn(binding.buttonOrgUnit)
                            .focusShape(FocusShape.ROUNDED_RECTANGLE)
                            .closeOnTouch(true)
                            .build();

                    FancyShowCaseView tuto6 = new FancyShowCaseView.Builder(getAbstractActivity())
                            .title(getString(R.string.tuto_main_6))
                            .focusOn(getAbstractActivity().findViewById(R.id.menu))
                            .closeOnTouch(true)
                            .dismissListener(new DismissListener() {
                                @Override
                                public void onDismiss(String id) {
                                    // do nothing
                                }

                                @Override
                                public void onSkipped(String id) {
                                    // do nothing
                                }
                            })
                            .build();

                    ArrayList<FancyShowCaseView> steps = new ArrayList<>();
                    steps.add(tuto1);
                    steps.add(tuto2);
                    steps.add(tuto3);
                    steps.add(tuto4);
                    steps.add(tuto5);
                    steps.add(tuto6);

                    if (binding.programRecycler.getAdapter().getItemCount() > 0) {
                        FancyShowCaseView tuto11 = new FancyShowCaseView.Builder(getAbstractActivity())
                                .title(getString(R.string.tuto_main_11))
                                .focusOn(getAbstractActivity().findViewById(R.id.sync_status))
                                .closeOnTouch(true)
                                .build();
                        steps.add(tuto11);
                    }

                    HelpManager.getInstance().setScreenHelp(getClass().getName(), steps);

                    if (!prefs.getBoolean(Constants.TUTORIAL_HOME,false) && !BuildConfig.DEBUG) {
                        HelpManager.getInstance().showHelp();
                        prefs.edit().putBoolean(Constants.TUTORIAL_HOME, true).apply();
                    }

                }, 500);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}