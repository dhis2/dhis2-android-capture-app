package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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
import org.dhis2.databinding.CategorySelectorBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.map.MapSelectorActivity;
import org.dhis2.usescases.qrCodes.eventsworegistration.QrEventsWORegistrationActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.EventCreationType;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.custom_views.CategoryOptionPopUp;
import org.dhis2.utils.custom_views.CustomDialog;
import org.dhis2.utils.custom_views.OrgUnitDialog;
import org.dhis2.utils.custom_views.PeriodDialog;
import org.dhis2.utils.custom_views.ProgressBarAnimation;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
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

    //Bundle variables
    private String programUid;
    private String eventUid;
    private EventCreationType eventCreationType;
    private String getTrackedEntityInstance;
    private String enrollmentUid;
    private String selectedOrgUnit;
    private PeriodType periodType;
    private String programStageUid;
    private EnrollmentStatus enrollmentStatus;
    private int eventScheduleInterval;

    private String selectedDateString;
    private Date selectedDate;
    private Date selectedOrgUnitOpeningDate;
    private Date selectedOrgUnitClosedDate;
    private ProgramStageModel programStageModel;

    private int totalFields;
    private int totalCompletedFields;
    private String tempCreate;
    private boolean fixedOrgUnit;
    private String catOptionComboUid;
    private CategoryCombo catCombo;
    private Map<String, CategoryOption> selectedCatOption;
    private OrgUnitDialog orgUnitDialog;
    private ProgramModel program;
    private String savedLat;
    private String savedLon;
    private ArrayList<String> sectionsToHide;
    private Boolean accessData;

    public static Bundle getBundle(String programUid, String eventUid, String eventCreationType,
                                   String teiUid, PeriodType eventPeriodType, String orgUnit, String stageUid,
                                   String enrollmentUid, int eventScheduleInterval, EnrollmentStatus enrollmentStatus) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.PROGRAM_UID, programUid);
        bundle.putString(Constants.EVENT_UID, eventUid);
        bundle.putString(Constants.EVENT_CREATION_TYPE, eventCreationType);
        bundle.putString(Constants.TRACKED_ENTITY_INSTANCE, teiUid);
        bundle.putString(Constants.ENROLLMENT_UID, enrollmentUid);
        bundle.putString(Constants.ORG_UNIT, orgUnit);
        bundle.putSerializable(Constants.EVENT_PERIOD_TYPE, eventPeriodType);
        bundle.putString(Constants.PROGRAM_STAGE_UID, stageUid);
        bundle.putInt(Constants.EVENT_SCHEDULE_INTERVAL, eventScheduleInterval);
        bundle.putSerializable(Constants.ENROLLMENT_STATUS, enrollmentStatus);
        return bundle;
    }

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
        enrollmentStatus = (EnrollmentStatus) getIntent().getSerializableExtra(Constants.ENROLLMENT_STATUS);
        eventScheduleInterval = getIntent().getIntExtra(Constants.EVENT_SCHEDULE_INTERVAL, 0);

        ((App) getApplicationContext()).userComponent().plus(new EventInitialModule(eventUid)).inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_initial);
        binding.setPresenter(presenter);
        this.selectedCatOption = new HashMap<>();

        setUpScrenByCreatinType(eventCreationType);

        initProgressBar();

        if (eventUid == null) {
            if (binding.actionButton != null)
                binding.actionButton.setText(R.string.next);
        } else {
            if (binding.actionButton != null)
                binding.actionButton.setText(R.string.update);
        }

        if (binding.actionButton != null) {
            binding.actionButton.setOnClickListener(v -> {
                String programStageModelUid = programStageModel == null ? "" : programStageModel.uid();
                if (eventUid == null) { // This is a new Event
                    if (eventCreationType == EventCreationType.REFERAL && tempCreate.equals(PERMANENT)) {
                        presenter.createEventPermanent(
                                enrollmentUid,
                                getTrackedEntityInstance,
                                programStageModelUid,
                                selectedDate,
                                selectedOrgUnit,
                                null,
                                catOptionComboUid,
                                isEmpty(binding.lat.getText()) ? null : binding.lat.getText().toString(),
                                isEmpty(binding.lon.getText()) ? null : binding.lon.getText().toString()
                        );
                    } else if (eventCreationType == EventCreationType.SCHEDULE) {
                        presenter.scheduleEvent(
                                enrollmentUid,
                                programStageModelUid,
                                selectedDate,
                                selectedOrgUnit,
                                null,
                                catOptionComboUid,
                                isEmpty(binding.lat.getText()) ? null : binding.lat.getText().toString(),
                                isEmpty(binding.lon.getText()) ? null : binding.lon.getText().toString()
                        );
                    } else {
                        presenter.createEvent(
                                enrollmentUid,
                                programStageModelUid,
                                selectedDate,
                                selectedOrgUnit,
                                null,
                                catOptionComboUid,
                                isEmpty(binding.lat.getText()) ? null : binding.lat.getText().toString(),
                                isEmpty(binding.lon.getText()) ? null : binding.lon.getText().toString(),
                                getTrackedEntityInstance);
                    }
                } else {
                    presenter.editEvent(getTrackedEntityInstance,
                            programStageModelUid,
                            eventUid,
                            DateUtils.databaseDateFormat().format(selectedDate), selectedOrgUnit, null,
                            catOptionComboUid,
                            isEmpty(binding.lat.getText()) ? null : binding.lat.getText().toString(),
                            isEmpty(binding.lon.getText()) ? null : binding.lon.getText().toString()
                    );
                    startFormActivity(eventUid);
                }
            });
        }
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

        if (eventCreationType == EventCreationType.SCHEDULE) {
            fixedOrgUnit = true;
            binding.orgUnitLayout.setVisibility(View.GONE);
        } else {
            fixedOrgUnit = false;
            binding.orgUnitLayout.setVisibility(View.VISIBLE);
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

    @Override
    public void checkActionButtonVisibility() {

        if (eventUid == null) {
            if (isFormCompleted())
                binding.actionButton.setVisibility(View.VISIBLE); //If creating a new event, show only if minimun data is completed
            else
                binding.actionButton.setVisibility(View.GONE);

        } else {
            if (eventModel != null) {
                if (eventModel.status() == EventStatus.OVERDUE && enrollmentStatus == EnrollmentStatus.CANCELLED)
                    binding.actionButton.setVisibility(View.GONE);
            } else
                binding.actionButton.setVisibility(View.VISIBLE); //Show actionButton always for already created events
        }
    }

    private boolean isFormCompleted() {

        if (!catComboIsDefaultOrNull())
            return isCompleted(selectedDateString) &&
                    isCompleted(selectedOrgUnit) &&
                    isSelectedDateBetweenOpeningAndClosedDates() &&
                    catCombo != null && catCombo.categories() != null && selectedCatOption.size() == catCombo.categories().size() &&
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
        boolean isAfterOpening = selectedOrgUnitOpeningDate == null || selectedDate.after(selectedOrgUnitOpeningDate);
        boolean isBeforeClosed = selectedOrgUnitClosedDate == null || selectedDate.before(selectedOrgUnitClosedDate);
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
            if (eventModel != null && !isEmpty(eventModel.enrollment()) && eventCreationType != EventCreationType.ADDNEW) {
                binding.orgUnit.setEnabled(false);
                binding.orgUnitLayout.setVisibility(View.GONE);
            }

            activityTitle = eventUid == null ? program.displayName() + " - " + getString(R.string.new_event) : program.displayName();
        }
        binding.setName(activityTitle);

        if (eventModel == null) {
            Calendar now = DateUtils.getInstance().getCalendar();
            if (periodType == null) {

                if (eventCreationType != EventCreationType.SCHEDULE)
                    selectedDate = now.getTime();
                else {
                    now.add(Calendar.DAY_OF_YEAR, eventScheduleInterval);
                    selectedDate = DateUtils.getInstance().getNextPeriod(null, now.getTime(), 1);
                }

                selectedDateString = DateUtils.uiDateFormat().format(selectedDate);

            } else {
                now.setTime(DateUtils.getInstance().getNextPeriod(periodType, now.getTime(), eventCreationType != EventCreationType.SCHEDULE ? 0 : 1));
                selectedDate = now.getTime();
                selectedDateString = DateUtils.getInstance().getPeriodUIString(periodType, selectedDate, Locale.getDefault());
            }

            binding.date.setText(selectedDateString);
        } else {
            if (!isEmpty(eventModel.enrollment()) && eventCreationType != EventCreationType.ADDNEW) {
                binding.orgUnit.setEnabled(false);
                binding.orgUnitLayout.setVisibility(View.GONE);
            }
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

        if (eventModel != null &&
                (DateUtils.getInstance().isEventExpired(eventModel.eventDate(), eventModel.completedDate(), eventModel.status(), program.completeEventsExpiryDays(), program.expiryPeriodType(), program.expiryDays()) ||
                        eventModel.status() == EventStatus.COMPLETED ||
                        eventModel.status() == EventStatus.SKIPPED)) {
            binding.date.setEnabled(false);
            for (int i = 0; i < binding.catComboLayout.getChildCount(); i++)
                binding.catComboLayout.getChildAt(i).findViewById(R.id.cat_combo).setEnabled(false);
            binding.lat.setEnabled(false);
            binding.lon.setEnabled(false);
            binding.orgUnit.setEnabled(false);
            binding.location1.setClickable(false);
            binding.location2.setClickable(false);
            binding.location1.setEnabled(false);
            binding.location2.setEnabled(false);
            binding.temp.setEnabled(false);
            binding.actionButton.setText(getString(R.string.check_event));
            binding.executePendingBindings();

        }

        if (program.captureCoordinates()) {
            binding.coordinatesLayout.setVisibility(View.VISIBLE);
            if (binding.location1.isClickable())
                binding.location1.setOnClickListener(v -> presenter.onLocationClick());
            if (binding.location2.isClickable())
                binding.location2.setOnClickListener(v -> presenter.onLocation2Click());
        } else
            binding.coordinatesLayout.setVisibility(View.GONE);


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
        catOptionComboUid = event.attributeOptionCombo();

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
        Intent intent = new Intent(this, EventCaptureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        intent.putExtras(EventCaptureActivity.getActivityBundle(eventUid, programUid));
        startActivity(intent);
        finish();
    }

    @Override
    public void setProgramStage(ProgramStageModel programStage) {
        this.programStageModel = programStage;
        if (programStageModel.captureCoordinates()) {
            binding.coordinatesLayout.setVisibility(View.VISIBLE);
            binding.location1.setOnClickListener(v -> {
                if (v.isClickable()) presenter.onLocationClick();
            });
            binding.location2.setOnClickListener(v -> {
                if (v.isClickable()) presenter.onLocation2Click();
            });
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

        if (eventCreationType == EventCreationType.SCHEDULE && programStage.hideDueDate()) {
            binding.dateLayout.setVisibility(View.GONE);

            Calendar now = DateUtils.getInstance().getCalendar();
            if (periodType == null) {
                now.add(Calendar.DAY_OF_YEAR, eventScheduleInterval);
                selectedDate = DateUtils.getInstance().getNextPeriod(null, now.getTime(), 0);
                selectedDateString = DateUtils.uiDateFormat().format(selectedDate);
            } else {
                now.setTime(DateUtils.getInstance().getNextPeriod(periodType, now.getTime(), eventCreationType != EventCreationType.SCHEDULE ? 0 : 1));
                selectedDate = now.getTime();
                selectedDateString = DateUtils.getInstance().getPeriodUIString(periodType, selectedDate, Locale.getDefault());
            }
        }
        presenter.getStageObjectStyle(programStageModel.uid());
    }

    @Override
    public void setCatComboOptions(CategoryCombo catCombo, Map<String, CategoryOption> stringCategoryOptionMap) {

        runOnUiThread(() -> {
            this.catCombo = catCombo;
            if (stringCategoryOptionMap != null)
                selectedCatOption = stringCategoryOptionMap;

            binding.catComboLayout.removeAllViews();

            if (!catCombo.isDefault() && catCombo.categories() != null)
                for (Category category : catCombo.categories()) {
                    CategorySelectorBinding catSelectorBinding = CategorySelectorBinding.inflate(LayoutInflater.from(this));
                    catSelectorBinding.catCombLayout.setHint(category.displayName());
                    catSelectorBinding.catCombo.setOnClickListener(
                            view ->
                                    CategoryOptionPopUp.getInstance()
                                            .setCategory(category)
                                            .setOnClick(item -> {
                                                if (item != null)
                                                    selectedCatOption.put(category.uid(), item);
                                                else
                                                    selectedCatOption.remove(category.uid());
                                                catSelectorBinding.catCombo.setText(item != null ? item.displayName() : null);
                                                if (selectedCatOption.size() == catCombo.categories().size()) {
                                                    catOptionComboUid = presenter.getCatOptionCombo(catCombo.categoryOptionCombos(), new ArrayList<>(selectedCatOption.values()));
                                                    checkActionButtonVisibility();
                                                }
                                            })
                                            .show(this, catSelectorBinding.getRoot())
                    );

                    if (stringCategoryOptionMap != null && stringCategoryOptionMap.get(category.uid()) != null)
                        catSelectorBinding.catCombo.setText(stringCategoryOptionMap.get(category.uid()).displayName());

                    binding.catComboLayout.addView(catSelectorBinding.getRoot());
                }
            else if (catCombo.isDefault())
                catOptionComboUid = catCombo.categoryOptionCombos().get(0).uid();

        });
    }

    @Override
    public void showDateDialog(DatePickerDialog.OnDateSetListener listener) {
        showNativeCalendar(listener);
    }

    private void showNativeCalendar(DatePickerDialog.OnDateSetListener listener) {
        Calendar calendar = Calendar.getInstance();

        if (selectedDate != null)
            calendar.setTime(selectedDate);

        if (eventCreationType == EventCreationType.SCHEDULE)
            calendar.add(Calendar.DAY_OF_YEAR, eventScheduleInterval);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        if (program.expiryPeriodType() != null) {
            Date minDate = DateUtils.getInstance().expDate(null, program.expiryDays() == null ? 0 : program.expiryDays(), program.expiryPeriodType());
            datePickerDialog.getDatePicker().setMinDate(minDate.getTime());
        }
        if (eventCreationType != EventCreationType.SCHEDULE)
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> {
                datePickerDialog.dismiss();
                showCustomCalendar(listener);
            });
        }

        datePickerDialog.show();
    }

    private void showCustomCalendar(DatePickerDialog.OnDateSetListener listener) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View datePickerView = layoutInflater.inflate(R.layout.widget_datepicker, null);
        final DatePicker datePicker = datePickerView.findViewById(R.id.widget_datepicker);

        Calendar calendar = Calendar.getInstance();

        if (selectedDate != null)
            calendar.setTime(selectedDate);

        if (eventCreationType == EventCreationType.SCHEDULE)
            calendar.add(Calendar.DAY_OF_YEAR, eventScheduleInterval);

        datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        if (program.expiryPeriodType() != null) {
            Date minDate = DateUtils.getInstance().expDate(null, program.expiryDays() == null ? 0 : program.expiryDays(), program.expiryPeriodType());
            datePicker.setMinDate(minDate.getTime());
        }
        if (eventCreationType != EventCreationType.SCHEDULE)
            datePicker.setMaxDate(System.currentTimeMillis() - 1000);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), R.style.DatePickerTheme)
                .setPositiveButton(R.string.action_accept, (dialog, which) -> {
                    listener.onDateSet(datePicker, datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                })
                .setNeutralButton(getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> showNativeCalendar(listener));

        alertDialog.setView(datePickerView);
        Dialog dialog = alertDialog.create();
        dialog.show();
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
        return this::swap;
    }

    @Override
    public void showProgramStageSelection() {
        presenter.getProgramStage(programStageUid);
    }

    void swap(@NonNull List<FieldViewModel> updates) {

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

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
            runOnUiThread(() -> {
                ProgressBarAnimation gainAnim = new ProgressBarAnimation(binding.progressGains, 0, completionPercent, false, EventInitialActivity.this);
                gainAnim.setDuration(PROGRESS_TIME);
                binding.progressGains.startAnimation(gainAnim);
            });
        else {
            binding.progressGains.setProgress(completionPercent);
            String text = String.valueOf(completionPercent) + "%";
            binding.progress.setText(text);
        }

    }

    @Override
    public void setHideSection(String sectionUid) {
        if (sectionsToHide == null || sectionUid == null)
            sectionsToHide = new ArrayList<>();

        if (sectionUid != null && !sectionsToHide.contains(sectionUid))
            sectionsToHide.add(sectionUid);
    }

    @Override
    public void renderObjectStyle(ObjectStyleModel data) {
        if (data.icon() != null) {
            Resources resources = getResources();
            String iconName = data.icon().startsWith("ic_") ? data.icon() : "ic_" + data.icon();
            int icon = resources.getIdentifier(iconName, "drawable", getPackageName());
            binding.programStageIcon.setImageResource(icon);
        }

        if (data.color() != null) {
            String color = data.color().startsWith("#") ? data.color() : "#" + data.color();
            int colorRes = Color.parseColor(color);
            ColorStateList colorStateList = ColorStateList.valueOf(colorRes);
            ViewCompat.setBackgroundTintList(binding.programStageIcon, colorStateList);
            Bindings.setFromResBgColor(binding.programStageIcon, colorRes);
        }
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
        this.accessData = canWrite;
        if (!canWrite || !presenter.isEnrollmentOpen()) {
            binding.date.setEnabled(false);
            binding.date.setClickable(false);
            binding.orgUnit.setEnabled(false);
            for (int i = 0; i < binding.catComboLayout.getChildCount(); i++)
                binding.catComboLayout.getChildAt(i).findViewById(R.id.cat_combo).setEnabled(false);
            binding.actionButton.setText(getString(R.string.check_event));
            binding.location1.setClickable(false);
            binding.location2.setClickable(false);
            binding.location1.setEnabled(false);
            binding.location2.setEnabled(false);
            binding.lat.setEnabled(false);
            binding.lon.setEnabled(false);
            binding.executePendingBindings();
        }

        if (!HelpManager.getInstance().isTutorialReadyForScreen(getClass().getName()))
            setTutorial();
    }

    @Override
    public void showOrgUnitSelector(List<OrganisationUnitModel> orgUnits) {
        if (orgUnits != null && !orgUnits.isEmpty()) {

            Iterator<OrganisationUnitModel> iterator = orgUnits.iterator();
            while (iterator.hasNext()) {
                OrganisationUnitModel orgUnit = iterator.next();
                if (orgUnit.closedDate() != null && selectedDate.after(orgUnit.closedDate()))
                    iterator.remove();
            }

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
        return (catCombo == null || catCombo.isDefault());
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
            Timber.e(e);
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
                default:
                    break;
            }
            return false;
        });
        popupMenu.getMenu().getItem(1).setVisible(accessData && presenter.isEnrollmentOpen());
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