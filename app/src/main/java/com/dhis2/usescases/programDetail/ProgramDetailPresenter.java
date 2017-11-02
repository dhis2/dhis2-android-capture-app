package com.dhis2.usescases.programDetail;

import com.dhis2.usescases.main.program.HomeViewModel;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 31/10/2017.
 */

public class ProgramDetailPresenter implements ProgramDetailContractModule.Presenter {

    private ProgramDetailContractModule.View view;
    private final ProgramDetailContractModule.Interactor interactor;
    public HomeViewModel program;

    @Inject
    ProgramDetailPresenter(ProgramDetailContractModule.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void init(ProgramDetailContractModule.View view, HomeViewModel program) {
        this.program = program;
        this.view = view;
        interactor.init(view, program.id());
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
