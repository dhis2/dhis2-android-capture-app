package com.dhis2.usescases.searchTrackEntity;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ppajuelo on 06/11/2017.
 */

public class SearchTEInteractor implements SearchTEContractsModule.Interactor {
    private final SearchRepository searchRepository;
    private SearchTEContractsModule.View view;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public SearchTEInteractor(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    @Override
    public void init(SearchTEContractsModule.View view) {
        this.view = view;
        getTrackedEntityAttributes();
    }

    @Override
    public void getTrackedEntityAttributes() {
        compositeDisposable.add(searchRepository.programAttributes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> view.setForm(data),
                        Timber::d)
        );
    }

    @Override
    public void getProgramTrackedEntityAttributes() {

    }
}
