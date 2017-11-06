package com.dhis2.usescases.searchTrackEntity;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class SearchTEPresenter implements SearchTEContractsModule.Presenter {

    private SearchTEContractsModule.View view;
    @Inject
    SearchTEContractsModule.Interactor interactor;

    @Inject
    SearchTEPresenter(SearchTEContractsModule.View view) {
        this.view = view;
    }

    @Override
    public void init() {
        interactor.init(view);
    }
}
