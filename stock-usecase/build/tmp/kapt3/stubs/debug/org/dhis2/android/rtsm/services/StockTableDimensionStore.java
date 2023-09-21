package org.dhis2.android.rtsm.services;

import org.dhis2.composetable.model.TableModel;
import org.hisp.dhis.android.core.D2;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\r\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\bH\u0002J\u0018\u0010\r\u001a\u00020\b2\u0006\u0010\u000e\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\u0010H\u0002J\b\u0010\u0011\u001a\u00020\bH\u0002J\u0010\u0010\u0012\u001a\u00020\b2\u0006\u0010\u000e\u001a\u00020\bH\u0002J\"\u0010\u0013\u001a\u001c\u0012\u0004\u0012\u00020\b\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00150\u0014\u0018\u00010\u0014H\u0002J0\u0010\u0016\u001a\u001c\u0012\u0004\u0012\u00020\b\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00150\u0014\u0018\u00010\u00142\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\b0\u0018H\u0002J0\u0010\u0019\u001a\u001c\u0012\u0004\u0012\u00020\b\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00150\u0014\u0018\u00010\u00142\u000e\u0010\u0017\u001a\n\u0012\u0004\u0012\u00020\u001b\u0018\u00010\u001aJ\u0014\u0010\u001c\u001a\u0010\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u0015\u0018\u00010\u0014J\u0014\u0010\u001d\u001a\u0010\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u0015\u0018\u00010\u0014J\u000e\u0010\u001e\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\bJ\u0010\u0010\u001f\u001a\u00020\b2\u0006\u0010\u000e\u001a\u00020\bH\u0002J\b\u0010 \u001a\u00020\bH\u0002J\u001e\u0010!\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\"\u001a\u00020\u0015J\u0016\u0010#\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\b2\u0006\u0010\"\u001a\u00020\u0015J\u0016\u0010$\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\b2\u0006\u0010\"\u001a\u00020\u0015J\u0018\u0010%\u001a\u00020\u000b2\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\bJ\u0010\u0010&\u001a\u00020\b2\u0006\u0010\u000e\u001a\u00020\bH\u0002J\b\u0010\'\u001a\u00020\bH\u0002R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006("}, d2 = {"Lorg/dhis2/android/rtsm/services/StockTableDimensionStore;", "", "d2", "Lorg/hisp/dhis/android/core/D2;", "(Lorg/hisp/dhis/android/core/D2;)V", "getD2", "()Lorg/hisp/dhis/android/core/D2;", "programUid", "", "sectionUid", "clearKey", "", "key", "columnWidthDataStoreKey", "tableId", "column", "", "columnWidthDataStoreKeysForProgram", "columnWidthDataStoreKeysForTable", "columnWidthForProgram", "", "", "columnWidthForTableModels", "tableList", "", "getColumnWidthForSection", "", "Lorg/dhis2/composetable/model/TableModel;", "getTableWidth", "getWidthForSection", "resetTable", "rowHeaderWidthDataStoreKey", "rowHeaderWidthDataStoreKeyForProgram", "saveColumnWidthForSection", "widthDpValue", "saveTableWidth", "saveWidthForSection", "setUids", "tableExtraWidthDataStoreKey", "tableExtraWidthForProgram", "psm-v2.9-DEV_debug"})
public final class StockTableDimensionStore {
    @org.jetbrains.annotations.NotNull
    private final org.hisp.dhis.android.core.D2 d2 = null;
    @org.jetbrains.annotations.NotNull
    private java.lang.String programUid = "";
    @org.jetbrains.annotations.NotNull
    private java.lang.String sectionUid = "";
    
    @javax.inject.Inject
    public StockTableDimensionStore(@org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.D2 d2) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final org.hisp.dhis.android.core.D2 getD2() {
        return null;
    }
    
    public final void setUids(@org.jetbrains.annotations.NotNull
    java.lang.String programUid, @org.jetbrains.annotations.NotNull
    java.lang.String sectionUid) {
    }
    
    public final void saveWidthForSection(@org.jetbrains.annotations.NotNull
    java.lang.String tableId, float widthDpValue) {
    }
    
    public final void saveColumnWidthForSection(@org.jetbrains.annotations.NotNull
    java.lang.String tableId, int column, float widthDpValue) {
    }
    
    public final void saveTableWidth(@org.jetbrains.annotations.NotNull
    java.lang.String tableId, float widthDpValue) {
    }
    
    public final void resetTable(@org.jetbrains.annotations.NotNull
    java.lang.String tableId) {
    }
    
    private final void clearKey(java.lang.String key) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.Map<java.lang.String, java.lang.Float> getTableWidth() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.Map<java.lang.String, java.lang.Float> getWidthForSection() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.Map<java.lang.String, java.util.Map<java.lang.Integer, java.lang.Float>> getColumnWidthForSection(@org.jetbrains.annotations.Nullable
    java.util.List<org.dhis2.composetable.model.TableModel> tableList) {
        return null;
    }
    
    private final java.util.Map<java.lang.String, java.util.Map<java.lang.Integer, java.lang.Float>> columnWidthForTableModels(java.util.List<java.lang.String> tableList) {
        return null;
    }
    
    private final java.util.Map<java.lang.String, java.util.Map<java.lang.Integer, java.lang.Float>> columnWidthForProgram() {
        return null;
    }
    
    private final java.lang.String columnWidthDataStoreKeysForProgram() {
        return null;
    }
    
    private final java.lang.String columnWidthDataStoreKeysForTable(java.lang.String tableId) {
        return null;
    }
    
    private final java.lang.String rowHeaderWidthDataStoreKeyForProgram() {
        return null;
    }
    
    private final java.lang.String columnWidthDataStoreKey(java.lang.String tableId, int column) {
        return null;
    }
    
    private final java.lang.String rowHeaderWidthDataStoreKey(java.lang.String tableId) {
        return null;
    }
    
    private final java.lang.String tableExtraWidthDataStoreKey(java.lang.String tableId) {
        return null;
    }
    
    private final java.lang.String tableExtraWidthForProgram() {
        return null;
    }
}