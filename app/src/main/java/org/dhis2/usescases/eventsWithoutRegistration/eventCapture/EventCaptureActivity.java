package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.snackbar.Snackbar;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ActivityEventCaptureBinding;
import org.dhis2.databinding.WidgetDatepickerBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.EventMode;
import org.dhis2.utils.FileResourcesUtil;
import org.dhis2.utils.customviews.CustomDialog;
import org.dhis2.utils.customviews.FormBottomDialog;
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;
import timber.log.Timber;

import static org.dhis2.utils.Constants.PROGRAM_UID;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_EVENT;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCaptureActivity extends ActivityGlobalAbstract implements EventCaptureContract.View, View.OnTouchListener, GestureDetector.OnGestureListener {

    private static final int RQ_GO_BACK = 1202;

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    private GestureDetector gestureScanner;

    private ActivityEventCaptureBinding binding;
    @Inject
    EventCaptureContract.Presenter presenter;
    private String programStageUid;
    private Boolean isEventCompleted = false;
    private EventMode eventMode;

    public static Bundle getActivityBundle(@NonNull String eventUid, @NonNull String programUid, @NonNull EventMode eventMode) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.EVENT_UID, eventUid);
        bundle.putString(Constants.PROGRAM_UID, programUid);
        bundle.putSerializable(Constants.EVENT_MODE, eventMode);
        return bundle;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(
                new EventCaptureModule(
                        this,
                        getIntent().getStringExtra(Constants.EVENT_UID)))
                .inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_capture);
        binding.setPresenter(presenter);
        eventMode = (EventMode) getIntent().getSerializableExtra(Constants.EVENT_MODE);
        gestureScanner = new GestureDetector(this, this);

        binding.calculationIndicator.text.setTextColor(ColorUtils.getContrastColor(ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY_LIGHT)));

        binding.eventViewPager.setAdapter(new EventCapturePagerAdapter(getSupportFragmentManager()));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        presenter.onDettach();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(eventMode == EventMode.NEW) {
            new CustomDialog(
                    this,
                    getString(R.string.title_delete_go_back),
                    getString(R.string.delete_go_back),
                    getString(R.string.cancel),
                    getString(R.string.missing_mandatory_fields_go_back),
                    RQ_GO_BACK,
                    new DialogClickListener() {
                        @Override
                        public void onPositive() { }

                        @Override
                        public void onNegative() {
                            presenter.deleteEvent();
                        }
                    }
            ).show();
        } else {
            finishDataEntry();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.GALLERY_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    presenter.saveImage(uuid, FileResourcesUtil.getFileFromGallery(this, imageUri).getPath());
                    presenter.nextCalculation(true);
                }
                break;
            case Constants.CAMERA_REQUEST:
                if (resultCode == RESULT_OK) {
                    File file = new File(FileResourceDirectoryHelper.getFileResourceDirectory(this), "tempFile.png");
                    if (file.exists()) {
                        presenter.saveImage(uuid, file.getPath());
                    } else
                        presenter.saveImage(uuid, null);
                    presenter.nextCalculation(true);
                }
                break;
        }
    }

    @Override
    public Consumer<Pair<Float, Float>> updatePercentage() {
        return pair -> {
            binding.completion.setCompletionPercentage(pair.val0());
            binding.completion.setSecondaryPercentage(pair.val1());
        };
    }

    @Override
    public void showCompleteActions(boolean canComplete, String completeMessage, Map<String, String> errors, Map<String, FieldViewModel> emptyMandatoryFields) {

        FormBottomDialog.getInstance()
                .setAccessDataWrite(presenter.canWrite())
                .setIsEnrollmentOpen(presenter.isEnrollmentOpen())
                .setIsExpired(presenter.hasExpired())
                .setCanComplete(canComplete)
                .setListener(this::setAction)
                .setMessageOnComplete(completeMessage)
                .setEmptyMandatoryFields(emptyMandatoryFields)
                .setFieldsWithErrors(!errors.isEmpty())
                .setMandatoryFields(!emptyMandatoryFields.isEmpty())
                .show(getSupportFragmentManager(), "SHOW_OPTIONS");
    }

    @Override
    public void attemptToReopen() {
        FormBottomDialog.getInstance()
                .setAccessDataWrite(presenter.canWrite())
                .setIsExpired(presenter.hasExpired())
                .setReopen(true)
                .setListener(this::setAction)
                .show(getSupportFragmentManager(), "SHOW_OPTIONS");
    }

    @Override
    public void attemptToSkip() {

        FormBottomDialog.getInstance()
                .setAccessDataWrite(presenter.canWrite())
                .setIsExpired(presenter.hasExpired())
                .setSkip(true)
                .setListener(this::setAction)
                .show(getSupportFragmentManager(), "SHOW_OPTIONS");
    }

    @Override
    public void attemptToReschedule() {
        FormBottomDialog.getInstance()
                .setAccessDataWrite(presenter.canWrite())
                .setIsExpired(presenter.hasExpired())
                .setReschedule(true)
                .setListener(this::setAction)
                .show(getSupportFragmentManager(), "SHOW_OPTIONS");
    }

    @Override
    public void setProgramStage(String programStageUid) {
        this.programStageUid = programStageUid;
    }

    @Override
    public void showRuleCalculation(Boolean shouldShow) {
        binding.calculationIndicator.getRoot().setVisibility(shouldShow ? View.VISIBLE : View.GONE);
    }

    private void setAction(FormBottomDialog.ActionType actionType) {
        switch (actionType) {
            case COMPLETE:
                isEventCompleted = true;
                presenter.completeEvent(false);
                break;
            case COMPLETE_ADD_NEW:
                presenter.completeEvent(true);
                break;
            case FINISH_ADD_NEW:
                restartDataEntry();
                break;
            case REOPEN:
                presenter.reopenEvent();
                break;
            case SKIP:
                presenter.skipEvent();
                break;
            case RESCHEDULE:
                reschedule();
                break;
            case CHECK_FIELDS:
                presenter.goToSection();
                break;
            case FINISH:
            default:
                finishDataEntry();
                break;
        }
    }

    @Override
    public void showErrorSnackBar() {
        showSnackBar(R.string.fix_error);
    }

    private void reschedule() {

    }

    private void showNativeCalendar() {
        Calendar calendar = DateUtils.getInstance().getCalendar();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar chosenDate = Calendar.getInstance();
            chosenDate.set(year, month, dayOfMonth);
            presenter.rescheduleEvent(chosenDate.getTime());
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> {
                datePickerDialog.dismiss();
                showCustomCalendar();
            });
        }

        datePickerDialog.show();
    }

    private void showCustomCalendar() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        WidgetDatepickerBinding widgetBinding = WidgetDatepickerBinding.inflate(layoutInflater);
        final DatePicker datePicker = widgetBinding.widgetDatepicker;

        Calendar c = DateUtils.getInstance().getCalendar();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        datePicker.updateDate(year, month, day);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), R.style.DatePickerTheme);

        alertDialog.setView(widgetBinding.getRoot());
        Dialog dialog = alertDialog.create();

        widgetBinding.changeCalendarButton.setOnClickListener(calendarButton -> {
            showNativeCalendar();
            dialog.dismiss();
        });
        widgetBinding.clearButton.setOnClickListener(clearButton -> dialog.dismiss());
        widgetBinding.acceptButton.setOnClickListener(acceptButton -> {
            Calendar chosenDate = Calendar.getInstance();
            chosenDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            presenter.rescheduleEvent(chosenDate.getTime());
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public void showSnackBar(int messageId) {
        Snackbar mySnackbar = Snackbar.make(binding.root, messageId, Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }

    @Override
    public View getSnackbarAnchor() {
        return binding.getRoot();
    }

    @Override
    public void clearFocus() {
        binding.root.requestFocus();
    }


    @Override
    public void restartDataEntry() {
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, getIntent().getStringExtra(Constants.PROGRAM_UID));
        startActivity(EventInitialActivity.class, bundle, true, false, null);
    }

    @Override
    public void finishDataEntry() {
        Intent intent = new Intent();
        if (isEventCompleted)
            intent.putExtra(Constants.EVENT_UID, getIntent().getStringExtra(Constants.EVENT_UID));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void setShowError(Map<String, String> errors) {
        new CustomDialog(
                getAbstracContext(),
                getAbstracContext().getString(R.string.error_fields_title),
                getAbstracContext().getString(R.string.error_fields_events),
                getAbstracContext().getString(R.string.button_ok),
                getString(R.string.check_mandatory_field),
                Constants.RQ_MANDATORY_EVENTS,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        showCompleteActions(false, null, errors, null);
                    }

                    @Override
                    public void onNegative() {
                        presenter.goToSection(errors.entrySet().iterator().next().getKey());
                    }
                })
                .show();
    }

    @Override
    public void showMessageOnComplete(boolean canComplete, String completeMessage) {
        String title = canComplete ?
                getString(R.string.warning_on_complete_title) :
                getString(R.string.error_on_complete_title);
        showInfoDialog(title, completeMessage);
    }

    @Override
    public void attemptToFinish(boolean canComplete) {
        FormBottomDialog.getInstance()
                .setAccessDataWrite(presenter.canWrite())
                .setIsExpired(presenter.hasExpired())
                .setMandatoryFields(canComplete)
                .setCanComplete(canComplete)
                .setListener(this::setAction)
                .show(getSupportFragmentManager(), "SHOW_OPTIONS");
    }


    @Override
    public void renderInitialInfo(String stageName, String eventDate, String orgUnit, String catOption) {
        binding.programStageName.setText(stageName);
        binding.eventSecundaryInfo.setText(String.format("%s | %s | %s", eventDate, orgUnit, catOption));
    }

    @Override
    public EventCaptureContract.Presenter getPresenter() {
        return presenter;
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
                    analyticsHelper().setEvent(SHOW_HELP, CLICK, SHOW_HELP);
                    showTutorial(false);
                    break;
                case R.id.menu_delete:
                    confirmDeleteEvent();
                    break;
                case R.id.menu_overview:
                    goToInitialScreen();
                    break;
                default:
                    break;
            }
            return false;
        });
        popupMenu.getMenu().getItem(1).setVisible(presenter.canWrite() && presenter.isEnrollmentOpen());
        popupMenu.show();
    }

    @Override
    public void showTutorial(boolean shaked) {
        showToast(getString(R.string.no_intructions));
    }

    private void goToInitialScreen() {
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, getIntent().getStringExtra(Constants.PROGRAM_UID));
        bundle.putString(Constants.EVENT_UID, getIntent().getStringExtra(Constants.EVENT_UID));
        bundle.putString(Constants.EVENT_UID, getIntent().getStringExtra(Constants.EVENT_UID));
        bundle.putString(Constants.PROGRAM_STAGE_UID, programStageUid);
        startActivity(EventInitialActivity.class, bundle, true, false, null);
    }

    private void confirmDeleteEvent() {
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
                        analyticsHelper().setEvent(DELETE_EVENT, CLICK, DELETE_EVENT);
                        presenter.deleteEvent();
                    }

                    @Override
                    public void onNegative() {
                        // dismiss
                    }
                }
        ).show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return gestureScanner.onTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        return gestureScanner.onTouchEvent(me);
    }

    public boolean onDown(MotionEvent e) {
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float
            velocityX, float velocityY) {
        boolean result = false;
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                }
            }
        } catch (Exception exception) {
            Timber.e(exception);
        }
        return result;
    }

    public void onLongPress(MotionEvent e) {
        // nothing
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float
            distanceX, float distanceY) {
        return true;
    }

    public void onShowPress(MotionEvent e) {
        // nothing
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureScanner.onTouchEvent(event);
    }

    public void onSwipeRight() {
        presenter.onPreviousSection();
    }

    public void onSwipeLeft() {
        presenter.onNextSection();
    }

    @Override
    public void back() {
        onBackPressed();
    }
}
