package org.dhis2.usescases.programEventDetail;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.android.flexbox.FlexboxLayout;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ActivityProgramEventDetailBinding;
import org.dhis2.databinding.CatCombFilterBinding;
import org.dhis2.databinding.WidgetDatepickerBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.main.program.OrgUnitHolder;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.Period;
import org.dhis2.utils.custom_views.RxDateDialog;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.Program;

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

import static org.dhis2.R.layout.activity_program_event_detail;
import static org.dhis2.utils.Period.DAILY;
import static org.dhis2.utils.Period.MONTHLY;
import static org.dhis2.utils.Period.NONE;
import static org.dhis2.utils.Period.WEEKLY;
import static org.dhis2.utils.Period.YEARLY;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */

public class ProgramEventDetailActivity extends ActivityGlobalAbstract implements ProgramEventDetailContract.View {

    private ActivityProgramEventDetailBinding binding;

    @Inject
    ProgramEventDetailContract.Presenter presenter;

    @Inject
    ProgramEventDetailAdapter adapter;
    private Period currentPeriod = Period.NONE;

    private Date chosenDateDay = new Date();
    private ArrayList<Date> chosenDateWeek = new ArrayList<>();
    private ArrayList<Date> chosenDateMonth = new ArrayList<>();
    private ArrayList<Date> chosenDateYear = new ArrayList<>();
    SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
    private AndroidTreeView treeView;
    private TreeNode treeNode;
    private boolean isFilteredByCatCombo = false;
    private ProgramEventDetailLiveAdapter liveAdapter;
    private Map<String, CategoryOption> catCombFilter;

    public static Bundle getBundle(String programUid, String period, List<Date> dates) {
        Bundle bundle = new Bundle();
        bundle.putString("PROGRAM_UID", programUid);
        bundle.putString("CURRENT_PERIOD", period);
        bundle.putSerializable("DATES", (ArrayList) dates);
        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new ProgramEventDetailModule(getIntent().getStringExtra("PROGRAM_UID"))).inject(this);
        super.onCreate(savedInstanceState);
        catCombFilter = new HashMap<>();
        currentPeriod = Period.valueOf(getIntent().getStringExtra("CURRENT_PERIOD"));

        chosenDateWeek.add(new Date());
        chosenDateMonth.add(new Date());
        chosenDateYear.add(new Date());

        binding = DataBindingUtil.setContentView(this, activity_program_event_detail);

        binding.setPresenter(presenter);

        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        liveAdapter = new ProgramEventDetailLiveAdapter(presenter);
        binding.recycler.setAdapter(liveAdapter);
        binding.recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.clearData();
        presenter.init(this, currentPeriod);
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
        binding.treeViewContainer.removeAllViews();
    }

    @Override
    public void setProgram(Program program) {
        binding.setName(program.displayName());
    }

    @Override
    public void openDrawer() {
        if (!binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.openDrawer(GravityCompat.END);
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        } else {
            binding.drawerLayout.closeDrawer(GravityCompat.END);
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    @SuppressLint({"CheckResult", "RxLeakedSubscription"})
    @Override
    public void showRageDatePicker() {
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
                                    text = monthFormat.format(date.get(0));
                                    chosenDateMonth = date;
                                    break;
                                case YEARLY:
                                    text = yearFormat.format(date.get(0));
                                    chosenDateYear = date;
                                    break;
                                default:
                                    break;
                            }
                            binding.buttonPeriodText.setText(text);
                            presenter.updateDateFilter(DateUtils.getInstance().getDatePeriodListFor(selectedDates, currentPeriod));
                        }
                    },
                    Timber::d);
        } else if (currentPeriod == DAILY) {
            showCustomCalendar(calendar);
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
//        View datePickerView = layoutInflater.inflate(R.layout.widget_datepicker, null);
        WidgetDatepickerBinding widgetBinding = WidgetDatepickerBinding.inflate(layoutInflater);
        final DatePicker datePicker = widgetBinding.widgetDatepicker;

        Calendar c = Calendar.getInstance();
        datePicker.updateDate(
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), R.style.DatePickerTheme);
            /*    .setPositiveButton(R.string.action_accept, (dialog, which) -> {
                    calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                    Date[] dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), currentPeriod);
                    ArrayList<Date> selectedDates = new ArrayList<>();
                    selectedDates.add(dates[0]);

                    presenter.updateDateFilter(DateUtils.getInstance().getDatePeriodListFor(selectedDates, currentPeriod));
                    binding.buttonPeriodText.setText(DateUtils.getInstance().formatDate(dates[0]));
                    chosenDateDay = dates[0];
                })
                .setNeutralButton(getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> showNativeCalendar(calendar));*/

        alertDialog.setView(widgetBinding.getRoot());
        Dialog dialog = alertDialog.create();

        widgetBinding.changeCalendarButton.setOnClickListener(calendarButton -> {
            showNativeCalendar(calendar);
            dialog.dismiss();
        });
        widgetBinding.clearButton.setOnClickListener(clearButton -> dialog.dismiss());
        widgetBinding.acceptButton.setOnClickListener(acceptButton -> {
            calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            Date[] dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), currentPeriod);
            ArrayList<Date> selectedDates = new ArrayList<>();
            selectedDates.add(dates[0]);

            presenter.updateDateFilter(DateUtils.getInstance().getDatePeriodListFor(selectedDates, currentPeriod));
            binding.buttonPeriodText.setText(DateUtils.getInstance().formatDate(dates[0]));
            chosenDateDay = dates[0];
            dialog.dismiss();
        });

        dialog.show();
    }


    @Override
    public void showTimeUnitPicker() {

        Drawable drawable = null;
        String textToShow = "";

        switch (currentPeriod) {
            case NONE:
                currentPeriod = DAILY;
                drawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_view_day);
                break;
            case DAILY:
                currentPeriod = WEEKLY;
                drawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_view_week);
                break;
            case WEEKLY:
                currentPeriod = MONTHLY;
                drawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_view_month);
                break;
            case MONTHLY:
                currentPeriod = YEARLY;
                drawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_view_year);
                break;
            case YEARLY:
                currentPeriod = NONE;
                drawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_view_none);
                break;
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
                if (!chosenDateMonth.isEmpty() && chosenDateMonth.size() > 1) textToShow += "... ";

                presenter.updateDateFilter(DateUtils.getInstance().getDatePeriodListFor(chosenDateMonth, currentPeriod));
                break;
            case YEARLY:
                if (!chosenDateYear.isEmpty())
                    textToShow = yearFormat.format(chosenDateYear.get(0));
                if (!chosenDateYear.isEmpty() && chosenDateYear.size() > 1) textToShow += "... ";

                presenter.updateDateFilter(DateUtils.getInstance().getDatePeriodListFor(chosenDateYear, currentPeriod));
                break;
        }

        binding.buttonPeriodText.setText(textToShow);
    }

    @Override
    public void addTree(TreeNode treeNode) {
        this.treeNode = treeNode;
        binding.treeViewContainer.removeAllViews();
        binding.orgUnitApply.setOnClickListener(view -> apply());
        binding.orgUnitCancel.setOnClickListener(view -> {
            binding.drawerLayout.closeDrawer(GravityCompat.END);
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        });
        binding.orgUnitAll.setOnClickListener(view -> {
            treeView.selectAll(false);
            for (TreeNode node : treeView.getSelected()) {
                ((OrgUnitHolder) node.getViewHolder()).check();
            }
        });

        binding.orgUnitUnselectAll.setOnClickListener(view -> {
            for (TreeNode node : treeView.getSelected()) {
                ((OrgUnitHolder) node.getViewHolder()).uncheck();
                ((OrgUnitHolder) node.getViewHolder()).update();
            }
            treeView.deselectAll();
            binding.buttonOrgUnit.setText(String.format(getString(R.string.org_unit_filter), treeView.getSelected().size()));

        });
        treeView = new AndroidTreeView(getContext(), treeNode);

        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
        treeView.setSelectionModeEnabled(true);
        treeView.setUseAutoToggle(false);

        binding.treeViewContainer.addView(treeView.getView());
        if (presenter.getOrgUnits().size() < 25)
            treeView.expandAll();

        treeView.setDefaultNodeClickListener((node, value) -> {
            if (treeView.getSelected().size() == 1 && !node.isSelected()) {
                binding.buttonOrgUnit.setText(String.format(getString(R.string.org_unit_filter), treeView.getSelected().size()));
            } else if (treeView.getSelected().size() > 1) {
                binding.buttonOrgUnit.setText(String.format(getString(R.string.org_unit_filter), treeView.getSelected().size()));

                if (node.getChildren().isEmpty())
                    presenter.onExpandOrgUnitNode(node, ((OrganisationUnitModel) node.getValue()).uid());
                else
                    node.setExpanded(node.isExpanded());
            }
        });

        binding.buttonOrgUnit.setText(String.format(getString(R.string.org_unit_filter), treeView.getSelected().size()));
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
    public void setLiveData(LiveData<PagedList<ProgramEventViewModel>> pagedListLiveData) {
        pagedListLiveData.observe(this, pagedList -> {
            binding.programProgress.setVisibility(View.GONE);
            liveAdapter.submitList(pagedList, () -> {
                if (binding.recycler.getAdapter() != null && binding.recycler.getAdapter().getItemCount() == 0) {
                    binding.emptyTeis.setVisibility(View.VISIBLE);
                } else {
                    binding.emptyTeis.setVisibility(View.GONE);
                }
            });

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
    public void setCatComboOptions(List<Category> categories) {
        if (binding.filterLayout.getChildCount() > 2)
            binding.filterLayout.removeViews(2, binding.filterLayout.getChildCount() - 1);
        if (categories != null && !categories.isEmpty()) {
            for (Category category : categories) {
                CatCombFilterBinding catCombFilterBinding = CatCombFilterBinding.inflate(LayoutInflater.from(this));
                PopupMenu menu = new PopupMenu(catCombFilterBinding.catCombo.getContext(), catCombFilterBinding.catCombo, Gravity.BOTTOM);
                menu.getMenu().add(Menu.NONE, Menu.NONE, 0, category.displayName());
                for (CategoryOption catOption : category.categoryOptions())
                    menu.getMenu().add(Menu.NONE, Menu.NONE, category.categoryOptions().indexOf(catOption) + 1, catOption.displayName());
                catCombFilterBinding.catCombo.setOnClickListener(view -> menu.show());
                menu.setOnMenuItemClickListener(item -> {
                    int position = item.getOrder();
                    if (position == 0) {
                        catCombFilter.remove(category.uid());
                        isFilteredByCatCombo = !catCombFilter.isEmpty();
                        presenter.updateCatOptCombFilter(new ArrayList<>(catCombFilter.values()));
                        catCombFilterBinding.catCombo.setText(category.displayName());
                    } else {
                        CategoryOption categoryOption = category.categoryOptions().get(position - 1);
                        isFilteredByCatCombo = true;
                        catCombFilter.put(category.uid(), categoryOption);
                        presenter.updateCatOptCombFilter(new ArrayList<>(catCombFilter.values()));
                        catCombFilterBinding.catCombo.setText(categoryOption.displayName());
                    }
                    return false;
                });

                FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()));
                lp.setFlexBasisPercent(50f);
                lp.setMargins(0, 10, 5, 0);
                catCombFilterBinding.getRoot().setLayoutParams(lp);
                catCombFilterBinding.catCombo.setText(category.displayName());
                binding.filterLayout.addView(catCombFilterBinding.getRoot());

            }
        }
    }

    @Override
    public void showHideFilter() {
        binding.filterLayout.setVisibility(binding.filterLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        checkFilterEnabled();
    }

    private void checkFilterEnabled() {
        if (binding.filterLayout.getVisibility() == View.VISIBLE) {
            binding.filter.setBackgroundColor(getPrimaryColor());
            binding.filter.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            binding.filter.setBackgroundResource(0);
        }
        // when filter layout is hidden
        else {
            // not applied period filter
            if (currentPeriod == Period.NONE && areAllOrgUnitsSelected() && !isFilteredByCatCombo) {
                binding.filter.setBackgroundColor(getPrimaryColor());
                binding.filter.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
                binding.filter.setBackgroundResource(0);
            }
            // applied period filter
            else {
                binding.filter.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()));
                binding.filter.setColorFilter(getPrimaryColor(), PorterDuff.Mode.SRC_IN);
                binding.filter.setBackgroundResource(R.drawable.white_circle);
            }
        }
    }

    public boolean areAllOrgUnitsSelected() {
        return treeNode != null && treeNode.getChildren().size() == treeView.getSelected().size();
    }

    @Override
    public void apply() {
        if (treeView != null && !treeView.getSelected().isEmpty()) {
            binding.drawerLayout.closeDrawers();
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            List<String> orgUnitsUids = new ArrayList<>();
            for (TreeNode treeNode : treeView.getSelected()) {
                orgUnitsUids.add(((OrganisationUnitModel) treeNode.getValue()).uid());
            }

            if (treeView.getSelected().size() >= 1) {
                binding.buttonOrgUnit.setText(String.format(getString(R.string.org_unit_filter), treeView.getSelected().size()));
            }
            presenter.updateOrgUnitFilter(orgUnitsUids);

        } else
            displayMessage(getString(R.string.org_unit_selection_warning));
    }

    @Override
    public void setWritePermission(Boolean canWrite) {
        binding.addEventButton.setVisibility(canWrite ? View.VISIBLE : View.GONE);
        if (binding.addEventButton.getVisibility() == View.VISIBLE) {
            binding.emptyTeis.setText(R.string.empty_tei_add);
        } else {
            binding.emptyTeis.setText(R.string.empty_tei_no_add);
        }
    }

    @Override
    public void setTutorial() {
        new Handler().postDelayed(() -> {
            SparseBooleanArray stepConditions = new SparseBooleanArray();
            stepConditions.put(2, findViewById(R.id.addEventButton).getVisibility() == View.VISIBLE);
            HelpManager.getInstance().show(getActivity(), HelpManager.TutorialName.PROGRAM_EVENT_LIST,
                    stepConditions);

        }, 500);
    }

    @Override
    public void orgUnitProgress(boolean showProgress) {
        binding.orgUnitProgress.setVisibility(showProgress ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showTutorial(boolean shaked) {
        setTutorial();
    }
}