package com.dhis2.usescases.main;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainInteractor implements MainContractsModule.Interactor {

    private MainContractsModule.View view;
    private final CompositeDisposable compositeDisposable;
    private HomeRepository homeRepository;

    MainInteractor(MainContractsModule.View view, HomeRepository homeRepository) {
        this.view = view;
        this.homeRepository = homeRepository;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void getData() {
        compositeDisposable.add(homeRepository.homeViewModels()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.swapData(),
                        throwable -> view.renderError(throwable.getMessage())
                )
        );
    }

    void clear() {
        compositeDisposable.clear();
    }

}