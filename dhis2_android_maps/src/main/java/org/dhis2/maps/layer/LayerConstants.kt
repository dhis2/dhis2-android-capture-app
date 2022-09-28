package org.dhis2.maps.layer

import android.graphics.Color
import com.mapbox.maps.extension.style.expressions.dsl.generated.literal
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import org.dhis2.maps.geometry.TEI_UID
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.managers.EventMapManager

val TEI_ICON_OFFSET = arrayOf(0f, -14.5f)

fun isPoint(): Expression = Expression.eq(
    Expression.literal(TYPE),
    Expression.literal(TYPE_POINT)
)

fun isPolygon(): Expression = Expression.eq(
    Expression.literal(TYPE),
    Expression.literal(TYPE_POLYGON)
)

fun isLine(): Expression = Expression.eq(
    Expression.literal(TYPE),
    Expression.literal(TYPE_LINE)
)

fun isBiderectional(): Expression = Expression.eq(
    Expression.get(MapRelationshipsToFeatureCollection.BIDIRECTIONAL),
    literal(true)
)

fun isUnidirectional(): Expression = Expression.eq(
    Expression.get(MapRelationshipsToFeatureCollection.BIDIRECTIONAL),
    literal(false)
)

fun SymbolLayer.withTEIMarkerProperties(): SymbolLayer = apply {
    iconImage(Expression.get(TEI_UID))
        .iconOffset(TEI_ICON_OFFSET.map { it.toDouble() }.toList())
        .iconAllowOverlap(true)
        .textAllowOverlap(true)
}

fun SymbolLayer.withInitialVisibility(visibility: Visibility): SymbolLayer = apply {
    visibility(visibility)
}

fun SymbolLayer.withDEIconAndTextProperties(): SymbolLayer = apply {
    iconImage("${EventMapManager.DE_ICON_ID}_$sourceId")
        .iconAllowOverlap(true)
        .textField(Expression.get(MapCoordinateFieldToFeatureCollection.FIELD_NAME))
        .textAllowOverlap(false)
        .textIgnorePlacement(true)
        .textAnchor(TextAnchor.TOP)
        .textRadialOffset(2.0)
        .textHaloWidth(1.0)
        .textHaloColor(Color.WHITE)
        .textSize(10.0)
}
