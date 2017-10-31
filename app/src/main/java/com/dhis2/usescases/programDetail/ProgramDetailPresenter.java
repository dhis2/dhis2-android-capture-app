package com.dhis2.usescases.programDetail;

/**
 * Created by ppajuelo on 31/10/2017.
 */

public class ProgramDetailPresenter implements ProgramDetailContractModule.Presenter {

    private final ProgramDetailContractModule.Interactor interactor;

    ProgramDetailPresenter(ProgramDetailContractModule.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void init() {
        interactor.getData();
    }
}
