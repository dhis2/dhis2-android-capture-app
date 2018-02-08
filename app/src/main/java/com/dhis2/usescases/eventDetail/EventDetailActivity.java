package com.dhis2.usescases.eventDetail;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ActivityEventDetailBinding;
import com.dhis2.databinding.FormEditTextDataBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.google.android.flexbox.FlexboxLayout;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

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
        ((App) getApplicationContext()).userComponent().plus(new EventDetailModule()).inject(this);
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

        for (TrackedEntityDataValueModel dataValueModel : eventDetailModel.getDataValueModelList()) {

            FormEditTextDataBinding editTextBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(this), R.layout.form_edit_text_data, binding.dataLayout, false
            );
            editTextBinding.setDataValue(dataValueModel);

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setFlexBasisPercent(.5f);
            binding.dataLayout.addView(editTextBinding.getRoot(), params);

        }
        binding.dataLayout.invalidate();


        supportStartPostponedEnterTransition();

    }
}
