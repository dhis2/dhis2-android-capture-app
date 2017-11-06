package com.dhis2.usescases.searchTrackEntity;

import android.app.DatePickerDialog;
import android.support.annotation.Nullable;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.usescases.general.AbstractActivityContracts;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.List;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 02/11/2017.
 */
@Module
public class SearchTEContractsModule {

    @Provides
    View provideView(SearchTEActivity searchTEActivity) {
        return searchTEActivity;
    }

    @Provides
    Presenter providePresenter(View view) {
        return new SearchTEPresenter(view);
    }

    @Provides
    Interactor provideInteractor(SearchRepository searchRepository) {
        return new SearchTEInteractor(searchRepository);
    }

    @Provides
    @PerActivity
    FormAdapter provideFormAdapter(Presenter presenter) {
        return new FormAdapter(presenter);
    }

    @Provides
    SearchRepository searchRepository(BriteDatabase briteDatabase) {
        return new SearchRepositoryImpl(briteDatabase);
    }

    interface View extends AbstractActivityContracts.View {
        void setForm(List<TrackedEntityAttributeModel> trackedEntityAttributeModels);

        void showDateDialog(DatePickerDialog.OnDateSetListener listener);
    }

    public interface Presenter {

        void init();

        void onDateClick(@Nullable DatePickerDialog.OnDateSetListener listener);
    }

    interface Interactor {
        void init(View view);

        void getTrackedEntityAttributes();

        void getProgramTrackedEntityAttributes();

    }

    interface Router {

    }
}
