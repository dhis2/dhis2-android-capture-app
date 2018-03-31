package com.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.data.forms.FormActivity;
import com.dhis2.data.forms.FormViewArguments;
import com.dhis2.databinding.ActivityEventInitialBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.map.MapSelectorActivity;
import com.dhis2.utils.CatComboAdapter2;
import com.dhis2.utils.Constants;
import com.dhis2.utils.CustomViews.ProgressBarAnimation;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventInitialActivity extends ActivityGlobalAbstract implements EventInitialContract.View, DatePickerDialog.OnDateSetListener, ProgressBarAnimation.OnUpdate {

    private static final int PROGRESS_TIME = 2000;

    @Inject
    EventInitialContract.Presenter presenter;

    private EventModel eventModel;

    private ActivityEventInitialBinding binding;
    private boolean isNewEvent;

    private String selectedDate;
    private String selectedOrgUnit;
    private CategoryOptionComboModel selectedCatOptionCombo;
    private CategoryComboModel selectedCatCombo;
    private ProgramStageModel programStageModel;
    private String selectedLat;
    private String selectedLon;
    private List<CategoryOptionComboModel> categoryOptionComboModels;
    private int completionPercent;
    private String eventId;
    private String programId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new EventInitialModule()).inject(this);
        super.onCreate(savedInstanceState);
        programId = getIntent().getStringExtra("PROGRAM_UID");
        isNewEvent = getIntent().getBooleanExtra("NEW_EVENT", true);
        eventId = getIntent().getStringExtra("EVENT_UID");
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

        initProgressBar();

        if (isNewEvent){
            binding.actionButton.setText(R.string.create);
        }
        else {
            binding.actionButton.setText(R.string.update);
        }

        binding.actionButton.setOnClickListener(v -> {
//            Bundle bundle = new Bundle();
//            bundle.putString(EventSummaryActivity.EVENT_ID, eventId);
//            bundle.putString(EventSummaryActivity.PROGRAM_ID, programId);
//            Intent intent = new Intent(this, EventSummaryActivity.class);
//            intent.putExtras(bundle);
//            startActivity(intent);
            if (isNewEvent){
                presenter.createEvent(programStageModel.uid(), selectedDate, selectedOrgUnit, selectedCatOptionCombo.uid(), selectedCatCombo.uid(), selectedLat, selectedLon);
            }
            else {
                try {
                    DateFormat dateFormat = DateFormat.getDateTimeInstance();
                    Date date = dateFormat.parse(selectedDate);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String formattedDate = simpleDateFormat.format(date);
                    presenter.editEvent(programStageModel.uid(), eventId, formattedDate, selectedOrgUnit, selectedCatOptionCombo.uid(), selectedLat, selectedLon);
                }
                catch (Exception e){
                    Timber.e(e);
                }
            }
        });
    }

    private void initProgressBar(){
        if (isNewEvent){
            binding.progressGains.setVisibility(View.GONE);
            binding.progress.setVisibility(View.GONE);
        }
        else {
            binding.progressGains.setVisibility(View.VISIBLE);
            binding.progress.setVisibility(View.VISIBLE);
            //TODO CRIS: GET REAL PERCENTAGE HERE
            completionPercent = 44;
            ProgressBarAnimation gainAnim = new ProgressBarAnimation(binding.progressGains, 0, completionPercent, false, this);
            gainAnim.setDuration(PROGRESS_TIME);
            binding.progressGains.setAnimation(gainAnim);
        }
    }

    @Override
    public void onUpdate(boolean lost, float interpolatedTime) {
        int progress = (int)(completionPercent * interpolatedTime);
        String text = String.valueOf(progress) + "%";
        binding.progress.setText(text);
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
        return isCompleted(selectedDate) && isCompleted(selectedOrgUnit) && isCompleted(selectedLat) && isCompleted(selectedLon) && selectedCatCombo != null && selectedCatOptionCombo != null;
    }

    private boolean isCompleted(String field){
        return field != null && !field.isEmpty();
    }

    @Override
    public void setProgram(@NonNull ProgramModel program) {
        presenter.setProgram(program);
        String activityTitle = isNewEvent ? program.displayName() + " - " + getString(R.string.new_event) : program.displayName();
        binding.setName(activityTitle);
        binding.date.setOnClickListener(v -> presenter.onDateClick(EventInitialActivity.this));

        if (program.captureCoordinates()) {
            binding.coordinatesLayout.setVisibility(View.VISIBLE);
            binding.location1.setOnClickListener(v -> presenter.onLocationClick());
            binding.location2.setOnClickListener(v -> presenter.onLocation2Click());
        }
        else{
            binding.coordinatesLayout.setVisibility(View.GONE);
            selectedLat = "0.0";
            selectedLon = "0.0";
        }
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

        if (treeView.getSelected() != null && !treeView.getSelected().isEmpty()) {
            binding.orgUnit.setText(((OrganisationUnitModel) treeView.getSelected().get(0).getValue()).displayShortName());
            selectedOrgUnit = ((OrganisationUnitModel) treeView.getSelected().get(0).getValue()).uid();
        }
        else {
            selectedOrgUnit = null;
            binding.orgUnit.setText(getString(R.string.org_unit));
        }
        checkActionButtonVisibility();
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
        checkActionButtonVisibility();
    }

    @Override
    public void onEventCreated(String eventUid) {
        showToast(getString(R.string.event_created));
        startFormActivity(eventUid);
    }

    @Override
    public void onEventUpdated(String eventUid) {
        showToast(getString(R.string.event_updated));
        startFormActivity(eventUid);
    }

    private void startFormActivity(String eventUid){
        FormViewArguments formViewArguments = FormViewArguments.createForEvent(eventUid);
        startActivity(FormActivity.create(getAbstractActivity(), formViewArguments));
    }

    @Override
    public void setProgramStage(ProgramStageModel programStage) {
        this.programStageModel = programStage;
        binding.setProgramStage(programStage);
    }

    @Override
    public void setCatComboOptions(CategoryComboModel catCombo, List<CategoryOptionComboModel> catComboList) {

        selectedCatCombo = catCombo;

        if (catCombo.isDefault() || catComboList == null || catComboList.isEmpty()){
            binding.catCombo.setVisibility(View.GONE);
            binding.catComboLine.setVisibility(View.GONE);
            if (catComboList != null && !catComboList.isEmpty()) {
                selectedCatOptionCombo = catComboList.get(0);
            }
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
                    selectedCatOptionCombo = catComboList.get(position - 1);
                else
                    selectedCatOptionCombo = null;
                checkActionButtonVisibility();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        presenter.getCatOption(eventModel.attributeOptionCombo());
        checkActionButtonVisibility();
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
        binding.orgUnit.setText("");
        presenter.filterOrgUnits(date);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case EventInitialPresenter.ACCESS_COARSE_LOCATION_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    presenter.onLocationClick();
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