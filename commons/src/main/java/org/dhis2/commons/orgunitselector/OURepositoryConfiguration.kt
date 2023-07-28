package org.dhis2.commons.orgunitselector

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitCollectionRepository

class OURepositoryConfiguration(
    private val d2: D2,
    private val orgUnitSelectorScope: OrgUnitSelectorScope
) {
    fun orgUnitRepository(name: String?): List<OrganisationUnit> {
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
        return orgUnitRepository.blockingGet()
    }

    fun hasChildren(parentUid: String, byScope: Boolean): Boolean {
        var repository = d2.organisationUnitModule().organisationUnits()
            .byParentUid().eq(parentUid)

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

    fun countChildren(parentOrgUnitUid: String, selectedOrgUnits: List<String>): Int {
        return d2.organisationUnitModule().organisationUnits()
            .byPath().like("%$parentOrgUnitUid%")
            .byUid().`in`(selectedOrgUnits)
            .blockingCount()
    }

    fun orgUnit(uid: String): OrganisationUnit? {
        return d2.organisationUnitModule().organisationUnits().uid(uid).blockingGet()
    }

    fun childrenOrgUnits(parentUid: String? = null): List<OrganisationUnit> =
        d2.organisationUnitModule().organisationUnits()
            .byParentUid().eq(parentUid)
            .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
            .blockingGet()

    private fun applyCaptureFilter(orgUnitRepository: OrganisationUnitCollectionRepository) =
        orgUnitRepository.byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)

    private fun applySearchFilter(orgUnitRepository: OrganisationUnitCollectionRepository) =
        orgUnitRepository
}
