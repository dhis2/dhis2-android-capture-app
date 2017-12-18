package com.dhis2.usescases.eventDetail;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dhis2.R;
import com.dhis2.databinding.ActivityEventDetailBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

/**
 * Created by Administrador on 18/12/2017.
 */

public class EventDetailActivity extends ActivityGlobalAbstract {

    ActivityEventDetailBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_detail);
    }
}
