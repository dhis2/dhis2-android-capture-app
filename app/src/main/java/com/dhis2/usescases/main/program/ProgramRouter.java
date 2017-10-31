package com.dhis2.usescases.main.program;

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
    public void goToProgramDetail() {
        view.startActivity(ProgramDetailActivity.class, null, false, false, null);
    }
}
