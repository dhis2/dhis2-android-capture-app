package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.support.annotation.NonNull;
import android.util.Log;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Quartet;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment.EventCaptureFormFragment;
import org.dhis2.utils.Result;
import org.dhis2.utils.RulesUtilsProvider;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCapturePresenterImpl implements EventCaptureContract.Presenter {

    private final EventCaptureContract.EventCaptureRepository eventCaptureRepository;
    private final RulesUtilsProvider rulesUtils;
    private CompositeDisposable compositeDisposable;
    private EventCaptureContract.View view;
    private int currentPosition;
    private FlowableProcessor<Integer> currentSectionPosition;

    private HashMap<String, List<FieldViewModel>> formModels;
    private List<FormSectionViewModel> sectionList;

    public EventCapturePresenterImpl(EventCaptureContract.EventCaptureRepository eventCaptureRepository, RulesUtilsProvider rulesUtils) {
        this.eventCaptureRepository = eventCaptureRepository;
        this.rulesUtils = rulesUtils;
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
                                data -> {
                                    this.sectionList = data;
                                    view.setUp();
                                },
                                Timber::e
                        )
        );

    }

    @Override
    public void subscribeToSection() {
        compositeDisposable.add(
                currentSectionPosition
                        .startWith(0)
                        .flatMap(position -> {
                            EventCaptureFormFragment.getInstance().setSectionTitle(sectionList.get(position));
                            EventCaptureFormFragment.getInstance().setSectionProgress(position, sectionList.size());
                            return Flowable.zip(
                                    eventCaptureRepository.list(sectionList.get(position).sectionUid()),
                                    eventCaptureRepository.calculate().subscribeOn(Schedulers.computation())
                                            .onErrorReturn(throwable -> Result.failure(new Exception(throwable))), this::applyEffects);
                        })
                        .observeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                EventCaptureFormFragment.getInstance().showFields(),
                                error -> Timber.log(1, "Something went wrong")
                        )
        );
    }

    @NonNull
    private List<FieldViewModel> applyEffects(
            @NonNull List<FieldViewModel> viewModels,
            @NonNull Result<RuleEffect> calcResult) {
        if (calcResult.error() != null) {
            calcResult.error().printStackTrace();
            return viewModels;
        }

        Map<String, FieldViewModel> fieldViewModels = toMap(viewModels);
        rulesUtils.applyRuleEffects(fieldViewModels, calcResult, EventCaptureFormFragment.getInstance());

        return new ArrayList<>(fieldViewModels.values());
    }

    @NonNull
    private static Map<String, FieldViewModel> toMap(@NonNull List<FieldViewModel> fieldViewModels) {
        Map<String, FieldViewModel> map = new LinkedHashMap<>();
        for (FieldViewModel fieldViewModel : fieldViewModels) {
            map.put(fieldViewModel.uid(), fieldViewModel);
        }
        return map;
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
    public void onSectionSelectorClick(boolean isCurrentSection, int position) {

        EventCaptureFormFragment.getInstance().showSectionSelector();
        if (isCurrentSection && position == -1) {
            Disposable disposable =
                    Flowable.just(sectionList)
                            .flatMapIterable(list -> list)
                            .flatMap(section -> Flowable.zip(
                                    eventCaptureRepository.list(section.sectionUid()),
                                    eventCaptureRepository.calculate().subscribeOn(Schedulers.computation()),
                                    this::applyEffects)
                                    .map(dataValues -> Pair.create(section, dataValues))
                                    .onBackpressureBuffer()
                            )
                            .observeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    EventCaptureFormFragment.getInstance().setSectionSelector(),
                                    e -> Timber.log(1, "Error"),
                                    () -> Log.d("COMPLETE", "COMPLETED!!!")
                            );
        } else {
            currentPosition = position;
            currentSectionPosition.onNext(position);
        }

    }

    @Override
    public void initCompletionPercentage(FlowableProcessor<Float> completionPercentage) {
        compositeDisposable.add(
                completionPercentage
                        .observeOn(Schedulers.io())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.updatePercentage(),
                                Timber::e
                        )
        );
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
