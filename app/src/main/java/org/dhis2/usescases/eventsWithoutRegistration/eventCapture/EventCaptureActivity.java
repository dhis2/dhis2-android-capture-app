package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import static org.dhis2.utils.Constants.PROGRAM_UID;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_EVENT;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.dhis2.commons.dialogs.AlertBottomDialog;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.commons.dialogs.DialogClickListener;
import org.dhis2.commons.popupmenu.AppMenuHelper;
import org.dhis2.databinding.ActivityEventCaptureBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.OnEditionListener;
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsComponent;
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsComponentProvider;
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsModule;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model.EventCompletionDialog;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.MapButtonObservable;
import org.dhis2.utils.Constants;
import org.dhis2.utils.EventMode;
import org.dhis2.utils.customviews.DataEntryBottomDialog;
import org.dhis2.utils.customviews.FormBottomDialog;
import org.dhis2.utils.customviews.FormBottomDialog.ActionType;
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import kotlin.Unit;

public class EventCaptureActivity extends ActivityGlobalAbstract implements EventCaptureContract.View, MapButtonObservable, EventDetailsComponentProvider {

    private static final int RQ_GO_BACK = 1202;
    private static final int NOTES_TAB_POSITION = 1;

    private ActivityEventCaptureBinding binding;
    @Inject
    EventCaptureContract.Presenter presenter;
    @Inject
    NavigationPageConfigurator pageConfigurator;

    private String programStageUid;
    private Boolean isEventCompleted = false;
    private EventMode eventMode;
    public EventCaptureComponent eventCaptureComponent;
    public String programUid;
    public String eventUid;
    private LiveData<Boolean> relationshipMapButton = new MutableLiveData<>(false);
    private OnEditionListener onEditionListener;
    private EventCapturePagerAdapter adapter;

    public static Bundle getActivityBundle(@NonNull String eventUid, @NonNull String programUid, @NonNull EventMode eventMode) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.EVENT_UID, eventUid);
        bundle.putString(Constants.PROGRAM_UID, programUid);
        bundle.putSerializable(Constants.EVENT_MODE, eventMode);
        return bundle;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        eventCaptureComponent = (ExtensionsKt.app(this)).userComponent().plus(
                new EventCaptureModule(
                        this,
                        getIntent().getStringExtra(Constants.EVENT_UID),
                        getContext()));
        eventCaptureComponent.inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_capture);
        binding.setPresenter(presenter);
        eventMode = (EventMode) getIntent().getSerializableExtra(Constants.EVENT_MODE);
        setUpViewPagerAdapter();
        setUpNavigationBar();
        showProgress();
        presenter.initNoteCounter();
        presenter.init();
    }

    private void setUpViewPagerAdapter() {
        binding.eventViewPager.setUserInputEnabled(false);
        this.adapter = new EventCapturePagerAdapter(
                this,
                getIntent().getStringExtra(PROGRAM_UID),
                getIntent().getStringExtra(Constants.EVENT_UID),
                pageConfigurator.displayAnalytics(),
                pageConfigurator.displayRelationships()
        );
        binding.eventViewPager.setAdapter(adapter);
        binding.eventViewPager.setCurrentItem(binding.navigationBar.getInitialPage(), false);
        ViewExtensionsKt.clipWithRoundedCorners(binding.eventViewPager, ExtensionsKt.getDp(16));
    }

    private void setUpNavigationBar() {
        binding.navigationBar.pageConfiguration(pageConfigurator);
        binding.navigationBar.setOnNavigationItemSelectedListener(item -> {
            binding.eventViewPager.setCurrentItem(adapter.getDynamicTabIndex(item.getItemId()));
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.refreshTabCounters();
    }

    @Override
    protected void onDestroy() {
        presenter.onDettach();
        super.onDestroy();
    }


    @Override
    public void goBack() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (onEditionListener != null) {
            onEditionListener.onEditionListener();
        }
        finishEditMode();
    }

    private void finishEditMode() {
        if (binding.navigationBar.isHidden()) {
            showNavigationBar();
        } else {
            attemptFinish();
        }
    }

    private void attemptFinish() {
        if (eventMode == EventMode.NEW) {
            AlertBottomDialog.Companion.getInstance()
                    .setTitle(getString(R.string.title_delete_go_back))
                    .setMessage(getString(R.string.discard_go_back))
                    .setPositiveButton(getString(R.string.keep_editing), null)
                    .setNegativeButton(getString(R.string.discard_changes), () -> {
                        presenter.deleteEvent();
                        return Unit.INSTANCE;
                    })
                    .show(getSupportFragmentManager(), AlertBottomDialog.class.getSimpleName());
        } else {
            finishDataEntry();
        }
    }

    @Override
    public void updatePercentage(float primaryValue) {
        binding.completion.setCompletionPercentage(primaryValue);
        if (!presenter.getCompletionPercentageVisibility()) {
            binding.completion.setVisibility(View.GONE);
        }
    }

    @Override
    public void showCompleteActions(
            boolean canComplete,
            String completeMessage,
            List<String> errors,
            Map<String, String> emptyMandatoryFields,
            EventCompletionDialog eventCompletionDialog) {
        if (binding.navigationBar.getSelectedItemId() == R.id.navigation_data_entry) {
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

            DataEntryBottomDialog dialog = new DataEntryBottomDialog(
                    eventCompletionDialog.getDataEntryDialogUiModel(),
                    () -> {
                        setAction(eventCompletionDialog.getMainButtonAction());
                        return Unit.INSTANCE;
                    },
                    () -> {
                        setAction(eventCompletionDialog.getSecondaryButtonAction());
                        return Unit.INSTANCE;
                    }
            );
            dialog.show(getSupportFragmentManager(), "SHOW_OPTIONS");
        }
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

    private void setAction(ActionType actionType) {
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

    @Override
    public void showSnackBar(int messageId) {
        Snackbar mySnackbar = Snackbar.make(binding.root, messageId, Snackbar.LENGTH_SHORT);
        mySnackbar.show();
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
    public void renderInitialInfo(String stageName, String eventDate, String orgUnit, String catOption) {
        binding.programStageName.setText(stageName);
        StringBuilder eventDataString = new StringBuilder(
                String.format("%s | %s", eventDate, orgUnit)
        );
        if (catOption != null && !catOption.isEmpty()) {
            eventDataString.append(
                    String.format(" | %s", catOption)
            );
        }
        binding.eventSecundaryInfo.setText(eventDataString);
    }

    @Override
    public EventCaptureContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void showMoreOptions(View view) {
        new AppMenuHelper.Builder().menu(this, R.menu.event_menu).anchor(view)
                .onMenuInflated(popupMenu -> {
                    popupMenu.getMenu().findItem(R.id.menu_delete).setVisible(presenter.canWrite() && presenter.isEnrollmentOpen());
                    popupMenu.getMenu().findItem(R.id.menu_share).setVisible(false);
                    return Unit.INSTANCE;
                })
                .onMenuItemClicked(itemId -> {
                    switch (itemId) {
                        case R.id.showHelp:
                            analyticsHelper().setEvent(SHOW_HELP, CLICK, SHOW_HELP);
                            showTutorial(false);
                            break;
                        case R.id.menu_delete:
                            confirmDeleteEvent();
                            break;
                        default:
                            break;
                    }
                    return false;
                })
                .build()
                .show();
    }

    @Override
    public void showTutorial(boolean shaked) {
        showToast(getString(R.string.no_intructions));
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
    public void showEventIntegrityAlert() {
        new MaterialAlertDialogBuilder(this, R.style.DhisMaterialDialog)
                .setTitle(R.string.conflict)
                .setMessage(R.string.event_date_in_future_message)
                .setPositiveButton(
                        R.string.change_event_date,
                        (dialogInterface, i) -> binding.navigationBar.selectItemAt(0)
                )
                .setNegativeButton(R.string.go_back, (dialogInterface, i) -> back())
                .setCancelable(false)
                .show();
    }

    @Override
    public void updateNoteBadge(int numberOfNotes) {
        binding.navigationBar.updateBadge(R.id.navigation_notes, numberOfNotes);
    }

    @Override
    public void showProgress() {
        runOnUiThread(() -> binding.toolbarProgress.show());
    }

    @Override
    public void hideProgress() {
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                        runOnUiThread(() ->
                                binding.toolbarProgress.hide()),
                1000);
    }

    @Override
    public void showNavigationBar() {
        binding.navigationBar.show();
    }

    @Override
    public void hideNavigationBar() {
        binding.navigationBar.hide();
    }

    @NotNull
    @Override
    public LiveData<Boolean> relationshipMap() {
        return relationshipMapButton;
    }

    @Override
    public void onRelationshipMapLoaded() {

    }

    public void setFormEditionListener(OnEditionListener onEditionListener) {
        this.onEditionListener = onEditionListener;
    }

    @Nullable
    @Override
    public EventDetailsComponent provideEventDetailsComponent(@Nullable EventDetailsModule module) {
        return eventCaptureComponent.plus(module);
    }
}