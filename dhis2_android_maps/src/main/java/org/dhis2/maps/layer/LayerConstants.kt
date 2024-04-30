package org.dhis2.maps.layer

import android.graphics.Color
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.Property.VISIBILITY
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import org.dhis2.maps.geometry.TEI_UID
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.layer.basemaps.DEFAULT_FONT
import org.dhis2.maps.managers.EventMapManager

val TEI_ICON_OFFSET = arrayOf(0f, -14.5f)

fun isPoint(): Expression = Expression.eq(
    Expression.literal(TYPE),
    Expression.literal(TYPE_POINT),
)

fun isPolygon(): Expression = Expression.eq(
    Expression.literal(TYPE),
    Expression.literal(TYPE_POLYGON),
)

fun isLine(): Expression = Expression.eq(
    Expression.literal(TYPE),
    Expression.literal(TYPE_LINE),
)

fun isBiderectional(): Expression = Expression.eq(
    Expression.get(MapRelationshipsToFeatureCollection.BIDIRECTIONAL),
    true,
)

fun isUnidirectional(): Expression = Expression.eq(
    Expression.get(MapRelationshipsToFeatureCollection.BIDIRECTIONAL),
    false,
)

fun SymbolLayer.withTEIMarkerProperties(): SymbolLayer = withProperties(
    PropertyFactory.iconImage(Expression.get(TEI_UID)),
    PropertyFactory.iconOffset(org.dhis2.maps.layer.TEI_ICON_OFFSET),
    PropertyFactory.iconAllowOverlap(true),
    PropertyFactory.textAllowOverlap(true),
)

fun SymbolLayer.withInitialVisibility(@VISIBILITY visibility: String): SymbolLayer = withProperties(
    PropertyFactory.visibility(visibility),
)

fun SymbolLayer.withDEIconAndTextProperties(): SymbolLayer = withProperties(
    PropertyFactory.iconImage("${EventMapManager.DE_ICON_ID}_$sourceId"),
    PropertyFactory.iconAllowOverlap(true),
    PropertyFactory.textFont(arrayOf(DEFAULT_FONT)),
    PropertyFactory.textColor(Color.BLACK),
    PropertyFactory.textField(Expression.get(MapCoordinateFieldToFeatureCollection.FIELD_NAME)),
    PropertyFactory.textAllowOverlap(false),
    PropertyFactory.textIgnorePlacement(true),
    PropertyFactory.textAnchor(Property.TEXT_ANCHOR_TOP),
    PropertyFactory.textRadialOffset(2f),
    PropertyFactory.textHaloWidth(1f),
    PropertyFactory.textHaloColor(Color.WHITE),
    PropertyFactory.textSize(10f),
)
