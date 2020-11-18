package org.dhis2.usescases.searchTrackEntity.adapters

fun List<SearchTeiModel>.uids(): List<String> {
    return map { it.tei.uid() }
}
