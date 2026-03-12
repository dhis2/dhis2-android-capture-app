package org.dhis2.maps.usecases

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.filters.internal.EnumFilterConnector
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector
import org.hisp.dhis.android.core.map.layer.MapLayer
import org.hisp.dhis.android.core.map.layer.MapLayerCollectionRepository
import org.hisp.dhis.android.core.map.layer.MapLayerPosition
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

private val stringFilterConnector: StringFilterConnector<MapLayerCollectionRepository> = mock()
private val enumFilterConnector: EnumFilterConnector<MapLayerCollectionRepository, MapLayerPosition> =
    mock()

fun mockBasemaps(
    d2: D2,
    mockedBasemap: List<MapLayer>,
) {
    val mockedMapLayerCollectionRepository: MapLayerCollectionRepository = mock()

    whenever(d2.mapsModule().mapLayers()) doReturn mockedMapLayerCollectionRepository
    whenever(mockedMapLayerCollectionRepository.withImageryProviders()) doReturn mockedMapLayerCollectionRepository
    whenever(
        mockedMapLayerCollectionRepository.byMapLayerPosition(),
    ) doReturn enumFilterConnector
    whenever(
        enumFilterConnector.eq(MapLayerPosition.BASEMAP),
    ) doReturn mockedMapLayerCollectionRepository
    whenever(
        mockedMapLayerCollectionRepository.blockingGet(),
    ) doReturn mockedBasemap
}

fun mockOverlays(
    d2: D2,
    mockedOverlays: List<MapLayer>,
) {
    val mockedMapLayerCollectionRepository: MapLayerCollectionRepository = mock()

    whenever(d2.mapsModule().mapLayers()) doReturn mockedMapLayerCollectionRepository
    whenever(mockedMapLayerCollectionRepository.withImageryProviders()) doReturn mockedMapLayerCollectionRepository
    whenever(
        mockedMapLayerCollectionRepository.byLinkedLayerUid(),
    ) doReturn stringFilterConnector
    whenever(
        stringFilterConnector.eq(any()),
    ) doReturn mockedMapLayerCollectionRepository
    whenever(
        mockedMapLayerCollectionRepository.byMapLayerPosition(),
    ) doReturn enumFilterConnector
    whenever(
        enumFilterConnector.eq(MapLayerPosition.OVERLAY),
    ) doReturn mockedMapLayerCollectionRepository
    whenever(
        mockedMapLayerCollectionRepository.blockingGet(),
    ) doReturn mockedOverlays
}
