package org.dhis2.uicomponents.map.model

import android.graphics.drawable.Drawable

data class MapStyle(
    var teiColor: Int,
    var teiSymbolIcon: Drawable?,
    var enrollmentColor: Int,
    var enrollmentSymbolIcon: Drawable?,
    var stagesStyle: HashMap<String, StageStyle>,
    var programDarkColor: Int
)

data class StageStyle(
    val stageColor: Int,
    val stageIcon: Drawable
)
