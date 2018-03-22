package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityEventInfoSectionsBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import javax.inject.Inject;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventInfoSectionsActivity extends ActivityGlobalAbstract implements EventInfoSectionsContract.View {

    @Inject
    EventInfoSectionsContract.Presenter presenter;

    private ActivityEventInfoSectionsBinding binding;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new EventInfoSectionsModule()).inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_info_sections);
        binding.setPresenter(presenter);
        String eventId = getIntent().getStringExtra("EVENT_UID");
        presenter.init(this, eventId);
    }
}
