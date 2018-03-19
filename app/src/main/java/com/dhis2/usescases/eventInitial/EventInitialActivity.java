package com.dhis2.usescases.eventInitial;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityEventInitialBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.map.MapSelectorActivity;
import com.dhis2.utils.CatComboAdapter2;
import com.dhis2.utils.Constants;
import com.dhis2.utils.DateUtils;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

/**
 * Created by Cristian on 01/03/2018.
 */

public class EventInitialActivity extends ActivityGlobalAbstract implements EventInitialContract.View, DatePickerDialog.OnDateSetListener {

    @Inject
    EventInitialContract.Presenter presenter;

    private EventModel eventModel;

    private ActivityEventInitialBinding binding;
    private boolean isNewEvent;

    private String selectedDate;
    private String selectedOrgUnit;
    private CategoryOptionComboModel selectedCatOption;
    private String selectedLat;
    private String selectedLon;
    private List<CategoryOptionComboModel> categoryOptionComboModels;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new EventInitialModule()).inject(this);
        super.onCreate(savedInstanceState);
        String programId = getIntent().getStringExtra("PROGRAM_UID");
        isNewEvent = getIntent().getBooleanExtra("NEW_EVENT", true);
        String eventId = getIntent().getStringExtra("EVENT_UID");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_initial);
        binding.setPresenter(presenter);
        binding.setIsNewEvent(isNewEvent);
        binding.date.clearFocus();
        presenter.init(this, programId, eventId);
        binding.date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedDate = s.toString();
                checkActionButtonVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.orgUnit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedOrgUnit = s.toString();
                checkActionButtonVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.lat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedLat = s.toString();
                checkActionButtonVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.lon.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedLon = s.toString();
                checkActionButtonVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void checkActionButtonVisibility(){
        if (isFormCompleted()){
            binding.actionButton.setVisibility(View.VISIBLE);
        }
        else {
            binding.actionButton.setVisibility(View.GONE);
        }
    }

    private boolean isFormCompleted(){
        return isCompleted(selectedDate) && isCompleted(selectedOrgUnit) && isCompleted(selectedLat) && isCompleted(selectedLon) && selectedCatOption != null;
    }

    private boolean isCompleted(String field){
        return field != null && !field.isEmpty();
    }

    @Override
    public void setProgram(ProgramModel program) {
        presenter.setProgram(program);
        String activityTitle = isNewEvent ? program.displayName() + " - " + getString(R.string.new_event) : program.displayName();
        binding.setName(activityTitle);
        binding.date.setOnClickListener(v -> presenter.onDateClick(EventInitialActivity.this));
        binding.location1.setOnClickListener(v -> presenter.onLocationClick());
        binding.location2.setOnClickListener(v -> presenter.onLocation2Click());
    }

    @Override
    public void openDrawer() {
        if (!binding.drawerLayout.isDrawerOpen(Gravity.END))
            binding.drawerLayout.openDrawer(Gravity.END);
        else
            binding.drawerLayout.closeDrawer(Gravity.END);
    }

    @Override
    public void addTree(TreeNode treeNode) {
        binding.treeViewContainer.removeAllViews();

        AndroidTreeView treeView = new AndroidTreeView(getContext(), treeNode);

        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
        treeView.setSelectionModeEnabled(true);
        binding.treeViewContainer.addView(treeView.getView());
        treeView.expandAll();

        treeView.setDefaultNodeClickListener((node, value) -> {
            treeView.selectNode(node, node.isSelected());
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
            binding.orgUnit.setText(((OrganisationUnitModel) value).displayShortName());
            binding.drawerLayout.closeDrawers();
        });

        if (treeView.getSelected() != null && treeView.getSelected().isEmpty()) {
            binding.orgUnit.setText(((OrganisationUnitModel) treeView.getSelected().get(0).getValue()).displayShortName());
        }

    }

    @Override
    public void setEvent(EventModel event) {
        binding.setEvent(event);
        eventModel = event;
    }

    @Override
    public void setCatOption(CategoryOptionComboModel categoryOptionComboModel) {
        if (categoryOptionComboModels != null) {
            for (int i = 0; i < categoryOptionComboModels.size(); i++) {
                if (categoryOptionComboModels.get(i).uid().equals(categoryOptionComboModel.uid())) {
                    binding.catCombo.setSelection(i + 1);
                }
            }
        }
    }

    @Override
    public void setLocation(double latitude, double longitude) {
        binding.lat.setText(String.valueOf(latitude));
        binding.lon.setText(String.valueOf(longitude));
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

        if (catCombo.isDefault() || catComboList == null || catComboList.isEmpty()){
            binding.catCombo.setVisibility(View.GONE);
            binding.catComboLine.setVisibility(View.GONE);
        }
        else {
            binding.catCombo.setVisibility(View.VISIBLE);
            binding.catComboLine.setVisibility(View.VISIBLE);
        }

        categoryOptionComboModels = catComboList;

        CatComboAdapter2 adapter = new CatComboAdapter2(this,
                R.layout.spinner_layout,
                R.id.spinner_text,
                catComboList,
                getString(R.string.category_option));

        binding.catCombo.setAdapter(adapter);
        binding.catCombo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (catComboList.size() > position - 1 && position > 0)
                    selectedCatOption = catComboList.get(position - 1);
                else
                    selectedCatOption = null;
                checkActionButtonVisibility();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //TODO CRIS: CON QUE SE PUEBLA ESTE SPINNER?
        presenter.getCatOption(eventModel.attributeOptionCombo());
    }

    @Override
    public void showDateDialog(DatePickerDialog.OnDateSetListener listener) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        String date = String.format(Locale.getDefault(), "%s-%02d-%02d", year, month + 1, day);
        binding.date.setText(date);
        binding.date.clearFocus();
        presenter.filterOrgUnits(DateUtils.getInstance().toDate(date));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case EventInitialPresenter.ACCESS_COARSE_LOCATION_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    presenter.onLocationClick();
                } else {
                    // TODO CRIS
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.RQ_MAP_LOCATION && resultCode == RESULT_OK) {
            setLocation(Double.valueOf(data.getStringExtra(MapSelectorActivity.LATITUDE)), Double.valueOf(data.getStringExtra(MapSelectorActivity.LONGITUDE)));
        }
    }
}
