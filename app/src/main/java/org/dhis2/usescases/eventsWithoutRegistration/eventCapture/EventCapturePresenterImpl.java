package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.support.annotation.NonNull;
import android.util.Log;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.DataEntryArguments;
import org.dhis2.data.forms.dataentry.DataEntryStore;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCapturePresenterImpl implements EventCaptureContract.Presenter {

    private final EventCaptureContract.EventCaptureRepository eventCaptureRepository;
    private final RulesUtilsProvider rulesUtils;
    private final DataEntryStore dataEntryStore;
    private CompositeDisposable compositeDisposable;
    private EventCaptureContract.View view;
    private int currentPosition;
    private FlowableProcessor<Integer> currentSectionPosition;
    private List<FormSectionViewModel> sectionList;
    private Map<String, FieldViewModel> emptyMandatoryFields;

    public EventCapturePresenterImpl(EventCaptureContract.EventCaptureRepository eventCaptureRepository, RulesUtilsProvider rulesUtils, DataEntryStore dataEntryStore) {
        this.eventCaptureRepository = eventCaptureRepository;
        this.rulesUtils = rulesUtils;
        this.dataEntryStore = dataEntryStore;
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
                            FormSectionViewModel formSectionViewModel = sectionList.get(position);
                            if (sectionList.size() > 1) {
                                DataEntryArguments arguments =
                                        DataEntryArguments.forEventSection(formSectionViewModel.uid(),
                                                formSectionViewModel.sectionUid(),
                                                formSectionViewModel.renderType());
                                EventCaptureFormFragment.getInstance().setSectionTitle(arguments, formSectionViewModel);
                            } else {
                                DataEntryArguments arguments =
                                        DataEntryArguments.forEvent(formSectionViewModel.uid());
                                EventCaptureFormFragment.getInstance().setSingleSection(arguments, formSectionViewModel);
                            }
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

        compositeDisposable.add(EventCaptureFormFragment.getInstance().dataEntryFlowable()
                .debounce(500, TimeUnit.MILLISECONDS, Schedulers.computation())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .switchMap(action ->
                        {
                            Timber.d("dataEntryRepository.save(uid=[%s], value=[%s])",
                                    action.id(), action.value());
                            return dataEntryStore.save(action.id(), action.value());
                        }
                ).subscribe(result -> Timber.d(result.toString()),
                        Timber::d)
        );

        compositeDisposable.add(
                Flowable.zip(
                        eventCaptureRepository.list(),
                        eventCaptureRepository.calculate().subscribeOn(Schedulers.computation()),
                        this::applyEffects)
                        .map(fields -> {
                            emptyMandatoryFields = new HashMap<>();
                            for (FieldViewModel fieldViewModel : fields) {
                                if (fieldViewModel.mandatory() && isEmpty(fieldViewModel.value()))
                                    emptyMandatoryFields.put(fieldViewModel.uid(), fieldViewModel);
                            }
                            return fields;
                        })
                        .map(fields -> {
                            HashMap<String, List<FieldViewModel>> fieldMap = new HashMap<>();

                            for (FieldViewModel fieldViewModel : fields) {
                                if (!fieldMap.containsKey(fieldViewModel.programStageSection()))
                                    fieldMap.put(fieldViewModel.programStageSection(), new ArrayList<>());
                                fieldMap.get(fieldViewModel.programStageSection()).add(fieldViewModel);
                            }

                            List<EventSectionModel> eventSectionModels = new ArrayList<>();
                            for (FormSectionViewModel sectionModel : sectionList) {
                                List<FieldViewModel> fieldViewModels = fieldMap.get(sectionModel.sectionUid());

                                int cont = 0;
                                for (FieldViewModel fieldViewModel : fieldViewModels)
                                    if (!isEmpty(fieldViewModel.value()))
                                        cont++;

                                eventSectionModels.add(EventSectionModel.create(sectionModel.label(), sectionModel.sectionUid(), cont, fieldViewModels.size()));
                            }

                            return eventSectionModels;
                        })
                        .observeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                EventCaptureFormFragment.getInstance().setSectionSelector(),
                                e -> Timber.log(1, "Error")
                        ));
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
        } else {
            if (!emptyMandatoryFields.isEmpty()) {
                view.setMandatoryWarning(emptyMandatoryFields);
            }else {
                //TODO: Check error | warnings
                //TODO: Check errorOnComplete | warningOnComplete
                //TODO: FINISH DATA ENTRY
                AtomicInteger cont = new AtomicInteger();
                compositeDisposable.add(
                        Observable.concat(
                                Observable.just(1, 2, 3, 4, 5),
                                Observable.just("a", "b", "c"))
                                .observeOn(Schedulers.io())
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        data -> cont.getAndIncrement(),
                                        Timber::e,
                                        () -> view.attemptToFinish()
                                )
                );
            }
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
        if (isCurrentSection && position != -1) {
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
