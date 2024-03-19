package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import org.dhis2.animations.CarouselViewAnimations;
import org.dhis2.commons.di.dagger.PerFragment;
import org.dhis2.commons.resources.MetadataIconProvider;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.maps.geometry.bound.GetBoundingBox;
import org.dhis2.maps.geometry.line.MapLineRelationshipToFeature;
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection;
import org.dhis2.maps.geometry.point.MapPointToFeature;
import org.dhis2.maps.geometry.polygon.MapPolygonToFeature;
import org.dhis2.maps.mapper.MapRelationshipToRelationshipMapModel;
import org.dhis2.maps.usecases.MapStyleConfiguration;
import org.dhis2.usescases.teiDashboard.TeiAttributesProvider;
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

    public RelationshipModule(RelationshipView view,
                              String programUid,
                              String teiUid,
                              String enrollmentUid,
                              String eventUid) {
        this.programUid = programUid;
        this.teiUid = teiUid;
        this.enrollmentUid = enrollmentUid;
        this.eventUid = eventUid;
        this.view = view;
    }

    @Provides
    @PerFragment
    RelationshipPresenter providesPresenter(D2 d2,
                                            RelationshipRepository relationshipRepository,
                                            SchedulerProvider schedulerProvider,
                                            AnalyticsHelper analyticsHelper,
                                            MapRelationshipsToFeatureCollection mapRelationshipsToFeatureCollection) {
        return new RelationshipPresenter(view,
                d2,
                programUid,
                teiUid,
                eventUid,
                relationshipRepository,
                schedulerProvider,
                analyticsHelper,
                new MapRelationshipToRelationshipMapModel(),
                mapRelationshipsToFeatureCollection,
                new MapStyleConfiguration(d2));
    }

    @Provides
    @PerFragment
    RelationshipRepository providesRepository(D2 d2,
                                              ResourceManager resourceManager,
                                              TeiAttributesProvider attributesProvider,
                                              MetadataIconProvider metadataIconProvider) {
        RelationshipConfiguration config;
        if (teiUid != null) {
            config = new TrackerRelationshipConfiguration(enrollmentUid, teiUid);
        } else {
            config = new EventRelationshipConfiguration(eventUid);
        }
        return new RelationshipRepositoryImpl(d2, config, resourceManager, attributesProvider, metadataIconProvider);
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
    CarouselViewAnimations animations() {
        return new CarouselViewAnimations();
    }

    @Provides
    @PerFragment
    TeiAttributesProvider teiAttributesProvider(D2 d2) {
        return new TeiAttributesProvider(d2);
    }
}
