package com.dhis2.usescases.programDetail;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityProgramDetailBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.main.program.HomeViewModel;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 31/10/2017.
 */

public class ProgramDetailActivity extends ActivityGlobalAbstract implements ProgramDetailContractModule.View {

    ActivityProgramDetailBinding binding;
    @Inject
    ProgramDetailContractModule.Presenter presenter;
    HomeViewModel homeViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
//        AndroidInjection.inject(this);
        ((App) getApplicationContext()).getUserComponent().plus(new ProgramDetailModule()).inject(this);

        super.onCreate(savedInstanceState);
        homeViewModel = (HomeViewModel) getIntent().getSerializableExtra("PROGRAM");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_program_detail);
        binding.setPresenter(presenter);

        presenter.init(homeViewModel);

    }

    @Override
    public void swapData(ArrayList<TrackedEntityInstance> trackedEntityInstances) {
        binding.recycler.setAdapter(); //TODO: NEW ADAPTER! SI QUIERES PUEDES INTENTAR METERLO POR DAGGER
    }
}
