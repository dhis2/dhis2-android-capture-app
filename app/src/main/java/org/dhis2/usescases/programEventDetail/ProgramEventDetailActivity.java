package org.dhis2.usescases.programEventDetail;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.dhis2.App;
import org.dhis2.BuildConfig;
import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ActivityProgramEventDetailBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.main.program.OrgUnitHolder;
import org.dhis2.utils.CatComboAdapter;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.EndlessRecyclerViewScrollListener;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.Period;
import org.dhis2.utils.custom_views.RxDateDialog;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import me.toptas.fancyshowcase.FancyShowCaseView;
import timber.log.Timber;

import static org.dhis2.utils.DateUtils.DATE_FORMAT_MONTH2;
import static org.dhis2.utils.DateUtils.DATE_FORMAT_YEAR;
import static org.dhis2.utils.Period.DAILY;
import static org.dhis2.utils.Period.MONTHLY;
import static org.dhis2.utils.Period.NONE;
import static org.dhis2.utils.Period.WEEKLY;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class ProgramEventDetailActivity extends ActivityGlobalAbstract implements ProgramEventDetailContract.ProgramEventDetailView {

    private ActivityProgramEventDetailBinding binding;

    @Inject
    ProgramEventDetailContract.ProgramEventDetailPresenter presenter;

    @Inject
    ProgramEventDetailAdapter adapter;
    private Period currentPeriod = Period.NONE;

    private Date chosenDateDay = new Date();
    private ArrayList<Date> chosenDateWeek = new ArrayList<>();
    private ArrayList<Date> chosenDateMonth = new ArrayList<>();
    private ArrayList<Date> chosenDateYear = new ArrayList<>();
    SimpleDateFormat monthFormat = new SimpleDateFormat(DATE_FORMAT_MONTH2, Locale.getDefault());
    SimpleDateFormat yearFormat = new SimpleDateFormat(DATE_FORMAT_YEAR, Locale.getDefault());
    private AndroidTreeView treeView;
    private TreeNode treeNode;
    private StringBuilder orgUnitFilter = new StringBuilder();
    private boolean isFilteredByCatCombo = false;
    private String programId;
    private PublishProcessor<Integer> pageProcessor;
    private EndlessRecyclerViewScrollListener endlessScrollListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new ProgramEventDetailModule()).inject(this);

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_program_event_detail);

        chosenDateWeek.add(new Date());
        chosenDateMonth.add(new Date());
        chosenDateYear.add(new Date());

        programId = getIntent().getStringExtra("PROGRAM_UID");
        binding.setPresenter(presenter);

        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        pageProcessor = PublishProcessor.create();

        endlessScrollListener = new EndlessRecyclerViewScrollListener(binding.recycler.getLayoutManager(), 2, 0) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                pageProcessor.onNext(page);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.clearData();
        presenter.init(this, programId, currentPeriod);
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
        binding.treeViewContainer.removeAllViews();
    }

    @Override
    public void setData(List<ProgramEventViewModel> events) {
        if (binding.recycler.getAdapter() == null) {
            binding.recycler.setAdapter(adapter);
            binding.recycler.addOnScrollListener(endlessScrollListener);
            binding.recycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        }
        adapter.setEvents(events, endlessScrollListener.getCurrentPage());

        if (!HelpManager.getInstance().isTutorialReadyForScreen(getClass().getName()))
            setTutorial();
    }


    @Override
    public void setProgram(ProgramModel program) {
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

    private void setSelectedDates(SimpleDateFormat weeklyFormat, List<Date> selectedDates) {
        if (currentPeriod == WEEKLY) {
            chosenDateWeek = (ArrayList<Date>) selectedDates;
        } else if (currentPeriod == MONTHLY) {
            chosenDateMonth = (ArrayList<Date>) selectedDates;
        } else {
            chosenDateYear = (ArrayList<Date>) selectedDates;
        }
        binding.buttonPeriodText.setText(DateUtils.setSelectedDatesTextToShow(currentPeriod, selectedDates, weeklyFormat));

        presenter.setFilters(selectedDates, currentPeriod, orgUnitFilter.toString());
        endlessScrollListener.resetState(0);
        pageProcessor.onNext(0);
    }

    @SuppressWarnings("common-java:DuplicatedBlocks")
    private void setNoSelectedDates(SimpleDateFormat weeklyFormat) {
        ArrayList<Date> date = new ArrayList<>();
        date.add(new Date());

        switch (currentPeriod) {
            case WEEKLY:
                chosenDateWeek = date;
                break;
            case MONTHLY:
                chosenDateMonth = date;
                break;
            case YEARLY:
                chosenDateYear = date;
                break;
            default:
                break;
        }

        binding.buttonPeriodText.setText(DateUtils.getNotSelectedDatesText(currentPeriod, weeklyFormat));
        presenter.setFilters(date, currentPeriod, orgUnitFilter.toString());
        endlessScrollListener.resetState(0);
        pageProcessor.onNext(0);
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
                            setSelectedDates(weeklyFormat, selectedDates);

                        } else {
                            setNoSelectedDates(weeklyFormat);
                        }
                    },
                    Timber::d);
        } else if (currentPeriod == DAILY) {
            setCurrentPeriodDaily(calendar);
        }
    }

    private void setCurrentPeriodDaily(Calendar calendar) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(chosenDateDay);
        DatePickerDialog pickerDialog;
        pickerDialog = new DatePickerDialog(getContext(), (datePicker, year, monthOfYear, dayOfMonth) -> {
            calendar.set(year, monthOfYear, dayOfMonth);
            Date[] dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), currentPeriod);
            ArrayList<Date> day = new ArrayList<>();
            day.add(dates[0]);

            presenter.setFilters(day, currentPeriod, orgUnitFilter.toString());
            endlessScrollListener.resetState(0);
            pageProcessor.onNext(0);
            binding.buttonPeriodText.setText(DateUtils.getInstance().formatDate(dates[0]));
            chosenDateDay = dates[0];
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        pickerDialog.show();
    }

    private Drawable getTimeUnitDrawable() {
        currentPeriod = DateUtils.getCurrentPeriod(currentPeriod);
        return DateUtils.getCurrentPeriodDrawable(getContext(), currentPeriod);
    }

    private String getDailyTextToShow() {
        String textToShow = "";
        ArrayList<Date> datesD = new ArrayList<>();
        datesD.add(chosenDateDay);
        if (!datesD.isEmpty())
            textToShow = DateUtils.getInstance().formatDate(datesD.get(0));
        if (!datesD.isEmpty() && datesD.size() > 1) {
            textToShow += "... ";
        }
        presenter.setFilters(datesD, currentPeriod, orgUnitFilter.toString());
        endlessScrollListener.resetState(0);
        pageProcessor.onNext(0);
        return textToShow;
    }

    private String getWeeklyTextToShow() {
        String textToShow = "";
        if (!chosenDateWeek.isEmpty()) {
            String week = getString(R.string.week);
            SimpleDateFormat weeklyFormat = new SimpleDateFormat("'" + week + "' w", Locale.getDefault());
            textToShow = weeklyFormat.format(chosenDateWeek.get(0)) + ", " + yearFormat.format(chosenDateWeek.get(0));
        }
        if (!chosenDateWeek.isEmpty() && chosenDateWeek.size() > 1) {
            textToShow += "... ";
        }

        presenter.setFilters(chosenDateWeek, currentPeriod, orgUnitFilter.toString());
        endlessScrollListener.resetState(0);
        pageProcessor.onNext(0);
        return textToShow;
    }

    private String getMonthlyTextToShow() {
        String textToShow = "";
        if (!chosenDateMonth.isEmpty()) {
            String dateFormatted = monthFormat.format(chosenDateMonth.get(0));
            textToShow = dateFormatted.substring(0, 1).toUpperCase() + dateFormatted.substring(1);
        }
        if (!chosenDateMonth.isEmpty() && chosenDateMonth.size() > 1) textToShow += "... ";

        presenter.setFilters(chosenDateMonth, currentPeriod, orgUnitFilter.toString());
        endlessScrollListener.resetState(0);
        pageProcessor.onNext(0);
        return textToShow;
    }

    private String getYearlyTextToShow() {
        String textToShow = "";
        if (!chosenDateYear.isEmpty())
            textToShow = yearFormat.format(chosenDateYear.get(0));
        if (!chosenDateYear.isEmpty() && chosenDateYear.size() > 1) textToShow += "... ";

        presenter.setFilters(chosenDateYear, currentPeriod, orgUnitFilter.toString());
        endlessScrollListener.resetState(0);
        pageProcessor.onNext(0);
        return textToShow;
    }

    private String getTimeUnitTextToShow() {
        String textToShow = "";
        switch (currentPeriod) {
            case NONE:
                presenter.setFilters(null, currentPeriod, orgUnitFilter.toString());
                endlessScrollListener.resetState(0);
                pageProcessor.onNext(0);
                textToShow = getString(R.string.period);
                break;
            case DAILY:
                textToShow = getDailyTextToShow();
                break;
            case WEEKLY:
                textToShow = getWeeklyTextToShow();
                break;
            case MONTHLY:
                textToShow = getMonthlyTextToShow();
                break;
            case YEARLY:
                textToShow = getYearlyTextToShow();
                break;
        }
        return textToShow;
    }


    @Override
    public void showTimeUnitPicker() {

        Drawable drawable = getTimeUnitDrawable();
        String textToShow = getTimeUnitTextToShow();

        binding.buttonTime.setImageDrawable(drawable);
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
            if ((treeView.getSelected().size() == 1 && !node.isSelected()) ||
                    (treeView.getSelected().size() > 1)) {
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
        ArrayList<CategoryOptionComboModel> catComboListFinal = new ArrayList<>();
        if (catComboList != null) {
            for (CategoryOptionComboModel categoryOptionComboModel : catComboList) {
                if (!"default".equals(categoryOptionComboModel.displayName()) && !categoryOptionComboModel.uid().equals(CategoryComboModel.DEFAULT_UID)) {
                    catComboListFinal.add(categoryOptionComboModel);
                }
            }
        }

        if (catCombo.isDefault() || "default".equals(catCombo.displayName()) || catCombo.uid().equals(CategoryComboModel.DEFAULT_UID) || catComboListFinal.isEmpty()) {
            binding.catCombo.setVisibility(View.GONE);
        } else {
            binding.catCombo.setVisibility(View.VISIBLE);
            CatComboAdapter catComboAdapter = new CatComboAdapter(this,
                    R.layout.spinner_layout,
                    R.id.spinner_text,
                    catComboListFinal,
                    catCombo.displayName(),
                    R.color.white_faf);

            binding.catCombo.setVisibility(View.VISIBLE);
            binding.catCombo.setAdapter(catComboAdapter);

            binding.catCombo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        isFilteredByCatCombo = false;
                        presenter.clearCatComboFilters();
                        endlessScrollListener.resetState();
                        pageProcessor.onNext(0);
                    } else {
                        isFilteredByCatCombo = true;
                        presenter.onCatComboSelected(catComboAdapter.getItem(position - 1));
                        endlessScrollListener.resetState();
                        pageProcessor.onNext(0);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // do nothing
                }
            });
        }
    }

    @Override
    public void showHideFilter() {
        binding.filterLayout.setVisibility(binding.filterLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        checkFilterEnabled(binding.filterLayout, binding.filter, currentPeriod, isFilteredByCatCombo, areAllOrgUnitsSelected(treeView, treeNode));
    }

    @Override
    public void apply() {
        binding.drawerLayout.closeDrawers();
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        orgUnitFilter = new StringBuilder();
        for (int i = 0; i < treeView.getSelected().size(); i++) {
            orgUnitFilter.append("'");
            orgUnitFilter.append(((OrganisationUnitModel) treeView.getSelected().get(i).getValue()).uid());
            orgUnitFilter.append("'");
            if (i < treeView.getSelected().size() - 1)
                orgUnitFilter.append(", ");
        }

        binding.buttonOrgUnit.setText(String.format(getString(R.string.org_unit_filter), treeView.getSelected().size()));

        switch (currentPeriod) {
            case NONE:
                presenter.setFilters(null, currentPeriod, orgUnitFilter.toString());
                endlessScrollListener.resetState(0);
                pageProcessor.onNext(0);
                break;
            case DAILY:
                ArrayList<Date> datesD = new ArrayList<>();
                datesD.add(chosenDateDay);
                presenter.setFilters(datesD, currentPeriod, orgUnitFilter.toString());
                endlessScrollListener.resetState(0);
                pageProcessor.onNext(0);
                break;
            case WEEKLY:
                presenter.setFilters(chosenDateWeek, currentPeriod, orgUnitFilter.toString());
                endlessScrollListener.resetState(0);
                pageProcessor.onNext(0);
                break;
            case MONTHLY:
                presenter.setFilters(chosenDateMonth, currentPeriod, orgUnitFilter.toString());
                endlessScrollListener.resetState(0);
                pageProcessor.onNext(0);
                break;
            case YEARLY:
                presenter.setFilters(chosenDateYear, currentPeriod, orgUnitFilter.toString());
                endlessScrollListener.resetState(0);
                pageProcessor.onNext(0);
                break;
        }
    }

    @Override
    public void setWritePermission(Boolean canWrite) {
        binding.addEventButton.setVisibility(canWrite ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setTutorial() {
        super.setTutorial();


        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);

        new Handler().postDelayed(() -> {
            FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .title(getString(R.string.tuto_program_event_1))
                    .closeOnTouch(true)
                    .build();
            FancyShowCaseView tuto2 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .title(getString(R.string.tuto_program_event_2))
                    .focusOn(getAbstractActivity().findViewById(R.id.addEventButton))
                    .closeOnTouch(true)
                    .build();


            ArrayList<FancyShowCaseView> steps = new ArrayList<>();
            steps.add(tuto1);
            steps.add(tuto2);

            HelpManager.getInstance().setScreenHelp(getClass().getName(), steps);

            if (!prefs.getBoolean("TUTO_PROGRAM_EVENT", false) && !BuildConfig.DEBUG) {
                HelpManager.getInstance().showHelp();
                prefs.edit().putBoolean("TUTO_PROGRAM_EVENT", true).apply();
            }

        }, 500);

    }

    @Override
    public Flowable<Integer> currentPage() {
        return pageProcessor;
    }

    @Override
    public void orgUnitProgress(boolean showProgress) {
        binding.orgUnitProgress.setVisibility(showProgress ? View.VISIBLE : View.GONE);
    }
}