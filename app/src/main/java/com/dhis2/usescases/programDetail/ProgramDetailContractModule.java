package com.dhis2.usescases.programDetail;

import com.dhis2.usescases.general.AbstractActivityContracts;

/**
 * Created by ppajuelo on 31/10/2017.
 */
public class ProgramDetailContractModule {


 /*   @Provides
    @PerActivity
    View provideView(ProgramDetailActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    Presenter providesPresenter(View view,
                                ProgramDetailInteractor interactor) {
        return new ProgramDetailPresenter(view, interactor);
    }

    @Provides
    @PerActivity
    Interactor provideInteractor(D2 d2,
                                 View view) {
        return new ProgramDetailInteractor(d2, view);
    }*/

    public interface View extends AbstractActivityContracts.View {

    }

    public interface Presenter {
        void init();
    }

    public interface Interactor {
        void getData();
    }

    public interface Router {

    }

}
