package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupMenu;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.databinding.ActivityEventCaptureBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment.EventCaptureFormFragment;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.CustomViews.CustomDialog;
import org.dhis2.utils.CustomViews.ProgressBarAnimation;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;
import timber.log.Timber;

import static org.dhis2.utils.Constants.PROGRAM_UID;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCaptureActivity extends ActivityGlobalAbstract implements EventCaptureContract.View, View.OnTouchListener, GestureDetector.OnGestureListener {

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    private GestureDetector gestureScanner;

    private ActivityEventCaptureBinding binding;
    @Inject
    EventCaptureContract.Presenter presenter;
    private int completionPercentage = 0;

    public static Bundle getActivityBundle(@NonNull String eventUid, @NonNull String programUid) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.EVENT_UID, eventUid);
        bundle.putString(Constants.PROGRAM_UID, programUid);
        return bundle;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(
                new EventCaptureModule(
                        getIntent().getStringExtra(Constants.EVENT_UID),
                        getIntent().getStringExtra(Constants.PROGRAM_UID)))
                .inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_capture);
        binding.setPresenter(presenter);
        gestureScanner = new GestureDetector(this, this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);

    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setUp() {
        binding.eventViewPager.setAdapter(new EventCapturePagerAdapter(getSupportFragmentManager()));
    }

    @Override
    public Consumer<Float> updatePercentage() {
        return percentage -> {
            int newPercentage = (int) (percentage * 100);

            ProgressBarAnimation gainAnim = new ProgressBarAnimation(binding.progressGains, completionPercentage, 0, newPercentage, false,
                    (lost, value) -> {
                        String text = String.valueOf((int) value) + "%";
                        binding.progress.setText(text);
                    });
            gainAnim.setDuration(1000);
            binding.progressGains.startAnimation(gainAnim);

            this.completionPercentage = (int) (percentage * 100);

        };
    }

    @Override
    public void setMandatoryWarning(Map<String, FieldViewModel> emptyMandatoryFields) {
        new CustomDialog(
                getAbstracContext(),
                getAbstracContext().getString(R.string.missing_mandatory_fields_title),
                getAbstracContext().getString(R.string.missing_mandatory_fields_events),
                getAbstracContext().getString(R.string.button_ok),
                "Check",
                Constants.RQ_MANDATORY_EVENTS,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        showCompleteActions(false);
                    }

                    @Override
                    public void onNegative() {
                        presenter.goToSection(emptyMandatoryFields.get(emptyMandatoryFields.entrySet().iterator().next().getKey()).programStageSection());
                    }
                })
                .show();
    }

    @Override
    public void showCompleteActions(boolean canComplete) {
        Utils.getPopUpMenu(this,
                EventCaptureFormFragment.getInstance().getSectionSelector(),
                Gravity.TOP,
                canComplete ? R.menu.event_form_complete_menu : R.menu.event_form_cant_complete_menu,
                item -> {
                    switch (item.getItemId()) {
                        case R.id.complete:
                            presenter.completeEvent(false);
                            break;
                        case R.id.completeAndAddNew:
                            presenter.completeEvent(true);
                            break;
                        case R.id.finishAndAddNew:
                            restartDataEntry();
                            break;
                        case R.id.completeLater:
                        case R.id.finish:
                            finishDataEntry();
                            break;
                    }
                    return false;
                },
                true).show();
    }

    @Override
    public void attemptToReopen() {
        Utils.getPopUpMenu(this,
                EventCaptureFormFragment.getInstance().getSectionSelector(),
                Gravity.TOP,
                R.menu.event_form_reopen_menu,
                item -> {
                    switch (item.getItemId()) {
                        case R.id.reopen:
                            presenter.reopenEvent();
                            break;
                        case R.id.finish:
                            finishDataEntry();
                            break;
                    }
                    return false;
                },
                true).show();
    }

    @Override
    public void attemptToSkip() {
        Utils.getPopUpMenu(this,
                EventCaptureFormFragment.getInstance().getSectionSelector(),
                Gravity.TOP,
                R.menu.event_form_overdue_menu,
                item -> {
                    switch (item.getItemId()) {
                        case R.id.skip:
                            presenter.skipEvent();
                            break;
                        case R.id.finish:
                            finishDataEntry();
                            break;
                    }
                    return false;
                },
                true).show();
    }

    @Override
    public void attemptToReschedule() {
        Utils.getPopUpMenu(this,
                EventCaptureFormFragment.getInstance().getSectionSelector(),
                Gravity.TOP,
                R.menu.event_form_skip_menu,
                item -> {
                    switch (item.getItemId()) {
                        case R.id.reschedule:
                            //TODO: OPEN DATE SELECTOR
                            break;
                        case R.id.finish:
                            finishDataEntry();
                            break;
                    }
                    return false;
                },
                true).show();
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
                "Check",
                Constants.RQ_MANDATORY_EVENTS,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        showCompleteActions(false);
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
        showCompleteActions(canComplete);
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
                case R.id.menu_overview:
                    goToInitialScreen();
                    break;
            }
            return false;
        });
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
}
