package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.commons.bindings.SdkExtensionsKt;
import org.dhis2.data.dhislogic.AuthoritiesKt;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.ValidationStrategy;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventEditableStatus;
import org.hisp.dhis.android.core.event.EventNonEditableReason;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.ProgramRule;
import org.hisp.dhis.android.core.program.ProgramRuleAction;
import org.hisp.dhis.android.core.program.ProgramRuleActionType;
import org.hisp.dhis.android.core.settings.ProgramConfigurationSetting;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class EventCaptureRepositoryImpl implements EventCaptureContract.EventCaptureRepository {

    private final String eventUid;
    private final D2 d2;

    public EventCaptureRepositoryImpl(String eventUid, D2 d2) {
        this.eventUid = eventUid;
        this.d2 = d2;
    }

    private Event getCurrentEvent() {
        return d2.eventModule().events().uid(eventUid).blockingGet();
    }

    @Override
    public boolean isEnrollmentOpen() {
        Event currentEvent = getCurrentEvent();
        return currentEvent.enrollment() == null || d2.enrollmentModule().enrollmentService().blockingIsOpen(currentEvent.enrollment());
    }

    @Override
    public boolean isEnrollmentCancelled() {
        Enrollment enrollment = d2.enrollmentModule().enrollments().uid(getCurrentEvent().enrollment()).blockingGet();
        if (enrollment == null)
            return false;
        else
            return enrollment.status() == EnrollmentStatus.CANCELLED;
    }

    @Override
    public boolean isEventEditable(@NonNull String eventUid) {
        return d2.eventModule().eventService().blockingIsEditable(eventUid);
    }

    @NonNull
    @Override
    public Flowable<String> programStageName() {
        return d2.programModule().programStages().uid(getCurrentEvent().programStage()).get()
                .map(BaseIdentifiableObject::displayName)
                .toFlowable();
    }

    @NonNull
    @Override
    public Flowable<OrganisationUnit> orgUnit() {
        return Flowable.just(
                Objects.requireNonNull(
                        d2.organisationUnitModule()
                                .organisationUnits()
                                .uid(getCurrentEvent().organisationUnit())
                                .blockingGet()
                )
        );
    }

    @NonNull
    @Override
    public Observable<Boolean> deleteEvent() {
        return d2.eventModule().events().uid(eventUid).delete()
                .andThen(Observable.just(true));
    }

    @NonNull
    @Override
    public Observable<Boolean> updateEventStatus(@NonNull EventStatus status) {

        return Observable.fromCallable(() -> {
            d2.eventModule().events().uid(eventUid)
                    .setStatus(status);
            return true;
        });
    }

    @NonNull
    @Override
    public Observable<Boolean> rescheduleEvent(@NonNull Date newDate) {
        return Observable.fromCallable(() -> {
            d2.eventModule().events().uid(eventUid)
                    .setDueDate(newDate);
            d2.eventModule().events().uid(eventUid)
                    .setStatus(EventStatus.SCHEDULE);
            return true;
        });
    }

    @NonNull
    @Override
    public Observable<String> programStage() {
        return Observable.just(Objects.requireNonNull(getCurrentEvent().programStage()));
    }

    @Override
    public boolean getAccessDataWrite() {
        return d2.eventModule().eventService().blockingIsEditable(eventUid);
    }

    @NonNull
    @Override
    public Flowable<EventStatus> eventStatus() {
        return Flowable.just(Objects.requireNonNull(getCurrentEvent().status()));
    }

    @NonNull
    @Override
    public Single<Boolean> canReOpenEvent() {
        return Single.fromCallable(() -> d2.userModule().authorities()
                .byName().in(AuthoritiesKt.AUTH_UNCOMPLETE_EVENT, AuthoritiesKt.AUTH_ALL).one().blockingExists()
        );
    }

    @NonNull
    @Override
    public Observable<Boolean> isCompletedEventExpired(@NonNull String eventUid) {
        return d2.eventModule().eventService().getEditableStatus(eventUid).map(editionStatus -> {
            if (editionStatus instanceof EventEditableStatus.NonEditable nonEditableStatus) {
                return nonEditableStatus.getReason() == EventNonEditableReason.EXPIRED;
            } else {
                return false;
            }
        }).toObservable();
    }

    @NonNull
    @Override
    public Flowable<Boolean> eventIntegrityCheck() {
        Event currentEvent = getCurrentEvent();
        return Flowable.just(currentEvent).map(event ->
                (event.status() == EventStatus.COMPLETED ||
                        event.status() == EventStatus.ACTIVE ||
                            event.status() == EventStatus.SKIPPED) &&
                        (event.eventDate() == null || !event.eventDate().after(new Date()))
        );
    }

    @NonNull
    @Override
    public Single<Integer> getNoteCount() {
        return d2.noteModule().notes().byEventUid().eq(eventUid).count();
    }

    @Override
    public boolean showCompletionPercentage() {
        if (getCurrentEvent() != null && d2.settingModule().appearanceSettings().blockingExists()) {
            ProgramConfigurationSetting programConfigurationSetting = d2.settingModule()
                    .appearanceSettings()
                    .getProgramConfigurationByUid(getCurrentEvent().program());

            if (programConfigurationSetting != null &&
                    programConfigurationSetting.completionSpinner() != null) {
                return programConfigurationSetting.completionSpinner();
            }
        }
        return true;
    }

    @Override
    public boolean hasAnalytics() {
        Event currentEvent = getCurrentEvent();
        boolean hasProgramIndicators = !d2.programModule().programIndicators().byProgramUid().eq(currentEvent.program()).blockingIsEmpty();
        List<ProgramRule> programRules = d2.programModule().programRules().withProgramRuleActions()
                .byProgramUid().eq(currentEvent.program()).blockingGet();
        boolean hasProgramRules = false;
        for (ProgramRule rule : programRules) {
            for (ProgramRuleAction action : Objects.requireNonNull(rule.programRuleActions())) {
                if (action.programRuleActionType() == ProgramRuleActionType.DISPLAYKEYVALUEPAIR ||
                        action.programRuleActionType() == ProgramRuleActionType.DISPLAYTEXT) {
                    hasProgramRules = true;
                }
            }
        }
        return hasProgramIndicators || hasProgramRules;
    }

    @Override
    public boolean hasRelationships() {
        return !d2.relationshipModule().relationshipTypes()
                .byAvailableForEvent(eventUid)
                .blockingIsEmpty();
    }

    @NonNull
    @Override
    public ValidationStrategy validationStrategy() {
        ValidationStrategy validationStrategy =
                SdkExtensionsKt.programStage(d2, programStage().blockingFirst())
                        .validationStrategy();

        return validationStrategy != null ? validationStrategy : ValidationStrategy.ON_COMPLETE;
    }

    @Override
    @Nullable
    public String getEnrollmentUid() {
        Event currentEvent = getCurrentEvent();
        if (currentEvent == null) {
            return null;
        }else{
            return currentEvent.enrollment();
        }
    }

    @Override
    @Nullable
    public String getTeiUid() {
        Enrollment enrollment = d2.enrollmentModule().enrollments().uid(getEnrollmentUid()).blockingGet();
        return enrollment != null ? enrollment.trackedEntityInstance() : null;
    }
}

