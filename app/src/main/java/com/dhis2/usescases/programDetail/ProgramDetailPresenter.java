package com.dhis2.usescases.programDetail;

import com.dhis2.usescases.main.program.HomeViewModel;

/**
 * Created by ppajuelo on 31/10/2017.
 */

public class ProgramDetailPresenter implements ProgramDetailContractModule.Presenter {

    private final ProgramDetailContractModule.Interactor interactor;
    public HomeViewModel program;

    ProgramDetailPresenter(ProgramDetailContractModule.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void init(HomeViewModel program) {
        this.program = program;
        interactor.getData(program.id());
    }

    @Override
    public void onTimeButtonClick() {

    }

    @Override
    public void onDateRangeButtonClick() {

    }

    @Override
    public void onOrgUnitButtonClick() {

    }

    @Override
    public void onCatComboButtonClick() {

    }

    @Override
    public HomeViewModel getCurrentProgram() {
        return program;
    }
}
