package org.dhis2.android.rtsm.services.rules;

import io.reactivex.Flowable;
import org.dhis2.android.rtsm.data.AppConfig;
import org.dhis2.android.rtsm.data.models.StockEntry;
import org.dhis2.android.rtsm.data.models.Transaction;
import org.hisp.dhis.rules.models.RuleEffect;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J@\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\t2\u0006\u0010\r\u001a\u00020\u000eH&\u00a8\u0006\u000f"}, d2 = {"Lorg/dhis2/android/rtsm/services/rules/RuleValidationHelper;", "", "evaluate", "Lio/reactivex/Flowable;", "", "Lorg/hisp/dhis/rules/models/RuleEffect;", "entry", "Lorg/dhis2/android/rtsm/data/models/StockEntry;", "program", "", "transaction", "Lorg/dhis2/android/rtsm/data/models/Transaction;", "eventUid", "appConfig", "Lorg/dhis2/android/rtsm/data/AppConfig;", "psm-v2.9-DEV_debug"})
public abstract interface RuleValidationHelper {
    
    @org.jetbrains.annotations.NotNull
    public abstract io.reactivex.Flowable<java.util.List<org.hisp.dhis.rules.models.RuleEffect>> evaluate(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.models.StockEntry entry, @org.jetbrains.annotations.NotNull
    java.lang.String program, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.models.Transaction transaction, @org.jetbrains.annotations.Nullable
    java.lang.String eventUid, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.AppConfig appConfig);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}