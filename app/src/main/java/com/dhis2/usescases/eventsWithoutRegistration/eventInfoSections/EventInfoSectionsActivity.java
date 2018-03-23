package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityEventInfoSectionsBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import org.hisp.dhis.android.core.program.ProgramStageSectionModel;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventInfoSectionsActivity extends ActivityGlobalAbstract implements EventInfoSectionsContract.View {

    public static final String EVENT_UID = "EVENT_UID";
    public static final String PROGRAM_STAGE_UID = "PROGRAM_STAGE_UID";

    @Inject
    EventInfoSectionsContract.Presenter presenter;

    private ActivityEventInfoSectionsBinding binding;
    private List<ProgramStageSectionModel> programStageSectionModelList;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new EventInfoSectionsModule()).inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_info_sections);
        binding.setPresenter(presenter);
        String eventId = getIntent().getStringExtra(EVENT_UID);
        String programStageId = getIntent().getStringExtra(PROGRAM_STAGE_UID);
        presenter.init(this, eventId, programStageId);
    }

    @Override
    public void setProgramStageSections(List<ProgramStageSectionModel> programStageSectionModelList) {
        this.programStageSectionModelList = programStageSectionModelList;
        binding.viewPager.setAdapter(new EventInfoSectionsViewPagerAdapter(getSupportFragmentManager()));
    }
}
