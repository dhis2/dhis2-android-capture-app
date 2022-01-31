package org.dhis2.form.data

import io.reactivex.Flowable
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.FieldViewModelFactory
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType

class EventDetailRepository(
    private val fieldFactory: FieldViewModelFactory,
    eventUid: String,
    private val d2: D2,
    private val resourceManager: ResourceManager,
    eventCreationType: String?
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

    private val creationType = eventCreationType?.let {
        EventCreationType.valueOf(it)
    } ?: EventCreationType.DEFAULT

    override fun list(): Flowable<MutableList<FieldUiModel>> {
        val eventInitialList = ArrayList<FieldUiModel>().apply {
            add(getEventReportDate())
        }
        return Flowable.just(eventInitialList)
    }

    private fun getEventReportDate(): FieldUiModel {
        return fieldFactory.create(
            id = UID,
            label = getEventLabel(),
            valueType = ValueType.DATE,
            mandatory = false,
            value = event.eventDate()?.let { DateUtils.oldUiDateFormat().format(it) },
            allowFutureDates = false,
            editable = true,
            description = programStage.description(),
            objectStyle = ObjectStyle.builder().build()
        )
    }

    override fun sectionUids() = Flowable.just(emptyList<String>())

    override fun isEvent() = true

    private fun getEventLabel(): String {
        return when (creationType) {
            EventCreationType.SCHEDULE ->
                programStage.dueDateLabel() ?: resourceManager.getString(R.string.due_date)
            else -> {
                programStage.executionDateLabel() ?: resourceManager.getString(R.string.event_date)
            }
        }
    }

    companion object {
        const val UID = "EVENT_INITIAL_UID"
    }
}