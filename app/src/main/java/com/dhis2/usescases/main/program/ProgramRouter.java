package com.dhis2.usescases.main.program;

import android.os.Bundle;

import com.dhis2.R;
import com.dhis2.usescases.programDetail.ProgramDetailActivity;
import com.dhis2.usescases.programDetailTablet.ProgramDetailTabletActivity;

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
        if (view.getContext().getResources().getBoolean(R.bool.is_tablet))
            view.startActivity(ProgramDetailTabletActivity.class, bundle, false, false, null);
        else
            view.startActivity(ProgramDetailActivity.class, bundle, false, false, null);
    }
}
