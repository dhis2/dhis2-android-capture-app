package org.dhis2.uicomponents.map.layer

import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.Property.VISIBILITY
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection.Companion.TEI_UID

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
    true
)

fun isUnidirectional(): Expression = Expression.eq(
    Expression.get(MapRelationshipsToFeatureCollection.BIDIRECTIONAL),
    false
)

fun SymbolLayer.withTEIMarkerProperties(): SymbolLayer = withProperties(
    PropertyFactory.iconImage(Expression.get(TEI_UID)),
    PropertyFactory.iconOffset(TEI_ICON_OFFSET),
    PropertyFactory.iconAllowOverlap(true),
    PropertyFactory.textAllowOverlap(true)
)

fun SymbolLayer.withInitialVisibility(@VISIBILITY visibility: String): SymbolLayer = withProperties(
    PropertyFactory.visibility(visibility)
)
