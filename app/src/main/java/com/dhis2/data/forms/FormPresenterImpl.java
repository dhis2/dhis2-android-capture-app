package com.dhis2.data.forms;

import android.support.annotation.NonNull;

import java.text.ParseException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import rx.exceptions.OnErrorNotImplementedException;
import timber.log.Timber;

import static com.dhis2.utils.Preconditions.isNull;

class FormPresenterImpl implements FormPresenter {

    @NonNull
    private final FormViewArguments formViewArguments;

    @NonNull
    private final FormRepository formRepository;

    @NonNull
    private final CompositeDisposable compositeDisposable;

    FormPresenterImpl(@NonNull FormViewArguments formViewArguments,
            @NonNull FormRepository formRepository) {
        this.formViewArguments = formViewArguments;
        this.formRepository = formRepository;
        this.compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void onAttach(@NonNull FormView view) {
        isNull(view, "FormView must not be null");

        compositeDisposable.add(formRepository.title()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view.renderTitle(), Timber::e));

        compositeDisposable.add(formRepository.reportDate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(date -> {
                    try {
                        return DateUtils.uiDateFormat().format(DateUtils.databaseDateFormat().parse(date));
                    } catch (ParseException e) {
                        Timber.e(e, "DashboardRepository: Unable to parse date. Expected format: " +
                                DateUtils.databaseDateFormat().toPattern() + ". Input: " + date);
                        return date;
                    }
                })
                .subscribe(view.renderReportDate(), Timber::e));

        compositeDisposable.add(formRepository.sections()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view.renderSectionViewModels(), Timber::e));

        compositeDisposable.add(view.reportDateChanged()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(formRepository.storeReportDate(), Timber::e));

        ConnectableFlowable<ReportStatus> statusObservable = formRepository.reportStatus()
                .distinctUntilChanged()
                .publish();

        compositeDisposable.add(statusObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .skip(1)
                .subscribe(status -> view.renderStatusChangeSnackBar(status), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));

        compositeDisposable.add(statusObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view.renderStatus(), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));

        compositeDisposable.add(statusObservable.connect());

        ConnectableObservable<ReportStatus> statusChangeObservable = view.eventStatusChanged()
                .distinctUntilChanged()
                .publish();

        compositeDisposable.add(statusChangeObservable
                .filter(eventStatus -> formViewArguments.type() != FormViewArguments.Type.ENROLLMENT)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(formRepository.storeReportStatus(), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));

        Observable<String> enrollmentDoneStream = statusChangeObservable
                .filter(eventStatus -> formViewArguments.type() == FormViewArguments.Type.ENROLLMENT)
                .map(reportStatus -> formViewArguments.uid())
                .observeOn(Schedulers.io()).share();

        compositeDisposable.add(enrollmentDoneStream
                .subscribeOn(Schedulers.io())
                .subscribe(formRepository.autoGenerateEvent(), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));

        compositeDisposable.add(enrollmentDoneStream
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(view.finishEnrollment(), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));

        compositeDisposable.add(statusChangeObservable.connect());
    }

    @Override
    public void onDetach() {
        compositeDisposable.clear();
    }
}