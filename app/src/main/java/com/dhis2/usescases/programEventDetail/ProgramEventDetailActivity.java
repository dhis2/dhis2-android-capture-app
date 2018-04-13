package com.dhis2.usescases.programEventDetail;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityProgramEventDetailBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.utils.CatComboAdapter;
import com.dhis2.utils.CustomViews.RxDateDialog;
import com.dhis2.utils.DateUtils;
import com.dhis2.utils.Period;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by Cristian on 13/02/2018.
 *
 */

public class ProgramEventDetailActivity extends ActivityGlobalAbstract implements ProgramEventDetailContract.View {

    private ActivityProgramEventDetailBinding binding;

    @Inject
    ProgramEventDetailContract.Presenter presenter;

    @Inject
    ProgramEventDetailAdapter adapter;
    private Period currentPeriod = Period.DAILY;
    ProgramModel programModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new ProgramEventDetailModule()).inject(this);

        super.onCreate(savedInstanceState);
        String programId = getIntent().getStringExtra("PROGRAM_UID");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_program_event_detail);
        binding.setPresenter(presenter);
        presenter.init(this, programId);
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

    @SuppressLint("CheckResult")
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
                    presenter.getProgramEventsWithDates(selectedDates, currentPeriod);
                } else {
                    binding.buttonPeriodText.setText(getString(currentPeriod.getNameResouce()));
                    Date[] dates = DateUtils.getInstance().getDateFromPeriod(currentPeriod);
                    presenter.getEvents(dates[0], dates[1]);
                }
            });
        } else {
            DatePickerDialog pickerDialog = new DatePickerDialog(getContext(), (datePicker, year, monthOfYear, dayOfMonth) -> {
                calendar.set(year, monthOfYear, dayOfMonth);
                Date[] dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), currentPeriod);
                presenter.getEvents(dates[0], dates[1]);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
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
        binding.buttonPeriodText.setText(getString(currentPeriod.getNameResouce()));
        Date[] dates = com.dhis2.utils.DateUtils.getInstance().getDateFromPeriod(currentPeriod);
        presenter.getEvents(dates[0], dates[1]);
    }

    @Override
    public void addTree(TreeNode treeNode) {
        binding.treeViewContainer.removeAllViews();

        AndroidTreeView treeView = new AndroidTreeView(getContext(), treeNode);

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

        if (catCombo.uid().equals(CategoryComboModel.DEFAULT_UID) || catComboList == null || catComboList.isEmpty()){
            binding.catCombo.setVisibility(View.GONE);
        }
        else {
            CatComboAdapter adapter = new CatComboAdapter(this,
                    R.layout.spinner_layout,
                    R.id.spinner_text,
                    catComboList,
                    catCombo.displayName(),
                    R.color.white_faf);

            binding.catCombo.setVisibility(View.VISIBLE);
            binding.catCombo.setAdapter(adapter);

            binding.catCombo.setOnItemClickListener((parent, view, position, id) -> {
                if (position == 0) {
                    presenter.clearCatComboFilters();
                } else {
                    presenter.onCatComboSelected(adapter.getItem(position - 1));
                }
            });
        }
    }
}