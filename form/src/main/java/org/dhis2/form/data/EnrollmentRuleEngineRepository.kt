package org.dhis2.form.data

import org.dhis2.commons.bindings.blockingGetCheck
import org.dhis2.form.bindings.toRuleAttributeValue
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.rules.RuleEngine
import org.hisp.dhis.rules.RuleEngineContext
import org.hisp.dhis.rules.models.RuleAttributeValue
import org.hisp.dhis.rules.models.RuleEffect
import org.hisp.dhis.rules.models.RuleEnrollment
import org.hisp.dhis.rules.models.TriggerEnvironment

class EnrollmentRuleEngineRepository(
    private val d2: D2,
    private val enrollmentUid: String,
) : RuleEngineRepository {

    private val ruleRepository = RulesRepository(d2)
    private lateinit var ruleEngine: RuleEngine
    private lateinit var ruleEnrollmentBuilder: RuleEnrollment.Builder

    private var enrollment: Enrollment =
        d2.enrollmentModule().enrollments()
            .uid(enrollmentUid)
            .blockingGet() ?: throw NullPointerException()

    private val program: Program by lazy {
        d2.programModule().programs()
            .uid(enrollment.program())
            .blockingGet() ?: throw NullPointerException()
    }

    init {
        configureRuleEngine(enrollment)
    }

    private fun configureRuleEngine(enrollment: Enrollment) {
        val rules = ruleRepository.rulesNew(program.uid()).blockingGet()
        val variables = ruleRepository.ruleVariables(program.uid()).blockingGet()
        val supplData = ruleRepository.supplementaryData(
            enrollment.organisationUnit()!!,
        ).blockingGet()
        val constants = ruleRepository.queryConstants().blockingGet()
        val events = ruleRepository.enrollmentEvents(enrollmentUid).blockingGet()

        ruleEngine = RuleEngineContext.builder()
            .rules(rules)
            .ruleVariables(variables)
            .supplementaryData(supplData)
            .constantsValue(constants)
            .build().toEngineBuilder().apply {
                triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT)
                events(events)
            }.build()

        ruleEnrollmentBuilder = RuleEnrollment.builder()
            .enrollment(enrollment.uid())
            .incidentDate(
                enrollment.incidentDate() ?: enrollment.enrollmentDate(),
            )
            .enrollmentDate(enrollment.enrollmentDate())
            .status(
                RuleEnrollment.Status.valueOf(enrollment.status()!!.name),
            )
            .organisationUnit(enrollment.organisationUnit())
            .organisationUnitCode(
                d2.organisationUnitModule().organisationUnits().uid(
                    enrollment.organisationUnit(),
                ).blockingGet()?.code(),
            )
            .programName(program.displayName())
    }

    override fun calculate(): List<RuleEffect> {
        val newEnrollment: Enrollment =
            d2.enrollmentModule().enrollments()
                .uid(enrollmentUid)
                .blockingGet() ?: throw NullPointerException()
        if (newEnrollment != enrollment) {
            enrollment = newEnrollment
            configureRuleEngine(enrollment)
        }
        val attributes = queryAttributes()
        return try {
            ruleEngine.evaluate(ruleEnrollmentBuilder.attributeValues(attributes).build()).call()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun queryAttributes(): List<RuleAttributeValue> {
        return d2.programModule().programTrackedEntityAttributes()
            .byProgram().eq(program.uid())
            .blockingGet()
            .filter {
                d2.trackedEntityModule().trackedEntityAttributeValues()
                    .value(
                        it.trackedEntityAttribute()!!.uid(),
                        enrollment.trackedEntityInstance()!!,
                    )
                    .blockingExists()
            }.mapNotNull {
                d2.trackedEntityModule().trackedEntityAttributeValues()
                    .value(
                        it.trackedEntityAttribute()!!.uid(),
                        enrollment.trackedEntityInstance()!!,
                    )
                    .blockingGetCheck(d2, it.trackedEntityAttribute()!!.uid())
            }.toRuleAttributeValue(d2, program.uid())
    }
}
