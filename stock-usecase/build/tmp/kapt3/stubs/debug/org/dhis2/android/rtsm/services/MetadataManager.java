package org.dhis2.android.rtsm.services;

import io.reactivex.Single;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\u001c\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\u0006\u0010\u0006\u001a\u00020\u0007H&J\u001c\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\u00040\u00032\u0006\u0010\n\u001a\u00020\u0007H&J\u0018\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\u00032\u0006\u0010\n\u001a\u00020\u0007H&\u00a8\u0006\r"}, d2 = {"Lorg/dhis2/android/rtsm/services/MetadataManager;", "", "destinations", "Lio/reactivex/Single;", "", "Lorg/hisp/dhis/android/core/option/Option;", "distributedTo", "", "facilities", "Lorg/hisp/dhis/android/core/organisationunit/OrganisationUnit;", "programUid", "stockManagementProgram", "Lorg/hisp/dhis/android/core/program/Program;", "psm-v2.9-DEV_debug"})
public abstract interface MetadataManager {
    
    @org.jetbrains.annotations.NotNull
    public abstract io.reactivex.Single<org.hisp.dhis.android.core.program.Program> stockManagementProgram(@org.jetbrains.annotations.NotNull
    java.lang.String programUid);
    
    @org.jetbrains.annotations.NotNull
    public abstract io.reactivex.Single<java.util.List<org.hisp.dhis.android.core.organisationunit.OrganisationUnit>> facilities(@org.jetbrains.annotations.NotNull
    java.lang.String programUid);
    
    @org.jetbrains.annotations.NotNull
    public abstract io.reactivex.Single<java.util.List<org.hisp.dhis.android.core.option.Option>> destinations(@org.jetbrains.annotations.NotNull
    java.lang.String distributedTo);
}