package com.dhis2.usescases.enrollment;

import com.dhis2.data.metadata.MetadataRepository;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.Observable;

/**
 * Created by ppajuelo on 31/01/2018.
 */

public class EnrollmentPresenter implements EnrollmentContracts.Presenter {
    private final MetadataRepository metadataRepository;
    private CompositeDisposable compositeDisposable;

    @Inject
    public EnrollmentPresenter(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
        this.compositeDisposable = new CompositeDisposable();
    }


    @Override
    public void init(String programUid) {
        Observable.zip(metadataRepository.getProgramTrackedEntityAttributes(programUid),
                metadataRepository.getProgramWithId(programUid),EnrollmentZipData::createZipData)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void deAttach() {
        compositeDisposable.dispose();
    }
}
