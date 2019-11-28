package org.dhis2.data.forms

import android.text.TextUtils.isEmpty
import io.reactivex.Single
import org.dhis2.Bindings.toRuleDataValue
import org.dhis2.Bindings.toRuleList
import org.dhis2.Bindings.toRuleVariable
import org.dhis2.Bindings.toRuleVariableList
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.ProgramRule
import org.hisp.dhis.rules.models.*
import timber.log.Timber
import java.util.*

class RulesRepository(private val d2: D2) {

    // ORG UNIT GROUPS
    // USER ROLES
    fun supplementaryData(orgUnitUid: String): Single<Map<String, List<String>>> {
        return Single.fromCallable {
            val supData = HashMap<String, MutableList<String>>()

            d2.organisationUnitModule().organisationUnits().withOrganisationUnitGroups().uid(orgUnitUid).blockingGet()
                    .let {orgUnit->
                        orgUnit.organisationUnitGroups()?.map {
                            supData[it.code()!!] = arrayListOf(orgUnit.uid())
                            supData[it.uid()!!] = arrayListOf(orgUnit.uid())
                        }
                    }

            val userRoleUids =
                    UidsHelper.getUidsList(d2.userModule().userRoles().blockingGet())
            supData["USER"] = userRoleUids

            supData
        }
    }

    fun rulesNew(programUid: String): Single<List<Rule>> {
        return queryRules(programUid)
                .map { it.toRuleList() }
    }

    fun ruleVariables(programUid: String): Single<List<RuleVariable>> {
        return d2.programModule().programRuleVariables().byProgramUid().eq(programUid).get()
                .map {
                    it.toRuleVariableList(
                            d2.trackedEntityModule().trackedEntityAttributes(),
                            d2.dataElementModule().dataElements()
                    )
                }
    }

    fun ruleVariablesProgramStages(programUid: String): Single<List<RuleVariable>> {
        return d2.programModule().programRuleVariables().byProgramUid().eq(programUid).get()
                .toFlowable().flatMapIterable { list -> list }
                .map {
                    it.toRuleVariable(
                            d2.trackedEntityModule().trackedEntityAttributes(),
                            d2.dataElementModule().dataElements()
                    )
                }
                .toList()
    }

    fun queryConstants(): Single<Map<String, String>> {
        return d2.constantModule().constants().get()
                .map { constants ->
                    val constantsMap = HashMap<String, String>()
                    for (constant in constants) {
                        constantsMap[constant.uid()] =
                                Objects.requireNonNull<Double>(constant.value()).toString()
                    }
                    constantsMap
                }
    }

    private fun queryRules(
            programUid: String
    ): Single<List<ProgramRule>> {
        return d2.programModule().programRules()
                .byProgramUid().eq(programUid)
                .withProgramRuleActions()
                .get()
    }

    fun otherEvents(eventUidToEvaluate: String): Single<List<RuleEvent>> {
        return d2.eventModule().events().uid(eventUidToEvaluate).get()
                .flatMap { eventToEvaluate ->
                    getOtherEventList(eventToEvaluate).toFlowable()
                            .flatMapIterable { eventList -> eventList }
                            .map { event ->
                                RuleEvent.builder()
                                        .event(event.uid())
                                        .programStage(event.programStage())
                                        .programStageName(
                                                d2.programModule().programStages().uid(event.programStage())
                                                        .blockingGet()!!.name()
                                        )
                                        .status(
                                                if (event.status() == EventStatus.VISITED) {
                                                    RuleEvent.Status.ACTIVE
                                                } else {
                                                    RuleEvent.Status.valueOf(event.status()!!.name)
                                                }
                                        )
                                        .eventDate(event.eventDate())
                                        .dueDate(
                                                if (event.dueDate() != null) {
                                                    event.dueDate()
                                                } else {
                                                    event.eventDate()
                                                }
                                        )
                                        .organisationUnit(event.organisationUnit())
                                        .organisationUnitCode(
                                                d2.organisationUnitModule().organisationUnits().uid(
                                                        event.organisationUnit()
                                                ).blockingGet()!!.code()
                                        )
                                        .dataValues(
                                                event.trackedEntityDataValues()?.toRuleDataValue(
                                                        event,
                                                        d2.dataElementModule().dataElements(),
                                                        d2.programModule().programRuleVariables(),
                                                        d2.optionModule().options()
                                                )
                                        )
                                        .build()
                            }
                            .toList()
                }
    }

    private fun getOtherEventList(eventToEvaluate: Event): Single<List<Event>> {
        return if (!isEmpty(eventToEvaluate.enrollment())) {
            d2.eventModule().events().byProgramUid().eq(eventToEvaluate.program())
                    .byEnrollmentUid().eq(eventToEvaluate.enrollment())
                    .byUid().notIn(eventToEvaluate.uid())
                    .byStatus().notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE)
                    .withTrackedEntityDataValues()
                    .orderByEventDate(RepositoryScope.OrderByDirection.DESC)
                    .get()
        } else {
            d2.eventModule().events()
                    .byProgramUid().eq(eventToEvaluate.program())
                    .byProgramStageUid().eq(eventToEvaluate.programStage())
                    .byOrganisationUnitUid().eq(eventToEvaluate.organisationUnit())
                    .byStatus().notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE)
                    .withTrackedEntityDataValues()
                    .orderByEventDate(RepositoryScope.OrderByDirection.DESC)
                    .get().map { list ->
                        var currentEventIndex = -1
                        var index = 0
                        do {
                            if (list[index].uid() == eventToEvaluate.uid()) {
                                currentEventIndex = index
                            } else {
                                index++
                            }
                        } while (currentEventIndex == -1)

                        var newEvents = list.subList(0, currentEventIndex)
                        var previousEvents = list.subList(currentEventIndex + 1, list.size)

                        if (newEvents.size > 10) {
                            newEvents = newEvents.subList(0, 10)
                        }
                        if (previousEvents.size > 10) {
                            previousEvents = previousEvents.subList(0, 10)
                        }

                        val finalList = ArrayList<Event>()
                        finalList.addAll(newEvents)
                        finalList.addAll(previousEvents)

                        finalList
                    }
        }
    }

    fun enrollmentEvents(enrollmentUid: String): Single<List<RuleEvent>> {
        return d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid)
                .byStatus().notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE)
                .withTrackedEntityDataValues()
                .get()
                .toFlowable().flatMapIterable { events -> events }
                .map { event ->
                    RuleEvent.builder()
                            .event(event.uid())
                            .programStage(event.programStage())
                            .programStageName(
                                    d2.programModule().programStages().uid(event.programStage())
                                            .blockingGet()!!.name()
                            )
                            .status(
                                    if (event.status() == EventStatus.VISITED) {
                                        RuleEvent.Status.ACTIVE
                                    } else {
                                        RuleEvent.Status.valueOf(event.status()!!.name)
                                    }
                            )
                            .eventDate(event.eventDate())
                            .dueDate(if (event.dueDate() != null) event.dueDate() else event.eventDate())
                            .organisationUnit(event.organisationUnit())
                            .organisationUnitCode(
                                    d2.organisationUnitModule()
                                            .organisationUnits().uid(event.organisationUnit())
                                            .blockingGet()!!.code()
                            )
                            .dataValues(
                                    event.trackedEntityDataValues()?.toRuleDataValue(
                                            event,
                                            d2.dataElementModule().dataElements(),
                                            d2.programModule().programRuleVariables(),
                                            d2.optionModule().options()
                                    )
                            )
                            .build()
                }.toList()
    }

    fun enrollment(eventUid: String): Single<RuleEnrollment> {
        return d2.eventModule().events().uid(eventUid).get()
                .flatMap { event ->
                    val ouCode = d2.organisationUnitModule().organisationUnits()
                            .uid(event.organisationUnit())
                            .blockingGet()!!.code()
                    val programName =
                            d2.programModule().programs().uid(event.program()).blockingGet()!!.name()
                    if (event.enrollment() == null) {
                        Single.just(
                                RuleEnrollment.create(
                                        "",
                                        Calendar.getInstance().time,
                                        Calendar.getInstance().time,
                                        RuleEnrollment.Status.CANCELLED,
                                        event.organisationUnit()!!,
                                        ouCode,
                                        ArrayList(),
                                        programName
                                )
                        )
                    } else {
                        d2.enrollmentModule().enrollments()
                                .uid(event.enrollment()).get()
                                .map { enrollment ->
                                    RuleEnrollment.create(
                                            enrollment.uid(),
                                            enrollment.incidentDate() ?: Date(),
                                            enrollment.enrollmentDate()!!,
                                            RuleEnrollment.Status.valueOf(enrollment.status()!!.name),
                                            event.organisationUnit()!!,
                                            ouCode,
                                            getAttributesValues(enrollment),
                                            programName
                                    )
                                }
                    }
                }
    }

    private fun getAttributesValues(enrollment: Enrollment): List<RuleAttributeValue> {
        val attributeValues = d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq(enrollment.trackedEntityInstance()).blockingGet()
        val ruleAttributeValues = ArrayList<RuleAttributeValue>()
        for (attributeValue in attributeValues) {
            val attribute = d2.trackedEntityModule().trackedEntityAttributes()
                    .uid(attributeValue.trackedEntityAttribute()).blockingGet()
            var value = attributeValue.value()
            if (attribute!!.optionSet() != null && !isEmpty(attribute.optionSet()!!.uid())) {
                val useOptionCode = d2.programModule().programRuleVariables().byProgramUid()
                        .eq(enrollment.program()).byTrackedEntityAttributeUid().eq(attribute.uid())
                        .byUseCodeForOptionSet().isTrue.blockingIsEmpty()
                if (!useOptionCode) {
                    value = d2.optionModule().options()
                            .byOptionSetUid().eq(attribute.optionSet()!!.uid())
                            .byCode().eq(value)
                            .one().blockingGet()!!.name()
                }
            }
            RuleAttributeValue.create(attributeValue.trackedEntityAttribute()!!, value!!)
        }
        return ruleAttributeValues
    }
}
