package com.dhis2.usescases.teiDashboard;

import android.databinding.BaseObservable;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.List;

/**
 * Created by ppajuelo on 04/12/2017.
 */

public class DashboardProgramModel extends BaseObservable {

    private ProgramModel program;
    private List<ProgramStageModel> programStages;


    public DashboardProgramModel(ProgramModel programModel, List<ProgramStageModel> programStages) {
        this.program = programModel;
        this.programStages = programStages;
    }

    public ProgramModel getProgram() {
        return program;
    }

    public List<ProgramStageModel> getProgramStages() {
        return programStages;
    }
}
