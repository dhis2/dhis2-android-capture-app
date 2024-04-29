package org.dhis2.data.forms.dataentry

interface SearchTEIRepository {
    fun isUniqueTEIAttributeOnline(
        uid: String,
        value: String?,
        teiUid: String,
        programUid: String?,
    ): Boolean
}
