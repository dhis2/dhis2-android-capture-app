package org.dhis2.mobile.commons.data

import org.dhis2.mobile.commons.model.internal.DomainOrgUnit

interface OrgUnitTreeRepository {
    suspend fun orgUnits(name: String? = null): List<DomainOrgUnit>

    suspend fun childrenOrgUnits(parentUid: String): List<DomainOrgUnit>

    suspend fun orgUnit(uid: String): DomainOrgUnit?

    suspend fun canBeSelected(orgUnitUid: String): Boolean

    suspend fun orgUnitHasChildren(uid: String): Boolean

    suspend fun countSelectedChildren(
        parentOrgUnitUid: String,
        selectedOrgUnits: List<String>,
    ): Int
}
