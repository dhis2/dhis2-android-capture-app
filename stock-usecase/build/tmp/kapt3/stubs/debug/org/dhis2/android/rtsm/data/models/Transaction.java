package org.dhis2.android.rtsm.data.models;

import android.os.Parcel;
import android.os.Parcelable;
import org.dhis2.android.rtsm.data.TransactionType;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\u0018\u0000 \u001b2\u00020\u0001:\u0001\u001bB\u000f\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004B\'\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\n\u0012\b\u0010\u000b\u001a\u0004\u0018\u00010\b\u00a2\u0006\u0002\u0010\fJ\b\u0010\u0014\u001a\u00020\u0015H\u0016J\b\u0010\u0016\u001a\u00020\nH\u0016J\u0018\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u00032\u0006\u0010\u001a\u001a\u00020\u0015H\u0016R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000eR\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013\u00a8\u0006\u001c"}, d2 = {"Lorg/dhis2/android/rtsm/data/models/Transaction;", "Landroid/os/Parcelable;", "parcel", "Landroid/os/Parcel;", "(Landroid/os/Parcel;)V", "transactionType", "Lorg/dhis2/android/rtsm/data/TransactionType;", "facility", "Lorg/dhis2/android/rtsm/data/models/IdentifiableModel;", "transactionDate", "", "distributedTo", "(Lorg/dhis2/android/rtsm/data/TransactionType;Lorg/dhis2/android/rtsm/data/models/IdentifiableModel;Ljava/lang/String;Lorg/dhis2/android/rtsm/data/models/IdentifiableModel;)V", "getDistributedTo", "()Lorg/dhis2/android/rtsm/data/models/IdentifiableModel;", "getFacility", "getTransactionDate", "()Ljava/lang/String;", "getTransactionType", "()Lorg/dhis2/android/rtsm/data/TransactionType;", "describeContents", "", "toString", "writeToParcel", "", "out", "flags", "CREATOR", "psm-v2.9-DEV_debug"})
public final class Transaction implements android.os.Parcelable {
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.data.TransactionType transactionType = null;
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.data.models.IdentifiableModel facility = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String transactionDate = null;
    @org.jetbrains.annotations.Nullable
    private final org.dhis2.android.rtsm.data.models.IdentifiableModel distributedTo = null;
    @org.jetbrains.annotations.NotNull
    public static final org.dhis2.android.rtsm.data.models.Transaction.CREATOR CREATOR = null;
    
    public Transaction(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.TransactionType transactionType, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.models.IdentifiableModel facility, @org.jetbrains.annotations.NotNull
    java.lang.String transactionDate, @org.jetbrains.annotations.Nullable
    org.dhis2.android.rtsm.data.models.IdentifiableModel distributedTo) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final org.dhis2.android.rtsm.data.TransactionType getTransactionType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final org.dhis2.android.rtsm.data.models.IdentifiableModel getFacility() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getTransactionDate() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final org.dhis2.android.rtsm.data.models.IdentifiableModel getDistributedTo() {
        return null;
    }
    
    public Transaction(@org.jetbrains.annotations.NotNull
    android.os.Parcel parcel) {
        super();
    }
    
    @java.lang.Override
    public void writeToParcel(@org.jetbrains.annotations.NotNull
    android.os.Parcel out, int flags) {
    }
    
    @java.lang.Override
    public int describeContents() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00022\u0006\u0010\u0005\u001a\u00020\u0006H\u0016J\u001d\u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0016\u00a2\u0006\u0002\u0010\u000b\u00a8\u0006\f"}, d2 = {"Lorg/dhis2/android/rtsm/data/models/Transaction$CREATOR;", "Landroid/os/Parcelable$Creator;", "Lorg/dhis2/android/rtsm/data/models/Transaction;", "()V", "createFromParcel", "parcel", "Landroid/os/Parcel;", "newArray", "", "size", "", "(I)[Lorg/dhis2/android/rtsm/data/models/Transaction;", "psm-v2.9-DEV_debug"})
    public static final class CREATOR implements android.os.Parcelable.Creator<org.dhis2.android.rtsm.data.models.Transaction> {
        
        private CREATOR() {
            super();
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public org.dhis2.android.rtsm.data.models.Transaction createFromParcel(@org.jetbrains.annotations.NotNull
        android.os.Parcel parcel) {
            return null;
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public org.dhis2.android.rtsm.data.models.Transaction[] newArray(int size) {
            return null;
        }
    }
}