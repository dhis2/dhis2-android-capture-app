package org.dhis2.commons.data

data class TeiAttributesInfo(
    val attributes: List<String>,
    val profileImage: String,
    val teTypeName: String,
) {
    private val attrListNotEmpty = attributes.filter { it.isNotEmpty() }

    fun teiMainLabel(formattedLabel: String?): String {
        return when (attrListNotEmpty.size) {
            0 -> formattedLabel?.format(teTypeName) ?: teTypeName
            1 -> attrListNotEmpty[0]
            else -> String.format("%s %s", attrListNotEmpty[0], attrListNotEmpty[1])
        }
    }

    fun teiSecondaryLabel(): String? {
        return when (attrListNotEmpty.size) {
            0, 1, 2 -> null
            else -> attrListNotEmpty[2]
        }
    }
}
