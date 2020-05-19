package org.dhis2.uicomponents.map.geometry.mapper

import com.mapbox.geojson.Feature
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel

fun Feature?.addTeiInfo(searchTeiModel: SearchTeiModel) : Feature? {
    if (this != null){
        addStringProperty(MapTeisToFeatureCollection.TEI_UID, searchTeiModel.tei.uid())
        addStringProperty(MapTeisToFeatureCollection.TEI_IMAGE, searchTeiModel.profilePicturePath)
        if (searchTeiModel.selectedEnrollment != null) {
            addStringProperty(MapTeisToFeatureCollection.ENROLLMENT_UID, searchTeiModel.selectedEnrollment.uid())
        }
    }
    return this
}

fun Feature?.addTeiEnrollmentInfo(searchTeiModel: SearchTeiModel) : Feature? {
    if (this != null) {
        addStringProperty(MapTeisToFeatureCollection.ENROLLMENT_UID, searchTeiModel.selectedEnrollment.uid())
        addStringProperty(MapTeisToFeatureCollection.TEI_UID, searchTeiModel.tei.uid())
    }
    return this
}