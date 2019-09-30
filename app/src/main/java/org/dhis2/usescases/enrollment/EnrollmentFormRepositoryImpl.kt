package org.dhis2.usescases.enrollment

import android.text.TextUtils.isEmpty
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Function5
import io.reactivex.schedulers.Schedulers
import org.dhis2.data.forms.RulesRepository
import org.dhis2.utils.Constants
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.RuleEngine
import org.hisp.dhis.rules.RuleEngineContext
import org.hisp.dhis.rules.RuleExpressionEvaluator
import org.hisp.dhis.rules.models.*
import java.util.*

class EnrollmentFormRepositoryImpl(
        val d2: D2,
        rulesRepository: RulesRepository,
        expressionEvaluator: RuleExpressionEvaluator,
        private val enrollmentRepository: EnrollmentObjectRepository,
        private val programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program>) : EnrollmentFormRepository {


    private var cachedRuleEngineFlowable: Flowable<RuleEngine>
    private var ruleEnrollmentBuilder: RuleEnrollment.Builder
    private var programUid: String = programRepository.blockingGet().uid()
    private var enrollmentUid: String = enrollmentRepository.blockingGet().uid()!!

    init {
        this.cachedRuleEngineFlowable =
                Single.zip<List<Rule>, List<RuleVariable>, List<RuleEvent>, Map<String, String>, Map<String, List<String>>, RuleEngine>(
                        rulesRepository.rulesNew(programUid).subscribeOn(Schedulers.io()),
                        rulesRepository.ruleVariables(programUid).subscribeOn(Schedulers.io()),
                        rulesRepository.enrollmentEvents(enrollmentRepository.blockingGet().uid()).subscribeOn(Schedulers.io()),
                        rulesRepository.queryConstants().subscribeOn(Schedulers.io()),
                        rulesRepository.getSuplementaryData().subscribeOn(Schedulers.io()),
                        Function5 { rules, variables, events, constants, supplData ->
                            val builder = RuleEngineContext.builder(expressionEvaluator)
                                    .rules(rules)
                                    .ruleVariables(variables)
                                    .calculatedValueMap(HashMap())
                                    .supplementaryData(supplData)
                                    .constantsValue(constants)
                                    .build().toEngineBuilder()
                            builder.triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT)
                            builder.events(events)
                            builder.build()
                        }).toFlowable()
                        .cacheWithInitialCapacity(1)

        this.ruleEnrollmentBuilder = RuleEnrollment.builder()
                .enrollment(enrollmentRepository.blockingGet().uid())
                .incidentDate(if (enrollmentRepository.blockingGet().incidentDate() == null) enrollmentRepository.blockingGet().enrollmentDate() else enrollmentRepository.blockingGet().incidentDate())
                .enrollmentDate(enrollmentRepository.blockingGet().enrollmentDate())
                .status(RuleEnrollment.Status.valueOf(enrollmentRepository.blockingGet().status()!!.name))
                .organisationUnit(enrollmentRepository.blockingGet().organisationUnit())
                .organisationUnitCode(d2.organisationUnitModule().organisationUnits.uid(enrollmentRepository.blockingGet().organisationUnit()).blockingGet().code())
                .programName(programRepository.blockingGet().displayName())
    }

    override fun ruleEngine(): Flowable<RuleEngine> {
        return cachedRuleEngineFlowable
    }

    override fun useFirstStageDuringRegistration(): Single<Pair<String, String>> {
        return programRepository.get()
                .flatMap {
                    if (it.useFirstStageDuringRegistration() == true)
                        getFirstStage()
                    else
                        checkOpenAfterEnrollment()
                }.map {
                    if (!isEmpty(it.second))
                        checkEventToOpen(it)
                    else
                        it
                }
    }

    private fun getFirstStage(): Single<Pair<String, String>> {
        return d2.programModule().programStages.byProgramUid().eq(programUid)
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                .get()
                .map {
                    if (it.isEmpty())
                        Pair(enrollmentUid, "")
                    else
                        Pair(enrollmentUid, it[0].uid())
                }
    }

    private fun checkOpenAfterEnrollment(): Single<Pair<String, String>> {
        return d2.programModule().programStages.byProgramUid().eq(programUid)
                .byOpenAfterEnrollment().isTrue
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                .get()
                .map {
                    if (it.isEmpty())
                        Pair(enrollmentUid, "")
                    else
                        Pair(enrollmentUid, it[0].uid())
                }
    }

    private fun checkEventToOpen(enrollmentStagePair: Pair<String, String>): Pair<String, String> {
        val eventCollectionRepository = d2.eventModule().events
                .byEnrollmentUid().eq(enrollmentUid)
                .byProgramStageUid().eq(enrollmentStagePair.second)

        return if (eventCollectionRepository.one().blockingExists())
            Pair(enrollmentStagePair.first, eventCollectionRepository.one().blockingGet().uid()!!)
        else {
            Pair(enrollmentStagePair.first, generateEvent(DateUtils.getInstance().today,
                    d2.programModule().programStages.uid(enrollmentStagePair.second).blockingGet()))
        }
    }

    override fun autoGenerateEvents(): Single<Boolean> {
        val now = DateUtils.getInstance().today
        return d2.programModule().programStages
                .byProgramUid().eq(programUid)
                .byAutoGenerateEvent().isTrue
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                .get()
                .flatMap {
                    it.forEach { programStage ->
                        generateEvent(now, programStage)
                    }
                    Single.just(true)
                }

    }

    private fun generateEvent(now: Date, programStage: ProgramStage): String {

        val eventToAdd = EventCreateProjection.builder()
                .enrollment(enrollmentRepository.blockingGet().uid())
                .program(programStage.program()!!.uid())
                .programStage(programStage.uid())
                .attributeOptionCombo(null)
                .organisationUnit(enrollmentRepository.blockingGet().organisationUnit())
                .build()

        val eventUid = d2.eventModule().events.blockingAdd(eventToAdd)

        val eventRepository = d2.eventModule().events.uid(eventUid)

        val hideDueDate = programStage.hideDueDate() ?: false
        val incidentDate = enrollmentRepository.blockingGet().incidentDate()
        val enrollmentDate = enrollmentRepository.blockingGet().enrollmentDate()
        val periodType = programStage.periodType()
        val generateByEnrollmentDate = programStage.generatedByEnrollmentDate() ?: false
        val reportDateToUse = programStage.reportDateToUse() ?: ""
        val minDaysFromStart = programStage.minDaysFromStart() ?: 0
        val calendar = DateUtils.getInstance().calendar

        when (reportDateToUse) {
            Constants.ENROLLMENT_DATE -> calendar.time = enrollmentDate
                    ?: Calendar.getInstance().time
            Constants.INCIDENT_DATE -> calendar.time = incidentDate ?: Calendar.getInstance().time
            else -> calendar.time = Calendar.getInstance().time
        }

        if (!generateByEnrollmentDate && incidentDate != null)
            calendar.time = incidentDate
        if (generateByEnrollmentDate)
            calendar.time = enrollmentDate

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DATE, minDaysFromStart)
        var eventDate = calendar.time
        if (periodType != null)
            eventDate = DateUtils.getInstance().getNextPeriod(periodType, eventDate, 0)

        if (eventDate.after(now) && !hideDueDate) {
            eventRepository.setDueDate(eventDate)
            eventRepository.setStatus(EventStatus.SCHEDULE)
        } else {
            eventRepository.setEventDate(eventDate)
        }

        return eventUid
    }

    override fun calculate(): Flowable<org.dhis2.utils.Result<RuleEffect>> {
        return queryAttributes()
                .map { ruleEnrollmentBuilder.attributeValues(it).build() }
                .switchMap { ruleEnrollment ->
                    ruleEngine().flatMap { ruleEngine ->
                        Flowable.fromCallable(ruleEngine.evaluate(ruleEnrollment))
                    }
                            .map {
                                org.dhis2.utils.Result.success(it)
                            }
                            .onErrorReturn {
                                org.dhis2.utils.Result.failure(Exception(it)) as org.dhis2.utils.Result<RuleEffect>
                            }
                }
    }

    private fun queryAttributes(): Flowable<List<RuleAttributeValue>> {
        return programRepository.get()
                .map { program ->
                    program.programTrackedEntityAttributes()!!.filter {
                        d2.trackedEntityModule().trackedEntityAttributeValues
                                .value(it.trackedEntityAttribute()!!.uid(), enrollmentRepository.blockingGet().trackedEntityInstance())
                                .blockingExists()
                    }.map {
                        val value = d2.trackedEntityModule().trackedEntityAttributeValues
                                .value(it.trackedEntityAttribute()!!.uid(), enrollmentRepository.blockingGet().trackedEntityInstance())
                                .blockingGet()
                        val attr = d2.trackedEntityModule().trackedEntityAttributes
                                .uid(it.trackedEntityAttribute()!!.uid())
                                .blockingGet()
                        val variable = d2.programModule().programRuleVariables
                                .byProgramUid().eq(programUid)
                                .byTrackedEntityAttributeUid().eq(attr.uid())
                                .one()
                                .blockingGet()
                        val finalValue =
                                if (variable!=null && variable.useCodeForOptionSet() != true && attr.optionSet() != null) {
                                    d2.optionModule().options.byOptionSetUid().eq(attr.optionSet()!!.uid())
                                            .byCode().eq(value.value()!!).one().blockingGet().name()!!
                                } else
                                    value.value()!!

                        RuleAttributeValue.create(attr.uid(), finalValue)

                    }
                }.toFlowable()
    }


}