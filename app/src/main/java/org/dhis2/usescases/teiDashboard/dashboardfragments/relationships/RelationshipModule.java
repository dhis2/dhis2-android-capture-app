package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import android.content.Context;

import org.dhis2.commons.data.ProgramConfigurationRepository;
import org.dhis2.commons.date.DateLabelProvider;
import org.dhis2.commons.date.DateUtils;
import org.dhis2.commons.di.dagger.PerFragment;
import org.dhis2.commons.network.NetworkUtils;
import org.dhis2.commons.resources.D2ErrorUtils;
import org.dhis2.commons.resources.MetadataIconProvider;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.viewmodel.DispatcherProvider;
import org.dhis2.maps.geometry.bound.GetBoundingBox;
import org.dhis2.maps.geometry.line.MapLineRelationshipToFeature;
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection;
import org.dhis2.maps.geometry.point.MapPointToFeature;
import org.dhis2.maps.geometry.polygon.MapPolygonToFeature;
import org.dhis2.maps.model.MapScope;
import org.dhis2.maps.usecases.MapStyleConfiguration;
import org.dhis2.tracker.data.ProfilePictureProvider;
import org.dhis2.tracker.relationships.data.EventRelationshipsRepository;
import org.dhis2.tracker.relationships.data.RelationshipsRepository;
import org.dhis2.tracker.relationships.data.TrackerRelationshipsRepository;
import org.dhis2.tracker.relationships.domain.AddRelationship;
import org.dhis2.tracker.relationships.domain.DeleteRelationships;
import org.dhis2.tracker.relationships.domain.GetRelationshipsByType;
import org.dhis2.tracker.relationships.ui.RelationshipsViewModel;
import org.dhis2.tracker.relationships.ui.mapper.RelationshipsUiStateMapper;
import org.dhis2.tracker.ui.AvatarProvider;
import org.dhis2.usescases.events.EventInfoProvider;
import org.dhis2.usescases.teiDashboard.TeiAttributesProvider;
import org.dhis2.usescases.tracker.TrackedEntityInstanceInfoProvider;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
public class RelationshipModule {

    private final String programUid;
    private final String teiUid;
    private final String enrollmentUid;
    private final String eventUid;
    private final RelationshipView view;
    private final Context moduleContext;

    public RelationshipModule(
            Context moduleContext,
            RelationshipView view,
            String programUid,
            String teiUid,
            String enrollmentUid,
            String eventUid) {
        this.moduleContext = moduleContext;
        this.programUid = programUid;
        this.teiUid = teiUid;
        this.enrollmentUid = enrollmentUid;
        this.eventUid = eventUid;
        this.view = view;
    }

    @Provides
    @PerFragment
    RelationshipPresenter providesPresenter(D2 d2,
                                            RelationshipMapsRepository relationshipMapsRepository,
                                            AnalyticsHelper analyticsHelper,
                                            MapRelationshipsToFeatureCollection mapRelationshipsToFeatureCollection,
                                            RelationshipsRepository relationshipsRepository,
                                            AvatarProvider avatarProvider,
                                            DateLabelProvider dateLabelProvider,
                                            DispatcherProvider dispatcherProvider,
                                            ProgramConfigurationRepository programConfigurationRepository
    ) {
        return new RelationshipPresenter(
                view,
                d2,
                teiUid,
                eventUid,
                relationshipMapsRepository,
                analyticsHelper,
                mapRelationshipsToFeatureCollection,
                new MapStyleConfiguration(
                        d2,
                        programUid,
                        MapScope.PROGRAM,
                        programConfigurationRepository
                ),
                relationshipsRepository,
                avatarProvider,
                dateLabelProvider,
                dispatcherProvider
        );
    }

    @Provides
    @PerFragment
    ProgramConfigurationRepository providesProgramConfigurationRepository(D2 d2) {
        return new ProgramConfigurationRepository(d2);
    }

    @Provides
    @PerFragment
    RelationshipMapsRepository providesRepository(
            D2 d2,
            ResourceManager resourceManager,
            MetadataIconProvider metadataIconProvider,
            DateLabelProvider dateLabelProvider,
            DateUtils dateUtils
    ) {
        RelationshipConfiguration config;
        if (teiUid != null) {
            config = new TrackerRelationshipConfiguration(enrollmentUid, teiUid);
        } else {
            config = new EventRelationshipConfiguration(eventUid);
        }
        ProfilePictureProvider profilePictureProvider = new ProfilePictureProvider(d2);
        return new RelationshipMapsRepositoryImpl(
                d2,
                config,
                new TrackedEntityInstanceInfoProvider(
                        d2,
                        profilePictureProvider,
                        dateLabelProvider,
                        metadataIconProvider
                ),
                new EventInfoProvider(
                        d2,
                        resourceManager,
                        dateLabelProvider,
                        metadataIconProvider,
                        profilePictureProvider,
                        dateUtils
                )
        );
    }

    @Provides
    @PerFragment
    MapRelationshipsToFeatureCollection provideMapRelationshipToFeatureCollection() {
        return new MapRelationshipsToFeatureCollection(
                new MapLineRelationshipToFeature(),
                new MapPointToFeature(),
                new MapPolygonToFeature(),
                new GetBoundingBox()
        );
    }

    @Provides
    @PerFragment
    TeiAttributesProvider teiAttributesProvider(D2 d2) {
        return new TeiAttributesProvider(d2);
    }

    @Provides
    @PerFragment
    RelationshipsViewModel provideRelationshipsViewModel(
            GetRelationshipsByType getRelationshipsByType,
            DeleteRelationships deleteRelationships,
            DispatcherProvider dispatcherProvider,
            AddRelationship addRelationship,
            D2ErrorUtils d2ErrorUtils,
            RelationshipsUiStateMapper relationshipsUiStateMapper
    ) {
        return new RelationshipsViewModel(
                dispatcherProvider,
                getRelationshipsByType,
                deleteRelationships,
                addRelationship,
                d2ErrorUtils,
                relationshipsUiStateMapper
        );
    }

    @Provides
    @PerFragment
    DateUtils provideDateUtils(
    ) {
        return DateUtils.getInstance();
    }

    @Provides
    @PerFragment
    GetRelationshipsByType provideGetRelationshipsByType(
            RelationshipsRepository relationshipsRepository
    ) {
        return new GetRelationshipsByType(relationshipsRepository);
    }

    @Provides
    @PerFragment
    DeleteRelationships provideDeleteRelationships(
            RelationshipsRepository relationshipsRepository
    ) {
        return new DeleteRelationships(relationshipsRepository);
    }

    @Provides
    @PerFragment
    AddRelationship provideAddRelationship(
            RelationshipsRepository relationshipsRepository
    ) {
        return new AddRelationship(relationshipsRepository);
    }

    @Provides
    @PerFragment
    RelationshipsRepository provideRelationshipsRepository(
            D2 d2,
            ResourceManager resourceManager,
            ProfilePictureProvider profilePictureProvider
    ) {
        if (teiUid != null) {
            return new TrackerRelationshipsRepository(
                    d2,
                    resourceManager,
                    teiUid,
                    enrollmentUid,
                    profilePictureProvider
            );
        } else {
            return new EventRelationshipsRepository(
                    d2,
                    resourceManager,
                    eventUid,
                    profilePictureProvider
            );
        }

    }

    @Provides
    @PerFragment
    DateLabelProvider provideDateLabelProvider(ResourceManager resourceManager) {
        return new DateLabelProvider(moduleContext, resourceManager);
    }

    @Provides
    @PerFragment
    ProfilePictureProvider provideProfilePictureProvider(D2 d2) {
        return new ProfilePictureProvider(d2);
    }

    @Provides
    @PerFragment
    AvatarProvider provideAvatarProvider(
            MetadataIconProvider metadataIconProvider
    ) {
        return new AvatarProvider(metadataIconProvider);
    }

    @Provides
    @PerFragment
    D2ErrorUtils provideD2ErrorUtils(
            NetworkUtils networkUtils
    ) {
        return new D2ErrorUtils(moduleContext, networkUtils);
    }

    @Provides
    @PerFragment
    RelationshipsUiStateMapper provideRelationshipsUiStateMapper(
            AvatarProvider avatarProvider,
            DateLabelProvider dateLabelProvider
    ) {
        return new RelationshipsUiStateMapper(avatarProvider, dateLabelProvider);
    }
}
