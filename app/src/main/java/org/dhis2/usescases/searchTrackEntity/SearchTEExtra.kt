package org.dhis2.usescases.searchTrackEntity

enum class SearchTEExtra(val key: String) {
    TEI_UID("TRACKED_ENTITY_UID"),
    PROGRAM_UID("PROGRAM_UID"),
    QUERY_ATTR("QUERY_DATA_ATTR"),
    QUERY_VALUES("QUERY_DATA_VALUES")
}

fun SearchTEActivity.teiUidExtra() =
    intent.getStringExtra(SearchTEExtra.TEI_UID.key)

fun SearchTEActivity.programUidExtra() =
    intent.getStringExtra(SearchTEExtra.PROGRAM_UID.key)

fun SearchTEActivity.queryDataExtra(): Map<String, String> {
    val attributes =
        intent.getStringArrayListExtra(SearchTEExtra.QUERY_ATTR.key)?.toList() ?: emptyList()
    val values =
        intent.getStringArrayListExtra(SearchTEExtra.QUERY_VALUES.key)?.toList() ?: emptyList()
    if (attributes.size != values.size) return emptyMap()
    return attributes.mapIndexed { index, attributeUid ->
        attributeUid to values[index]
    }.toMap()
}
