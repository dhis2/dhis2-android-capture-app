package org.dhis2.android.rtsm.services;

import io.reactivex.Single;
import org.dhis2.android.rtsm.data.AppConfig;
import org.dhis2.android.rtsm.data.models.SearchParametersModel;
import org.dhis2.android.rtsm.data.models.SearchResult;
import org.dhis2.android.rtsm.data.models.StockEntry;
import org.dhis2.android.rtsm.data.models.Transaction;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J,\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH&J\"\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u0012\u001a\u00020\u000bH&\u00a8\u0006\u0013"}, d2 = {"Lorg/dhis2/android/rtsm/services/StockManager;", "", "saveTransaction", "Lio/reactivex/Single;", "", "items", "", "Lorg/dhis2/android/rtsm/data/models/StockEntry;", "transaction", "Lorg/dhis2/android/rtsm/data/models/Transaction;", "appConfig", "Lorg/dhis2/android/rtsm/data/AppConfig;", "search", "Lorg/dhis2/android/rtsm/data/models/SearchResult;", "query", "Lorg/dhis2/android/rtsm/data/models/SearchParametersModel;", "ou", "", "config", "psm-v2.9-DEV_debug"})
public abstract interface StockManager {
    
    /**
     * Get the list of stock items
     *
     * @param query The query object which comprises the search query, OU and other parameters
     * @param ou The organisation unit under consideration (optional)
     *
     * @return The search result containing the livedata of paged list of the matching stock items
     * and total count of matched items
     */
    @org.jetbrains.annotations.NotNull
    public abstract org.dhis2.android.rtsm.data.models.SearchResult search(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.models.SearchParametersModel query, @org.jetbrains.annotations.Nullable
    java.lang.String ou, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.AppConfig config);
    
    @org.jetbrains.annotations.NotNull
    public abstract io.reactivex.Single<kotlin.Unit> saveTransaction(@org.jetbrains.annotations.NotNull
    java.util.List<org.dhis2.android.rtsm.data.models.StockEntry> items, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.models.Transaction transaction, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.AppConfig appConfig);
}