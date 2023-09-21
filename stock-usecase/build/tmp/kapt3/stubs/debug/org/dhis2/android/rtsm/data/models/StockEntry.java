package org.dhis2.android.rtsm.data.models;

import android.os.Parcelable;
import kotlinx.parcelize.Parcelize;
import java.util.Calendar;
import java.util.Date;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0014\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B;\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u0018\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\u0019\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\u001a\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\tH\u00c6\u0003JA\u0010\u001c\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\b\u001a\u00020\tH\u00c6\u0001J\t\u0010\u001d\u001a\u00020\u001eH\u00d6\u0001J\u0013\u0010\u001f\u001a\u00020 2\b\u0010!\u001a\u0004\u0018\u00010\"H\u00d6\u0003J\t\u0010#\u001a\u00020\u001eH\u00d6\u0001J\t\u0010$\u001a\u00020\u0005H\u00d6\u0001J\u0019\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020(2\u0006\u0010)\u001a\u00020\u001eH\u00d6\u0001R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u001c\u0010\u0007\u001a\u0004\u0018\u00010\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u000e\"\u0004\b\u0014\u0010\u0010R\u001c\u0010\u0006\u001a\u0004\u0018\u00010\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0015\u0010\u000e\"\u0004\b\u0016\u0010\u0010\u00a8\u0006*"}, d2 = {"Lorg/dhis2/android/rtsm/data/models/StockEntry;", "Landroid/os/Parcelable;", "item", "Lorg/dhis2/android/rtsm/data/models/StockItem;", "qty", "", "stockOnHand", "errorMessage", "date", "Ljava/util/Date;", "(Lorg/dhis2/android/rtsm/data/models/StockItem;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/Date;)V", "getDate", "()Ljava/util/Date;", "getErrorMessage", "()Ljava/lang/String;", "setErrorMessage", "(Ljava/lang/String;)V", "getItem", "()Lorg/dhis2/android/rtsm/data/models/StockItem;", "getQty", "setQty", "getStockOnHand", "setStockOnHand", "component1", "component2", "component3", "component4", "component5", "copy", "describeContents", "", "equals", "", "other", "", "hashCode", "toString", "writeToParcel", "", "parcel", "Landroid/os/Parcel;", "flags", "psm-v2.9-DEV_debug"})
@kotlinx.parcelize.Parcelize
public final class StockEntry implements android.os.Parcelable {
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.data.models.StockItem item = null;
    @org.jetbrains.annotations.Nullable
    private java.lang.String qty;
    @org.jetbrains.annotations.Nullable
    private java.lang.String stockOnHand;
    @org.jetbrains.annotations.Nullable
    private java.lang.String errorMessage;
    @org.jetbrains.annotations.NotNull
    private final java.util.Date date = null;
    
    public StockEntry(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.models.StockItem item, @org.jetbrains.annotations.Nullable
    java.lang.String qty, @org.jetbrains.annotations.Nullable
    java.lang.String stockOnHand, @org.jetbrains.annotations.Nullable
    java.lang.String errorMessage, @org.jetbrains.annotations.NotNull
    java.util.Date date) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final org.dhis2.android.rtsm.data.models.StockItem getItem() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getQty() {
        return null;
    }
    
    public final void setQty(@org.jetbrains.annotations.Nullable
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getStockOnHand() {
        return null;
    }
    
    public final void setStockOnHand(@org.jetbrains.annotations.Nullable
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    public final void setErrorMessage(@org.jetbrains.annotations.Nullable
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.Date getDate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final org.dhis2.android.rtsm.data.models.StockItem component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.Date component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final org.dhis2.android.rtsm.data.models.StockEntry copy(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.models.StockItem item, @org.jetbrains.annotations.Nullable
    java.lang.String qty, @org.jetbrains.annotations.Nullable
    java.lang.String stockOnHand, @org.jetbrains.annotations.Nullable
    java.lang.String errorMessage, @org.jetbrains.annotations.NotNull
    java.util.Date date) {
        return null;
    }
    
    @java.lang.Override
    public int describeContents() {
        return 0;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
    
    @java.lang.Override
    public void writeToParcel(@org.jetbrains.annotations.NotNull
    android.os.Parcel parcel, int flags) {
    }
}