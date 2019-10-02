package org.dhis2.usescases.events

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap

class ScheduledEventPresenterImpl(val d2: D2,
                                  val eventUid: String) : ScheduledEventContract.Presenter {


    private lateinit var view: ScheduledEventContract.View
    private lateinit var disposable: CompositeDisposable

    override fun init(view: ScheduledEventContract.View) {
        this.view = view
        disposable = CompositeDisposable()

        disposable.add(
                d2.eventModule().events.uid(eventUid).get()
                        .flatMap { event ->
                            Single.zip(
                                    d2.programModule().programStages.withStyle().uid(event.programStage()).get(),
                                    d2.programModule().programs.withCategoryCombo().uid(event.program()).get(),
                                    BiFunction<ProgramStage, Program, Triple<ProgramStage, Program, Event>> { stage, program -> Triple(stage, program, event) })
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { stageProgramEventData ->
                                    val (stage, program, event) = stageProgramEventData
                                    view.setProgram(program)
                                    view.setStage(stage)
                                    view.setEvent(event)
                                    if (program.categoryCombo() !== null && program.categoryCombo()!!.isDefault == false)
                                        view.setCatCombo(program.categoryCombo()!!, getCatOptions(event.attributeOptionCombo()))
                                },
                                { Timber.e(it) }
                        )
        )

    }

    override fun finish() {
        disposable.clear()
    }

    override fun onBackClick() {
        view.back()
    }

    private fun getCatOptions(categoryOptionComboUid: String?): HashMap<String, CategoryOption> {
        var map = HashMap<String, CategoryOption>()


        return map
    }

    override fun setEventDate(date: Date) {
        d2.eventModule().events.uid(eventUid).setEventDate(date)
        d2.eventModule().events.uid(eventUid).setStatus(EventStatus.ACTIVE)
        view.back()
    }

    override fun setDueDate(date: Date) {
        d2.eventModule().events.uid(eventUid).setDueDate(date)
        d2.eventModule().events.uid(eventUid).setStatus(EventStatus.SCHEDULE)
        view.back()
    }

    override fun skipEvent() {
        d2.eventModule().events.uid(eventUid).setStatus(EventStatus.SKIPPED)
        view.back()
    }

    override fun setCatOptionCombo(catComboUid: String, arrayList: ArrayList<CategoryOption>) {
        val catOptComboUid = d2.categoryModule().categoryOptionCombos.byCategoryOptions(UidsHelper.getUidsList(arrayList))
                .byCategoryComboUid().eq(catComboUid).one().blockingGet().uid()
        d2.eventModule().events.uid(eventUid).setAttributeOptionComboUid(catOptComboUid)
    }
}
