package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.dhis2.commons.bindings.programStage
import org.dhis2.data.dhislogic.AUTH_ALL
import org.dhis2.data.dhislogic.AUTH_UNCOMPLETE_EVENT
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract.EventCaptureRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValidationStrategy
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventEditableStatus
import org.hisp.dhis.android.core.event.EventNonEditableReason
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import java.util.Date
import java.util.Objects
import java.util.concurrent.Callable

class EventCaptureRepositoryImpl(
    private val d2: D2,
    private val eventUid: String,
) : EventCaptureRepository {
    private val currentEvent by lazy {
        requireNotNull(
            d2.eventModule().events().uid(eventUid).blockingGet()
        )
    }

    override val isEnrollmentOpen: Boolean
        get() {
            val currentEvent = this.currentEvent
            return currentEvent.enrollment() == null || d2.enrollmentModule().enrollmentService()
                .blockingIsOpen(currentEvent.enrollment()!!)
        }

    override val isEnrollmentCancelled: Boolean
        get() {
            val enrollment =
                d2.enrollmentModule().enrollments().uid(this.currentEvent.enrollment())
                    .blockingGet()
            return if (enrollment == null) {
                false
            } else {
                enrollment.status() == EnrollmentStatus.CANCELLED
            }
        }

    override fun isEventEditable(eventUid: String): Boolean {
        return d2.eventModule().eventService().blockingIsEditable(eventUid)
    }

    override fun programStageName(): Flowable<String> {
        return d2.programModule().programStages().uid(this.currentEvent.programStage()).get()
            .map { programStage -> programStage.displayName() ?: programStage.uid() }
            .toFlowable()
    }

    override fun orgUnit(): Flowable<OrganisationUnit> {
        return Flowable.just(
            Objects.requireNonNull<OrganisationUnit>(
                d2.organisationUnitModule()
                    .organisationUnits()
                    .uid(this.currentEvent.organisationUnit())
                    .blockingGet()
            )
        )
    }

    override fun deleteEvent(): Observable<Boolean> {
        return d2.eventModule().events().uid(eventUid).delete()
            .andThen(Observable.just(true))
    }

    override fun updateEventStatus(status: EventStatus): Observable<Boolean> {
        return Observable.fromCallable(Callable {
            d2.eventModule().events().uid(eventUid)
                .setStatus(status)
            true
        })
    }

    override fun rescheduleEvent(newDate: Date): Observable<Boolean> {
        return Observable.fromCallable(Callable {
            d2.eventModule().events().uid(eventUid)
                .setDueDate(newDate)
            d2.eventModule().events().uid(eventUid)
                .setStatus(EventStatus.SCHEDULE)
            true
        })
    }

    override fun programStage(): Observable<String> {
        return Observable.just(
            Objects.requireNonNull<String?>(
                this.currentEvent.programStage()
            )
        )
    }

    override val accessDataWrite: Boolean
        get() = d2.eventModule().eventService().blockingIsEditable(eventUid)

    override fun eventStatus(): Flowable<EventStatus> {
        return Flowable.just(Objects.requireNonNull<EventStatus>(this.currentEvent.status()))
    }

    override fun canReOpenEvent(): Single<Boolean> {
        return Single.fromCallable(Callable {
            d2.userModule().authorities()
                .byName().`in`(AUTH_UNCOMPLETE_EVENT, AUTH_ALL).one().blockingExists()
        }
        )
    }

    override fun isCompletedEventExpired(eventUid: String): Observable<Boolean> {
        return d2.eventModule().eventService().getEditableStatus(eventUid)
            .map { editionStatus ->
                if (editionStatus is EventEditableStatus.NonEditable) {
                    editionStatus.reason == EventNonEditableReason.EXPIRED
                } else {
                    false
                }
            }.toObservable()
    }

    override fun eventIntegrityCheck(): Flowable<Boolean> {
        val currentEvent = this.currentEvent
        return Flowable.just(currentEvent).map { event ->
            (event.status() == EventStatus.COMPLETED || event.status() == EventStatus.ACTIVE || event.status() == EventStatus.SKIPPED) &&
                    (event.eventDate() == null || !event.eventDate()!!.after(Date()))
        }
    }

    override val noteCount: Single<Int>
        get() = d2.noteModule().notes().byEventUid().eq(eventUid).count()

    override fun showCompletionPercentage(): Boolean {
        if (d2.settingModule().appearanceSettings().blockingExists()) {
            val programConfigurationSetting = d2.settingModule()
                .appearanceSettings()
                .getProgramConfigurationByUid(this.currentEvent.program())

            if (programConfigurationSetting != null &&
                programConfigurationSetting.completionSpinner() != null
            ) {
                return programConfigurationSetting.completionSpinner()!!
            }
        }
        return true
    }

    override fun hasAnalytics(): Boolean {
        val currentEvent = this.currentEvent
        val hasProgramIndicators =
            !d2.programModule().programIndicators().byProgramUid().eq(currentEvent.program())
                .blockingIsEmpty()
        val programRules =
            d2.programModule().programRules().withProgramRuleActions()
                .byProgramUid().eq(currentEvent.program()).blockingGet()
        var hasProgramRules = false
        for (rule in programRules) {
            for (action in Objects.requireNonNull(rule.programRuleActions())) {
                if (action.programRuleActionType() == ProgramRuleActionType.DISPLAYKEYVALUEPAIR ||
                    action.programRuleActionType() == ProgramRuleActionType.DISPLAYTEXT
                ) {
                    hasProgramRules = true
                }
            }
        }
        return hasProgramIndicators || hasProgramRules
    }

    override fun hasRelationships(): Boolean {
        return !d2.relationshipModule().relationshipTypes()
            .byAvailableForEvent(eventUid)
            .blockingIsEmpty()
    }

    override fun validationStrategy(): ValidationStrategy {
        val validationStrategy =
            d2.programStage(programStage().blockingFirst()!!)!!
                .validationStrategy()

        return validationStrategy ?: ValidationStrategy.ON_COMPLETE
    }

    override fun getEnrollmentUid(): String? {
        return currentEvent.enrollment()
    }

    override fun getTeiUid(): String? {
        val enrollment = d2.enrollmentModule().enrollments().uid(getEnrollmentUid()).blockingGet()
        return enrollment?.trackedEntityInstance()
    }
}

