package org.dhis2.data.forms;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.enrollment.EnrollmentTableInfo;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueTableInfo;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceTableInfo;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.RuleEngineContext;
import org.hisp.dhis.rules.RuleExpressionEvaluator;
import org.hisp.dhis.rules.models.TriggerEnvironment;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.text.TextUtils.isEmpty;

@SuppressWarnings({
        "PMD.AvoidDuplicateLiterals"
})
public class EnrollmentFormRepository implements FormRepository {

    @NonNull
    private Flowable<RuleEngine> cachedRuleEngineFlowable;

    @NonNull
    private final String enrollmentUid;
    private final D2 d2;
    private final RulesRepository rulesRepository;
    private final RuleExpressionEvaluator expressionEvaluator;

    public EnrollmentFormRepository(@NonNull RuleExpressionEvaluator expressionEvaluator,
                                    @NonNull RulesRepository rulesRepository,
                                    @NonNull String enrollmentUid,
                                    @NonNull D2 d2) {
        this.d2 = d2;
        this.enrollmentUid = enrollmentUid;
        this.rulesRepository = rulesRepository;
        this.expressionEvaluator = expressionEvaluator;

        // We don't want to rebuild RuleEngine on each request, since metadata of
        // the event is not changing throughout lifecycle of FormComponent.
        this.cachedRuleEngineFlowable = enrollmentProgram()
                .switchMap(program -> Single.zip(
                        rulesRepository.rulesNew(program).subscribeOn(Schedulers.io()),
                        rulesRepository.ruleVariables(program).subscribeOn(Schedulers.io()),
                        rulesRepository.enrollmentEvents(enrollmentUid).subscribeOn(Schedulers.io()),
                        rulesRepository.queryConstants().subscribeOn(Schedulers.io()),
                        rulesRepository.supplementaryData().subscribeOn(Schedulers.io()),
                        (rules, variables, events, constants, supplementaryData) -> {
                            RuleEngine.Builder builder = RuleEngineContext.builder(expressionEvaluator)
                                    .rules(rules)
                                    .ruleVariables(variables)
                                    .calculatedValueMap(new HashMap<>())
                                    .supplementaryData(supplementaryData)
                                    .constantsValue(constants)
                                    .build().toEngineBuilder();
                            builder.triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT);
                            builder.events(events);
                            return builder.build();
                        }).toFlowable())
                .cacheWithInitialCapacity(1);
    }

    @Override
    public Flowable<RuleEngine> restartRuleEngine() {
        return this.cachedRuleEngineFlowable = enrollmentProgram()
                .switchMap(program -> Single.zip(
                        rulesRepository.rulesNew(program),
                        rulesRepository.ruleVariables(program),
                        rulesRepository.enrollmentEvents(enrollmentUid),
                        rulesRepository.queryConstants(),
                        rulesRepository.supplementaryData(),
                        (rules, variables, events, constants, supplementaryData) -> {
                            RuleEngine.Builder builder = RuleEngineContext.builder(expressionEvaluator)
                                    .rules(rules)
                                    .ruleVariables(variables)
                                    .calculatedValueMap(new HashMap<>())
                                    .supplementaryData(supplementaryData)
                                    .constantsValue(constants)
                                    .build().toEngineBuilder();
                            builder.triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT);
                            builder.events(events);
                            return builder.build();
                        }).toFlowable())
                .cacheWithInitialCapacity(1);
    }

    @NonNull
    @Override
    public Flowable<RuleEngine> ruleEngine() {
        return cachedRuleEngineFlowable;
    }

    @NonNull
    private Flowable<String> enrollmentProgram() {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .map(Enrollment::program)
                .toFlowable();
    }
}