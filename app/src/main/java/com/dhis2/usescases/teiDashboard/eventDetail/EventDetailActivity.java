package com.dhis2.usescases.teiDashboard.eventDetail;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.data.forms.FormFragment;
import com.dhis2.data.forms.FormViewArguments;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ActivityEventDetailBinding;
import com.dhis2.databinding.FormEditTextDataBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.utils.DateUtils;
import com.dhis2.utils.CustomViews.CustomDialog;
import com.dhis2.utils.DialogClickListener;
import com.google.android.flexbox.FlexboxLayout;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageDataElementModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;

import javax.inject.Inject;

/**
 * Created by Cristian E. on 18/12/2017.
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
        binding.toolbarTitle.setText(getIntent().getStringExtra("TOOLBAR_TITLE"));
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
        binding.executePendingBindings();

        if (!eventDetailModel.getStageSections().isEmpty()) {
            setSectionDataElements(eventDetailModel, "null");

            for (ProgramStageSectionModel section : eventDetailModel.getStageSections()) {

                TextView sectionTitle = new TextView(this);
                sectionTitle.setText(section.displayName());
                sectionTitle.setTextColor(ContextCompat.getColor(this, R.color.cell_text_color));
                sectionTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                sectionTitle.setHintTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setFlexBasisPercent(1f);
                binding.dataLayout.addView(sectionTitle, params);

                setSectionDataElements(eventDetailModel, section.uid());
            }
        } else
            setSectionDataElements(eventDetailModel, null);

        binding.dataLayout.invalidate();

        supportStartPostponedEnterTransition();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.dataFragment, FormFragment.newInstance(
                        FormViewArguments.createForEvent(eventUid), false,
                        false))
                .commit();
    }
    @Override
    public void isEventExpired(ProgramModel program) {
        EventModel event = eventDetailModel.getEventModel();
        if(event.status() == EventStatus.COMPLETED &&
                DateUtils.getInstance().hasExpired(eventDetailModel.getEventModel().completedDate(), program.expiryDays(), program.completeEventsExpiryDays(), program.expiryPeriodType())) {
            // TODO implement event expiration logic
        }


    @Override
    public void setDataEditable() {
        if (binding.getStage().accessDataWrite()) {
            isEditable.set(!isEditable.get());
            binding.dataLayout.invalidate();
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

    private void setSectionDataElements(EventDetailModel eventDetailModel, String sectionUid) {

        for (ProgramStageDataElementModel dataValueModel : eventDetailModel.getDataElementsForSection(sectionUid)) {

            FormEditTextDataBinding editTextBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(this), R.layout.form_edit_text_data, binding.dataLayout, false
            );
            editTextBinding.setDataValue(dataValueModel);

            editTextBinding.formEdittext.setText(eventDetailModel.getValueForDE(dataValueModel.dataElement()));

            editTextBinding.setIsEditable(isEditable);

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setFlexBasisPercent(1f);
            binding.dataLayout.addView(editTextBinding.getRoot(), params);

            editTextBinding.formEdittext.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // unused
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    presenter.saveData(dataValueModel.dataElement(), s.toString());

                }

                @Override
                public void afterTextChanged(Editable s) {
                    // unused
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        presenter.back();
    }
}
