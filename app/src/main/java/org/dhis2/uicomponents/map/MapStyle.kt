package org.dhis2.uicomponents.map

import android.graphics.drawable.Drawable

data class MapStyle(
    var teiColor: Int,
    var teiSymbolIcon: Drawable,
    var enrollmentColor: Int,
    var enrollmentSymbolIcon: Drawable,
    var programDarkColor: Int
)
