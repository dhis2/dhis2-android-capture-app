package org.dhis2.commons.orgunitselector

import org.dhis2.mobile.commons.data.OrgUnitTreeRepository
import org.dhis2.mobile.commons.model.internal.DomainOrgUnit
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class OUTreeRepository(
    private val orgUnitRepositoryConfiguration: OURepositoryConfiguration,
) : OrgUnitTreeRepository {
    private var availableOrgUnits: List<DomainOrgUnit> = emptyList()

    override suspend fun orgUnits(name: String?): List<DomainOrgUnit> {
        availableOrgUnits =
            orgUnitRepositoryConfiguration.orgUnitRepository(name).map { orgUnit ->
                DomainOrgUnit(
                    uid = orgUnit.uid,
                    label = orgUnit.displayName ?: orgUnit.uid,
                    level = orgUnit.level ?: 0,
                    path = orgUnit.path ?: "",
                    namePath = orgUnit.displayNamePath,
                )
            }
        return availableOrgUnits.order().sortedBy { it.namePath.joinToString(" ") }
    }

    override suspend fun childrenOrgUnits(parentUid: String): List<DomainOrgUnit> =
        availableOrgUnits
            .filter { it.uid != parentUid && it.path.contains(parentUid) }
            .sortedBy { it.namePath.joinToString(" ") }

    override suspend fun orgUnit(uid: String): DomainOrgUnit? = availableOrgUnits.firstOrNull { it.uid == uid }

    override suspend fun canBeSelected(orgUnitUid: String): Boolean = availableOrgUnits.any { it.uid == orgUnitUid }

    override suspend fun orgUnitHasChildren(uid: String): Boolean = availableOrgUnits.filter { it.uid != uid }.any { it.path.contains(uid) }

    override suspend fun countSelectedChildren(
        parentOrgUnitUid: String,
        selectedOrgUnits: List<String>,
    ): Int =
        orgUnitRepositoryConfiguration.countChildren(
            parentOrgUnitUid,
            selectedOrgUnits,
        )

    private fun List<DomainOrgUnit>.order(): List<DomainOrgUnit> {
        val listWithParents = this.toMutableList()
        val minLevel = minOf { it.level }
        this.forEach { organisationUnit ->
            var isParentInParentList = false
            organisationUnit.path
                .split("/")
                .filter { it.isNotEmpty() && it != organisationUnit.uid }
                .forEach { parentUid ->
                    if (listWithParents.any { it.uid == parentUid }) {
                        isParentInParentList = true
                    }
                }
            if (!isParentInParentList && listWithParents.indexOf(organisationUnit) != 0) {
                listWithParents.remove(organisationUnit)
                listWithParents.add(0, organisationUnit.copy(level = minLevel))
            }
        }
        return listWithParents
    }

    suspend fun legacyOrgUnit(uid: String): OrganisationUnit? = orgUnitRepositoryConfiguration.orgUnit(uid)
}
