package org.dhis2.usescases.searchTrackEntity

import android.os.Bundle
import org.dhis2.commons.Constants

enum class SearchTEExtra(val key: String) {
    TEI_UID("TRACKED_ENTITY_UID"),
    PROGRAM_UID("PROGRAM_UID"),
    QUERY_ATTR("QUERY_DATA_ATTR"),
    QUERY_VALUES("QUERY_DATA_VALUES"),
}

fun SearchTEActivity.teiUidExtra() = intent.getStringExtra(SearchTEExtra.TEI_UID.key)

fun SearchTEActivity.programUidExtra() = intent.getStringExtra(SearchTEExtra.PROGRAM_UID.key)

fun SearchTEActivity.queryDataExtra(savedInstanceState: Bundle?): Map<String, String> {
    return when {
        savedInstanceState == null -> {
            val attributes =
                intent.getStringArrayListExtra(SearchTEExtra.QUERY_ATTR.key)
                    ?.toList() ?: emptyList()
            val values =
                intent.getStringArrayListExtra(SearchTEExtra.QUERY_VALUES.key)
                    ?.toList() ?: emptyList()
            if (attributes.size != values.size) return emptyMap()
            attributes.mapIndexed { index, attributeUid ->
                attributeUid to values[index]
            }.toMap()
        }
        savedInstanceState.containsKey(Constants.QUERY_DATA) -> {
            savedInstanceState.getSerializable(Constants.QUERY_DATA) as Map<String, String>
        }
        else -> {
            emptyMap()
        }
    }
}
