package com.dhis2.usescases.teiDashboard.eventDetail;

import android.databinding.DataBindingUtil;
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
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ActivityEventDetailBinding;
import com.dhis2.databinding.FormEditTextDataBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.google.android.flexbox.FlexboxLayout;

import org.hisp.dhis.android.core.program.ProgramStageDataElementModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;

import javax.inject.Inject;

/**
 * Created by Cristian E. on 18/12/2017.
 *
 */

public class EventDetailActivity extends ActivityGlobalAbstract implements EventDetailContracts.View {

    ActivityEventDetailBinding binding;
    @Inject
    EventDetailContracts.Presenter presenter;

    private String eventUid;

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
        binding.setEvent(eventDetailModel.getEventModel());
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

    }

    private void setSectionDataElements(EventDetailModel eventDetailModel, String sectionUid) {

        for (ProgramStageDataElementModel dataValueModel : eventDetailModel.getDataElementsForSection(sectionUid)) {

            FormEditTextDataBinding editTextBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(this), R.layout.form_edit_text_data, binding.dataLayout, false
            );
            editTextBinding.setDataValue(dataValueModel);

            editTextBinding.formEdittext.setText(eventDetailModel.getValueForDE(dataValueModel.dataElement()));

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setFlexBasisPercent(.5f);
            binding.dataLayout.addView(editTextBinding.getRoot(), params);

            editTextBinding.formEdittext.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    presenter.saveData(dataValueModel.dataElement(), s.toString());

                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

        }
    }
}
