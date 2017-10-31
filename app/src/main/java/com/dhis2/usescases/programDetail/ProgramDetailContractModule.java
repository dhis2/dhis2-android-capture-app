package com.dhis2.usescases.programDetail;

import com.dhis2.usescases.general.AbstractActivityContracts;
import com.dhis2.usescases.main.program.HomeViewModel;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;

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
        void swapData(ArrayList<TrackedEntityInstance> trackedEntityInstances);

        void addTree(TreeNode treeNode);
    }

    public interface Presenter {
        void init(View view, HomeViewModel program);

        void onTimeButtonClick();

        void onDateRangeButtonClick();

        void onOrgUnitButtonClick();

        void onCatComboButtonClick();

        HomeViewModel getCurrentProgram();

    }

    public interface Interactor {
        void init(View view, String programId);

        void getOrgUnits();

        void getData();
    }

    public interface Router {

    }

}
