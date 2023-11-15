package org.dhis2.usescases.events

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.data.dhislogic.DhisEventUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.EventStatus
import timber.log.Timber
import java.util.Date

class ScheduledEventPresenterImpl(
    val view: ScheduledEventContract.View,
    val d2: D2,
    val eventUid: String,
    val eventUtils: DhisEventUtils,
) : ScheduledEventContract.Presenter {

    private lateinit var disposable: CompositeDisposable

    override fun init() {
        disposable = CompositeDisposable()

        disposable.add(
            d2.eventModule().events().uid(eventUid).get()
                .flatMap {
                    Single.zip(
                        d2.programModule().programStages().uid(it.programStage()).get(),
                        d2.programModule().programs().uid(it.program()).get(),
                    ) { stage, program ->
                        Triple(stage, program, it)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { stageProgramEventData ->
                        val (stage, program, event) = stageProgramEventData
                        view.setProgram(program)
                        view.setStage(stage, event)
                        view.setEvent(event)
                    },
                    { Timber.e(it) },
                ),
        )
    }

    override fun finish() {
        disposable.clear()
    }

    override fun onBackClick() {
        view.back()
    }

    override fun getEventTei(): String {
        return d2.eventModule().events().uid(eventUid)
            .get()
            .map {
                d2.enrollmentModule().enrollments().uid(it.enrollment()).blockingGet()
                    ?.trackedEntityInstance()
            }.blockingGet()!!
    }

    override fun getEnrollment(): Enrollment? {
        return d2.eventModule().events().uid(eventUid)
            .get()
            .map { it.enrollment() }
            .flatMap { d2.enrollmentModule().enrollments().uid(it).get() }
            .blockingGet()
    }

    override fun setEventDate(date: Date) {
        d2.eventModule().events().uid(eventUid).setEventDate(date)
        d2.eventModule().events().uid(eventUid).setStatus(EventStatus.ACTIVE)
        if (eventUtils.newEventNeedsExtraInfo(eventUid)) {
            view.openInitialActivity()
        } else {
            view.openFormActivity()
        }
    }

    override fun setDueDate(date: Date) {
        d2.eventModule().events().uid(eventUid).setDueDate(date)
        d2.eventModule().events().uid(eventUid).setStatus(EventStatus.SCHEDULE)
        view.back()
    }

    override fun skipEvent() {
        d2.eventModule().events().uid(eventUid).setStatus(EventStatus.SKIPPED)
        view.back()
    }

    override fun setCatOptionCombo(catComboUid: String, arrayList: ArrayList<CategoryOption>) {
        val catOptComboUid = d2
            .categoryModule()
            .categoryOptionCombos()
            .byCategoryOptions(UidsHelper.getUidsList(arrayList))
            .byCategoryComboUid()
            .eq(catComboUid)
            .one()
            .blockingGet()
            ?.uid()
        d2.eventModule().events().uid(eventUid).setAttributeOptionComboUid(catOptComboUid)
    }
}
