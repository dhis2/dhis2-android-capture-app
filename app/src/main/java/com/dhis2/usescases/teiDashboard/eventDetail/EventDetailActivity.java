package com.dhis2.usescases.teiDashboard.eventDetail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.data.forms.FormFragment;
import com.dhis2.data.forms.FormViewArguments;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ActivityEventDetailBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.utils.Constants;
import com.dhis2.utils.CustomViews.CustomDialog;
import com.dhis2.utils.CustomViews.OrgUnitDialog;
import com.dhis2.utils.DateUtils;
import com.dhis2.utils.DialogClickListener;
import com.dhis2.utils.HelpManager;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;

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
        ((App) getApplicationContext()).userComponent().plus(new EventDetailModule(getIntent().getStringExtra("EVENT_UID"))).inject(this);
        supportPostponeEnterTransition();
        super.onCreate(savedInstanceState);
        eventUid = getIntent().getStringExtra("EVENT_UID");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_detail);
        binding.title.setText(getIntent().getStringExtra("TOOLBAR_TITLE"));
        binding.setPresenter(presenter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);
        presenter.getEventData(eventUid);
    }

    @Override
    public void setData(EventDetailModel eventDetailModel, MetadataRepository metadataRepository) {
        this.eventDetailModel = eventDetailModel;
        presenter.getExpiryDate(eventDetailModel.getEventModel().uid());
        binding.setEvent(eventDetailModel.getEventModel());
        binding.setStage(eventDetailModel.getProgramStage());
        binding.orgUnit.setText(eventDetailModel.getOrgUnitName());

        if(eventDetailModel.getOptionComboList().isEmpty()){
            binding.categoryComboLayout.setVisibility(View.GONE);
        }else{
            binding.categoryComboLayout.setVisibility(View.VISIBLE);
            binding.categoryComboLayout.setHint(eventDetailModel.getCatComboName());
            binding.categoryCombo.setText(eventDetailModel.getEventCatComboOptionName());
        }

        binding.categoryComboLayout.setVisibility(eventDetailModel.getOptionComboList().isEmpty()
                ? View.GONE : View.VISIBLE);
        updateActionButton(eventDetailModel.getEventModel().status());
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

        if(!HelpManager.getInstance().isTutorialReadyForScreen(getClass().getName()))
            setTutorial();
    }

    @Override
    public void isEventExpired(ProgramModel program) {
        EventModel event = eventDetailModel.getEventModel();
        if (event.status() == EventStatus.COMPLETED &&
                DateUtils.getInstance().hasExpired(eventDetailModel.getEventModel().completedDate(), program.expiryDays(), program.completeEventsExpiryDays(), program.expiryPeriodType())) {
            // TODO implement event expiration logic
        }
    }

    @Override
    public void setDataEditable() {
        if (binding.getStage().accessDataWrite()) {
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
            if (eventDetailModel.getEventModel().status() == EventStatus.COMPLETED)
                intent.putExtra(Constants.EVENT_UID, eventUid);
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

    @Override
    public void showOrgUnitSelector(OrgUnitDialog orgUnitDialog) {
        orgUnitDialog.show(getSupportFragmentManager(), "EVENT_ORG_UNITS");
    }

    @Override
    public void setSelectedOrgUnit(OrganisationUnitModel selectedOrgUnit) {
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
    public void onBackPressed() {
        presenter.back();
    }

    @Override
    public void setTutorial() {
        super.setTutorial();

        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                "com.dhis2", Context.MODE_PRIVATE);

        new Handler().postDelayed(() -> {
            FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .title(getString(R.string.tuto_tei_event_1))
                    .focusOn(getAbstractActivity().findViewById(R.id.toolbarDelete))
                    .closeOnTouch(true)
                    .build();
            FancyShowCaseView tuto2 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .title(getString(R.string.tuto_tei_event_2))
                    .focusOn(getAbstractActivity().findViewById(R.id.deactivate_button))
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .closeOnTouch(true)
                    .build();


            ArrayList<FancyShowCaseView> steps = new ArrayList<>();
            steps.add(tuto1);
            steps.add(tuto2);

            HelpManager.getInstance().setScreenHelp(getClass().getName(), steps);

            if (!prefs.getBoolean("TUTO_TEI_EVENT", false)) {
                HelpManager.getInstance().showHelp();/* getAbstractActivity().fancyShowCaseQueue.show();*/
                prefs.edit().putBoolean("TUTO_TEI_EVENT", true).apply();
            }

        }, 500);

    }
}
