package org.dhis2.commons.orgunitselector

import org.dhis2.commons.bindings.addIf
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitCollectionRepository

class OUTreeRepository(
    private val d2: D2,
    private val orgUnitSelectorScope: OrgUnitSelectorScope
) {
    private var cachedOrgUnits: List<String> = emptyList()
    private var availableOrgUnits: List<String> = emptyList()

    fun orgUnits(name: String? = null): List<String> {
        var orgUnitRepository = d2.organisationUnitModule().organisationUnits()
            .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)

        orgUnitRepository = when {
            !name.isNullOrEmpty() -> orgUnitRepository.byDisplayName().like("%$name%")
            else -> orgUnitRepository
        }

        orgUnitRepository = when (orgUnitSelectorScope) {
            is OrgUnitSelectorScope.DataSetCaptureScope,
            is OrgUnitSelectorScope.ProgramCaptureScope,
            is OrgUnitSelectorScope.UserCaptureScope ->
                applyCaptureFilter(orgUnitRepository)
            is OrgUnitSelectorScope.ProgramSearchScope,
            is OrgUnitSelectorScope.DataSetSearchScope,
            is OrgUnitSelectorScope.UserSearchScope ->
                applySearchFilter(orgUnitRepository)
        }

        orgUnitRepository = when (orgUnitSelectorScope) {
            is OrgUnitSelectorScope.DataSetCaptureScope,
            is OrgUnitSelectorScope.DataSetSearchScope ->
                orgUnitRepository.byDataSetUids(listOf(orgUnitSelectorScope.uid))
            is OrgUnitSelectorScope.ProgramCaptureScope,
            is OrgUnitSelectorScope.ProgramSearchScope ->
                orgUnitRepository.byProgramUids(listOf(orgUnitSelectorScope.uid))
            is OrgUnitSelectorScope.UserCaptureScope,
            is OrgUnitSelectorScope.UserSearchScope ->
                orgUnitRepository
        }

        val orderedList = mutableListOf<String>()
        val orgUnits = orgUnitRepository.blockingGet()
        val minLevel = orgUnits.minOfOrNull { it.level() ?: 0 }
        availableOrgUnits = orgUnits.map { it.uid() }
        cachedOrgUnits = availableOrgUnits
        orderList(
            orgUnits.filter { it.level() == minLevel },
            orderedList
        )
        /*orgUnits.filter { it.level() == minLevel }.forEach {org->
            org.path()?.split("/")
                ?.filter { it.isNotEmpty() }
                ?.forEach { str ->
                    orderedList.addIf(!orderedList.contains(str), str)
                }
            if (orgUnitHasChildren(org.uid())){
                childrenOrgUnits()
            }
        }*/
        /*orgUnits.forEach { org ->
            org.path()?.split("/")
                ?.filter { it.isNotEmpty() }
                ?.forEach { str ->
                    orderedList.addIf(!orderedList.contains(str), str)
                }
            orderedList.addIf(!orderedList.contains(org.uid()), org.uid())
        }*/
        cachedOrgUnits = availableOrgUnits

        return orderedList
    }

    private fun orderList(
        orgUnitToOrder: List<OrganisationUnit>,
        orderedList: MutableList<String>
    ) {
        orgUnitToOrder.forEach { org ->
            org.path()?.split("/")
                ?.filter { it.isNotEmpty() }
                ?.forEach { str ->
                    orderedList.addIf(!orderedList.contains(str), str)
                }
            if (orgUnitHasChildren(org.uid())) {
                orderList(childrenOrgUnits(org.uid()), orderedList)
            }
        }
    }

    fun childrenOrgUnits(parentUid: String? = null): List<OrganisationUnit> {
        val childrenOrgUnits = d2.organisationUnitModule().organisationUnits()
            .byParentUid().eq(parentUid)
            .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
            .blockingGet()
        return childrenOrgUnits.filter {
            cachedOrgUnits.contains(it.uid())
        }
    }

    fun orgUnit(uid: String): OrganisationUnit? =
        d2.organisationUnitModule().organisationUnits().uid(uid).blockingGet()

    fun canBeSelected(orgUnitUid: String): Boolean =
        availableOrgUnits.any { it == orgUnitUid }

    fun orgUnitHasChildren(uid: String): Boolean {
        var repository = d2.organisationUnitModule().organisationUnits()
            .byParentUid().eq(uid)
        val byScope = availableOrgUnits.contains(uid)

        if (byScope) {
            repository = when (orgUnitSelectorScope) {
                is OrgUnitSelectorScope.DataSetCaptureScope,
                is OrgUnitSelectorScope.DataSetSearchScope ->
                    repository.byDataSetUids(listOf(orgUnitSelectorScope.uid))
                is OrgUnitSelectorScope.ProgramCaptureScope,
                is OrgUnitSelectorScope.ProgramSearchScope ->
                    repository.byProgramUids(listOf(orgUnitSelectorScope.uid))
                is OrgUnitSelectorScope.UserCaptureScope,
                is OrgUnitSelectorScope.UserSearchScope ->
                    repository
            }
        }

        return !repository.blockingIsEmpty()
    }

    fun countSelectedChildren(
        parentOrgUnitUid: String,
        selectedOrgUnits: List<String>
    ): Int {
        return d2.organisationUnitModule().organisationUnits()
            .byPath().like("%$parentOrgUnitUid%")
            .byUid().`in`(selectedOrgUnits).blockingCount()
    }

    private fun applyCaptureFilter(orgUnitRepository: OrganisationUnitCollectionRepository) =
        orgUnitRepository.byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)

    private fun applySearchFilter(orgUnitRepository: OrganisationUnitCollectionRepository) =
        orgUnitRepository
}
