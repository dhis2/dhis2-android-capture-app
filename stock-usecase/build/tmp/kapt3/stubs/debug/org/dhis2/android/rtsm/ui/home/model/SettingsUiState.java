package org.dhis2.android.rtsm.ui.home.model;

import org.dhis2.android.rtsm.R;
import org.dhis2.android.rtsm.data.TransactionType;
import org.dhis2.android.rtsm.utils.UIText;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import java.time.LocalDateTime;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0012\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B9\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010\u0019\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u000b\u0010\u001a\u001a\u0004\u0018\u00010\tH\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u000bH\u00c6\u0003J?\u0010\u001c\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t2\b\b\u0002\u0010\n\u001a\u00020\u000bH\u00c6\u0001J\b\u0010\u001d\u001a\u0004\u0018\u00010\u001eJ\u0013\u0010\u001f\u001a\u00020 2\b\u0010!\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\u0006\u0010\"\u001a\u00020\u0003J\u0006\u0010#\u001a\u00020\u001eJ\u0006\u0010$\u001a\u00020 J\u0006\u0010%\u001a\u00020 J\t\u0010&\u001a\u00020\'H\u00d6\u0001J\t\u0010(\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016\u00a8\u0006)"}, d2 = {"Lorg/dhis2/android/rtsm/ui/home/model/SettingsUiState;", "", "programUid", "", "transactionType", "Lorg/dhis2/android/rtsm/data/TransactionType;", "facility", "Lorg/hisp/dhis/android/core/organisationunit/OrganisationUnit;", "destination", "Lorg/hisp/dhis/android/core/option/Option;", "transactionDate", "Ljava/time/LocalDateTime;", "(Ljava/lang/String;Lorg/dhis2/android/rtsm/data/TransactionType;Lorg/hisp/dhis/android/core/organisationunit/OrganisationUnit;Lorg/hisp/dhis/android/core/option/Option;Ljava/time/LocalDateTime;)V", "getDestination", "()Lorg/hisp/dhis/android/core/option/Option;", "getFacility", "()Lorg/hisp/dhis/android/core/organisationunit/OrganisationUnit;", "getProgramUid", "()Ljava/lang/String;", "getTransactionDate", "()Ljava/time/LocalDateTime;", "getTransactionType", "()Lorg/dhis2/android/rtsm/data/TransactionType;", "component1", "component2", "component3", "component4", "component5", "copy", "deliverToLabel", "Lorg/dhis2/android/rtsm/utils/UIText;", "equals", "", "other", "facilityName", "fromFacilitiesLabel", "hasDestinationSelected", "hasFacilitySelected", "hashCode", "", "toString", "psm-v2.9-DEV_debug"})
public final class SettingsUiState {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String programUid = null;
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.data.TransactionType transactionType = null;
    @org.jetbrains.annotations.Nullable
    private final org.hisp.dhis.android.core.organisationunit.OrganisationUnit facility = null;
    @org.jetbrains.annotations.Nullable
    private final org.hisp.dhis.android.core.option.Option destination = null;
    @org.jetbrains.annotations.NotNull
    private final java.time.LocalDateTime transactionDate = null;
    
    public SettingsUiState(@org.jetbrains.annotations.NotNull
    java.lang.String programUid, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.TransactionType transactionType, @org.jetbrains.annotations.Nullable
    org.hisp.dhis.android.core.organisationunit.OrganisationUnit facility, @org.jetbrains.annotations.Nullable
    org.hisp.dhis.android.core.option.Option destination, @org.jetbrains.annotations.NotNull
    java.time.LocalDateTime transactionDate) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getProgramUid() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final org.dhis2.android.rtsm.data.TransactionType getTransactionType() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final org.hisp.dhis.android.core.organisationunit.OrganisationUnit getFacility() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final org.hisp.dhis.android.core.option.Option getDestination() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.time.LocalDateTime getTransactionDate() {
        return null;
    }
    
    public final boolean hasFacilitySelected() {
        return false;
    }
    
    public final boolean hasDestinationSelected() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final org.dhis2.android.rtsm.utils.UIText fromFacilitiesLabel() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final org.dhis2.android.rtsm.utils.UIText deliverToLabel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String facilityName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final org.dhis2.android.rtsm.data.TransactionType component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final org.hisp.dhis.android.core.organisationunit.OrganisationUnit component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final org.hisp.dhis.android.core.option.Option component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.time.LocalDateTime component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final org.dhis2.android.rtsm.ui.home.model.SettingsUiState copy(@org.jetbrains.annotations.NotNull
    java.lang.String programUid, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.TransactionType transactionType, @org.jetbrains.annotations.Nullable
    org.hisp.dhis.android.core.organisationunit.OrganisationUnit facility, @org.jetbrains.annotations.Nullable
    org.hisp.dhis.android.core.option.Option destination, @org.jetbrains.annotations.NotNull
    java.time.LocalDateTime transactionDate) {
        return null;
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
}