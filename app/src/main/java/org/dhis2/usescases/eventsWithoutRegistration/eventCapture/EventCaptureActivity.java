package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

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
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.commons.dialogs.DialogClickListener;
import org.dhis2.commons.popupmenu.AppMenuHelper;
import org.dhis2.databinding.ActivityEventCaptureBinding;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.OnEditionListener;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.MapButtonObservable;
import org.dhis2.utils.Constants;
import org.dhis2.utils.EventMode;
import org.dhis2.utils.FileResourcesUtil;
import org.dhis2.utils.ImageUtils;
import org.dhis2.utils.RuleUtilsProviderResultKt;
import org.dhis2.utils.RulesUtilsProviderConfigurationError;
import org.dhis2.utils.customviews.FormBottomDialog;
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator;
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import kotlin.Unit;

import static org.dhis2.utils.Constants.PROGRAM_UID;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_EVENT;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

public class EventCaptureActivity extends ActivityGlobalAbstract implements EventCaptureContract.View, MapButtonObservable {

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
        ViewExtensionsKt.clipWithRoundedCorners(binding.eventViewPager, ExtensionsKt.getDp(16));
    }

    private void setUpNavigationBar() {
        binding.navigationBar.pageConfiguration(pageConfigurator);
        binding.navigationBar.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_details){
                goToInitialScreen();
            } else {
                binding.eventViewPager.setCurrentItem(adapter.getDynamicTabIndex(item.getItemId()));
            }
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
            new CustomDialog(
                    this,
                    getString(R.string.title_delete_go_back),
                    getString(R.string.delete_go_back),
                    getString(R.string.cancel),
                    getString(R.string.missing_mandatory_fields_go_back),
                    RQ_GO_BACK,
                    new DialogClickListener() {
                        @Override
                        public void onPositive() {
                        }

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
                    try {
                        presenter.saveImage(uuid, FileResourcesUtil.getFileFromGallery(this, imageUri).getPath());
                        presenter.nextCalculation(true);
                    } catch (Exception e) {
                        crashReportController.logException(e);
                        Toast.makeText(this, getString(R.string.something_wrong), Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case Constants.CAMERA_REQUEST:
                if (resultCode == RESULT_OK) {
                    File imageFile = new File(FileResourceDirectoryHelper.getFileResourceDirectory(this), "tempFile.png");
                    File file = new ImageUtils().rotateImage(this, imageFile);
                    try {
                        presenter.saveImage(uuid, file.exists() ? file.getPath() : null);
                        presenter.nextCalculation(true);
                    } catch (Exception e) {
                        crashReportController.logException(e);
                        Toast.makeText(this, getString(R.string.something_wrong), Toast.LENGTH_LONG).show();
                    }
                }
                break;
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
    public void showCompleteActions(boolean canComplete, String completeMessage, Map<String, String> errors, Map<String, FieldUiModel> emptyMandatoryFields) {
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
    public void updateProgramStageName(String stageName) {
        binding.programStageName.setText(stageName);
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
    public void showEventIntegrityAlert() {
        new MaterialAlertDialogBuilder(this, R.style.DhisMaterialDialog)
                .setTitle(R.string.conflict)
                .setMessage(R.string.event_date_in_future_message)
                .setPositiveButton(R.string.change_event_date, (dialogInterface, i) -> goToInitialScreen())
                .setNegativeButton(R.string.go_back, (dialogInterface, i) -> back())
                .setCancelable(false)
                .show();
    }

    @Override
    public void updateNoteBadge(int numberOfNotes) {
        binding.navigationBar.updateBadge(R.id.navigation_notes, numberOfNotes);
    }

    @Override
    public void showLoopWarning() {
        new MaterialAlertDialogBuilder(this, R.style.DhisMaterialDialog)
                .setTitle("Program rules warning")
                .setMessage("There is a configuration issue causing a loop in the rules. Contact you administrator.")
                .setPositiveButton(R.string.action_accept, (dialogInterface, i) -> {
                })
                .setCancelable(false)
                .show();
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

    @Override
    public void displayConfigurationErrors(List<RulesUtilsProviderConfigurationError> configurationError) {
        new MaterialAlertDialogBuilder(this, R.style.DhisMaterialDialog)
                .setTitle(R.string.warning_on_complete_title)
                .setMessage(RuleUtilsProviderResultKt.toMessage(configurationError, this))
                .setPositiveButton(R.string.action_close, (dialogInterface, i) -> {
                })
                .setNegativeButton(R.string.action_do_not_show_again, (dialogInterface, i) -> {
                    presenter.disableConfErrorMessage();
                })
                .setCancelable(false)
                .show();
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
}