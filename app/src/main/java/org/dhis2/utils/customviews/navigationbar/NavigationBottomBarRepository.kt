package org.dhis2.utils.customviews.navigationbar

interface NavigationBottomBarRepository {
    fun getRelationshipTypeCount(teiUid: String): Int
}
