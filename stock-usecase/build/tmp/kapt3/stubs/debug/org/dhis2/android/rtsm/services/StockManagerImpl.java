package org.dhis2.android.rtsm.services;

import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import org.apache.commons.lang3.math.NumberUtils;
import org.dhis2.android.rtsm.commons.Constants;
import org.dhis2.android.rtsm.data.AppConfig;
import org.dhis2.android.rtsm.data.models.IdentifiableModel;
import org.dhis2.android.rtsm.data.models.SearchParametersModel;
import org.dhis2.android.rtsm.data.models.SearchResult;
import org.dhis2.android.rtsm.data.models.StockEntry;
import org.dhis2.android.rtsm.data.models.StockItem;
import org.dhis2.android.rtsm.data.models.Transaction;
import org.dhis2.android.rtsm.services.rules.RuleValidationHelper;
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider;
import org.dhis2.android.rtsm.utils.AttributeHelper;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.EventCreateProjection;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.rules.models.RuleActionAssign;
import org.hisp.dhis.rules.models.RuleEffect;
import timber.log.Timber;
import java.util.Collections;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0088\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ0\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018H\u0002J(\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u001d\u001a\u00020\u001aH\u0002J\u001c\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020 0\u001f2\f\u0010!\u001a\b\u0012\u0004\u0012\u00020 0\u001fH\u0002J\u0012\u0010\"\u001a\u0004\u0018\u00010\u00142\u0006\u0010#\u001a\u00020\u001aH\u0002J\u001a\u0010$\u001a\u0004\u0018\u00010\u001a2\u0006\u0010%\u001a\u00020 2\u0006\u0010&\u001a\u00020\u001aH\u0002J \u0010\'\u001a\u00020\u000e2\u000e\u0010(\u001a\n\u0012\u0004\u0012\u00020)\u0018\u00010\u001f2\u0006\u0010*\u001a\u00020\u001aH\u0002J,\u0010+\u001a\b\u0012\u0004\u0012\u00020\u000e0,2\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00100\u001f2\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018H\u0016J\"\u0010.\u001a\u00020/2\u0006\u00100\u001a\u0002012\b\u00102\u001a\u0004\u0018\u00010\u001a2\u0006\u00103\u001a\u00020\u0018H\u0016J$\u00104\u001a\b\u0012\u0004\u0012\u0002050\u001f2\f\u00106\u001a\b\u0012\u0004\u0012\u00020 0\u001f2\u0006\u00103\u001a\u00020\u0018H\u0002J0\u00107\u001a\u00020\u000e2\u0006\u00108\u001a\u00020\u00102\u0006\u00109\u001a\u00020\u001a2\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010*\u001a\u00020\u001a2\u0006\u0010\u0017\u001a\u00020\u0018H\u0002R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006:"}, d2 = {"Lorg/dhis2/android/rtsm/services/StockManagerImpl;", "Lorg/dhis2/android/rtsm/services/StockManager;", "d2", "Lorg/hisp/dhis/android/core/D2;", "disposable", "Lio/reactivex/disposables/CompositeDisposable;", "schedulerProvider", "Lorg/dhis2/android/rtsm/services/scheduler/BaseSchedulerProvider;", "ruleValidationHelper", "Lorg/dhis2/android/rtsm/services/rules/RuleValidationHelper;", "(Lorg/hisp/dhis/android/core/D2;Lio/reactivex/disposables/CompositeDisposable;Lorg/dhis2/android/rtsm/services/scheduler/BaseSchedulerProvider;Lorg/dhis2/android/rtsm/services/rules/RuleValidationHelper;)V", "getD2", "()Lorg/hisp/dhis/android/core/D2;", "createEvent", "", "item", "Lorg/dhis2/android/rtsm/data/models/StockEntry;", "programStage", "Lorg/hisp/dhis/android/core/program/ProgramStage;", "enrollment", "Lorg/hisp/dhis/android/core/enrollment/Enrollment;", "transaction", "Lorg/dhis2/android/rtsm/data/models/Transaction;", "appConfig", "Lorg/dhis2/android/rtsm/data/AppConfig;", "createEventProjection", "", "facility", "Lorg/dhis2/android/rtsm/data/models/IdentifiableModel;", "programUid", "filterDeleted", "", "Lorg/hisp/dhis/android/core/trackedentity/TrackedEntityInstance;", "list", "getEnrollment", "teiUid", "getStockOnHand", "tei", "stockOnHandUid", "performRuleActions", "ruleEffects", "Lorg/hisp/dhis/rules/models/RuleEffect;", "eventUid", "saveTransaction", "Lio/reactivex/Single;", "items", "search", "Lorg/dhis2/android/rtsm/data/models/SearchResult;", "query", "Lorg/dhis2/android/rtsm/data/models/SearchParametersModel;", "ou", "config", "transform", "Lorg/dhis2/android/rtsm/data/models/StockItem;", "teis", "updateStockOnHand", "entry", "program", "psm-v2.9-DEV_debug"})
public final class StockManagerImpl implements org.dhis2.android.rtsm.services.StockManager {
    @org.jetbrains.annotations.NotNull
    private final org.hisp.dhis.android.core.D2 d2 = null;
    @org.jetbrains.annotations.NotNull
    private final io.reactivex.disposables.CompositeDisposable disposable = null;
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider schedulerProvider = null;
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.services.rules.RuleValidationHelper ruleValidationHelper = null;
    
    @javax.inject.Inject
    public StockManagerImpl(@org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.D2 d2, @org.jetbrains.annotations.NotNull
    io.reactivex.disposables.CompositeDisposable disposable, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider schedulerProvider, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.rules.RuleValidationHelper ruleValidationHelper) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final org.hisp.dhis.android.core.D2 getD2() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public org.dhis2.android.rtsm.data.models.SearchResult search(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.models.SearchParametersModel query, @org.jetbrains.annotations.Nullable
    java.lang.String ou, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.AppConfig config) {
        return null;
    }
    
    private final java.util.List<org.dhis2.android.rtsm.data.models.StockItem> transform(java.util.List<? extends org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance> teis, org.dhis2.android.rtsm.data.AppConfig config) {
        return null;
    }
    
    private final java.lang.String getStockOnHand(org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance tei, java.lang.String stockOnHandUid) {
        return null;
    }
    
    private final java.util.List<org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance> filterDeleted(java.util.List<? extends org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance> list) {
        return null;
    }
    
    private final java.lang.String createEventProjection(org.dhis2.android.rtsm.data.models.IdentifiableModel facility, org.hisp.dhis.android.core.program.ProgramStage programStage, org.hisp.dhis.android.core.enrollment.Enrollment enrollment, java.lang.String programUid) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public io.reactivex.Single<kotlin.Unit> saveTransaction(@org.jetbrains.annotations.NotNull
    java.util.List<org.dhis2.android.rtsm.data.models.StockEntry> items, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.models.Transaction transaction, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.AppConfig appConfig) {
        return null;
    }
    
    private final void createEvent(org.dhis2.android.rtsm.data.models.StockEntry item, org.hisp.dhis.android.core.program.ProgramStage programStage, org.hisp.dhis.android.core.enrollment.Enrollment enrollment, org.dhis2.android.rtsm.data.models.Transaction transaction, org.dhis2.android.rtsm.data.AppConfig appConfig) {
    }
    
    private final void updateStockOnHand(org.dhis2.android.rtsm.data.models.StockEntry entry, java.lang.String program, org.dhis2.android.rtsm.data.models.Transaction transaction, java.lang.String eventUid, org.dhis2.android.rtsm.data.AppConfig appConfig) {
    }
    
    private final void performRuleActions(java.util.List<? extends org.hisp.dhis.rules.models.RuleEffect> ruleEffects, java.lang.String eventUid) {
    }
    
    private final org.hisp.dhis.android.core.enrollment.Enrollment getEnrollment(java.lang.String teiUid) {
        return null;
    }
}