package org.dhis2.data.forms.dataentry;

import static android.text.TextUtils.isEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.dhis2.Bindings.RuleExtensionsKt;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramRule;
import org.hisp.dhis.android.core.program.ProgramRuleAction;
import org.hisp.dhis.android.core.program.ProgramRuleActionType;
import org.hisp.dhis.android.core.program.ProgramRuleVariable;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.models.Rule;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleAttributeValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEnrollment;
import org.jetbrains.annotations.NotNull;

import com.squareup.sqlbrite2.BriteDatabase;

import androidx.annotation.NonNull;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

public final class EnrollmentRuleEngineRepository
    implements
    RuleEngineRepository
{

    private static final String QUERY_ATTRIBUTE_VALUES = "SELECT\n" + "  Field.id,\n" + "  Value.value,\n"
        + "  ProgramRuleVariable.useCodeForOptionSet,\n" + "  Option.code,\n" + "  Option.name\n"
        + "FROM (Enrollment INNER JOIN Program ON Program.uid = Enrollment.program)\n" + "  INNER JOIN (\n"
        + "      SELECT\n" + "        TrackedEntityAttribute.uid AS id,\n"
        + "        TrackedEntityAttribute.optionSet AS optionSet,\n"
        + "        ProgramTrackedEntityAttribute.program AS program\n"
        + "      FROM ProgramTrackedEntityAttribute INNER JOIN TrackedEntityAttribute\n"
        + "          ON TrackedEntityAttribute.uid = ProgramTrackedEntityAttribute.trackedEntityAttribute\n"
        + "    ) AS Field ON Field.program = Program.uid\n" + "  INNER JOIN TrackedEntityAttributeValue AS Value ON (\n"
        + "    Value.trackedEntityAttribute = Field.id\n"
        + "        AND Value.trackedEntityInstance = Enrollment.trackedEntityInstance)\n"
        + "  LEFT JOIN ProgramRuleVariable ON ProgramRuleVariable.trackedEntityAttribute = Field.id "
        + "  LEFT JOIN Option ON (Option.optionSet = Field.optionSet AND Option.code = Value.value) "
        + "WHERE Enrollment.uid = ? AND Value.value IS NOT NULL;";

    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private final FormRepository formRepository;

    @NonNull
    private final String enrollmentUid;

    private RuleEnrollment.Builder ruleEnrollmentBuilder;

    @NonNull
    private final D2 d2;

    private Map<String, ProgramRuleVariable> attrRuleVariableMap;

    private Map<String, RuleAttributeValue> ruleAttributeValueMap;

    private Map<String, List<Rule>> attributeRules = new HashMap<>();

    private String lastUpdatedAttr = null;

    private boolean getIndicators = false;

    private List<ProgramRule> mandatoryRules;

    public EnrollmentRuleEngineRepository( @NonNull BriteDatabase briteDatabase, @NonNull FormRepository formRepository,
        @NonNull String enrollmentUid, @NotNull D2 d2 )
    {
        this.d2 = d2;
        this.briteDatabase = briteDatabase;
        this.formRepository = formRepository;
        this.enrollmentUid = enrollmentUid;
        this.ruleAttributeValueMap = new HashMap<>();

        initData();

    }

    public void initData()
    {
        Enrollment enrollment = d2.enrollmentModule().enrollments().uid( enrollmentUid ).blockingGet();
        OrganisationUnit ou = d2.organisationUnitModule().organisationUnits().uid( enrollment.organisationUnit() )
            .blockingGet();
        Program program = d2.programModule().programs().uid( enrollment.program() )
            .blockingGet();

        attrRuleVariableMap = new HashMap<>();
        List<ProgramRuleVariable> ruleVariables = d2.programModule().programRuleVariables().byProgramUid()
            .eq( enrollment.program() ).blockingGet();
        for ( ProgramRuleVariable ruleVariable : ruleVariables )
        {
            if ( ruleVariable.trackedEntityAttribute() != null )
                attrRuleVariableMap.put( ruleVariable.trackedEntityAttribute().uid(), ruleVariable );
        }

        ruleEnrollmentBuilder = RuleEnrollment.builder().enrollment( enrollment.uid() )
            .incidentDate( enrollment.incidentDate() == null ? enrollment.enrollmentDate() : enrollment.incidentDate() )
            .enrollmentDate( enrollment.enrollmentDate() )
            .status( RuleEnrollment.Status.valueOf( enrollment.status().name() ) )
            .organisationUnit( enrollment.organisationUnit() ).organisationUnitCode( ou.code() )
            .programName( program.displayName() );

        loadAttrRules( program.uid() );
    }

    private Map<String, String> getAttributesValueMap( Enrollment enrollment, Program program )
    {
        List<TrackedEntityAttributeValue> attributeValueList = d2.trackedEntityModule().trackedEntityAttributeValues()
            .byTrackedEntityInstance().eq( enrollment.trackedEntityInstance() ).blockingGet();

        Map<String, String> attrValueMap = new HashMap<>();
        for ( TrackedEntityAttributeValue attributeValue : attributeValueList )
        {
            String uid = attributeValue.trackedEntityAttribute();
            String value = attributeValue.value();
            TrackedEntityAttribute attr = d2.trackedEntityModule().trackedEntityAttributes()
                .uid( attributeValue.trackedEntityAttribute() ).blockingGet();
            if ( attr != null && attr.optionSet() != null )
            {
                List<Option> options = d2.optionModule().options().byOptionSetUid().eq( attr.optionSet().uid() )
                    .blockingGet();
                ProgramRuleVariable ruleVariable = attrRuleVariableMap.get( attr.uid() );
                if ( ruleVariable != null
                    && (ruleVariable.useCodeForOptionSet() == null || !ruleVariable.useCodeForOptionSet()) )
                {
                    for ( Option option : options )
                    {
                        if ( value.equals( option.code() ) )
                            value = option.displayName();
                    }
                }
            }else if(attr.valueType().isNumeric()){
                value = Float.valueOf(value).toString();
            }

            attrValueMap.put( uid, value );
        }

        List<ProgramTrackedEntityAttribute> programAttributes = d2.programModule().programTrackedEntityAttributes()
            .byProgram().eq( program.uid() ).blockingGet();

        for ( ProgramTrackedEntityAttribute prgAttr : programAttributes )
        {
            if ( !attrValueMap.containsKey( prgAttr.uid() ) )
                attrValueMap.put( prgAttr.uid(), "" );
        }
        return attrValueMap;
    }

    private void loadAttrRules( String programUid )
    {
        List<ProgramRule> rules = d2.programModule().programRules().byProgramUid().eq( programUid )
            .withProgramRuleActions().blockingGet();
        mandatoryRules = new ArrayList<>();
        Iterator<ProgramRule> ruleIterator = rules.iterator();
        while ( ruleIterator.hasNext() )
        {
            ProgramRule rule = ruleIterator.next();
            if ( rule.condition() == null || rule.programStage() != null )
                ruleIterator.remove();
            else
                for ( ProgramRuleAction action : rule.programRuleActions() )
                    if ( action.programRuleActionType() == ProgramRuleActionType.HIDEFIELD
                        || action.programRuleActionType() == ProgramRuleActionType.HIDESECTION
                        || action.programRuleActionType() == ProgramRuleActionType.ASSIGN
                        || action.programRuleActionType() == ProgramRuleActionType.SHOWWARNING
                        || action.programRuleActionType() == ProgramRuleActionType.SHOWERROR
                        || action.programRuleActionType() == ProgramRuleActionType.DISPLAYKEYVALUEPAIR
                        || action.programRuleActionType() == ProgramRuleActionType.DISPLAYTEXT
                        || action.programRuleActionType() == ProgramRuleActionType.HIDEOPTIONGROUP
                        || action.programRuleActionType() == ProgramRuleActionType.HIDEOPTION
                        || action.programRuleActionType() == ProgramRuleActionType.SETMANDATORYFIELD )
                        if ( !mandatoryRules.contains( rule ) )
                            mandatoryRules.add( rule );
        }

        List<ProgramRuleVariable> variables = d2.programModule().programRuleVariables().byProgramUid().eq( programUid )
            .blockingGet();
        Iterator<ProgramRuleVariable> variableIterator = variables.iterator();
        while ( variableIterator.hasNext() )
        {
            ProgramRuleVariable variable = variableIterator.next();
            if ( variable.trackedEntityAttribute() == null )
                variableIterator.remove();
        }
        List<Rule> finalMandatoryRules = trasformToRule( mandatoryRules );
        for ( ProgramRuleVariable variable : variables )
        {
            if ( variable.trackedEntityAttribute() != null
                && !attributeRules.containsKey( variable.trackedEntityAttribute().uid() ) )
                attributeRules.put( variable.trackedEntityAttribute().uid(), finalMandatoryRules );
            for ( ProgramRule rule : rules )
            {
                if ( rule.condition().contains( variable.displayName() )
                    || actionsContainsAttr( rule.programRuleActions(), variable.displayName() ) )
                {
                    if ( attributeRules.get( variable.trackedEntityAttribute().uid() ) == null )
                        attributeRules.put( variable.trackedEntityAttribute().uid(), trasformToRule( mandatoryRules ) );
                    attributeRules.get( variable.trackedEntityAttribute().uid() ).add( trasformToRule( rule ) );
                }
            }
        }

    }

    private Rule trasformToRule( ProgramRule rule )
    {
        return Rule.create( rule.programStage() != null ? rule.programStage().uid() : null, rule.priority(),
            rule.condition(), transformToRuleAction( rule.programRuleActions() ), rule.displayName() );
    }

    private List<Rule> trasformToRule( List<ProgramRule> rules )
    {
        List<Rule> finalRules = new ArrayList<>();
        for ( ProgramRule rule : rules )
        {
            if ( rule.programStage() == null )
                finalRules
                    .add( Rule.create( rule.programStage() != null ? rule.programStage().uid() : null, rule.priority(),
                        rule.condition(), transformToRuleAction( rule.programRuleActions() ), rule.displayName() ) );
        }
        return finalRules;
    }

    private List<RuleAction> transformToRuleAction( List<ProgramRuleAction> programRuleActions )
    {
        List<RuleAction> ruleActions = new ArrayList<>();
        if ( programRuleActions != null )
        {
            ruleActions = RuleExtensionsKt.toRuleActionList( programRuleActions );
        }
        return ruleActions;
    }

    private boolean actionsContainsAttr( List<ProgramRuleAction> programRuleActions, String variableName )
    {
        boolean actionContainsDe = false;
        for ( ProgramRuleAction ruleAction : programRuleActions )
        {
            if ( ruleAction.data() != null && ruleAction.data().contains( variableName ) )
                actionContainsDe = true;

        }
        return actionContainsDe;
    }

    private void setRuleAttributeMap( Map<String, String> attrValueMap )
    {
        for ( Map.Entry<String, String> attrValueEntry : attrValueMap.entrySet() )
        {
            ruleAttributeValueMap.put( attrValueEntry.getKey(),
                RuleAttributeValue.create( attrValueEntry.getKey(), attrValueEntry.getValue() ) );
        }
    }

    private List<RuleAttributeValue> getRuleAttributeValueMap()
    {
        Enrollment enrollment = d2.enrollmentModule().enrollments().uid( enrollmentUid ).blockingGet();
        Program program = d2.programModule().programs().uid( enrollment.program() ).blockingGet();
        setRuleAttributeMap( getAttributesValueMap( enrollment, program ) );
        return new ArrayList<>( ruleAttributeValueMap.values() );
    }

    @Override
    public void updateRuleAttributeMap( String uid, String value )
    {
        lastUpdatedAttr = uid;
        TrackedEntityAttribute attr = d2.trackedEntityModule().trackedEntityAttributes().uid( uid )
            .blockingGet();
        if ( attr != null && attr.optionSet() != null )
        {
            ProgramRuleVariable ruleVariable = attrRuleVariableMap.get( attr.uid() );
            List<Option> options = d2.optionModule().options().byOptionSetUid().eq( attr.optionSet().uid() )
                .blockingGet();
            if ( (ruleVariable != null
                && (ruleVariable.useCodeForOptionSet() == null || !ruleVariable.useCodeForOptionSet()))
                && options != null )
            {
                for ( Option option : options )
                {
                    if ( Objects.equals( value, option.code() ) )
                        value = option.displayName();
                }
            }
        }else if( value!= null && attr.valueType().isNumeric()){
            value = Float.valueOf(value).toString();
        }
        if ( value != null )
        {
            ruleAttributeValueMap.put( uid, RuleAttributeValue.create( uid, value ) );
        }
        else
        {
            ruleAttributeValueMap.remove( uid );
        }
    }

    @Override
    public Flowable<RuleEngine> updateRuleEngine()
    {
        return this.formRepository.restartRuleEngine();
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> calculate()
    {
        return queryAttributeValues()
            .map( ruleAttributeValues -> ruleEnrollmentBuilder.attributeValues( ruleAttributeValues ).build() )
            .switchMap( enrollment -> formRepository.ruleEngine().switchMap( ruleEngine -> {
                if ( isEmpty( lastUpdatedAttr ) && !getIndicators )
                    return Flowable.fromCallable( ruleEngine.evaluate( enrollment ) );
                else
                    return Flowable
                        .just( attributeRules.get( lastUpdatedAttr ) != null ? attributeRules.get( lastUpdatedAttr )
                            : trasformToRule( mandatoryRules ) )
                        .flatMap( rules -> Flowable.fromCallable( ruleEngine.evaluate( enrollment, rules ) ) );
            } ).map( Result::success ).onErrorReturn( error -> Result.failure( new Exception( error ) ) ) );
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> reCalculate()
    {
        initData();
        getIndicators = true;
        return calculate();
    }

    @NonNull
    private Flowable<List<RuleAttributeValue>> queryAttributeValues()
    {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .flatMap(enrollment -> d2.trackedEntityModule().trackedEntityAttributeValues()
                        .byTrackedEntityInstance().eq(enrollment.trackedEntityInstance()).get()
                        .map(list ->
                                RuleExtensionsKt.toRuleAttributeValue(list, d2, enrollment.program()))).toFlowable();
    }
}