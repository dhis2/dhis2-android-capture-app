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
import com.dhis2.data.forms.FormSectionViewModel;
import com.dhis2.data.forms.FormViewArguments;
import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.dhis2.databinding.ActivityEventInitialBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.map.MapSelectorActivity;
import com.dhis2.utils.CatComboAdapter2;
import com.dhis2.utils.Constants;
import com.dhis2.utils.CustomViews.OrgUnitDialog;
import com.dhis2.utils.CustomViews.ProgressBarAnimation;
import com.dhis2.utils.DateUtils;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;
import timber.log.Timber;

import static com.dhis2.utils.Constants.RQ_PROGRAM_STAGE;


/**
 * QUADRAM. Created by Cristian on 01/03/2018.
 */

public class EventInitialActivity extends ActivityGlobalAbstract implements EventInitialContract.View, DatePickerDialog.OnDateSetListener, ProgressBarAnimation.OnUpdate {

    private static final int PROGRESS_TIME = 2000;

    public static final String EVENT_CREATION_TYPE = "EVENT_CREATION_TYPE";
    public static final String TRACKED_ENTITY_INSTANCE = "TRACKED_ENTITY_INSTANCE";
    public static final String REFERRAL = "REFERRAL";
    public static final String ADDNEW = "ADDNEW";
    public static final String SCHEDULENEW = "SCHEDULENEW";
    public static final String PROGRAM_UID = "PROGRAM_UID";
    public static final String NEW_EVENT = "NEW_EVENT";
    public static final String EVENT_UID = "EVENT_UID";
    public static final String ORG_UNIT = "ORG_UNIT";
    public static final String ONE_TIME = "ONE_TIME";
    public static final String PERMANENT = "PERMANENT";
    public static final String ENROLLMENT_UID = "ENROLLMENT_UID";
    public static final String EVENT_REPEATABLE = "EVENT_REPEATABLE";
    public static final String EVENT_PERIOD_TYPE = "EVENT_PERIOD_TYPE";
    @Inject
    EventInitialContract.Presenter presenter;

    private EventModel eventModel;

    private ActivityEventInitialBinding binding;
    private boolean isNewEvent;

    private String selectedDateString;
    private String selectedOrgUnit;
    private Date selectedOrgUnitOpeningDate;
    private Date selectedOrgUnitClosedDate;
    private CategoryOptionComboModel selectedCatOptionCombo;
    private CategoryComboModel selectedCatCombo;
    private ProgramStageModel programStageModel;
    private String selectedLat;
    private String selectedLon;
    private List<CategoryOptionComboModel> categoryOptionComboModels;
    private int completionPercent;
    private String eventId;
    private String programId;
    private String eventCreationType;
    private String getTrackedEntityInstance;
    private int totalFields;
    private int totalCompletedFields;
    private String tempCreate;
    private boolean fixedOrgUnit;
    public static final String PROGRAM_STAGE_UID = "PROGRAM_STAGE_UID";
    private String enrollmentUid;
    private boolean isRepeatable;
    private PeriodType periodType;
    private String programStageUid;
    private OrgUnitDialog orgUnitDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setScreenName(this.getLocalClassName());
        super.onCreate(savedInstanceState);

        programId = getIntent().getStringExtra(PROGRAM_UID);
        isNewEvent = getIntent().getBooleanExtra(NEW_EVENT, true);
        eventId = getIntent().getStringExtra(EVENT_UID);
        eventCreationType = getIntent().getStringExtra(EVENT_CREATION_TYPE);
        getTrackedEntityInstance = getIntent().getStringExtra(TRACKED_ENTITY_INSTANCE);
        enrollmentUid = getIntent().getStringExtra(ENROLLMENT_UID);
        if (eventCreationType == null)
            eventCreationType = "DEFAULT";
        String orgUnit = getIntent().getStringExtra(ORG_UNIT);
        selectedOrgUnit = orgUnit;
        isRepeatable = getIntent().getBooleanExtra(EVENT_REPEATABLE, false);
        periodType = (PeriodType) getIntent().getSerializableExtra(EVENT_PERIOD_TYPE);
        programStageUid = getIntent().getStringExtra(PROGRAM_STAGE_UID);

        ((App) getApplicationContext()).userComponent().plus(new EventInitialModule(eventId)).inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_initial);
        binding.setPresenter(presenter);
        binding.setIsNewEvent(isNewEvent);
        binding.date.clearFocus();

        if (eventCreationType.equals(REFERRAL)) {
            binding.temp.setVisibility(View.VISIBLE);
            binding.oneTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    tempCreate = ONE_TIME;
                } else {
                    tempCreate = null;
                }
                checkActionButtonVisibility();
            });
            binding.permanent.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    tempCreate = PERMANENT;
                } else {
                    tempCreate = null;
                }
                checkActionButtonVisibility();
            });
        } else {
            binding.temp.setVisibility(View.GONE);
        }

        if (eventCreationType.equals(ADDNEW) || eventCreationType.equals(SCHEDULENEW)) {
            fixedOrgUnit = true;
            selectedOrgUnit = orgUnit;
            binding.orgUnit.setVisibility(View.GONE);
        } else {
            fixedOrgUnit = false;
            binding.orgUnit.setVisibility(View.VISIBLE);
            binding.orgUnit.setOnClickListener(v -> {
                if (!fixedOrgUnit)
                    presenter.onOrgUnitButtonClick();
            });
        }

        binding.date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedDateString = s.toString();
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
//                selectedOrgUnit = s.toString();
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

        if (isNewEvent) {
            binding.actionButton.setText(R.string.create);
        } else {
            binding.actionButton.setText(R.string.update);
        }

        binding.actionButton.setOnClickListener(v -> {

            String formattedDate = null;
            Date date = null;
            try {
                date = DateUtils.uiDateFormat().parse(selectedDateString);
                formattedDate = DateUtils.databaseDateFormat().format(date);
            } catch (Exception e) {
                Timber.e(e);
            }

            if (isNewEvent) {
                if (eventCreationType.equals(REFERRAL) && tempCreate.equals(PERMANENT)) {
                    presenter.createEventPermanent(
                            enrollmentUid,
                            getTrackedEntityInstance,
                            programStageModel.uid(),
                            date,
                            selectedOrgUnit,
                            null,
                            catComboIsDefaultOrNull() ? null : selectedCatOptionCombo.uid(),
                            selectedLat, selectedLon);
                } else if (eventCreationType.equals(SCHEDULENEW)) {
                    presenter.scheduleEvent(
                            enrollmentUid,
                            programStageModel.uid(),
                            date,
                            selectedOrgUnit,
                            null,
                            catComboIsDefaultOrNull() ?
                                    null : selectedCatOptionCombo.uid(),
                            selectedLat, selectedLon);
                } else {
                    presenter.createEvent(
                            enrollmentUid,
                            programStageModel.uid(),
                            date,
                            selectedOrgUnit,
                            null,
                            catComboIsDefaultOrNull() ? null : selectedCatOptionCombo.uid(),
                            selectedLat, selectedLon);
                }
            } else {
                presenter.editEvent(
                        programStageModel.uid(),
                        eventId,
                        formattedDate,
                        selectedOrgUnit,
                        null,
                        catComboIsDefaultOrNull() ? null : selectedCatOptionCombo.uid(),
                        selectedLat, selectedLon);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this, programId, eventId, selectedOrgUnit);
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    private void initProgressBar() {
        if (isNewEvent) {
            binding.progressGains.setVisibility(View.GONE);
            binding.progress.setVisibility(View.GONE);
        } else {
            binding.progressGains.setVisibility(View.VISIBLE);
            binding.progress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onUpdate(boolean lost, float interpolatedTime) {
        int progress = (int) (completionPercent * interpolatedTime);
        String text = String.valueOf(progress) + "%";
        binding.progress.setText(text);
    }

    private void checkActionButtonVisibility() {
        if (isFormCompleted() && isEventOpen()) {
            binding.actionButton.setVisibility(View.VISIBLE);
        } else if (isFormCompleted()) {
            binding.actionButton.setText(getString(R.string.check_event));
            binding.actionButton.setVisibility(View.VISIBLE);
        } else {
            binding.actionButton.setVisibility(View.GONE);
        }
    }

    private boolean isFormCompleted() {

        if (!catComboIsDefaultOrNull())
            return isCompleted(selectedDateString) &&
                    isCompleted(selectedOrgUnit) &&
                    isSelectedDateBetweenOpeningAndClosedDates() &&
                    (!(programStageModel != null && programStageModel.captureCoordinates()) || (isCompleted(selectedLat) && isCompleted(selectedLon))) &&
                    selectedCatCombo != null && selectedCatOptionCombo != null &&
                    ((!eventCreationType.equals(REFERRAL)) || (eventCreationType.equals(REFERRAL) && tempCreate != null));
        else
            return isCompleted(selectedDateString) &&
                    isCompleted(selectedOrgUnit) &&
                    isSelectedDateBetweenOpeningAndClosedDates() &&
                    (!(programStageModel != null && programStageModel.captureCoordinates()) || (isCompleted(selectedLat) && isCompleted(selectedLon))) &&
                    ((!eventCreationType.equals(REFERRAL)) || (eventCreationType.equals(REFERRAL) && tempCreate != null));
    }

    private boolean isEventOpen() {
        return isNewEvent || (eventModel != null && eventModel.status() != EventStatus.COMPLETED);
    }

    private boolean isSelectedDateBetweenOpeningAndClosedDates() {
        if (selectedDateString == null)
            return false;
        try {
            Date selectedDate = DateUtils.uiDateFormat().parse(selectedDateString);
            boolean isAfterOpening = selectedOrgUnitOpeningDate == null || (selectedOrgUnitOpeningDate != null && selectedDate.after(selectedOrgUnitOpeningDate));
            boolean isBeforeClosed = selectedOrgUnitClosedDate == null || (selectedOrgUnitClosedDate != null && selectedDate.before(selectedOrgUnitClosedDate));
            return isAfterOpening && isBeforeClosed;

        } catch (ParseException e) {
            Timber.e(e);
            return false;
        }
    }

    private boolean isCompleted(String field) {
        return field != null && !field.isEmpty();
    }

    @Override
    public void setProgram(@NonNull ProgramModel program) {
        String activityTitle;
        if (eventCreationType.equals(REFERRAL)) {
            activityTitle = program.displayName() + " - " + getString(R.string.referral);
        } else {
            activityTitle = isNewEvent ? program.displayName() + " - " + getString(R.string.new_event) : program.displayName();
        }
        binding.setName(activityTitle);

        if (periodType == null)
            binding.date.setOnClickListener(v -> presenter.onDateClick(EventInitialActivity.this));

        if (program.captureCoordinates()) {
            binding.coordinatesLayout.setVisibility(View.VISIBLE);
            binding.location1.setOnClickListener(v -> presenter.onLocationClick());
            binding.location2.setOnClickListener(v -> presenter.onLocation2Click());
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

        if (presenter.getOrgUnits().size() < 25)
            treeView.expandAll();

        treeView.setDefaultNodeClickListener((node, value) -> {
            if (node.isSelectable()) {
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
                if (!fixedOrgUnit) {
                    selectedOrgUnit = ((OrganisationUnitModel) value).uid();
                    binding.orgUnit.setText(((OrganisationUnitModel) value).displayShortName());
                }
                binding.drawerLayout.closeDrawers();
            }
        });

        if (treeView.getSelected() != null && !treeView.getSelected().isEmpty() && !fixedOrgUnit) {
            binding.orgUnit.setText(((OrganisationUnitModel) treeView.getSelected().get(0).getValue()).displayShortName());
            selectedOrgUnit = ((OrganisationUnitModel) treeView.getSelected().get(0).getValue()).uid();
            selectedOrgUnitOpeningDate = ((OrganisationUnitModel) treeView.getSelected().get(0).getValue()).openingDate();
            selectedOrgUnitClosedDate = ((OrganisationUnitModel) treeView.getSelected().get(0).getValue()).closedDate();
        }
        checkActionButtonVisibility();
    }

    @Override
    public void setEvent(EventModel event) {
        binding.setEvent(event);

        if (event.eventDate() != null)
            binding.setEventDate(DateUtils.uiDateFormat().format(event.eventDate()));
        else
            binding.setEventDate("");

        if (event.latitude() != null && event.longitude() != null) {
            runOnUiThread(() -> {
                binding.lat.setText(event.latitude());
                binding.lon.setText(event.longitude());
            });

        }

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
        binding.lat.setText(String.format(Locale.US, "%.5f", latitude));
        binding.lon.setText(String.format(Locale.US, "%.5f", longitude));
        checkActionButtonVisibility();
    }

    @Override
    public void onEventCreated(String eventUid) {
        showToast(getString(R.string.event_created));
        if (!eventCreationType.equals(SCHEDULENEW) && !eventCreationType.equals(REFERRAL)) {
            startFormActivity(eventUid);
        } else {
            finish();
        }
    }

    @Override
    public void onEventUpdated(String eventUid) {
        showToast(getString(R.string.event_updated));
        startFormActivity(eventUid);
    }

    private void startFormActivity(String eventUid) {
        FormViewArguments formViewArguments = FormViewArguments.createForEvent(eventUid);
        startActivity(FormActivity.create(getAbstractActivity(), formViewArguments, false));
        finish();
    }

    @Override
    public void setProgramStage(ProgramStageModel programStage) {
        this.programStageModel = programStage;
        if (programStageModel.captureCoordinates()) {
            binding.coordinatesLayout.setVisibility(View.VISIBLE);
            binding.location1.setOnClickListener(v -> presenter.onLocationClick());
            binding.location2.setOnClickListener(v -> presenter.onLocation2Click());
        } else {
            binding.coordinatesLayout.setVisibility(View.GONE);
        }
        binding.setProgramStage(programStage);
    }

    @Override
    public void setCatComboOptions(CategoryComboModel catCombo, List<CategoryOptionComboModel> catComboList) {

        runOnUiThread(() -> {

            selectedCatCombo = catCombo;

            if (catComboIsDefaultOrNull() || catComboList.isEmpty()) {
                binding.catCombo.setVisibility(View.GONE);
                binding.catComboLine.setVisibility(View.GONE);
            } else {

                if (!catComboList.isEmpty()) {
                    selectedCatOptionCombo = catComboList.get(0);
                }
                binding.catCombo.setVisibility(View.VISIBLE);
                binding.catComboLine.setVisibility(View.VISIBLE);

                categoryOptionComboModels = catComboList;

                CatComboAdapter2 adapter = new CatComboAdapter2(EventInitialActivity.this,
                        R.layout.spinner_layout,
                        R.id.spinner_text,
                        catComboList,
                        catCombo.displayName() != null ? catCombo.displayName() : getString(R.string.category_option));

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


                if (eventModel != null)//TODO: Check all this
                    presenter.getCatOption(eventModel.attributeOptionCombo());
            }


            if (periodType != null) {
                binding.dateLayout.setHint(periodType.name());
                presenter.getEvents(programId, enrollmentUid, programStageUid, periodType);
            }
            checkActionButtonVisibility();
        });
    }

    @Override
    public void showDateDialog(DatePickerDialog.OnDateSetListener listener) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        if (programStageModel != null && programStageModel.hideDueDate())
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
        else {
            // ONLY FUTURE DATES
            if (eventCreationType.equals(SCHEDULENEW)) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            }
            // ONLY PAST DATES AND TODAY
            else if (eventCreationType.equals(ADDNEW)) {
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
            }
        }
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        String date = String.format(Locale.getDefault(), "%s-%02d-%02d", year, month + 1, day);
        binding.date.setText(date);
        binding.date.clearFocus();
        if (!fixedOrgUnit)
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
        if (requestCode == RQ_PROGRAM_STAGE) {
            if (resultCode == RESULT_OK) {
                presenter.getProgramStage(data.getStringExtra(PROGRAM_STAGE_UID));
            } else {
                finish();
            }
        }
    }

    @Override
    public void onEventSections(List<FormSectionViewModel> formSectionViewModels) {
        for (FormSectionViewModel formSectionViewModel : formSectionViewModels) {
            presenter.getSectionCompletion(formSectionViewModel.sectionUid());
        }
    }

    @NonNull
    @Override
    public Consumer<List<FieldViewModel>> showFields(String sectionUid) {
        return fields -> swap(fields, sectionUid);
    }

    @Override
    public void showProgramStageSelection() {
        presenter.getProgramStage(programStageUid);
    }

    void swap(@NonNull List<FieldViewModel> updates, String sectionUid) {
        int completedSectionFields = calculateCompletedFields(updates);
        int totalSectionFields = updates.size();
        totalFields = totalFields + totalSectionFields;
        totalCompletedFields = totalCompletedFields + completedSectionFields;
        float completionPerone = (float) totalCompletedFields / (float) totalFields;
        completionPercent = (int) (completionPerone * 100);

        runOnUiThread(() -> {
            ProgressBarAnimation gainAnim = new ProgressBarAnimation(binding.progressGains, 0, completionPercent, false, EventInitialActivity.this);
            gainAnim.setDuration(PROGRESS_TIME);
            binding.progressGains.startAnimation(gainAnim);
        });

    }

    private int calculateCompletedFields(@NonNull List<FieldViewModel> updates) {
        int total = 0;
        for (FieldViewModel fieldViewModel : updates) {
            if (fieldViewModel.value() != null && !fieldViewModel.value().isEmpty())
                total++;
        }
        return total;
    }

    @Override
    public void setReportDate(String date) {
        selectedDateString = date;
        binding.date.setText(selectedDateString);
        binding.executePendingBindings();
        checkActionButtonVisibility();
    }

    @Override
    public void setOrgUnit(String orgUnitId, String orgUnitName) {
        this.selectedOrgUnit = orgUnitId;
        binding.orgUnit.setText(orgUnitName);
    }

    @Override
    public void showNoOrgUnits() {
        renderError(getString(R.string.no_org_units));
        selectedDateString = null;
        binding.date.setText("");
        binding.executePendingBindings();
        checkActionButtonVisibility();
    }

    @Override
    public void setAccessDataWrite(Boolean canWrite) {
        if (!canWrite) {
            binding.date.setEnabled(false);
            binding.orgUnit.setEnabled(false);
            binding.catCombo.setEnabled(false);
            binding.actionButton.setText("Check");
            binding.location1.setEnabled(false);
            binding.location2.setEnabled(false);
            binding.lat.setEnabled(false);
            binding.lon.setEnabled(false);
        }
    }

    @Override
    public void showOrgUnitSelector(List<OrganisationUnitModel> orgUnits) {
        orgUnitDialog = new OrgUnitDialog()
                .setTitle(getString(R.string.org_unit))
                .setMultiSelection(false)
                .setOrgUnits(orgUnits)
                .setPossitiveListener(data -> {
                    setOrgUnit(orgUnitDialog.getSelectedOrgUnit(), orgUnitDialog.getSelectedOrgUnitName());
                    orgUnitDialog.dismiss();
                })
                .setNegativeListener(data -> orgUnitDialog.dismiss());
        orgUnitDialog.show(getSupportFragmentManager(), "ORG_UNIT_DIALOG");
    }

    private boolean catComboIsDefaultOrNull() {
        return (selectedCatCombo == null || selectedCatCombo.isDefault() || CategoryComboModel.DEFAULT_UID.equals(selectedCatCombo.uid()));
    }
}