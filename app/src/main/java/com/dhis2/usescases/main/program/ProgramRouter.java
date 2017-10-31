package com.dhis2.usescases.main.program;

import android.os.Bundle;

import com.dhis2.usescases.programDetail.ProgramDetailActivity;

/**
 * Created by ppajuelo on 31/10/2017.
 */

public class ProgramRouter implements ProgramContractModule.Router {

    ProgramContractModule.View view;

    public ProgramRouter(ProgramContractModule.View view) {
        this.view = view;
    }

    @Override
    public void goToProgramDetail(HomeViewModel homeViewModel) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("PROGRAM", homeViewModel);
        view.startActivity(ProgramDetailActivity.class, bundle, false, false, null);
    }
}
