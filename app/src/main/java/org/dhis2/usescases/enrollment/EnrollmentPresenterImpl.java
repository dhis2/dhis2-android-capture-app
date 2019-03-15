package org.dhis2.usescases.enrollment;

import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.program.Program;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class EnrollmentPresenterImpl implements EnrollmentContracts.Presenter {

    private final EnrollmentRepository enrollmentRepository;
    private final CompositeDisposable compositeDisposable;
    private final D2 d2;
    private final String enrollmentUid;
    private EnrollmentContracts.View view;

    EnrollmentPresenterImpl(String enrollmentUid, EnrollmentRepository enrollmentRepository, D2 d2) {
        this.enrollmentUid = enrollmentUid;
        this.enrollmentRepository = enrollmentRepository;
        this.compositeDisposable = new CompositeDisposable();
        this.d2 = d2;
    }


    @Override
    public void init(EnrollmentContracts.View view) {
        compositeDisposable.add(
                Observable.just(d2.enrollmentModule().enrollments.uid(enrollmentUid).get())
                        .flatMap(enrollment -> Observable.just(d2.programModule().programs.uid(enrollment.program()).get())
                                .map(program -> Pair.create(program, enrollment)))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                programEnrollmentPair -> {
                                    Program program = programEnrollmentPair.val0();
                                    Enrollment enrollment = programEnrollmentPair.val1();
                                    view.renderEnrollmentDate(program.enrollmentDateLabel(), DateUtils.uiDateFormat().format(enrollment.enrollmentDate()));
                                    if (program.displayIncidentDate())
                                        view.renderIncidentDate(program.incidentDateLabel(), DateUtils.uiDateFormat().format(enrollment.incidentDate()));
                                    view.showCoordinates(program.featureType());
                                },
                                Timber::e
                        )
        );

        //TODO: GET FIELDS
    }

    @Override
    public void onNextClick() {
        //TODO: CHECK MANDATORIES
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

}
