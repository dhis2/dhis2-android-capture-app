package org.dhis2.android.rtsm.services.rules;

import io.reactivex.Flowable;
import io.reactivex.Single;
import org.apache.commons.lang3.math.NumberUtils;
import org.dhis2.android.rtsm.data.AppConfig;
import org.dhis2.android.rtsm.data.TransactionType;
import org.dhis2.android.rtsm.data.models.StockEntry;
import org.dhis2.android.rtsm.data.models.Transaction;
import org.dhis2.android.rtsm.utils.ConfigUtils;
import org.dhis2.android.rtsm.utils.RuleEngineHelper;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.models.RuleActionAssign;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEvent;
import org.hisp.dhis.rules.models.RuleVariable;
import timber.log.Timber;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0088\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010!\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001a\u0010\u0005\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\b0\u00070\u0006H\u0002J:\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\b2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000f2\u0006\u0010\u0011\u001a\u00020\u00122\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\bH\u0002J\u001a\u0010\u0014\u001a\u0004\u0018\u00010\u00152\u0006\u0010\u0016\u001a\u00020\b2\u0006\u0010\u0017\u001a\u00020\bH\u0002J\u001e\u0010\u0018\u001a\u0010\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000f\u0018\u00010\u00062\u0006\u0010\u0019\u001a\u00020\u0015H\u0002J8\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00100\u000f2\b\u0010\u001b\u001a\u0004\u0018\u00010\b2\u0006\u0010\u000b\u001a\u00020\b2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u00122\u0006\u0010\u001f\u001a\u00020 H\u0002J>\u0010!\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020#0\u000f0\"2\u0006\u0010$\u001a\u00020%2\u0006\u0010&\u001a\u00020\b2\u0006\u0010\u001c\u001a\u00020\u001d2\b\u0010\u0013\u001a\u0004\u0018\u00010\b2\u0006\u0010\u001f\u001a\u00020 H\u0016JR\u0010\'\u001a,\u0012(\u0012&\u0012\f\u0012\n **\u0004\u0018\u00010#0# **\u0012\u0012\f\u0012\n **\u0004\u0018\u00010#0#\u0018\u00010\u000f0)0(2\u0006\u0010+\u001a\u00020,2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u0012H\u0002J6\u0010-\u001a\u001c\u0012\u0018\u0012\u0016\u0012\u0004\u0012\u00020. **\n\u0012\u0004\u0012\u00020.\u0018\u00010\u000f0\u000f0\u00062\u0006\u0010\u0017\u001a\u00020\b2\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\bH\u0002J\u0012\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\u0017\u001a\u00020\bH\u0002J\u001e\u0010+\u001a\b\u0012\u0004\u0012\u00020,0\"2\u0006\u0010\u0016\u001a\u00020\b2\u0006\u0010\u0017\u001a\u00020\bH\u0002J\u001c\u0010/\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002000\u000f0\u00062\u0006\u0010\u0017\u001a\u00020\bH\u0002J \u00101\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0004\u0012\u00020\b\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u000f0\u00070\u0006H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00062"}, d2 = {"Lorg/dhis2/android/rtsm/services/rules/RuleValidationHelperImpl;", "Lorg/dhis2/android/rtsm/services/rules/RuleValidationHelper;", "d2", "Lorg/hisp/dhis/android/core/D2;", "(Lorg/hisp/dhis/android/core/D2;)V", "constants", "Lio/reactivex/Single;", "", "", "createRuleEvent", "Lorg/hisp/dhis/rules/models/RuleEvent;", "programStage", "Lorg/hisp/dhis/android/core/program/ProgramStage;", "organisationUnit", "dataValues", "", "Lorg/hisp/dhis/rules/models/RuleDataValue;", "period", "Ljava/util/Date;", "eventUid", "currentEnrollment", "Lorg/hisp/dhis/android/core/enrollment/Enrollment;", "teiUid", "programUid", "enrollmentEvents", "enrollment", "entryDataValues", "qty", "transaction", "Lorg/dhis2/android/rtsm/data/models/Transaction;", "eventDate", "appConfig", "Lorg/dhis2/android/rtsm/data/AppConfig;", "evaluate", "Lio/reactivex/Flowable;", "Lorg/hisp/dhis/rules/models/RuleEffect;", "entry", "Lorg/dhis2/android/rtsm/data/models/StockEntry;", "program", "prepareForDataEntry", "Ljava/util/concurrent/Callable;", "", "kotlin.jvm.PlatformType", "ruleEngine", "Lorg/hisp/dhis/rules/RuleEngine;", "programRules", "Lorg/hisp/dhis/rules/models/Rule;", "ruleVariables", "Lorg/hisp/dhis/rules/models/RuleVariable;", "supplementaryData", "psm-v2.9-DEV_debug"})
public final class RuleValidationHelperImpl implements org.dhis2.android.rtsm.services.rules.RuleValidationHelper {
    @org.jetbrains.annotations.NotNull
    private final org.hisp.dhis.android.core.D2 d2 = null;
    
    @javax.inject.Inject
    public RuleValidationHelperImpl(@org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.D2 d2) {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public io.reactivex.Flowable<java.util.List<org.hisp.dhis.rules.models.RuleEffect>> evaluate(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.models.StockEntry entry, @org.jetbrains.annotations.NotNull
    java.lang.String program, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.models.Transaction transaction, @org.jetbrains.annotations.Nullable
    java.lang.String eventUid, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.AppConfig appConfig) {
        return null;
    }
    
    /**
     * Evaluate the program rules on a blank new rule event in preparation for
     * data entry
     */
    private final java.util.concurrent.Callable<java.util.List<org.hisp.dhis.rules.models.RuleEffect>> prepareForDataEntry(org.hisp.dhis.rules.RuleEngine ruleEngine, org.hisp.dhis.android.core.program.ProgramStage programStage, org.dhis2.android.rtsm.data.models.Transaction transaction, java.util.Date eventDate) {
        return null;
    }
    
    private final org.hisp.dhis.rules.models.RuleEvent createRuleEvent(org.hisp.dhis.android.core.program.ProgramStage programStage, java.lang.String organisationUnit, java.util.List<? extends org.hisp.dhis.rules.models.RuleDataValue> dataValues, java.util.Date period, java.lang.String eventUid) {
        return null;
    }
    
    private final org.hisp.dhis.android.core.program.ProgramStage programStage(java.lang.String programUid) {
        return null;
    }
    
    private final io.reactivex.Single<java.util.List<org.hisp.dhis.rules.models.RuleVariable>> ruleVariables(java.lang.String programUid) {
        return null;
    }
    
    private final io.reactivex.Single<java.util.Map<java.lang.String, java.lang.String>> constants() {
        return null;
    }
    
    private final io.reactivex.Single<java.util.Map<java.lang.String, java.util.List<java.lang.String>>> supplementaryData() {
        return null;
    }
    
    private final io.reactivex.Flowable<org.hisp.dhis.rules.RuleEngine> ruleEngine(java.lang.String teiUid, java.lang.String programUid) {
        return null;
    }
    
    private final org.hisp.dhis.android.core.enrollment.Enrollment currentEnrollment(java.lang.String teiUid, java.lang.String programUid) {
        return null;
    }
    
    private final io.reactivex.Single<java.util.List<org.hisp.dhis.rules.models.RuleEvent>> enrollmentEvents(org.hisp.dhis.android.core.enrollment.Enrollment enrollment) {
        return null;
    }
    
    private final io.reactivex.Single<java.util.List<org.hisp.dhis.rules.models.Rule>> programRules(java.lang.String programUid, java.lang.String eventUid) {
        return null;
    }
    
    private final java.util.List<org.hisp.dhis.rules.models.RuleDataValue> entryDataValues(java.lang.String qty, java.lang.String programStage, org.dhis2.android.rtsm.data.models.Transaction transaction, java.util.Date eventDate, org.dhis2.android.rtsm.data.AppConfig appConfig) {
        return null;
    }
}