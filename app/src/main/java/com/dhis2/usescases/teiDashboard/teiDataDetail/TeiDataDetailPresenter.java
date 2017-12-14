package com.dhis2.usescases.teiDashboard.teiDataDetail;

import javax.inject.Inject;

/**
 * Created by frodriguez on 12/13/2017.
 */

public class TeiDataDetailPresenter implements TeiDataDetailContracts.Presenter {

    private TeiDataDetailContracts.View view;

    @Inject
    TeiDataDetailInteractor interactor;
    private String teiUid;
    private String programUid;

    @Inject
    public TeiDataDetailPresenter() {

    }

    @Override
    public void init(TeiDataDetailContracts.View view, String uid, String programUid) {
        this.view = view;
        this.teiUid = uid;
        this.programUid = programUid;
        interactor.init(view, uid, programUid);
    }

    @Override
    public void onBackPressed() {
        view.back();
    }
}
