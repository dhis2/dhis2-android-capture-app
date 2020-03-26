package org.dhis2.Bindings

import android.content.Context
import android.widget.TextView
import org.hisp.dhis.android.core.dataelement.DataElement

fun List<DataElement>.toDisplayNameList(): List<String> {
    return map { it.displayName() ?: it.uid() }
}

fun List<DataElement>.measureText(context: Context, widthFactor: Int): Triple<String, Int, Int> {
    return toDisplayNameList()
        .calculateWidth(context)
        .calculateHeight(context, widthFactor)
}

fun List<String>.calculateWidth(context: Context): Pair<String, Int> {
    var maxLabel = ""
    var minWidth = 0
    forEach { label ->
        TextView(context).apply {
            text = label
            measure(0, 0)
            if (measuredWidth > minWidth) {
                maxLabel = label
                minWidth = measuredWidth
            }
        }
    }
    return Pair(maxLabel, minWidth)
}

fun Pair<String, Int>.calculateHeight(
    context: Context,
    widthFactor: Int
): Triple<String, Int, Int> {
    var minHeight = 0
    val minWidth: Int
    val currentWidth = context.resources.displayMetrics.widthPixels
    if (second > currentWidth / widthFactor) {
        minWidth = currentWidth / widthFactor
        TextView(context).apply {
            width = minWidth
            text = first
            measure(0, 0)
            minHeight = measuredHeight
        }
    } else {
        minWidth = second
    }

    return Triple(first, minWidth, minHeight)
}

fun Pair<String, Int>.calculateHeight(
    context: Context
): Triple<String, Int, Int> {
    var minHeight = 0
    TextView(context).apply {
        width = second
        text = first
        measure(0, 0)
        minHeight = measuredHeight
    }

    return Triple(first, second, minHeight)
}