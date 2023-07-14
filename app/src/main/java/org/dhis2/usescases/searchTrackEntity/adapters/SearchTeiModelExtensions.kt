package org.dhis2.usescases.searchTrackEntity.adapters

import org.dhis2.commons.data.SearchTeiModel

fun List<SearchTeiModel>.uids(): List<String> {
    return map { it.tei.uid() }
}
