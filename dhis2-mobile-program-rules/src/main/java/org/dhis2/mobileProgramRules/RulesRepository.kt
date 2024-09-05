package org.dhis2.mobileProgramRules

import android.os.Build
import android.text.TextUtils.isEmpty
import io.reactivex.Single
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.bindings.event
import org.dhis2.commons.bindings.organisationUnit
import org.dhis2.commons.bindings.program
import org.dhis2.commons.bindings.programStage
import org.dhis2.commons.rules.toRuleEngineInstant
import org.dhis2.commons.rules.toRuleEngineLocalDate
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.ProgramRule
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleAttributeValue
import org.hisp.dhis.rules.models.RuleDataValue
import org.hisp.dhis.rules.models.RuleEnrollment
import org.hisp.dhis.rules.models.RuleEnrollmentStatus
import org.hisp.dhis.rules.models.RuleEvent
import org.hisp.dhis.rules.models.RuleEventStatus
import org.hisp.dhis.rules.models.RuleVariable
import java.util.Calendar
import java.util.Date

class RulesRepository(private val d2: D2) {

    // ORG UNIT GROUPS
    // USER ROLES
    suspend fun supplementaryData(orgUnitUid: String): Map<String, List<String>> {
        val supData = HashMap<String, List<String>>()

        d2.organisationUnitModule().organisationUnits()
            .withOrganisationUnitGroups().uid(orgUnitUid).blockingGet()
            .let { orgUnit ->
                orgUnit?.organisationUnitGroups()?.map {
                    it.code()?.let { code -> supData[code] = arrayListOf(orgUnit.uid()) }
                    supData[it.uid()] = arrayListOf(orgUnit.uid())
                }
            }

        val userRoleUids =
            UidsHelper.getUidsList(d2.userModule().userRoles().blockingGet())
        supData["USER"] = userRoleUids
        supData["android_version"] = listOf(Build.VERSION.SDK_INT.toString())

        return supData
    }

    suspend fun rules(programUid: String, eventUid: String? = null): List<Rule> {
        val programStage =
            eventUid?.let { d2.eventModule().events().uid(eventUid).blockingGet()?.programStage() }

        return queryRules(programUid).toRuleList().filter {
            it.programStage == null || it.programStage == programStage
        }
    }

    suspend fun ruleVariables(programUid: String): List<RuleVariable> {
        return d2.programModule().programRuleVariables()
            .byProgramUid().eq(programUid)
            .blockingGet()
            .toRuleVariableList(
                d2.trackedEntityModule().trackedEntityAttributes(),
                d2.dataElementModule().dataElements(),
                d2.optionModule().options(),
            )
    }

    fun ruleVariablesProgramStages(programUid: String): Single<List<RuleVariable>> {
        return d2.programModule().programRuleVariables().byProgramUid().eq(programUid).get()
            .toFlowable().flatMapIterable { list -> list }
            .map {
                it.toRuleVariable(
                    d2.trackedEntityModule().trackedEntityAttributes(),
                    d2.dataElementModule().dataElements(),
                    d2.optionModule().options(),
                )
            }
            .toList()
    }

    suspend fun constants(): Map<String, String> {
        return d2.constantModule().constants().blockingGet()
            .associate { constant ->
                constant.uid() to constant.value()!!.toString()
            }
    }

    private fun queryRules(programUid: String): List<ProgramRule> {
        return d2.programModule().programRules()
            .byProgramUid().eq(programUid)
            .withProgramRuleActions()
            .blockingGet()
    }

    suspend fun otherEvents(eventUidToEvaluate: String): List<RuleEvent> {
        return d2.eventModule().events().uid(eventUidToEvaluate).blockingGet()
            ?.let { eventToEvaluate ->
                getOtherEventList(eventToEvaluate)
                    .map { event ->
                        RuleEvent(
                            event = event.uid(),
                            programStage = event.programStage()!!,
                            programStageName = d2.programModule().programStages()
                                .uid(event.programStage())
                                .blockingGet()!!.name()!!,
                            status = if (event.status() == EventStatus.VISITED) {
                                RuleEventStatus.ACTIVE
                            } else {
                                RuleEventStatus.valueOf(event.status()!!.name)
                            },
                            eventDate = Instant.fromEpochMilliseconds(event.eventDate()!!.time),
                            dueDate = event.dueDate()?.let {
                                Instant.fromEpochMilliseconds(it.time)
                                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                            },
                            completedDate = event.completedDate()?.let {
                                Instant.fromEpochMilliseconds(it.time)
                                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                            },
                            organisationUnit = event.organisationUnit()!!,
                            organisationUnitCode = d2.organisationUnitModule().organisationUnits()
                                .uid(
                                    event.organisationUnit(),
                                ).blockingGet()?.code(),
                            dataValues = event.trackedEntityDataValues()?.toRuleDataValue(
                                event,
                                d2.dataElementModule().dataElements(),
                                d2.programModule().programRuleVariables(),
                                d2.optionModule().options(),
                            ) ?: emptyList(),
                        )
                    }
                    .toList()
            } ?: emptyList()
    }

    private fun getOtherEventList(eventToEvaluate: Event): List<Event> {
        return if (!isEmpty(eventToEvaluate.enrollment())) {
            d2.eventModule().events().byProgramUid().eq(eventToEvaluate.program())
                .byEnrollmentUid().eq(eventToEvaluate.enrollment())
                .byUid().notIn(eventToEvaluate.uid())
                .byStatus().notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE)
                .byEventDate().beforeOrEqual(Date())
                .withTrackedEntityDataValues()
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC)
                .blockingGet()
        } else {
            d2.eventModule().events()
                .byProgramUid().eq(eventToEvaluate.program())
                .byProgramStageUid().eq(eventToEvaluate.programStage())
                .byOrganisationUnitUid().eq(eventToEvaluate.organisationUnit())
                .byStatus().notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE)
                .byEventDate().beforeOrEqual(Date())
                .withTrackedEntityDataValues()
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC)
                .blockingGet().let { list ->
                    val currentEventIndex = list.indexOfFirst { it.uid() == eventToEvaluate.uid() }

                    var newEvents = if (currentEventIndex != -1) {
                        list.subList(0, currentEventIndex)
                    } else {
                        emptyList()
                    }
                    var previousEvents = if (currentEventIndex != -1) {
                        list.subList(currentEventIndex + 1, list.size)
                    } else {
                        list
                    }

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

    suspend fun enrollmentEvents(enrollmentUid: String): List<RuleEvent> {
        return d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid)
            .byStatus().notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE)
            .byEventDate().beforeOrEqual(Date())
            .withTrackedEntityDataValues()
            .blockingGet()
            .map { event ->
                RuleEvent(
                    event = event.uid(),
                    programStage = event.programStage()!!,
                    programStageName =
                    d2.programModule().programStages().uid(event.programStage())
                        .blockingGet()!!.name()!!,
                    status =
                    if (event.status() == EventStatus.VISITED) {
                        RuleEventStatus.ACTIVE
                    } else {
                        RuleEventStatus.valueOf(event.status()!!.name)
                    },
                    eventDate = event.eventDate()!!.toRuleEngineInstant(),
                    dueDate = event.dueDate()?.toRuleEngineLocalDate(),
                    completedDate = event.completedDate()?.toRuleEngineLocalDate(),
                    organisationUnit = event.organisationUnit()!!,
                    organisationUnitCode = d2.organisationUnitModule()
                        .organisationUnits().uid(event.organisationUnit())
                        .blockingGet()?.code(),
                    dataValues =
                    event.trackedEntityDataValues()?.toRuleDataValue(
                        event,
                        d2.dataElementModule().dataElements(),
                        d2.programModule().programRuleVariables(),
                        d2.optionModule().options(),
                    ) ?: emptyList(),
                )
            }.toList()
    }

    suspend fun enrollment(eventUid: String): RuleEnrollment {
        val event = d2.eventModule().events().uid(eventUid).blockingGet()!!

        val ouCode = d2.organisationUnitModule().organisationUnits()
            .uid(event.organisationUnit())
            .blockingGet()?.code() ?: ""
        val programName =
            d2.programModule().programs().uid(event.program()).blockingGet()!!.name()
        return if (event.enrollment() == null) {
            RuleEnrollment(
                "",
                programName!!,
                Calendar.getInstance().time.toRuleEngineLocalDate(),
                Calendar.getInstance().time.toRuleEngineLocalDate(),
                RuleEnrollmentStatus.CANCELLED,
                event.organisationUnit()!!,
                ouCode,
                ArrayList(),
            )
        } else {
            val enrollment = d2.enrollmentModule().enrollments()
                .uid(event.enrollment()).blockingGet()!!
            RuleEnrollment(
                enrollment.uid(),
                programName!!,
                (enrollment.incidentDate() ?: Date()).toRuleEngineLocalDate(),
                enrollment.enrollmentDate()!!.toRuleEngineLocalDate(),
                RuleEnrollmentStatus.valueOf(enrollment.status()!!.name),
                event.organisationUnit()!!,
                ouCode,
                getAttributesValues(enrollment),
            )
        }
    }

    private fun getAttributesValues(enrollment: Enrollment): List<RuleAttributeValue> {
        val attributeValues = d2.trackedEntityModule().trackedEntityAttributeValues()
            .byTrackedEntityInstance().eq(enrollment.trackedEntityInstance()).blockingGet()
        return attributeValues.toRuleAttributeValue(d2, enrollment.program()!!)
    }

    fun enrollmentProgram(enrollmentUid: String): Pair<String, String> {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet().let {
            Pair(it?.program()!!, it.organisationUnit()!!)
        }
    }

    fun eventProgram(eventUid: String): Pair<String, String> {
        return d2.eventModule().events().uid(eventUid).blockingGet().let {
            Pair(it?.program()!!, it.organisationUnit()!!)
        }
    }

    fun queryDataValues(eventUid: String): List<RuleDataValue> {
        return d2.eventModule().events().uid(eventUid).blockingGet()
            ?.let { event ->
                d2.trackedEntityModule().trackedEntityDataValues().byEvent().eq(eventUid)
                    .byValue().isNotNull.blockingGet()
                    .toRuleDataValue(
                        event,
                        d2.dataElementModule().dataElements(),
                        d2.programModule().programRuleVariables(),
                        d2.optionModule().options(),
                    )
            } ?: emptyList()
    }

    fun queryAttributeValues(enrollmentUid: String): List<RuleAttributeValue> {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()
            ?.let { enrollment ->
                d2.trackedEntityModule().trackedEntityAttributeValues()
                    .byTrackedEntityInstance().eq(enrollment.trackedEntityInstance()).blockingGet()
                    .toRuleAttributeValue(
                        d2,
                        enrollment.program()!!,
                    )
            } ?: emptyList()
    }

    fun getRuleEnrollment(enrollmentUid: String): RuleEnrollment {
        val enrollment = d2.enrollment(enrollmentUid) ?: throw NullPointerException()
        return RuleEnrollment(
            enrollment = enrollment.uid(),
            programName = d2.program(enrollment.program()!!)?.name()!!,
            incidentDate = (enrollment.incidentDate() ?: Date()).toRuleEngineLocalDate(),
            enrollmentDate = (enrollment.enrollmentDate() ?: Date()).toRuleEngineLocalDate(),
            status = RuleEnrollmentStatus.valueOf(enrollment.status()!!.name),
            organisationUnit = enrollment.organisationUnit()!!,
            organisationUnitCode = d2.organisationUnit(enrollment.organisationUnit()!!)
                ?.code() ?: "",
            attributeValues = emptyList(),
        )
    }

    fun getRuleEvent(eventUid: String): RuleEvent {
        val event = d2.event(eventUid) ?: throw NullPointerException()
        return RuleEvent(
            event = event.uid(),
            programStage = event.programStage()!!,
            programStageName = d2.programStage(event.programStage()!!)?.name()!!,
            status = RuleEventStatus.valueOf(event.status()!!.name),
            eventDate = event.eventDate()!!.toRuleEngineInstant(),
            dueDate = event.dueDate()?.toRuleEngineLocalDate(),
            completedDate = event.completedDate()?.toRuleEngineLocalDate(),
            organisationUnit = event.organisationUnit()!!,
            organisationUnitCode = d2.organisationUnit(event.organisationUnit()!!)?.code(),
            dataValues = emptyList(),
        )
    }
}
