package org.dhis2.uicomponents.map.geometry

import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import org.dhis2.maps.geometry.bound.BoundsGeometry
import org.dhis2.maps.geometry.bound.GetBoundingBox
import org.dhis2.maps.geometry.line.MapLineRelationshipToFeature
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.Companion.ENROLLMENT
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.Companion.TEI
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.Companion.TEI_IMAGE
import org.dhis2.maps.geometry.point.MapPointToFeature
import org.dhis2.maps.geometry.polygon.MapPolygonPointToFeature
import org.dhis2.maps.geometry.polygon.MapPolygonToFeature
import org.dhis2.maps.model.MapItemModel
import org.dhis2.maps.model.RelatedInfo
import org.dhis2.maps.model.RelationshipDirection
import org.dhis2.ui.avatar.AvatarProviderConfiguration
import org.dhis2.uicomponents.map.mocks.RelationshipViewModelDummy
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.junit.Before
import org.junit.Test

class MapTeisToFeatureCollectionTest {

    private lateinit var mapTeisToFeatureCollection: MapTeisToFeatureCollection

    @Before
    fun setup() {
        mapTeisToFeatureCollection =
            MapTeisToFeatureCollection(
                bounds = BoundsGeometry(),
                mapPointToFeature = MapPointToFeature(),
                mapPolygonToFeature = MapPolygonToFeature(),
                mapPolygonPointToFeature = MapPolygonPointToFeature(),
                mapRelationshipsToFeatureCollection = MapRelationshipsToFeatureCollection(
                    mapLineToFeature = MapLineRelationshipToFeature(),
                    mapPointToFeature = MapPointToFeature(),
                    mapPolygonToFeature = MapPolygonToFeature(),
                    bounds = GetBoundingBox(),
                ),
            )
    }

    @Test
    fun `Should map tei models to point feature collection`() {
        val teiList = createSearchTeiModelList()

        val result = mapTeisToFeatureCollection.map(teiList, true, emptyList())
        val featureCollectionResults = result.first[TEI]

        val pointFeatureResult = featureCollectionResults?.features()?.get(0)?.geometry() as Point
        val uid = featureCollectionResults.features()?.get(0)
            ?.getStringProperty(MapTeisToFeatureCollection.TEI_UID)
        val image = featureCollectionResults.features()?.get(0)?.getStringProperty(TEI_IMAGE)
        assertThat(pointFeatureResult.longitude(), `is`(POINT_LONGITUDE))
        assertThat(pointFeatureResult.latitude(), `is`(POINT_LATITUDE))
        assertThat(uid, `is`(TEI_UID))
        assertThat(image, `is`(TEI_UID_IMAGE))
    }

    @Test
    fun `Should map tei models to relationship feature collection`() {
        val teiList = createSearchTeiModelWithRelationships()
        val mapRelationships = createSearchTeiModelWithRelationships()

        val result = mapTeisToFeatureCollection.map(teiList, true, mapRelationships)
        val featureCollectionResults =
            result.first[RelationshipViewModelDummy.DISPLAY_NAME]

        val relationshipFeatureCollection =
            featureCollectionResults?.features()?.get(0)?.geometry() as LineString
        assertThat(
            relationshipFeatureCollection.coordinates()[0].longitude(),
            `is`(POINT_LONGITUDE),
        )
        assertThat(relationshipFeatureCollection.coordinates()[0].latitude(), `is`(POINT_LATITUDE))
        assertThat(
            relationshipFeatureCollection.coordinates()[1].longitude(),
            `is`(POINT_LONGITUDE_ENROLLMENT),
        )
        assertThat(
            relationshipFeatureCollection.coordinates()[1].latitude(),
            `is`(POINT_LATITUDE_ENROLLMENT),
        )
    }

    @Test
    fun `Should map tei models to point enrollment feature collection`() {
        val teiList = createSearchTeiModelWithEnrollment()

        val result = mapTeisToFeatureCollection.map(teiList, true, emptyList())
        val featureTeiCollectionResults = result.first[TEI]
        val featureEnrollmentCollectionResults = result.first[ENROLLMENT]

        val teiPointFeatureResult =
            featureTeiCollectionResults?.features()?.get(0)?.geometry() as Point
        val uid = featureTeiCollectionResults.features()?.get(0)
            ?.getStringProperty(MapTeisToFeatureCollection.TEI_UID)
        assertThat(teiPointFeatureResult.longitude(), `is`(POINT_LONGITUDE))
        assertThat(teiPointFeatureResult.latitude(), `is`(POINT_LATITUDE))
        assertThat(uid, `is`(TEI_UID))

        val enrollmentUid = featureEnrollmentCollectionResults?.features()?.get(0)
            ?.getStringProperty(MapTeisToFeatureCollection.ENROLLMENT_UID)
        val enrollmentPointFeatureResult =
            featureEnrollmentCollectionResults?.features()?.get(0)?.geometry() as Point
        assertThat(enrollmentPointFeatureResult.longitude(), `is`(POINT_LONGITUDE_ENROLLMENT))
        assertThat(enrollmentPointFeatureResult.latitude(), `is`(POINT_LATITUDE_ENROLLMENT))
        assertThat(enrollmentUid, `is`(ENROLLMENT_UID))
    }

    @Test
    fun `Should map tei models to polygon enrollment feature collection`() {
        val teiList = createSearchTeiModelListWithEnrollmentPolygon()

        val result = mapTeisToFeatureCollection.map(teiList, true, emptyList())
        val featureTeiCollectionResults = result.first[TEI]
        val featureEnrollmentCollectionResults = result.first[ENROLLMENT]

        val featureTeiResult =
            featureTeiCollectionResults?.features()?.get(0)?.geometry() as Point
        val uidTeiFeature = featureTeiCollectionResults.features()?.get(0)
            ?.getStringProperty(MapTeisToFeatureCollection.TEI_UID)
        assertThat(featureTeiResult.longitude(), `is`(POINT_LONGITUDE))
        assertThat(featureTeiResult.latitude(), `is`(POINT_LATITUDE))
        assertThat(uidTeiFeature, `is`(TEI_UID))

        val featureTeiEnrollmentResult =
            featureEnrollmentCollectionResults?.features()?.get(1)?.geometry() as Point
        val uidTeiEnrollmentFeature = featureEnrollmentCollectionResults.features()?.get(1)
            ?.getStringProperty(MapTeisToFeatureCollection.TEI_UID)
        assertThat(
            featureTeiEnrollmentResult.longitude(),
            `is`(POINT_LONGITUDE_ENROLLMENT),
        )
        assertThat(
            featureTeiEnrollmentResult.latitude(),
            `is`(POINT_LATITUDE_ENROLLMENT),
        )
        assertThat(uidTeiEnrollmentFeature, `is`(TEI_UID))

        val featureEnrollmentFirstResult =
            featureEnrollmentCollectionResults.features()?.get(0)?.geometry() as Polygon
        val featureEnrollmentFirstUid = featureEnrollmentCollectionResults.features()?.get(0)
            ?.getStringProperty(MapTeisToFeatureCollection.TEI_UID)
        assertThat(
            featureEnrollmentFirstResult.coordinates()[0][0].longitude(),
            `is`(POINT_LONGITUDE_ENROLLMENT),
        )
        assertThat(
            featureEnrollmentFirstResult.coordinates()[0][0].latitude(),
            `is`(POINT_LATITUDE_ENROLLMENT),
        )
        assertThat(featureEnrollmentFirstUid, `is`(TEI_UID))

        val featureEnrollmentSecondResult =
            featureEnrollmentCollectionResults.features()?.get(1)?.geometry() as Point
        val featureEnrollmentSecondUid = featureEnrollmentCollectionResults.features()?.get(1)
            ?.getStringProperty(MapTeisToFeatureCollection.TEI_UID)
        assertThat(
            featureEnrollmentSecondResult.longitude(),
            `is`(POINT_LONGITUDE_ENROLLMENT),
        )
        assertThat(
            featureEnrollmentSecondResult.latitude(),
            `is`(POINT_LATITUDE_ENROLLMENT),
        )
        assertThat(featureEnrollmentSecondUid, `is`(TEI_UID))
    }

    private fun createSearchTeiModelListWithEnrollmentPolygon(): List<MapItemModel> {
        return listOf(
            MapItemModel(
                uid = TEI_UID,
                avatarProviderConfiguration = AvatarProviderConfiguration.ProfilePic(
                    TEI_UID_IMAGE,
                    "",
                ),
                title = "",
                description = null,
                lastUpdated = "",
                additionalInfoList = emptyList(),
                isOnline = false,
                geometry = GeometryHelper.createPointGeometry(
                    listOf(POINT_LONGITUDE, POINT_LATITUDE),
                ),
                relatedInfo = RelatedInfo(
                    enrollment = RelatedInfo.Enrollment(
                        uid = ENROLLMENT_UID,
                        geometry = GeometryHelper.createPolygonGeometry(
                            listOf(
                                listOf(
                                    listOf(
                                        POINT_LONGITUDE_ENROLLMENT,
                                        POINT_LATITUDE_ENROLLMENT,
                                    ),
                                ),
                                listOf(
                                    listOf(
                                        POINT_LONGITUDE_ENROLLMENT,
                                        POINT_LATITUDE_ENROLLMENT,
                                    ),
                                ),
                                listOf(
                                    listOf(
                                        POINT_LONGITUDE_ENROLLMENT,
                                        POINT_LATITUDE_ENROLLMENT,
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
                state = State.SYNCED,
            ),
        )
    }

    private fun createSearchTeiModelList(): List<MapItemModel> {
        return listOf(
            MapItemModel(
                uid = TEI_UID,
                avatarProviderConfiguration = AvatarProviderConfiguration.ProfilePic(
                    TEI_UID_IMAGE,
                    "",
                ),
                title = "",
                description = null,
                lastUpdated = "",
                additionalInfoList = emptyList(),
                isOnline = false,
                geometry = Geometry.builder()
                    .type(FeatureType.POINT)
                    .coordinates("[$POINT_LONGITUDE, $POINT_LATITUDE]")
                    .build(),
                relatedInfo = null,
                state = State.SYNCED,
            ),
        )
    }

    private fun createSearchTeiModelWithEnrollment(): List<MapItemModel> {
        return listOf(
            MapItemModel(
                uid = TEI_UID,
                avatarProviderConfiguration = AvatarProviderConfiguration.ProfilePic(
                    TEI_UID_IMAGE,
                    "",
                ),
                title = "",
                description = null,
                lastUpdated = "",
                additionalInfoList = emptyList(),
                isOnline = false,
                geometry = Geometry.builder()
                    .type(FeatureType.POINT)
                    .coordinates("[$POINT_LONGITUDE, $POINT_LATITUDE]")
                    .build(),
                relatedInfo = RelatedInfo(
                    enrollment = RelatedInfo.Enrollment(
                        uid = ENROLLMENT_UID,
                        geometry = Geometry.builder()
                            .type(FeatureType.POINT)
                            .coordinates("[$POINT_LONGITUDE_ENROLLMENT, $POINT_LATITUDE_ENROLLMENT]")
                            .build(),
                    ),
                ),
                state = State.SYNCED,
            ),
        )
    }

    private fun createSearchTeiModelWithRelationships(): List<MapItemModel> {
        return listOf(
            MapItemModel(
                uid = TEI_UID,
                avatarProviderConfiguration = AvatarProviderConfiguration.ProfilePic(
                    TEI_UID_IMAGE,
                    "",
                ),
                title = "",
                description = null,
                lastUpdated = "",
                additionalInfoList = emptyList(),
                isOnline = false,
                geometry = Geometry.builder()
                    .type(FeatureType.POINT)
                    .coordinates("[$POINT_LONGITUDE, $POINT_LATITUDE]")
                    .build(),
                relatedInfo = RelatedInfo(
                    relationship = RelatedInfo.Relationship(
                        uid = RelationshipViewModelDummy.UID,
                        displayName = RelationshipViewModelDummy.DISPLAY_NAME,
                        relationshipTypeUid = RelationshipViewModelDummy.RELATIONSHIP_TYPE,
                        relatedUid = RelationshipViewModelDummy.UID,
                        relationshipDirection = RelationshipDirection.FROM,
                    ),
                ),
                state = State.SYNCED,
            ),
            MapItemModel(
                uid = TEI_UID + "_2",
                avatarProviderConfiguration = AvatarProviderConfiguration.ProfilePic(
                    TEI_UID_IMAGE + "_2",
                    "",
                ),
                title = "",
                description = null,
                lastUpdated = "",
                additionalInfoList = emptyList(),
                isOnline = false,
                geometry = Geometry.builder()
                    .type(FeatureType.POINT)
                    .coordinates("[$POINT_LONGITUDE_ENROLLMENT, ${POINT_LATITUDE_ENROLLMENT}]")
                    .build(),
                relatedInfo = RelatedInfo(
                    relationship = RelatedInfo.Relationship(
                        uid = RelationshipViewModelDummy.UID,
                        displayName = RelationshipViewModelDummy.DISPLAY_NAME,
                        relationshipTypeUid = RelationshipViewModelDummy.RELATIONSHIP_TYPE,
                        relatedUid = RelationshipViewModelDummy.UID,
                        relationshipDirection = RelationshipDirection.TO,
                    ),
                ),
                state = State.SYNCED,
            ),
        )
    }

    companion object {
        const val POINT_LONGITUDE = 43.34532
        const val POINT_LATITUDE = -23.98234
        const val POINT_LONGITUDE_ENROLLMENT = 47.34532
        const val POINT_LATITUDE_ENROLLMENT = -27.98234
        const val TEI_UID = "123"
        const val TEI_UID_IMAGE = "/random/path"
        const val ENROLLMENT_UID = "enrollment_uid"
    }
}
