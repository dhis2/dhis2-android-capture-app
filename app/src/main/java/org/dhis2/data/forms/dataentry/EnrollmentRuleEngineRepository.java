package org.dhis2.data.forms.dataentry;

import androidx.annotation.NonNull;

import org.dhis2.Bindings.RuleExtensionsKt;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramRule;
import org.hisp.dhis.android.core.program.ProgramRuleAction;
import org.hisp.dhis.android.core.program.ProgramRuleActionType;
import org.hisp.dhis.android.core.program.ProgramRuleVariable;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.models.Rule;
import org.hisp.dhis.rules.models.RuleAttributeValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEnrollment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

public final class EnrollmentRuleEngineRepository
        implements
        RuleEngineRepository {

    @NonNull
    private final FormRepository formRepository;

    @NonNull
    private final String enrollmentUid;

    private RuleEnrollment.Builder ruleEnrollmentBuilder;

    @NonNull
    private final D2 d2;

    private Map<String, ProgramRuleVariable> attrRuleVariableMap;

    private Map<String, List<Rule>> attributeRules = new HashMap<>();

    private String lastUpdatedAttr = null;

    private boolean getIndicators = false;

    private List<ProgramRule> mandatoryRules;

    public EnrollmentRuleEngineRepository(@NonNull FormRepository formRepository,
                                          @NonNull String enrollmentUid, @NotNull D2 d2) {
        this.d2 = d2;
        this.formRepository = formRepository;
        this.enrollmentUid = enrollmentUid;

        initData();

    }

    public void initData() {
        Enrollment enrollment = d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet();
        OrganisationUnit ou = d2.organisationUnitModule().organisationUnits().uid(enrollment.organisationUnit())
                .blockingGet();
        Program program = d2.programModule().programs().uid(enrollment.program())
                .blockingGet();

        attrRuleVariableMap = new HashMap<>();
        List<ProgramRuleVariable> ruleVariables = d2.programModule().programRuleVariables().byProgramUid()
                .eq(enrollment.program()).blockingGet();
        for (ProgramRuleVariable ruleVariable : ruleVariables) {
            if (ruleVariable.trackedEntityAttribute() != null)
                attrRuleVariableMap.put(ruleVariable.trackedEntityAttribute().uid(), ruleVariable);
        }

        ruleEnrollmentBuilder = RuleEnrollment.builder().enrollment(enrollment.uid())
                .incidentDate(enrollment.incidentDate() == null ? enrollment.enrollmentDate() : enrollment.incidentDate())
                .enrollmentDate(enrollment.enrollmentDate())
                .status(RuleEnrollment.Status.valueOf(enrollment.status().name()))
                .organisationUnit(enrollment.organisationUnit()).organisationUnitCode(ou.code())
                .programName(program.displayName());

        loadAttrRules(program.uid());
    }

    private void loadAttrRules(String programUid) {
        List<ProgramRule> rules = d2.programModule().programRules().byProgramUid().eq(programUid)
                .withProgramRuleActions().blockingGet();
        mandatoryRules = new ArrayList<>();
        Iterator<ProgramRule> ruleIterator = rules.iterator();
        while (ruleIterator.hasNext()) {
            ProgramRule rule = ruleIterator.next();
            if (rule.condition() == null || rule.programStage() != null)
                ruleIterator.remove();
            else
                for (ProgramRuleAction action : rule.programRuleActions())
                    if (action.programRuleActionType() == ProgramRuleActionType.HIDEFIELD
                            || action.programRuleActionType() == ProgramRuleActionType.HIDESECTION
                            || action.programRuleActionType() == ProgramRuleActionType.ASSIGN
                            || action.programRuleActionType() == ProgramRuleActionType.SHOWWARNING
                            || action.programRuleActionType() == ProgramRuleActionType.SHOWERROR
                            || action.programRuleActionType() == ProgramRuleActionType.DISPLAYKEYVALUEPAIR
                            || action.programRuleActionType() == ProgramRuleActionType.DISPLAYTEXT
                            || action.programRuleActionType() == ProgramRuleActionType.HIDEOPTIONGROUP
                            || action.programRuleActionType() == ProgramRuleActionType.HIDEOPTION
                            || action.programRuleActionType() == ProgramRuleActionType.SETMANDATORYFIELD)
                        if (!mandatoryRules.contains(rule))
                            mandatoryRules.add(rule);
        }

        List<ProgramRuleVariable> variables = d2.programModule().programRuleVariables().byProgramUid().eq(programUid)
                .blockingGet();
        Iterator<ProgramRuleVariable> variableIterator = variables.iterator();
        while (variableIterator.hasNext()) {
            ProgramRuleVariable variable = variableIterator.next();
            if (variable.trackedEntityAttribute() == null)
                variableIterator.remove();
        }
        List<Rule> finalMandatoryRules = RuleExtensionsKt.toRuleList(mandatoryRules);
        for (ProgramRuleVariable variable : variables) {
            if (variable.trackedEntityAttribute() != null
                    && !attributeRules.containsKey(variable.trackedEntityAttribute().uid()))
                attributeRules.put(variable.trackedEntityAttribute().uid(), finalMandatoryRules);
            for (ProgramRule rule : rules) {
                if (rule.condition().contains(variable.displayName())
                        || actionsContainsAttr(rule.programRuleActions(), variable.displayName())) {
                    if (attributeRules.get(variable.trackedEntityAttribute().uid()) == null)
                        attributeRules.put(variable.trackedEntityAttribute().uid(), finalMandatoryRules);
                    attributeRules.get(variable.trackedEntityAttribute().uid()).add(RuleExtensionsKt.toRuleEngineObject(rule));
                }
            }
        }
    }

    private boolean actionsContainsAttr(List<ProgramRuleAction> programRuleActions, String variableName) {
        boolean actionContainsDe = false;
        for (ProgramRuleAction ruleAction : programRuleActions) {
            if (ruleAction.data() != null && ruleAction.data().contains(variableName))
                actionContainsDe = true;
        }
        return actionContainsDe;
    }

    @Override
    public Flowable<RuleEngine> updateRuleEngine() {
        return this.formRepository.restartRuleEngine();
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> calculate() {
        return queryAttributeValues()
                .map(ruleAttributeValues -> ruleEnrollmentBuilder.attributeValues(ruleAttributeValues).build())
                .switchMap(enrollment -> formRepository.ruleEngine().switchMap(ruleEngine -> {
                    if (isEmpty(lastUpdatedAttr) && !getIndicators)
                        return Flowable.fromCallable(ruleEngine.evaluate(enrollment));
                    else
                        return Flowable
                                .just(attributeRules.get(lastUpdatedAttr) != null ? attributeRules.get(lastUpdatedAttr)
                                        : RuleExtensionsKt.toRuleList(mandatoryRules))
                                .flatMap(rules -> Flowable.fromCallable(ruleEngine.evaluate(enrollment, rules)));
                }).map(Result::success).onErrorReturn(error -> Result.failure(new Exception(error))));
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> reCalculate() {
        initData();
        getIndicators = true;
        return calculate();
    }

    @NonNull
    private Flowable<List<RuleAttributeValue>> queryAttributeValues() {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .flatMap(enrollment -> d2.trackedEntityModule().trackedEntityAttributeValues()
                        .byTrackedEntityInstance().eq(enrollment.trackedEntityInstance()).get()
                        .map(list ->
                                RuleExtensionsKt.toRuleAttributeValue(list, d2, enrollment.program()))).toFlowable();
    }
}

