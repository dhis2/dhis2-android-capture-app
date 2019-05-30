package org.dhis2.usescases.datasets.datasetDetail;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityDatasetDetailBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.main.program.OrgUnitHolder;
import org.dhis2.utils.CatComboAdapter;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Period;
import org.dhis2.utils.custom_views.RxDateDialog;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import timber.log.Timber;

import static org.dhis2.utils.Period.DAILY;
import static org.dhis2.utils.Period.MONTHLY;
import static org.dhis2.utils.Period.NONE;
import static org.dhis2.utils.Period.WEEKLY;
import static org.dhis2.utils.Period.YEARLY;

public class DataSetDetailActivity extends ActivityGlobalAbstract implements DataSetDetailContract.View {

    private ActivityDatasetDetailBinding binding;
    private ArrayList<Date> chosenDateWeek = new ArrayList<>();
    private ArrayList<Date> chosenDateMonth = new ArrayList<>();
    private ArrayList<Date> chosenDateYear = new ArrayList<>();
    private String dataSetUid;
    private Period currentPeriod = Period.NONE;
    private StringBuilder orgUnitFilter = new StringBuilder();
    private SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
    private Date chosenDateDay = new Date();
    private TreeNode treeNode;
    private AndroidTreeView treeView;
    private boolean isFilteredByCatCombo = false;
    @Inject
    DataSetDetailContract.Presenter presenter;

    private static PublishProcessor<Integer> currentPage;
    DataSetDetailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) getApplicationContext()).userComponent().plus(new DataSetDetailModule()).inject(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_detail);

        chosenDateWeek.add(new Date());
        chosenDateMonth.add(new Date());
        chosenDateYear.add(new Date());

        dataSetUid = getIntent().getStringExtra("DATASET_UID");
        binding.setPresenter(presenter);

        adapter = new DataSetDetailAdapter(presenter);

        currentPage = PublishProcessor.create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);

        presenter.getDataSetWithDates(null, currentPeriod, orgUnitFilter.toString());
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
        binding.treeViewContainer.removeAllViews();
    }

    @Override
    public void setData(List<DataSetDetailModel> datasets) {
        if (binding.recycler.getAdapter() == null) {
            binding.recycler.setAdapter(adapter);
            binding.recycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        }
        adapter.setDatasets(datasets);
    }

    @Override
    public void addTree(TreeNode treeNode) {
        this.treeNode = treeNode;
        binding.treeViewContainer.removeAllViews();
        binding.orgUnitApply.setOnClickListener(view -> apply());
        treeView = new AndroidTreeView(getContext(), treeNode);

        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
        treeView.setSelectionModeEnabled(true);

        binding.treeViewContainer.addView(treeView.getView());
        if (presenter.getOrgUnits().size() < 25)
            treeView.expandAll();

        treeView.setDefaultNodeClickListener((node, value) -> {
            if (treeView.getSelected().size() == 1 && !node.isSelected()) {
                ((OrgUnitHolder) node.getViewHolder()).update();
                binding.buttonOrgUnit.setText(String.format(getString(R.string.org_unit_filter), treeView.getSelected().size()));
            } else if (treeView.getSelected().size() > 1) {
                ((OrgUnitHolder) node.getViewHolder()).update();
                binding.buttonOrgUnit.setText(String.format(getString(R.string.org_unit_filter), treeView.getSelected().size()));
            }
        });

        binding.buttonOrgUnit.setText(String.format(getString(R.string.org_unit_filter), treeView.getSelected().size()));
    }

    @Override
    public void openDrawer() {
        if (!binding.drawerLayout.isDrawerOpen(Gravity.END))
            binding.drawerLayout.openDrawer(Gravity.END);
        else
            binding.drawerLayout.closeDrawer(Gravity.END);
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
                // TODO
                presenter.getDataSetWithDates(null, currentPeriod, orgUnitFilter.toString());
                textToShow = getString(R.string.period);
                break;
            case DAILY:
                ArrayList<Date> datesD = new ArrayList<>();
                datesD.add(chosenDateDay);
                if (!datesD.isEmpty())
                    textToShow = DateUtils.getInstance().formatDate(datesD.get(0));
                if (!datesD.isEmpty() && datesD.size() > 1) textToShow += "... ";
                // TODO
                presenter.getDataSetWithDates(datesD, currentPeriod, orgUnitFilter.toString());
                break;
            case WEEKLY:
                if (!chosenDateWeek.isEmpty()) {
                    String week = getString(R.string.week);
                    SimpleDateFormat weeklyFormat = new SimpleDateFormat("'" + week + "' w", Locale.getDefault());
                    textToShow = weeklyFormat.format(chosenDateWeek.get(0)) + ", " + yearFormat.format(chosenDateWeek.get(0));
                }
                if (!chosenDateWeek.isEmpty() && chosenDateWeek.size() > 1) textToShow += "... ";
                // TODO
                presenter.getDataSetWithDates(chosenDateWeek, currentPeriod, orgUnitFilter.toString());
                break;
            case MONTHLY:
                if (!chosenDateMonth.isEmpty()) {
                    String dateFormatted = monthFormat.format(chosenDateMonth.get(0));
                    textToShow = dateFormatted.substring(0, 1).toUpperCase() + dateFormatted.substring(1);
                }
                if (!chosenDateMonth.isEmpty() && chosenDateMonth.size() > 1) textToShow += "... ";
                // TODO
                presenter.getDataSetWithDates(chosenDateMonth, currentPeriod, orgUnitFilter.toString());
                break;
            case YEARLY:
                if (!chosenDateYear.isEmpty())
                    textToShow = yearFormat.format(chosenDateYear.get(0));
                if (!chosenDateYear.isEmpty() && chosenDateYear.size() > 1) textToShow += "... ";
                // TODO
                presenter.getDataSetWithDates(chosenDateYear, currentPeriod, orgUnitFilter.toString());
                break;
        }

        binding.buttonPeriodText.setText(textToShow);
    }

    @SuppressLint({"RxLeakedSubscription", "CheckResult"})
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

                            // TODO
                            presenter.getDataSetWithDates(selectedDates, currentPeriod, orgUnitFilter.toString());

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

                            // TODO
                            presenter.getDataSetWithDates(date, currentPeriod, orgUnitFilter.toString());
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
            ArrayList<Date> day = new ArrayList<>();
            day.add(dates[0]);
            // TODO
            presenter.getDataSetWithDates(day, currentPeriod, orgUnitFilter.toString());
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
        c.setTime(chosenDateDay);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        datePicker.updateDate(year, month, day);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), R.style.DatePickerTheme)
                .setPositiveButton(R.string.action_accept, (dialog, which) -> {
                    calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                    Date[] dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), currentPeriod);
                    ArrayList<Date> dayArray = new ArrayList<>();
                    dayArray.add(dates[0]);
                    // TODO
                    presenter.getDataSetWithDates(dayArray, currentPeriod, orgUnitFilter.toString());
                    binding.buttonPeriodText.setText(DateUtils.getInstance().formatDate(dates[0]));
                    chosenDateDay = dates[0];
                })
                .setNeutralButton(getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> showNativeCalendar(calendar));

        alertDialog.setView(datePickerView);
        Dialog dialog = alertDialog.create();
        dialog.show();
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
                        isFilteredByCatCombo = false;
                        presenter.clearCatComboFilters(orgUnitFilter.toString());
                    } else {
                        isFilteredByCatCombo = true;
                        presenter.onCatComboSelected(adapter.getItem(position - 1), orgUnitFilter.toString());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    isFilteredByCatCombo = false;
                    presenter.clearCatComboFilters(orgUnitFilter.toString());
                }
            });
        }
    }

    @Override
    public void setOrgUnitFilter(StringBuilder orgUnitFilter) {
        this.orgUnitFilter = orgUnitFilter;
    }

    @Override
    public void showHideFilter() {
        binding.filterLayout.setVisibility(binding.filterLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        checkFilterEnabled();
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
            case NONE:
                // TODO
                presenter.getDataSetWithDates(null, currentPeriod, orgUnitFilter.toString());
                break;
            case DAILY:
                ArrayList<Date> datesD = new ArrayList<>();
                datesD.add(chosenDateDay);
                // TODO
                presenter.getDataSetWithDates(datesD, currentPeriod, orgUnitFilter.toString());
                break;
            case WEEKLY:
                // TODO
                presenter.getDataSetWithDates(chosenDateWeek, currentPeriod, orgUnitFilter.toString());
                break;
            case MONTHLY:
                // TODO
                presenter.getDataSetWithDates(chosenDateMonth, currentPeriod, orgUnitFilter.toString());
                break;
            case YEARLY:
                // TODO
                presenter.getDataSetWithDates(chosenDateYear, currentPeriod, orgUnitFilter.toString());
                break;
        }
    }

    @Override
    public void setWritePermission(Boolean canWrite) {
        binding.addDatasetButton.setVisibility(canWrite ? View.VISIBLE : View.GONE);
    }

    @Override
    public Flowable<Integer> dataSetPage() {
        return currentPage;
    }

    @Override
    public String dataSetUid() {
        return dataSetUid;
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

    private boolean areAllOrgUnitsSelected() {
        return treeNode != null && treeNode.getChildren().size() == treeView.getSelected().size();
    }

}
