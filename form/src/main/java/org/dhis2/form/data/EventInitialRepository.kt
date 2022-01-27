package org.dhis2.form.data

import io.reactivex.Flowable
import org.dhis2.commons.date.DateUtils
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.FieldViewModelFactory
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType

class EventInitialRepository(
    private val fieldFactory: FieldViewModelFactory,
    private val eventUid: String,
    private val d2: D2
) : DataEntryBaseRepository(d2, fieldFactory) {

    private val programStage = d2.eventModule()
        .events()
        .byUid()
        .eq(eventUid).one().blockingGet().let {
            d2.programModule()
                .programStages()
                .byUid()
                .eq(it.programStage()).one().blockingGet()
        }

    private val event = d2.eventModule().events().uid(eventUid).blockingGet()

    override fun list(): Flowable<MutableList<FieldUiModel>> {
        val eventInitialList = ArrayList<FieldUiModel>().apply {
            add(getEventReportDate())
        }
        return Flowable.just(eventInitialList)
    }

    private fun getEventReportDate(): FieldUiModel {
        return fieldFactory.create(
            id = UID,
            label = programStage.dueDateLabel() ?:"Due date",
            valueType = ValueType.DATE,
            mandatory = true,
            optionSet = null,
            value = event.eventDate()?.let { DateUtils.oldUiDateFormat().format(it) },
            programStageSection = null,
            allowFutureDates = false,
            editable = true,
            renderingType = null,
            description = programStage.description(),
            fieldRendering = null,
            optionCount = null,
            objectStyle = ObjectStyle.builder().build(),
            fieldMask = null,
            legendValue = null,
            options =  null,
            featureType = null
        )
    }

    override fun sectionUids() = Flowable.just(emptyList<String>())

    override fun isEvent() = true

    companion object {
        const val UID = "EVENT_INITIAL_UID"
    }
}