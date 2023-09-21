package org.dhis2.android.rtsm.services;

import io.reactivex.Single;
import org.dhis2.android.rtsm.exceptions.InitializationException;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001c\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\t\u001a\u00020\nH\u0016J\u001c\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00070\u00062\u0006\u0010\r\u001a\u00020\nH\u0016J\u0018\u0010\u000e\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\u00062\u0006\u0010\r\u001a\u00020\nH\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lorg/dhis2/android/rtsm/services/MetadataManagerImpl;", "Lorg/dhis2/android/rtsm/services/MetadataManager;", "d2", "Lorg/hisp/dhis/android/core/D2;", "(Lorg/hisp/dhis/android/core/D2;)V", "destinations", "Lio/reactivex/Single;", "", "Lorg/hisp/dhis/android/core/option/Option;", "distributedTo", "", "facilities", "Lorg/hisp/dhis/android/core/organisationunit/OrganisationUnit;", "programUid", "stockManagementProgram", "Lorg/hisp/dhis/android/core/program/Program;", "psm-v2.9-DEV_debug"})
public final class MetadataManagerImpl implements org.dhis2.android.rtsm.services.MetadataManager {
    @org.jetbrains.annotations.NotNull
    private final org.hisp.dhis.android.core.D2 d2 = null;
    
    @javax.inject.Inject
    public MetadataManagerImpl(@org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.D2 d2) {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public io.reactivex.Single<org.hisp.dhis.android.core.program.Program> stockManagementProgram(@org.jetbrains.annotations.NotNull
    java.lang.String programUid) {
        return null;
    }
    
    /**
     * Get the program OUs which the user has access to and also
     * set as the user's the data capture OU. This is simply the
     * intersection of the program OUs (without DESCENDANTS) and
     * the user data capture OUs (with DESCENDANTS)
     */
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public io.reactivex.Single<java.util.List<org.hisp.dhis.android.core.organisationunit.OrganisationUnit>> facilities(@org.jetbrains.annotations.NotNull
    java.lang.String programUid) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public io.reactivex.Single<java.util.List<org.hisp.dhis.android.core.option.Option>> destinations(@org.jetbrains.annotations.NotNull
    java.lang.String distributedTo) {
        return null;
    }
}