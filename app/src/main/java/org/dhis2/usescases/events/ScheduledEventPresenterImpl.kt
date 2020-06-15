package org.dhis2.usescases.events

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.util.Date
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import timber.log.Timber

class ScheduledEventPresenterImpl(
    val d2: D2,
    val eventUid: String
) : ScheduledEventContract.Presenter {

    private lateinit var view: ScheduledEventContract.View
    private lateinit var disposable: CompositeDisposable

    override fun init(view: ScheduledEventContract.View) {
        this.view = view
        disposable = CompositeDisposable()

        disposable.add(
            d2.eventModule().events().uid(eventUid).get()
                .flatMap {
                    Single.zip(
                        d2.programModule().programStages().uid(it.programStage()).get(),
                        d2.programModule().programs().uid(it.program()).get(),
                        BiFunction<ProgramStage, Program, Triple<ProgramStage, Program, Event>>
                        { stage, program ->
                            Triple(stage, program, it)
                        }
                    )
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { stageProgramEventData ->
                        val (stage, program, event) = stageProgramEventData
                        val catComboUid = program.categoryComboUid()
                        view.setProgram(program)
                        view.setStage(stage)
                        view.setEvent(event)
                        if (program.categoryComboUid() !== null && d2
                            .categoryModule()
                            .categoryCombos()
                            .uid(catComboUid)
                            .blockingGet()
                            .isDefault == false
                        ) {
                            view.setCatCombo(
                                d2
                                    .categoryModule()
                                    .categoryCombos().uid(catComboUid).blockingGet()!!,
                                getCatOptions(event.attributeOptionCombo())
                            )
                        }
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
        d2.eventModule().events().uid(eventUid).setEventDate(date)
        d2.eventModule().events().uid(eventUid).setStatus(EventStatus.ACTIVE)
        view.back()
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

    override fun setCatOptionCombo(
        catComboUid: String,
        arrayList: ArrayList<CategoryOption>
    ) {
        val catOptComboUid = d2
            .categoryModule()
            .categoryOptionCombos()
            .byCategoryOptions(UidsHelper.getUidsList(arrayList))
            .byCategoryComboUid()
            .eq(catComboUid)
            .one()
            .blockingGet()
            .uid()
        d2.eventModule().events().uid(eventUid).setAttributeOptionComboUid(catOptComboUid)
    }
}
