package org.dhis2.usescases.searchTrackEntity

import android.os.Bundle
import org.dhis2.commons.Constants
import kotlin.text.get

enum class SearchTEExtra(
    val key: String,
) {
    TEI_UID("TRACKED_ENTITY_UID"),
    PROGRAM_UID("PROGRAM_UID"),
    QUERY_ATTR("QUERY_DATA_ATTR"),
    QUERY_VALUES("QUERY_DATA_VALUES"),
}

fun SearchTEActivity.teiUidExtra() = intent.getStringExtra(SearchTEExtra.TEI_UID.key)

fun SearchTEActivity.programUidExtra() = intent.getStringExtra(SearchTEExtra.PROGRAM_UID.key)

fun SearchTEActivity.queryDataExtra(savedInstanceState: Bundle?): Map<String, List<String>> {
    return when {
        savedInstanceState == null -> {
            val attributes =
                intent
                    .getStringArrayListExtra(SearchTEExtra.QUERY_ATTR.key)
                    ?.toList() ?: emptyList()
            val values =
                intent
                    .getStringArrayListExtra(SearchTEExtra.QUERY_VALUES.key)
                    ?.toList() ?: emptyList()
            if (attributes.size != values.size) return emptyMap()
            attributes
                .mapIndexed { index, attributeUid ->
                    attributeUid to listOf(values[index])
                }.toMap()
        }
        savedInstanceState.containsKey(Constants.QUERY_DATA) -> {
            @Suppress("UNCHECKED_CAST")
            savedInstanceState.getSerializable(Constants.QUERY_DATA) as Map<String, List<String>>
        }
        else -> {
            emptyMap()
        }
    }
}
