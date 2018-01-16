package com.dhis2.usescases.main.program;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.dhis2.usescases.general.AbstractActivityContracts;
import com.squareup.sqlbrite2.BriteDatabase;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * Created by ppajuelo on 18/10/2017.
 */
@Module
public class ProgramContractModule {

    @Provides
    View provideView(ProgramFragment fragment) {
        return fragment;
    }

    @Provides
    Presenter provideProgramPresenter(View view, HomeRepository homeRepository) {
        return new ProgramPresenter(view, homeRepository);
    }

    @Provides
    HomeRepository homeRepository(BriteDatabase briteDatabase) {
        return new HomeRepositoryImpl(briteDatabase);
    }

    interface View extends AbstractActivityContracts.View {

        void showRageDatePicker();

        void showTimeUnitPicker();

        void setUpRecycler();

        Consumer<List<ProgramModel>> swapProgramData();

        @UiThread
        void renderError(String message);

        @NonNull
        @UiThread
        void addTree(TreeNode treeNode);

        void setOrgUnits(List<OrganisationUnitModel> orgUnits);

        void openDrawer();
    }

    public interface Presenter {
        void onItemClick(ProgramModel homeViewModel);

        void onOrgUnitButtonClick();

        void onDateRangeButtonClick();

        void onTimeButtonClick();

        void getOrgUnits();

        Observable<List<EventModel>> getEvents(ProgramModel programModel);

    }


}
