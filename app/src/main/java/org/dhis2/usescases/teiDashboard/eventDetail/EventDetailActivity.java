package org.dhis2.usescases.teiDashboard.eventDetail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.forms.FormFragment;
import org.dhis2.data.forms.FormViewArguments;
import org.dhis2.databinding.ActivityEventDetailBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.custom_views.CategoryComboDialog;
import org.dhis2.utils.custom_views.CustomDialog;
import org.dhis2.utils.custom_views.OrgUnitDialog;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;
import timber.log.Timber;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_EVENT;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

/**
 * QUADRAM. Created by Cristian E. on 18/12/2017.
 */

public class EventDetailActivity extends ActivityGlobalAbstract implements EventDetailContracts.View {

    ActivityEventDetailBinding binding;
    @Inject
    EventDetailContracts.Presenter presenter;

    EventDetailModel eventDetailModel;
    private String eventUid;
    private ObservableBoolean isEditable = new ObservableBoolean(false);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(
                new EventDetailModule(getIntent().getStringExtra("EVENT_UID"),
                        getIntent().getStringExtra("TEI_UID"))).inject(this);
        supportPostponeEnterTransition();
        super.onCreate(savedInstanceState);
        eventUid = getIntent().getStringExtra("EVENT_UID");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_detail);
        binding.teiName.setText(getIntent().getStringExtra("TOOLBAR_TITLE"));
        binding.setPresenter(presenter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);
        presenter.getEventData(eventUid);
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setData(EventDetailModel eventDetailModel) {
        if (eventDetailModel.getEvent().status() != EventStatus.SCHEDULE && eventDetailModel.getEvent().eventDate() != null) {
            Intent intent2 = new Intent(this, EventCaptureActivity.class);
            intent2.putExtras(EventCaptureActivity.getActivityBundle(eventDetailModel.getEvent().uid(), eventDetailModel.getEvent().program()));
            startActivity(intent2, null);
            finish();
        } else {
            this.eventDetailModel = eventDetailModel;
            presenter.getExpiryDate(eventDetailModel.getEvent().uid());
            binding.setEvent(eventDetailModel.getEvent());
            binding.setStage(eventDetailModel.getProgramStage());
            binding.setEnrollmentActive(eventDetailModel.isEnrollmentActive());
            setDataEditable();
            binding.orgUnit.setText(eventDetailModel.getOrgUnitName());

            if (eventDetailModel.getOptionComboList().isEmpty()) {
                binding.categoryComboLayout.setVisibility(View.GONE);
            } else {
                binding.categoryComboLayout.setVisibility(View.VISIBLE);
                binding.categoryComboLayout.setHint(eventDetailModel.getCatComboName());
                binding.categoryCombo.setText(eventDetailModel.getEventCatComboOptionName());
            }

            binding.categoryComboLayout.setVisibility(eventDetailModel.getOptionComboList().isEmpty()
                    ? View.GONE : View.VISIBLE);
            updateActionButton(eventDetailModel.getEvent().status());
            binding.executePendingBindings();

            supportStartPostponedEnterTransition();

            if (getSupportFragmentManager().findFragmentByTag("EVENT_DATA_ENTRY") != null)
                getSupportFragmentManager().beginTransaction()
                        .remove(getSupportFragmentManager().findFragmentByTag("EVENT_DATA_ENTRY"))
                        .commit();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.dataFragment, FormFragment.newInstance(
                            FormViewArguments.createForEvent(eventUid), false,
                            false, true), "EVENT_DATA_ENTRY")
                    .commit();
        }
    }

    @Override
    public void isEventExpired(Program program) {
        if (eventDetailModel.hasExpired()) {
            // TODO implement event expiration logic
        }
    }

    @Override
    public void setDataEditable() {
        if (binding.getStage().access().data().write()) {
            isEditable.set(!isEditable.get());
        } else
            displayMessage(null);
    }

    @Override
    public void showConfirmDeleteEvent() {
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
    public void showEventWasDeleted() {
        showToast(getString(R.string.event_was_deleted));
        finish();
    }

    @Override
    public void goBack(boolean changedEventStatus) {
        if (changedEventStatus) {
            Intent intent = new Intent();
            if (eventDetailModel.getEvent().status() == EventStatus.COMPLETED)
                intent.putExtra(Constants.EVENT_UID, eventUid);
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

    @Override
    public void showOrgUnitSelector(OrgUnitDialog orgUnitDialog) {
        if (!orgUnitDialog.isAdded())
            orgUnitDialog.show(getSupportFragmentManager(), "EVENT_ORG_UNITS");
    }

    @Override
    public void setSelectedOrgUnit(OrganisationUnit selectedOrgUnit) {
        binding.orgUnit.setText(selectedOrgUnit.displayName());
        //TODO: Save org unit change
    }

    @Override
    public void updateActionButton(EventStatus eventStatus) {
        switch (eventStatus) {
            case COMPLETED:
                binding.deactivateButton.setText(getString(R.string.re_open));
                if (eventDetailModel.hasExpired())
                    binding.deactivateButton.setVisibility(View.GONE);
                break;
            case ACTIVE:
                binding.deactivateButton.setText(getString(R.string.complete));
                break;
            case OVERDUE:
                binding.deactivateButton.setText(R.string.skip);
                break;
            default:
                binding.deactivateButton.setVisibility(View.GONE);
                break;
        }
    }

    @NonNull
    @Override
    public Consumer<EventStatus> updateStatus(EventStatus eventStatus) {
        return eventStatus1 -> updateStatus(eventStatus);
    }

    @Override
    public void setDate(String result) {
        binding.eventDate.setText(result);
    }

    @Override
    public void showCatOptionDialog() {
        new CategoryComboDialog(getAbstracContext(), eventDetailModel.getCatComboName(), eventDetailModel.getOptionComboList(), 123, selectedOption -> {
            binding.categoryCombo.setText(selectedOption.displayName());
            presenter.changeCatOption(selectedOption);
        }, eventDetailModel.getProgramStage().displayName()).show();
    }

    @Override
    public void onBackPressed() {
        presenter.back();
    }

    @Override
    public void setTutorial() {
        new Handler().postDelayed(() -> {
            SparseBooleanArray stepConditions = new SparseBooleanArray();
            stepConditions.put(2, getAbstractActivity().findViewById(R.id.deactivate_button).getVisibility() == View.VISIBLE);
            HelpManager.getInstance().show(getActivity(), HelpManager.TutorialName.EVENT_DETAIL, stepConditions);
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
                    analyticsHelper().setEvent(SHOW_HELP, CLICK, SHOW_HELP);
                    setTutorial();
                    break;
                case R.id.menu_delete:
                    presenter.confirmDeleteEvent();
                    break;
                default:
                    break;
            }
            return false;
        });
        popupMenu.getMenu().getItem(1).setVisible(binding.getStage().access().data().write() && eventDetailModel.isEnrollmentActive());
        popupMenu.show();
    }
}
