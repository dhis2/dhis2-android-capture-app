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
import java.util.Collections
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
    override fun event(eventId: String?): Observable<Event?> {
        return d2.eventModule().events().uid(eventId).get().toObservable()
    }

    fun orgUnits(
        programId: String,
        parentUid: String,
    ): Observable<List<OrganisationUnit>> {
        return d2.organisationUnitModule().organisationUnits()
            .byProgramUids(listOf(programId))
            .byParentUid().eq(parentUid)
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
            .get()
            .toObservable()
    }

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
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return Observable.fromCallable(
            Callable {
                d2.eventModule().events().blockingAdd(
                    EventCreateProjection.builder()
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
            if (d2.programModule().programStages()
                    .uid(eventRepository.blockingGet()!!.programStage())
                    .blockingGet()!!.featureType() != null
            ) {
                when (
                    d2.programModule().programStages().uid(
                        eventRepository.blockingGet()!!.programStage(),
                    )
                        .blockingGet()!!.featureType()
                ) {
                    FeatureType.POINT, FeatureType.POLYGON, FeatureType.MULTI_POLYGON -> eventRepository.setGeometry(
                        geometry,
                    )

                    else -> {}
                }
            }
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
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return Observable.fromCallable(
            Callable {
                d2.eventModule().events().blockingAdd(
                    EventCreateProjection.builder()
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
            if (d2.programModule().programStages()
                    .uid(eventRepository.blockingGet()!!.programStage())
                    .blockingGet()!!.featureType() != null
            ) {
                when (
                    d2.programModule().programStages().uid(
                        eventRepository.blockingGet()!!.programStage(),
                    )
                        .blockingGet()!!.featureType()
                ) {
                    FeatureType.POINT, FeatureType.POLYGON, FeatureType.MULTI_POLYGON ->
                        eventRepository.setGeometry(geometry)

                    else -> {}
                }
            }
            uid
        }
    }

    override fun programStage(programUid: String): Observable<ProgramStage?> {
        return d2.programModule().programStages().byProgramUid().eq(programUid).one().get()
            .toObservable()
    }

    override fun programStageWithId(programStageUid: String?): Observable<ProgramStage?> {
        return d2.programModule().programStages().uid(programStageUid).get().toObservable()
    }

    override fun programStageForEvent(eventId: String?): Flowable<ProgramStage?> {
        return d2.eventModule().events()
            .uid(eventId)
            .get().toFlowable()
            .map { event ->
                d2.programModule().programStages()
                    .byUid().eq(event.programStage())
                    .one()
                    .blockingGet()
            }
    }

    override fun accessDataWrite(programUid: String): Observable<Boolean> {
        return if (eventUid != null) {
            d2.eventModule().eventService().isEditable(eventUid).toObservable()
        } else {
            d2.programModule().programStages().uid(stageUid).get().toObservable()
                .map { programStage ->
                    programStage.access().data().write()
                }
        }
    }

    override fun deleteEvent(eventId: String?, trackedEntityInstance: String?) {
        try {
            d2.eventModule().events().uid(eventId).blockingDelete()
        } catch (d2Error: D2Error) {
            Timber.e(d2Error)
        }
    }

    override fun isEnrollmentOpen(): Boolean {
        val event = d2.eventModule().events().uid(eventUid).blockingGet()
        return event == null || event.enrollment() == null || d2.enrollmentModule().enrollments()
            .uid(event.enrollment()).blockingGet()!!
            .status() == EnrollmentStatus.ACTIVE
    }

    override fun getProgramWithId(programUid: String?): Observable<Program?> {
        return d2.programModule().programs()
            .withTrackedEntityType().uid(programUid).get().toObservable()
    }

    override fun showCompletionPercentage(): Boolean {
        if (d2.settingModule().appearanceSettings().blockingExists()) {
            val programUid = d2.eventModule().events().uid(eventUid).blockingGet()!!.program()
            val programConfigurationSetting = d2.settingModule()
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

    override fun eventSections(): Flowable<MutableList<FormSectionViewModel?>?>? {
        return d2.eventModule().events().uid(eventUid).get()
            .map { eventSingle: Event? ->
                val formSection: MutableList<FormSectionViewModel?> = mutableListOf()
                if (eventSingle!!.deleted() == null || !eventSingle.deleted()!!) {
                    val stage = d2.programModule().programStages().uid(eventSingle.programStage())
                        .blockingGet()
                    val stageSections =
                        d2.programModule().programStageSections().byProgramStageUid().eq(
                            stage!!.uid(),
                        ).blockingGet()
                    if (stageSections.isNotEmpty()) {
                        Collections.sort(
                            stageSections,
                            Comparator { one: ProgramStageSection?, two: ProgramStageSection? ->
                                one!!.sortOrder()!!
                                    .compareTo(two!!.sortOrder()!!)
                            },
                        )

                        for (section in stageSections) formSection.add(
                            FormSectionViewModel(
                                eventUid!!,
                                section.uid(),
                                section.displayName(),
                                if (section.renderType()!!.mobile() != null) {
                                    section.renderType()!!
                                        .mobile()!!
                                        .type()!!.name
                                } else {
                                    null
                                },
                            ),
                        )
                    } else {
                        formSection.add(
                            FormSectionViewModel(
                                eventUid!!,
                                "",
                                "",
                                SectionRenderingType.LISTING.name,
                            ),
                        )
                    }
                }
                formSection
            }.toFlowable()
    }

    override fun list(): Flowable<List<FieldUiModel>> {
        return d2.eventModule().events().withTrackedEntityDataValues().uid(eventUid).get()
            .map { event ->
                buildList {
                    val stage =
                        d2.programModule().programStages().uid(event.programStage()).blockingGet()
                    val sections = d2.programModule().programStageSections().withDataElements()
                        .byProgramStageUid().eq(stage!!.uid())
                        .blockingGet()
                    val stageDataElements = d2.programModule().programStageDataElements()
                        .byProgramStage().eq(stage.uid())
                        .blockingGet()

                    if (!sections.isEmpty()) {
                        for (stageSection in sections) {
                            for (programStageDataElement in stageDataElements) {
                                if (getUidsList<DataElement>(stageSection.dataElements()!!).contains(
                                        programStageDataElement.dataElement()!!.uid(),
                                    )
                                ) {
                                    val dataElement = d2.dataElementModule().dataElements().uid(
                                        programStageDataElement.dataElement()!!.uid(),
                                    ).blockingGet()
                                    add(
                                        transform(
                                            programStageDataElement,
                                            dataElement!!,
                                            searchValueDataElement(
                                                programStageDataElement.dataElement()!!
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
                    } else {
                        for (programStageDataElement in stageDataElements) {
                            val dataElement = d2.dataElementModule().dataElements().uid(
                                programStageDataElement.dataElement()!!.uid(),
                            ).blockingGet()
                            add(
                                transform(
                                    programStageDataElement,
                                    dataElement!!,
                                    searchValueDataElement(
                                        programStageDataElement.dataElement()!!
                                            .uid(),
                                        event.trackedEntityDataValues()!!,
                                    ),
                                    null,
                                    event.status(),
                                ),
                            )
                        }
                    }
                }
            }.toFlowable()
    }

    override fun calculate(): Flowable<Result<List<RuleEffect>>> {
        return if (ruleEngineHelper != null) {
            Flowable.just(ruleEngineHelper.evaluate())
                .map { data -> Result.success(data) }
        } else {
            Flowable.just(Result.success(emptyList()))
        }
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
                d2.optionModule().options().byOptionSetUid().eq(optionSet)
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).byCode().eq(dataValue)
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
                d2.optionModule().options().byOptionSetUid().eq(optionSet)
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).byCode().eq(dataValue)
                    .blockingGet()
            if (!dataValueOptions.isEmpty()) {
                dataValue = option[0].displayName()
            }
            optionSetConfig = OptionSetConfiguration(
                null,
                { query: String? -> null },
                optionDataFlow(
                    d2.optionModule().options().byOptionSetUid().eq(optionSet)
                        .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).getPagingData(10),
                ) { option1: Option? -> metadataIconProvider.invoke(option1!!.style()) },
            )
        }

        val fieldRendering = if (stage.renderType() == null) null else stage.renderType()!!.mobile()

        val objectStyle = d2.dataElementModule().dataElements().uid(uid).blockingGet()!!.style()

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
        dataValues: MutableList<TrackedEntityDataValue>,
    ): String? {
        for (dataValue in dataValues) if (dataValue.dataElement() == dataElement) {
            return dataValue.value()
        }

        return ""
    }

    override fun getEditableStatus(): Flowable<EventEditableStatus?>? {
        return d2.eventModule().eventService().getEditableStatus(eventUid!!).toFlowable()
    }
}
