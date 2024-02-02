package org.dhis2.usescases.searchTrackEntity.searchparameters.model

import org.hisp.dhis.android.core.common.ValueType

data class SearchParameter(
    val uid: String,
    val label: String,
    val valueType: ValueType,
    val helper: String = "Optional",
    val value: String? = null,
)
