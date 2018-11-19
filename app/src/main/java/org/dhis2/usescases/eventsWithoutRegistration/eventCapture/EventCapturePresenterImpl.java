package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.tuples.Quartet;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment.EventCaptureFormFragment;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCapturePresenterImpl implements EventCaptureContract.Presenter {

    private final EventCaptureContract.EventCaptureRepository eventCaptureRepository;
    private CompositeDisposable compositeDisposable;
    private EventCaptureContract.View view;
    private int currentPosition;
    private FlowableProcessor<Integer> currentSectionPosition;

    private HashMap<String, List<FieldViewModel>> formModels;
    private List<FormSectionViewModel> sectionList;

    public EventCapturePresenterImpl(EventCaptureContract.EventCaptureRepository eventCaptureRepository) {
        this.eventCaptureRepository = eventCaptureRepository;
        this.formModels = new HashMap<>();
        this.currentPosition = 0;
        currentSectionPosition = PublishProcessor.create();

    }


    @Override
    public void init(EventCaptureContract.View view) {
        this.compositeDisposable = new CompositeDisposable();
        this.view = view;

        compositeDisposable.add(
                Flowable.zip(
                        eventCaptureRepository.programStageName(),
                        eventCaptureRepository.eventDate(),
                        eventCaptureRepository.orgUnit(),
                        eventCaptureRepository.catOption(),
                        Quartet::create
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> view.renderInitialInfo(data.val0(), data.val1(), data.val2(), data.val3()),
                                Timber::e
                        )

        );

        compositeDisposable.add(
                eventCaptureRepository.eventSections()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> this.sectionList = data,
                                Timber::e
                        )
        );

    }

    @Override
    public void subscribeToSection() {
        compositeDisposable.add(
                currentSectionPosition
                        .startWith(0)
                        .observeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                position -> {
                                    EventCaptureFormFragment.getInstance().setSectionTitle(sectionList.get(position));
                                    EventCaptureFormFragment.getInstance().setSectionProgress(position, sectionList.size());
                                },
                                Timber::e
                        )
        );
    }

    @Override
    public void onNextSection() {
        if (currentPosition < sectionList.size() - 1) {
            currentPosition++;
            currentSectionPosition.onNext(currentPosition);
        }
    }

    @Override
    public void onPreviousSection() {
        if (currentPosition != 0) {
            currentPosition--;
            currentSectionPosition.onNext(currentPosition);
        }
    }

    @Override
    public Observable<List<OrganisationUnitModel>> getOrgUnits() {
        return null;
    }


    @Override
    public void onDettach() {
        this.compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

}
