package com.dhis2.usescases.searchTrackEntity;

import android.app.DatePickerDialog;
import android.support.annotation.Nullable;
import android.view.View;

import org.hisp.dhis.android.core.option.OptionModel;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class SearchTEPresenter implements SearchTEContractsModule.Presenter {

    private SearchTEContractsModule.View view;
    @Inject
    SearchTEContractsModule.Interactor interactor;

    @Inject
    SearchTEPresenter() {
    }

    @Override
    public void init(SearchTEContractsModule.View view) {
        this.view = view;
        interactor.init(view);
    }

    @Override
    public void onDateClick(@Nullable DatePickerDialog.OnDateSetListener listener) {
        view.showDateDialog(listener);
    }

    @Override
    public Observable<List<OptionModel>> getOptions(String optionSetId) {
        return interactor.getOptions(optionSetId);
    }

    @Override
    public void query(String filter) {
        interactor.filterTrackEntities(filter);
    }
}
