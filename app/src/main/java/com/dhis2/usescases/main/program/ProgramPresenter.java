package com.dhis2.usescases.main.program;

import android.view.Gravity;

import com.dhis2.usescases.main.MainActivity;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ppajuelo on 18/10/2017.
 */

public class ProgramPresenter implements ProgramContractModule.Presenter {

    ProgramContractModule.View view;
    private final HomeRepository homeRepository;
    private final CompositeDisposable compositeDisposable;

    @Inject
    ProgramPresenter(ProgramContractModule.View view, HomeRepository homeRepository) {
        this.view = view;
        this.homeRepository = homeRepository;
        this.compositeDisposable = new CompositeDisposable();
    }

    void init() {
        compositeDisposable.add(homeRepository.homeViewModels()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.swapData(),
                        throwable -> view.renderError(throwable.getMessage())));
    }

    @Override
    public void onItemClick(HomeViewModel homeViewModel) {

    }

    @Override
    public void onOrgUnitButtonClick() {
        ((MainActivity) view.getAbstracContext()).openDrawer(Gravity.END);
    }

    @Override
    public void onDateRangeButtonClick() {
        view.showRageDatePicker();
    }


    @Override
    public void onTimeButtonClick() {

    }

    @Override
    public void onCatComboButtonClick() {

    }
}
