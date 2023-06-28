package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import org.dhis2.R;
import org.dhis2.ui.dialogs.bottomsheet.FieldWithIssue;
import org.dhis2.commons.data.tuples.Quartet;
import org.dhis2.commons.prefs.Preference;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain.ConfigureEventCompletionDialog;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model.EventCompletionDialog;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.event.EventStatus;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;
import timber.log.Timber;

@Singleton
public class EventCapturePresenterImpl implements EventCaptureContract.Presenter {

    private final EventCaptureContract.EventCaptureRepository eventCaptureRepository;
    private final String eventUid;
    private final SchedulerProvider schedulerProvider;
    public CompositeDisposable compositeDisposable;
    private final EventCaptureContract.View view;
    private boolean hasExpired;
    private final PublishProcessor<Unit> notesCounterProcessor;
    private final PreferenceProvider preferences;
    private final ConfigureEventCompletionDialog configureEventCompletionDialog;

    public EventCapturePresenterImpl(EventCaptureContract.View view, String eventUid,
                                     EventCaptureContract.EventCaptureRepository eventCaptureRepository,
                                     SchedulerProvider schedulerProvider,
                                     PreferenceProvider preferences,
                                     ConfigureEventCompletionDialog configureEventCompletionDialog
    ) {
        this.view = view;
        this.eventUid = eventUid;
        this.eventCaptureRepository = eventCaptureRepository;
        this.schedulerProvider = schedulerProvider;
        this.compositeDisposable = new CompositeDisposable();
        this.preferences = preferences;
        this.configureEventCompletionDialog = configureEventCompletionDialog;

        notesCounterProcessor = PublishProcessor.create();
    }

    @Override
    public void init() {

        compositeDisposable.add(
                eventCaptureRepository.eventIntegrityCheck()
                        .filter(check -> !check)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                checkDidNotPass -> view.showEventIntegrityAlert(),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                Flowable.zip(
                        eventCaptureRepository.programStageName(),
                        eventCaptureRepository.eventDate(),
                        eventCaptureRepository.orgUnit(),
                        eventCaptureRepository.catOption(),
                        Quartet::create
                )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                data -> {
                                    preferences.setValue(Preference.CURRENT_ORG_UNIT, data.val2().uid());
                                    view.renderInitialInfo(data.val0(), data.val1(), data.val2().displayName(), data.val3());
                                },
                                Timber::e
                        )

        );

        checkExpiration();
    }

    @Override
    public void refreshProgramStage() {
        String stageName = eventCaptureRepository.programStageName().blockingFirst();
        view.updateProgramStageName(stageName);
    }

    private void checkExpiration() {
        if (getEventStatus() == EventStatus.COMPLETED)
            compositeDisposable.add(
                    eventCaptureRepository.isCompletedEventExpired(eventUid)
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .subscribe(
                                    hasExpiredResult -> this.hasExpired = hasExpiredResult && !eventCaptureRepository.isEventEditable(eventUid),
                                    Timber::e
                            )
            );
        else
            this.hasExpired = !eventCaptureRepository.isEventEditable(eventUid);
    }

    @Override
    public void onBackClick() {
        view.goBack();
    }

    @Override
    public void attemptFinish(boolean canComplete, String onCompleteMessage,
                              List<FieldWithIssue> errorFields,
                              Map<String, String> emptyMandatoryFields,
                              List<FieldWithIssue> warningFields) {

        if (!errorFields.isEmpty()) {
            view.showErrorSnackBar();
        }

        EventStatus eventStatus = getEventStatus();
        if (eventStatus != EventStatus.ACTIVE) {
            setUpActionByStatus(eventStatus);
        } else {

            EventCompletionDialog eventCompletionDialog = configureEventCompletionDialog.invoke(
                    errorFields,
                    emptyMandatoryFields,
                    warningFields,
                    canComplete,
                    onCompleteMessage
            );

            view.showCompleteActions(
                    canComplete && eventCaptureRepository.isEnrollmentOpen(),
                    emptyMandatoryFields,
                    eventCompletionDialog
            );
        }

        view.showNavigationBar();
    }

    private void setUpActionByStatus(EventStatus eventStatus) {
        switch (eventStatus) {
            case COMPLETED:
                if (!hasExpired && !eventCaptureRepository.isEnrollmentCancelled())
                    view.SaveAndFinish();
                else
                    view.finishDataEntry();
                break;
            case OVERDUE:
                view.attemptToSkip();
                break;
            case SKIPPED:
                view.attemptToReschedule();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isEnrollmentOpen() {
        return eventCaptureRepository.isEnrollmentOpen();
    }

    @Override
    public void completeEvent(boolean addNew) {
        compositeDisposable.add(
                eventCaptureRepository.completeEvent()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                success -> {
                                    if (addNew)
                                        view.restartDataEntry();
                                    else {
                                        preferences.setValue(Preference.PREF_COMPLETED_EVENT, eventUid);
                                        view.finishDataEntry();
                                    }
                                },
                                Timber::e
                        ));
    }

    @Override
    public void deleteEvent() {
        compositeDisposable.add(eventCaptureRepository.deleteEvent()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        result -> {
                            if (result)
                                view.showSnackBar(R.string.event_was_deleted);
                        },
                        Timber::e,
                        view::finishDataEntry
                )
        );
    }

    @Override
    public void skipEvent() {
        compositeDisposable.add(eventCaptureRepository.updateEventStatus(EventStatus.SKIPPED)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        result -> view.showSnackBar(R.string.event_was_skipped),
                        Timber::e,
                        view::finishDataEntry
                )
        );
    }

    @Override
    public void rescheduleEvent(Date time) {
        compositeDisposable.add(
                eventCaptureRepository.rescheduleEvent(time)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                result -> view.finishDataEntry(),
                                Timber::e
                        )
        );
    }

    @Override
    public boolean canWrite() {
        return eventCaptureRepository.getAccessDataWrite();
    }

    @Override
    public boolean hasExpired() {
        return hasExpired;
    }

    @Override
    public void onDettach() {
        this.compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    @Override
    public void initNoteCounter() {
        if (!notesCounterProcessor.hasSubscribers()) {
            compositeDisposable.add(
                    notesCounterProcessor.startWith(new Unit())
                            .flatMapSingle(unit ->
                                    eventCaptureRepository.getNoteCount())
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .subscribe(
                                    view::updateNoteBadge,
                                    Timber::e
                            )
            );
        } else {
            notesCounterProcessor.onNext(new Unit());
        }
    }

    @Override
    public void refreshTabCounters() {
        initNoteCounter();
    }

    @Override
    public void hideProgress() {
        view.hideProgress();
    }

    @Override
    public void showProgress() {
        view.showProgress();
    }

    @Override
    public boolean getCompletionPercentageVisibility() {
        return eventCaptureRepository.showCompletionPercentage();
    }

    private EventStatus getEventStatus() {
        return eventCaptureRepository.eventStatus().blockingFirst();
    }
}
