package org.dhis2.usescases.events

import androidx.paging.PagingData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.dhis2.commons.bindings.event
import org.dhis2.commons.bindings.programStage
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.periods.data.EventPeriodRepository
import org.dhis2.commons.periods.domain.GetEventPeriods
import org.dhis2.commons.periods.model.Period
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.DEFAULT_MAX_DATE
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.DEFAULT_MIN_DATE
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.InputDateValues
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScheduledEventPresenterImpl(
    val view: ScheduledEventContract.View,
    val d2: D2,
    val eventUid: String,
) : ScheduledEventContract.Presenter {
    private lateinit var disposable: CompositeDisposable
    private val getEventPeriods = GetEventPeriods(EventPeriodRepository(d2))

    override fun init() {
        disposable = CompositeDisposable()

        disposable.add(
            d2
                .eventModule()
                .events()
                .uid(eventUid)
                .get()
                .flatMap {
                    Single.zip(
                        d2
                            .programModule()
                            .programStages()
                            .uid(it.programStage())
                            .get(),
                        d2
                            .programModule()
                            .programs()
                            .uid(it.program())
                            .get(),
                    ) { stage, program ->
                        Triple(stage, program, it)
                    }
                }.observeOn(AndroidSchedulers.mainThread())
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

    override fun getEventTei(): String =
        d2
            .eventModule()
            .events()
            .uid(eventUid)
            .get()
            .map {
                d2
                    .enrollmentModule()
                    .enrollments()
                    .uid(it.enrollment())
                    .blockingGet()
                    ?.trackedEntityInstance()
            }.blockingGet()!!

    override fun getEnrollment(): Enrollment? =
        d2
            .eventModule()
            .events()
            .uid(eventUid)
            .get()
            .map { it.enrollment() }
            .flatMap {
                d2
                    .enrollmentModule()
                    .enrollments()
                    .uid(it)
                    .get()
            }.blockingGet()

    override fun setEventDate(date: Date) {
        d2
            .eventModule()
            .events()
            .uid(eventUid)
            .setEventDate(date)
        d2
            .eventModule()
            .events()
            .uid(eventUid)
            .setStatus(EventStatus.ACTIVE)
        view.openFormActivity()
    }

    override fun formatDateValues(date: InputDateValues): Date {
        val calendar = Calendar.getInstance()
        calendar[date.year, date.month - 1, date.day, 0, 0] = 0
        calendar[Calendar.MILLISECOND] = 0
        return calendar.time
    }

    override fun getDateFormatConfiguration(): String? =
        d2
            .systemInfoModule()
            .systemInfo()
            .blockingGet()
            ?.dateFormat()

    override fun getSelectableDates(
        program: Program,
        isDueDate: Boolean,
    ): SelectableDates {
        val minDate =
            if (program.expiryPeriodType() != null) {
                DateUtils.getInstance().expDate(
                    null,
                    program.expiryDays() ?: 0,
                    program.expiryPeriodType(),
                )
            } else {
                null
            }
        val minDateString =
            if (minDate == null) null else SimpleDateFormat("ddMMyyyy", Locale.US).format(minDate)
        val maxDateString =
            if (isDueDate) {
                DEFAULT_MAX_DATE
            } else {
                SimpleDateFormat(
                    "ddMMyyyy",
                    Locale.US,
                ).format(Date(System.currentTimeMillis() - 1000))
            }
        return SelectableDates(minDateString ?: DEFAULT_MIN_DATE, maxDateString)
    }

    override fun fetchPeriods(scheduling: Boolean): Flow<PagingData<Period>> {
        val event = d2.event(eventUid) ?: return emptyFlow()
        val stage = event.programStage()?.let { d2.programStage(it) } ?: return emptyFlow()

        return getEventPeriods(
            eventUid = eventUid,
            periodType = stage.periodType() ?: PeriodType.Daily,
            selectedDate =
                if (scheduling) {
                    event.dueDate()
                } else {
                    event.eventDate()
                },
            programStage = stage,
            isScheduling = scheduling,
            eventEnrollmentUid = event.enrollment(),
        )
    }

    override fun setDueDate(date: Date) {
        d2
            .eventModule()
            .events()
            .uid(eventUid)
            .setDueDate(date)
        d2
            .eventModule()
            .events()
            .uid(eventUid)
            .setStatus(EventStatus.SCHEDULE)
        view.back()
    }

    override fun skipEvent() {
        d2
            .eventModule()
            .events()
            .uid(eventUid)
            .setStatus(EventStatus.SKIPPED)
        view.back()
    }

    override fun setCatOptionCombo(
        catComboUid: String,
        arrayList: ArrayList<CategoryOption>,
    ) {
        val catOptComboUid =
            d2
                .categoryModule()
                .categoryOptionCombos()
                .byCategoryOptions(UidsHelper.getUidsList(arrayList))
                .byCategoryComboUid()
                .eq(catComboUid)
                .one()
                .blockingGet()
                ?.uid()
        d2
            .eventModule()
            .events()
            .uid(eventUid)
            .setAttributeOptionComboUid(catOptComboUid)
    }
}
