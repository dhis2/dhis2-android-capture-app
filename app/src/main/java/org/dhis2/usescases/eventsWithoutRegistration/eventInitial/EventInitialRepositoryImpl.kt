package org.dhis2.usescases.eventsWithoutRegistration.eventInitial

import io.reactivex.Flowable
import io.reactivex.Observable
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.data.forms.FormSectionViewModel
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.model.OptionSetConfiguration.Companion.optionDataFlow
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper.getUidsList
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.event.EventEditableStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramStageDataElement
import org.hisp.dhis.android.core.program.ProgramStageSection
import org.hisp.dhis.android.core.program.SectionRenderingType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import java.util.concurrent.Callable

class EventInitialRepositoryImpl internal constructor(
    private val eventUid: String?,
    private val stageUid: String?,
    private val d2: D2,
    private val fieldFactory: FieldViewModelFactory,
    private val ruleEngineHelper: RuleEngineHelper?,
    private val metadataIconProvider: MetadataIconProvider,
) : EventInitialRepository {
    override fun event(eventId: String?): Observable<Event?> =
        d2
            .eventModule()
            .events()
            .uid(eventId)
            .get()
            .toObservable()

    fun orgUnits(
        programId: String,
        parentUid: String,
    ): Observable<List<OrganisationUnit>> =
        d2
            .organisationUnitModule()
            .organisationUnits()
            .byProgramUids(listOf(programId))
            .byParentUid()
            .eq(parentUid)
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
            .get()
            .toObservable()

    override fun createEvent(
        enrollmentUid: String,
        trackedEntityInstanceUid: String?,
        programUid: String,
        programStage: String,
        date: Date,
        orgUnitUid: String,
        categoryOptionComboUid: String,
        geometry: Geometry?,
    ): Observable<String> {
        val cal = Calendar.getInstance()
        cal.setTime(date)
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0

        return Observable
            .fromCallable(
                Callable {
                    d2.eventModule().events().blockingAdd(
                        EventCreateProjection
                            .builder()
                            .enrollment(enrollmentUid)
                            .program(programUid)
                            .programStage(programStage)
                            .organisationUnit(orgUnitUid)
                            .attributeOptionCombo(categoryOptionComboUid)
                            .build(),
                    )
                },
            ).map { uid ->
                val eventRepository = d2.eventModule().events().uid(uid)
                eventRepository.setEventDate(cal.getTime())
                updateEventGeometry(uid, geometry)
                uid
            }
    }

    override fun scheduleEvent(
        enrollmentUid: String?,
        trackedEntityInstanceUid: String?,
        programUid: String,
        programStage: String,
        dueDate: Date,
        orgUnitUid: String,
        catOptionUid: String,
        geometry: Geometry?,
    ): Observable<String> {
        val cal = Calendar.getInstance()
        cal.setTime(dueDate)
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0

        return Observable
            .fromCallable(
                Callable {
                    d2.eventModule().events().blockingAdd(
                        EventCreateProjection
                            .builder()
                            .enrollment(enrollmentUid)
                            .program(programUid)
                            .programStage(programStage)
                            .organisationUnit(orgUnitUid)
                            .attributeOptionCombo(catOptionUid)
                            .build(),
                    )
                },
            ).map { uid ->
                val eventRepository = d2.eventModule().events().uid(uid)
                eventRepository.setDueDate(cal.getTime())
                eventRepository.setStatus(EventStatus.SCHEDULE)
                updateEventGeometry(uid, geometry)
                uid
            }
    }

    private fun updateEventGeometry(
        eventUid: String,
        geometry: Geometry?,
    ) {
        val event =
            d2
                .eventModule()
                .events()
                .uid(eventUid)
                .blockingGet()
        val stageFeatureType =
            d2
                .programModule()
                .programStages()
                .uid(event?.programStage())
                .blockingGet()
                ?.featureType()
        when (stageFeatureType) {
            FeatureType.POINT, FeatureType.POLYGON, FeatureType.MULTI_POLYGON ->
                d2
                    .eventModule()
                    .events()
                    .uid(eventUid)
                    .setGeometry(geometry)

            else -> {
                // do nothing
            }
        }
    }

    override fun programStage(programUid: String): Observable<ProgramStage?> =
        d2
            .programModule()
            .programStages()
            .byProgramUid()
            .eq(programUid)
            .one()
            .get()
            .toObservable()

    override fun programStageWithId(programStageUid: String?): Observable<ProgramStage?> =
        d2
            .programModule()
            .programStages()
            .uid(programStageUid)
            .get()
            .toObservable()

    override fun programStageForEvent(eventId: String?): Flowable<ProgramStage?> =
        d2
            .eventModule()
            .events()
            .uid(eventId)
            .get()
            .toFlowable()
            .map { event ->
                d2
                    .programModule()
                    .programStages()
                    .byUid()
                    .eq(event.programStage())
                    .one()
                    .blockingGet()
            }

    override fun accessDataWrite(programUid: String): Observable<Boolean> =
        if (eventUid != null) {
            d2
                .eventModule()
                .eventService()
                .isEditable(eventUid)
                .toObservable()
        } else {
            d2
                .programModule()
                .programStages()
                .uid(stageUid)
                .get()
                .toObservable()
                .map { programStage ->
                    programStage.access().data().write()
                }
        }

    override fun deleteEvent(
        eventId: String?,
        trackedEntityInstance: String?,
    ) {
        try {
            d2
                .eventModule()
                .events()
                .uid(eventId)
                .blockingDelete()
        } catch (d2Error: D2Error) {
            Timber.e(d2Error)
        }
    }

    override fun isEnrollmentOpen(): Boolean {
        val event =
            d2
                .eventModule()
                .events()
                .uid(eventUid)
                .blockingGet()
        return event == null ||
            event.enrollment() == null ||
            d2
                .enrollmentModule()
                .enrollments()
                .uid(event.enrollment())
                .blockingGet()!!
                .status() == EnrollmentStatus.ACTIVE
    }

    override fun getProgramWithId(programUid: String?): Observable<Program?> =
        d2
            .programModule()
            .programs()
            .withTrackedEntityType()
            .uid(programUid)
            .get()
            .toObservable()

    override fun showCompletionPercentage(): Boolean {
        if (d2.settingModule().appearanceSettings().blockingExists()) {
            val programUid =
                d2
                    .eventModule()
                    .events()
                    .uid(eventUid)
                    .blockingGet()!!
                    .program()
            val programConfigurationSetting =
                d2
                    .settingModule()
                    .appearanceSettings()
                    .getProgramConfigurationByUid(programUid)

            if (programConfigurationSetting != null &&
                programConfigurationSetting.completionSpinner() != null
            ) {
                return programConfigurationSetting.completionSpinner()!!
            }
        }
        return true
    }

    override fun eventSections(): Flowable<List<FormSectionViewModel>> {
        return d2
            .eventModule()
            .events()
            .uid(eventUid)
            .get()
            .map { eventSingle ->
                return@map if (eventSingle.deleted() != true) {
                    val stage =
                        d2
                            .programModule()
                            .programStages()
                            .uid(eventSingle.programStage())
                            .blockingGet()
                    val stageSections =
                        d2
                            .programModule()
                            .programStageSections()
                            .byProgramStageUid()
                            .eq(stage!!.uid())
                            .blockingGet()

                    stageSections
                        .takeIf { it.isNotEmpty() }
                        ?.sortedWith(
                            Comparator { one: ProgramStageSection?, two: ProgramStageSection? ->
                                one!!
                                    .sortOrder()!!
                                    .compareTo(two!!.sortOrder()!!)
                            },
                        )?.map { section ->
                            FormSectionViewModel(
                                eventUid!!,
                                section.uid(),
                                section.displayName(),
                                if (section.renderType()!!.mobile() != null) {
                                    section
                                        .renderType()!!
                                        .mobile()!!
                                        .type()!!
                                        .name
                                } else {
                                    null
                                },
                            )
                        } ?: listOf(
                        FormSectionViewModel(
                            eventUid!!,
                            "",
                            "",
                            SectionRenderingType.LISTING.name,
                        ),
                    )
                } else {
                    emptyList()
                }
            }.toFlowable()
    }

    override fun list(): Flowable<List<FieldUiModel>> =
        d2
            .eventModule()
            .events()
            .withTrackedEntityDataValues()
            .uid(eventUid)
            .get()
            .map { event ->
                val sections =
                    d2
                        .programModule()
                        .programStageSections()
                        .withDataElements()
                        .byProgramStageUid()
                        .eq(event.programStage())
                        .blockingGet()
                val stageDataElements =
                    d2
                        .programModule()
                        .programStageDataElements()
                        .byProgramStage()
                        .eq(event.programStage())
                        .blockingGet()

                if (sections.isNotEmpty()) {
                    buildListForSections(sections, stageDataElements, event)
                } else {
                    buildListWithoutSection(stageDataElements, event)
                }
            }.toFlowable()

    private fun buildListForSections(
        sections: List<ProgramStageSection>,
        stageDataElements: List<ProgramStageDataElement>,
        event: Event,
    ) = buildList {
        for (stageSection in sections) {
            for (programStageDataElement in stageDataElements) {
                if (getUidsList<DataElement>(stageSection.dataElements()!!).contains(
                        programStageDataElement.dataElement()!!.uid(),
                    )
                ) {
                    val dataElement =
                        d2
                            .dataElementModule()
                            .dataElements()
                            .uid(
                                programStageDataElement.dataElement()!!.uid(),
                            ).blockingGet()
                    add(
                        transform(
                            programStageDataElement,
                            dataElement!!,
                            searchValueDataElement(
                                programStageDataElement
                                    .dataElement()!!
                                    .uid(),
                                event.trackedEntityDataValues()!!,
                            ),
                            stageSection.uid(),
                            event.status(),
                        ),
                    )
                }
            }
        }
    }

    private fun buildListWithoutSection(
        stageDataElements: List<ProgramStageDataElement>,
        event: Event,
    ) = buildList {
        for (programStageDataElement in stageDataElements) {
            val dataElement =
                d2
                    .dataElementModule()
                    .dataElements()
                    .uid(
                        programStageDataElement.dataElement()!!.uid(),
                    ).blockingGet()
            add(
                transform(
                    programStageDataElement,
                    dataElement!!,
                    searchValueDataElement(
                        programStageDataElement
                            .dataElement()!!
                            .uid(),
                        event.trackedEntityDataValues()!!,
                    ),
                    null,
                    event.status(),
                ),
            )
        }
    }

    override fun calculate(): Flowable<Result<List<RuleEffect>>> =
        if (ruleEngineHelper != null) {
            Flowable
                .just(ruleEngineHelper.evaluate())
                .map { data -> Result.success(data) }
        } else {
            Flowable.just(Result.success(emptyList()))
        }

    private fun transform(
        stage: ProgramStageDataElement,
        dataElement: DataElement,
        value: String?,
        programStageSection: String?,
        eventStatus: EventStatus?,
    ): FieldUiModel {
        val uid = dataElement.uid()
        val displayName = dataElement.displayName()
        val valueTypeName = dataElement.valueType()!!.name
        val mandatory: Boolean = stage.compulsory()!!
        val optionSet = dataElement.optionSetUid()
        var dataValue = value
        val option =
            if (optionSet != null) {
                d2
                    .optionModule()
                    .options()
                    .byOptionSetUid()
                    .eq(optionSet)
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                    .byCode()
                    .eq(dataValue)
                    .blockingGet()
            } else {
                emptyList()
            }
        val allowFutureDates: Boolean = stage.allowFutureDate()!!
        val formName = dataElement.displayFormName()
        val description = dataElement.displayDescription()

        var optionSetConfig: OptionSetConfiguration? = null
        if (optionSet != null) {
            val dataValueOptions =
                d2
                    .optionModule()
                    .options()
                    .byOptionSetUid()
                    .eq(optionSet)
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                    .byCode()
                    .eq(dataValue)
                    .blockingGet()
            if (dataValueOptions.isNotEmpty()) {
                dataValue = option[0].displayName()
            }
            optionSetConfig =
                OptionSetConfiguration(
                    searchEmitter = null,
                    onSearch = { _ -> null },
                    optionFlow =
                        optionDataFlow(
                            d2
                                .optionModule()
                                .options()
                                .byOptionSetUid()
                                .eq(optionSet)
                                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                                .getPagingData(10),
                        ) { option1: Option? -> metadataIconProvider.invoke(option1!!.style()) },
                )
        }

        val fieldRendering = if (stage.renderType() == null) null else stage.renderType()!!.mobile()

        val objectStyle =
            d2
                .dataElementModule()
                .dataElements()
                .uid(uid)
                .blockingGet()!!
                .style()

        return fieldFactory.create(
            uid,
            (formName ?: displayName)!!,
            ValueType.valueOf(valueTypeName),
            mandatory,
            optionSet,
            dataValue,
            programStageSection,
            allowFutureDates,
            eventStatus == EventStatus.ACTIVE,
            null,
            description,
            fieldRendering,
            objectStyle,
            dataElement.fieldMask(),
            optionSetConfig,
            null,
            null,
            null,
            null,
            null,
            null,
        )
    }

    private fun searchValueDataElement(
        dataElement: String?,
        dataValues: List<TrackedEntityDataValue>,
    ): String? =
        dataValues
            .firstOrNull { dataValue ->
                dataValue.dataElement() == dataElement
            }?.value() ?: ""

    override fun getEditableStatus(): Flowable<EventEditableStatus?>? =
        d2
            .eventModule()
            .eventService()
            .getEditableStatus(eventUid!!)
            .toFlowable()
}
