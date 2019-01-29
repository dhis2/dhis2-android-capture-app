package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.PopupMenu;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.dhis2.App;
import org.dhis2.Bindings.Bindings;
import org.dhis2.BuildConfig;
import org.dhis2.R;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.databinding.ActivityEventInitialBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.map.MapSelectorActivity;
import org.dhis2.usescases.qrCodes.eventsworegistration.QrEventsWORegistrationActivity;
import org.dhis2.utils.CatComboAdapter2;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.EventCreationType;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.custom_views.CustomDialog;
import org.dhis2.utils.custom_views.OrgUnitDialog;
import org.dhis2.utils.custom_views.PeriodDialog;
import org.dhis2.utils.custom_views.ProgressBarAnimation;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import io.reactivex.functions.Consumer;
import me.toptas.fancyshowcase.FancyShowCaseView;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.utils.Constants.ENROLLMENT_UID;
import static org.dhis2.utils.Constants.EVENT_CREATION_TYPE;
import static org.dhis2.utils.Constants.EVENT_PERIOD_TYPE;
import static org.dhis2.utils.Constants.ONE_TIME;
import static org.dhis2.utils.Constants.ORG_UNIT;
import static org.dhis2.utils.Constants.PERMANENT;
import static org.dhis2.utils.Constants.PROGRAM_UID;
import static org.dhis2.utils.Constants.RQ_PROGRAM_STAGE;
import static org.dhis2.utils.Constants.TRACKED_ENTITY_INSTANCE;


/**
 * QUADRAM. Created by Cristian on 01/03/2018.
 */

public class EventInitialActivity extends ActivityGlobalAbstract implements EventInitialContract.View, DatePickerDialog.OnDateSetListener, ProgressBarAnimation.OnUpdate {

    private static final int PROGRESS_TIME = 2000;
    @Inject
    EventInitialContract.Presenter presenter;

    private EventModel eventModel;

    private ActivityEventInitialBinding binding;

    private String selectedDateString;
    private Date selectedDate;
    private String selectedOrgUnit;
    private Date selectedOrgUnitOpeningDate;
    private Date selectedOrgUnitClosedDate;
    private CategoryOptionComboModel selectedCatOptionCombo;
    private CategoryComboModel selectedCatCombo;
    private ProgramStageModel programStageModel;
    private String selectedLat;
    private String selectedLon;
    private List<CategoryOptionComboModel> categoryOptionComboModels;
    private String eventUid;
    private String programUid;
    private EventCreationType eventCreationType;
    private int totalFields;
    private int totalCompletedFields;
    private String tempCreate;
    private boolean fixedOrgUnit;
    private String enrollmentUid;
    private PeriodType periodType;
    private String programStageUid;
    private OrgUnitDialog orgUnitDialog;
    private ProgramModel program;
    private String savedLat;
    private String savedLon;
    private ArrayList<String> sectionsToHide;
    private String getTrackedEntityInstance;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setScreenName(this.getLocalClassName());
        super.onCreate(savedInstanceState);

        programUid = getIntent().getStringExtra(PROGRAM_UID);
        eventUid = getIntent().getStringExtra(Constants.EVENT_UID);
        eventCreationType = getIntent().getStringExtra(EVENT_CREATION_TYPE) != null ?
                EventCreationType.valueOf(getIntent().getStringExtra(EVENT_CREATION_TYPE)) :
                EventCreationType.DEFAULT;
        getTrackedEntityInstance = getIntent().getStringExtra(TRACKED_ENTITY_INSTANCE);
        enrollmentUid = getIntent().getStringExtra(ENROLLMENT_UID);
        selectedOrgUnit = getIntent().getStringExtra(ORG_UNIT);
        periodType = (PeriodType) getIntent().getSerializableExtra(EVENT_PERIOD_TYPE);
        programStageUid = getIntent().getStringExtra(Constants.PROGRAM_STAGE_UID);

        ((App) getApplicationContext()).userComponent().plus(new EventInitialModule(eventUid)).inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_initial);
        binding.setPresenter(presenter);

        setUpScrenByCreatinType(eventCreationType);


        binding.date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //unused
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedDateString = s.toString();
                checkActionButtonVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //unused
            }
        });
        binding.orgUnit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //unused
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkActionButtonVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //unused
            }
        });
        binding.lat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //unused
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedLat = s.toString();
                checkActionButtonVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //unused
            }
        });
        binding.lon.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //unused
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedLon = s.toString();
                checkActionButtonVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //unused
            }
        });

        initProgressBar();

        if (eventUid == null) {
            if (binding.actionButton != null)
                binding.actionButton.setText(R.string.create);
        } else {
            if (binding.actionButton != null)
                binding.actionButton.setText(R.string.update);

        }

        if (binding.actionButton != null) {
            binding.actionButton.setOnClickListener(v -> {

                if (eventUid == null) { // This is a new Event
                    if (eventCreationType == EventCreationType.REFERAL && tempCreate.equals(PERMANENT)) {
                        presenter.createEventPermanent(
                                enrollmentUid,
                                getTrackedEntityInstance,
                                programStageModel.uid(),
                                selectedDate,
                                selectedOrgUnit,
                                null,
                                catComboIsDefaultOrNull() ? null : selectedCatOptionCombo.uid(),
                                selectedLat, selectedLon);
                    } else if (eventCreationType == EventCreationType.SCHEDULE) {
                        presenter.scheduleEvent(
                                enrollmentUid,
                                programStageModel.uid(),
                                selectedDate,
                                selectedOrgUnit,
                                null,
                                catComboIsDefaultOrNull() ? null : selectedCatOptionCombo.uid(),
                                selectedLat, selectedLon);
                    } else {
                        presenter.createEvent(
                                enrollmentUid,
                                programStageModel.uid(),
                                selectedDate,
                                selectedOrgUnit,
                                null,
                                catComboIsDefaultOrNull() ? null : selectedCatOptionCombo.uid(),
                                selectedLat,
                                selectedLon,
                                getTrackedEntityInstance);
                    }
                } else {
                    presenter.editEvent(getTrackedEntityInstance, programStageModel.uid(), eventUid, DateUtils.databaseDateFormat().format(selectedDate), selectedOrgUnit, null,
                            catComboIsDefaultOrNull() ? null : selectedCatOptionCombo.uid(), selectedLat, selectedLon);
                    //TODO: WHERE TO UPDATE CHANGES IN DATE, ORGUNIT, CATCOMBO, COORDINATES
                    startFormActivity(eventUid);
                }
            });
        }
        Bindings.setObjectStyleAndTint(binding.programStageIcon, binding.programStageIcon, programStageUid);

    }

    private void setUpScrenByCreatinType(EventCreationType eventCreationType) {

        if (eventCreationType == EventCreationType.REFERAL) {
            binding.temp.setVisibility(View.VISIBLE);
            binding.oneTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    tempCreate = ONE_TIME;
                }
                checkActionButtonVisibility();
            });
            binding.permanent.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    tempCreate = PERMANENT;
                }
                checkActionButtonVisibility();
            });
        } else {
            binding.temp.setVisibility(View.GONE);
        }

        if (eventCreationType == EventCreationType.ADDNEW || eventCreationType == EventCreationType.SCHEDULE) {
            fixedOrgUnit = true;
            binding.orgUnit.setVisibility(View.GONE);
        } else {
            fixedOrgUnit = false;
            binding.orgUnit.setVisibility(View.VISIBLE);
            binding.orgUnit.setOnClickListener(v -> {
                if (!fixedOrgUnit)
                    presenter.onOrgUnitButtonClick();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this, programUid, eventUid, selectedOrgUnit, programStageUid);
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    private void initProgressBar() {
        binding.progressGains.setVisibility(eventUid == null ? View.GONE : View.VISIBLE);
        binding.progress.setVisibility(eventUid == null ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onUpdate(boolean lost, float value) {
        String text = String.valueOf((int) value) + "%";
        binding.progress.setText(text);
    }

    private void checkActionButtonVisibility() {

        if (eventUid == null) {
            if (isFormCompleted())
                binding.actionButton.setVisibility(View.VISIBLE); //If creating a new event, show only if minimun data is completed
            else
                binding.actionButton.setVisibility(View.GONE);

        } else {
            binding.actionButton.setVisibility(View.VISIBLE); //Show actionButton always for already created events
        }
    }

    private boolean isFormCompleted() {

        if (!catComboIsDefaultOrNull())
            return isCompleted(selectedDateString) &&
                    isCompleted(selectedOrgUnit) &&
                    isSelectedDateBetweenOpeningAndClosedDates() &&
                    selectedCatCombo != null && selectedCatOptionCombo != null &&
                    ((eventCreationType != EventCreationType.REFERAL) || (eventCreationType == EventCreationType.REFERAL && tempCreate != null));
        else
            return isCompleted(selectedDateString) &&
                    isCompleted(selectedOrgUnit) &&
                    isSelectedDateBetweenOpeningAndClosedDates() &&
                    ((eventCreationType != EventCreationType.REFERAL) || (eventCreationType == EventCreationType.REFERAL && tempCreate != null));
    }

    private boolean isSelectedDateBetweenOpeningAndClosedDates() {
        if (selectedDate == null)
            return false;
        boolean isAfterOpening = selectedOrgUnitOpeningDate == null || (selectedOrgUnitOpeningDate != null && selectedDate.after(selectedOrgUnitOpeningDate));
        boolean isBeforeClosed = selectedOrgUnitClosedDate == null || (selectedOrgUnitClosedDate != null && selectedDate.before(selectedOrgUnitClosedDate));
        return isAfterOpening && isBeforeClosed;

    }

    private boolean isCompleted(String field) {
        return field != null && !field.isEmpty();
    }

    @Override
    public void setProgram(@NonNull ProgramModel program) {
        this.program = program;

        String activityTitle;
        if (eventCreationType == EventCreationType.REFERAL) {
            activityTitle = program.displayName() + " - " + getString(R.string.referral);
        } else {
            activityTitle = eventUid == null ? program.displayName() + " - " + getString(R.string.new_event) : program.displayName();
        }
        binding.setName(activityTitle);

        if(eventModel==null) {
            Calendar now = DateUtils.getInstance().getCalendar();
            if (periodType == null) {

                if (eventCreationType != EventCreationType.SCHEDULE)
                    selectedDate = now.getTime();
                else {
                    now.add(Calendar.DAY_OF_YEAR, getIntent().getIntExtra(Constants.EVENT_SCHEDULE_INTERVAL, 0));
                    selectedDate = DateUtils.getInstance().getNextPeriod(null, now.getTime(), 1);
                }

                selectedDateString = DateUtils.uiDateFormat().format(selectedDate);

            } else {
                now.setTime(DateUtils.getInstance().getNextPeriod(periodType, now.getTime(), eventCreationType != EventCreationType.SCHEDULE ? 0 : 1));
                selectedDate = now.getTime();
                selectedDateString = DateUtils.getInstance().getPeriodUIString(periodType, selectedDate, Locale.getDefault());
            }

            binding.date.setText(selectedDateString);
        }

        binding.date.setOnClickListener(view -> {
            if (periodType == null)
                presenter.onDateClick(EventInitialActivity.this);
            else
                new PeriodDialog()
                        .setPeriod(periodType)
                        .setPossitiveListener(selectedDate -> {
                            this.selectedDate = selectedDate;
                            binding.date.setText(DateUtils.getInstance().getPeriodUIString(periodType, selectedDate, Locale.getDefault()));
                            binding.date.clearFocus();
                            if (!fixedOrgUnit)
                                binding.orgUnit.setText("");
                            presenter.filterOrgUnits(DateUtils.uiDateFormat().format(selectedDate));
                        })
                        .show(getSupportFragmentManager(), PeriodDialog.class.getSimpleName());
        });

        presenter.filterOrgUnits(DateUtils.uiDateFormat().format(selectedDate));


        if (program.captureCoordinates()) {
            binding.coordinatesLayout.setVisibility(View.VISIBLE);
            binding.location1.setOnClickListener(v -> presenter.onLocationClick());
            binding.location2.setOnClickListener(v -> presenter.onLocation2Click());
        }

        if (eventModel != null &&
                (DateUtils.getInstance().isEventExpired(null, eventModel.completedDate(), program.completeEventsExpiryDays()) ||
                        eventModel.status() == EventStatus.COMPLETED ||
                        eventModel.status() == EventStatus.SKIPPED)) {
            binding.date.setEnabled(false);
            binding.catCombo.setEnabled(false);
            binding.lat.setEnabled(false);
            binding.lon.setEnabled(false);
            binding.orgUnit.setEnabled(false);
            binding.location1.setEnabled(false);
            binding.location2.setEnabled(false);
            binding.temp.setEnabled(false);
            binding.actionButton.setText(getString(R.string.check_event));

        }
    }

    @Override
    public void openDrawer() {
        if (!binding.drawerLayout.isDrawerOpen(GravityCompat.END))
            binding.drawerLayout.openDrawer(GravityCompat.END);
        else
            binding.drawerLayout.closeDrawer(GravityCompat.END);
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
                    binding.orgUnit.setText(((OrganisationUnitModel) value).displayName());
                }
                binding.drawerLayout.closeDrawers();
            }
        });

        if (treeView.getSelected() != null && !treeView.getSelected().isEmpty() && !fixedOrgUnit) {
            binding.orgUnit.setText(((OrganisationUnitModel) treeView.getSelected().get(0).getValue()).displayName());
            selectedOrgUnit = ((OrganisationUnitModel) treeView.getSelected().get(0).getValue()).uid();
            selectedOrgUnitOpeningDate = ((OrganisationUnitModel) treeView.getSelected().get(0).getValue()).openingDate();
            selectedOrgUnitClosedDate = ((OrganisationUnitModel) treeView.getSelected().get(0).getValue()).closedDate();
        }
        checkActionButtonVisibility();
    }

    @Override
    public void setEvent(EventModel event) {

        binding.setEvent(event);

        if (event.eventDate() != null) {
            selectedDate = event.eventDate();
            binding.date.setText(DateUtils.uiDateFormat().format(selectedDate));
        }

        if (event.latitude() != null && event.longitude() != null) {
            runOnUiThread(() -> {
                if (isEmpty(savedLat)) {
                    binding.lat.setText(event.latitude());
                    binding.lon.setText(event.longitude());
                } else {
                    binding.lat.setText(savedLat);
                    binding.lon.setText(savedLon);
                    savedLon = null;
                    savedLat = null;
                }
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
        if (eventCreationType != EventCreationType.SCHEDULE && eventCreationType != EventCreationType.REFERAL) {
            startFormActivity(eventUid);
        } else {
            finish();
        }
    }

    @Override
    public void onEventUpdated(String eventUid) {
        startFormActivity(eventUid);
    }

    private void startFormActivity(String eventUid) {

//        if (enrollmentUid == null)+
        Intent intent = new Intent(this, EventCaptureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        intent.putExtras(EventCaptureActivity.getActivityBundle(eventUid, programUid));
        startActivity(intent);
        finish();
       /* else {
            FormViewArguments formViewArguments = FormViewArguments.createForEvent(eventUid);
            startActivity(FormActivity.create(getAbstractActivity(), formViewArguments, false));
            finish();
        }*/
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
        if (periodType == null)
            periodType = programStage.periodType();

        if (eventCreationType == EventCreationType.SCHEDULE)
            binding.dateLayout.setHint(getString(R.string.due_date));
        else if (programStage.executionDateLabel() != null)
            binding.dateLayout.setHint(programStage.executionDateLabel());
        else
            binding.dateLayout.setHint(getString(R.string.event_date));
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
                        //unused
                    }
                });


                if (eventModel != null)
                    presenter.getCatOption(eventModel.attributeOptionCombo());
            }


            if (periodType != null) {
                binding.dateLayout.setHint(periodType.name());
                presenter.getEvents(programUid, enrollmentUid, programStageUid, periodType);
            }
            checkActionButtonVisibility();
        });
    }

    @Override
    public void showDateDialog(DatePickerDialog.OnDateSetListener listener) {
        Calendar calendar = Calendar.getInstance();

        if(eventCreationType == EventCreationType.SCHEDULE)
            calendar.add(Calendar.DAY_OF_YEAR, getIntent().getIntExtra(Constants.EVENT_SCHEDULE_INTERVAL, 0));

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // ONLY FUTURE DATES
       /* if (eventCreationType == EventCreationType.SCHEDULE) {
//            if (getIntent().getIntExtra(Constants.EVENT_SCHEDULE_INTERVAL, 0) > 0)
            calendar.add(Calendar.DAY_OF_YEAR, getIntent().getIntExtra(Constants.EVENT_SCHEDULE_INTERVAL, 1));
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        }*/
        // ONLY PAST DATES AND TODAY
//        else {
        //If expiryPeriodType is not null set a minumn date
        if (program.expiryPeriodType() != null) {
            Date minDate = DateUtils.getInstance().expDate(null, program.expiryDays() == null ? 0 : program.expiryDays(), program.expiryPeriodType());
            datePickerDialog.getDatePicker().setMinDate(minDate.getTime());
        }
        if (eventCreationType != EventCreationType.SCHEDULE)
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
//        }
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        String date = String.format(Locale.getDefault(), "%s-%02d-%02d", year, month + 1, day);
        try {
            selectedDate = DateUtils.uiDateFormat().parse(date);
        } catch (ParseException e) {
            Timber.e(e);
        }
        selectedDateString = DateUtils.getInstance().getPeriodUIString(periodType, selectedDate, Locale.getDefault());
        binding.date.setText(selectedDateString);
        binding.date.clearFocus();
        if (!fixedOrgUnit)
            binding.orgUnit.setText("");
        presenter.filterOrgUnits(DateUtils.uiDateFormat().format(selectedDate));
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
            savedLat = data.getStringExtra(MapSelectorActivity.LATITUDE);
            savedLon = data.getStringExtra(MapSelectorActivity.LONGITUDE);
            setLocation(Double.valueOf(savedLat), Double.valueOf(savedLon));
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

        List<FieldViewModel> realUpdates = new ArrayList<>();
        if (sectionsToHide != null && !sectionsToHide.isEmpty()) {
            for (FieldViewModel fieldViewModel : updates)
                if (!sectionsToHide.contains(fieldViewModel.programStageSection()))
                    realUpdates.add(fieldViewModel);
        } else
            realUpdates.addAll(updates);

        int completedSectionFields = calculateCompletedFields(realUpdates);
        int totalSectionFields = realUpdates.size();
        totalFields = totalFields + totalSectionFields;
        totalCompletedFields = totalCompletedFields + completedSectionFields;
        float completionPerone = (float) totalCompletedFields / (float) totalFields;
        int completionPercent = (int) (completionPerone * 100);

        runOnUiThread(() -> {
            ProgressBarAnimation gainAnim = new ProgressBarAnimation(binding.progressGains, 0, completionPercent, false, EventInitialActivity.this);
            gainAnim.setDuration(PROGRESS_TIME);
            binding.progressGains.startAnimation(gainAnim);
        });

    }

    @Override
    public void setHideSection(String sectionUid) {
        if (sectionsToHide == null || sectionUid == null)
            sectionsToHide = new ArrayList<>();

        if (sectionUid != null && !sectionsToHide.contains(sectionUid))
            sectionsToHide.add(sectionUid);
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
    public void setReportDate(Date date) {
        selectedDate = date;
        selectedDateString = DateUtils.getInstance().getPeriodUIString(periodType, date, Locale.getDefault());
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
        selectedDate = null;
        binding.date.setText("");
        binding.executePendingBindings();
        checkActionButtonVisibility();
    }

    @Override
    public void setAccessDataWrite(Boolean canWrite) {
        if (!canWrite) {
            Boolean canWrite1 = canWrite;
            binding.date.setEnabled(false);
            binding.orgUnit.setEnabled(false);
            binding.catCombo.setEnabled(false);
            binding.actionButton.setText(getString(R.string.check_event));
            binding.location1.setEnabled(false);
            binding.location2.setEnabled(false);
            binding.lat.setEnabled(false);
            binding.lon.setEnabled(false);
        }

        if (!HelpManager.getInstance().isTutorialReadyForScreen(getClass().getName()))
            setTutorial();
    }

    @Override
    public void showOrgUnitSelector(List<OrganisationUnitModel> orgUnits) {
        Iterator<OrganisationUnitModel> iterator = orgUnits.iterator();
        while (iterator.hasNext()) {
            OrganisationUnitModel orgUnit = iterator.next();
            if (orgUnit.closedDate() != null && selectedDate.after(orgUnit.closedDate()))
                iterator.remove();
        }
        if (orgUnits != null && !orgUnits.isEmpty()) {
            orgUnitDialog = new OrgUnitDialog()
                    .setTitle(getString(R.string.org_unit))
                    .setMultiSelection(false)
                    .setOrgUnits(orgUnits)
                    .setPossitiveListener(data -> {
                        setOrgUnit(orgUnitDialog.getSelectedOrgUnit(), orgUnitDialog.getSelectedOrgUnitName());
                        orgUnitDialog.dismiss();
                    })
                    .setNegativeListener(data -> orgUnitDialog.dismiss());
            if (!orgUnitDialog.isAdded())
                orgUnitDialog.show(getSupportFragmentManager(), "ORG_UNIT_DIALOG");
        } else {
            showNoOrgUnits();
        }
    }

    @Override
    public void showQR() {
        Intent intent = new Intent(EventInitialActivity.this, QrEventsWORegistrationActivity.class);
        intent.putExtra(Constants.EVENT_UID, eventUid);
        startActivity(intent);
    }

    private boolean catComboIsDefaultOrNull() {
        return (selectedCatCombo == null || selectedCatCombo.isDefault() || CategoryComboModel.DEFAULT_UID.equals(selectedCatCombo.uid()));
    }

    @Override
    public void setTutorial() {
        super.setTutorial();

        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);

        new Handler().postDelayed(() -> {
            ArrayList<FancyShowCaseView> steps = new ArrayList<>();

            if (eventUid == null) {

                FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(getAbstractActivity())
                        .title(getString(R.string.tuto_event_initial_new_1))
                        .closeOnTouch(true)
                        .build();
                steps.add(tuto1);

                HelpManager.getInstance().setScreenHelp(getClass().getName(), steps);

                if (!prefs.getBoolean("TUTO_EVENT_INITIAL_NEW", false) && !BuildConfig.DEBUG) {
                    HelpManager.getInstance().showHelp();
                    prefs.edit().putBoolean("TUTO_EVENT_INITIAL_NEW", true).apply();
                }
            } else {

                FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(getAbstractActivity())
                        .title(getString(R.string.tuto_event_initial_1))
                        .focusOn(binding.percentage)
                        .closeOnTouch(true)
                        .build();
                steps.add(tuto1);

                HelpManager.getInstance().setScreenHelp(getClass().getName(), steps);

                if (!prefs.getBoolean("TUTO_EVENT_INITIAL", false) && !BuildConfig.DEBUG) {
                    HelpManager.getInstance().showHelp();
                    prefs.edit().putBoolean("TUTO_EVENT_INITIAL", true).apply();
                }
            }


        }, 500);
    }

    @Override
    public void showMoreOptions(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.BOTTOM);
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        popupMenu.getMenuInflater().inflate(R.menu.event_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.showHelp:
                    showTutorial(false);
                    break;
                case R.id.menu_delete:
                    confirmDeleteEvent();
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    public void confirmDeleteEvent() {
        new CustomDialog(
                this,
                getString(R.string.delete_event),
                getString(R.string.confirm_delete_event),
                getString(R.string.delete),
                getString(R.string.cancel),
                0,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        presenter.deleteEvent(getTrackedEntityInstance);
                    }

                    @Override
                    public void onNegative() {
                        // dismiss
                    }
                }
        ).show();
    }

    @Override
    public void showEventWasDeleted() {
        showToast(getString(R.string.event_was_deleted));
        finish();
    }
}