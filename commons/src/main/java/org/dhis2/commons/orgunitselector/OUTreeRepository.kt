package org.dhis2.commons.orgunitselector

import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitCollectionRepository

class OUTreeRepository(
    private val d2: D2,
    private val orgUnitSelectorScope: OrgUnitSelectorScope
) {

    fun orgUnits(
        parentUid: String? = null,
        name: String? = null
    ): Single<MutableList<OrganisationUnit>> {
        var orgUnitRepository = d2.organisationUnitModule().organisationUnits()
            .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)

        orgUnitRepository = when {
            parentUid != null -> orgUnitRepository.byParentUid().eq(parentUid)
            name != null -> orgUnitRepository.byDisplayName().like("%$name%")
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

        return orgUnitRepository.get()
    }

    fun orgUnit(uid: String): OrganisationUnit? =
        d2.organisationUnitModule().organisationUnits().uid(uid).blockingGet()

    fun orgUnitHasChildren(uid: String): Boolean {
        var repository = d2.organisationUnitModule().organisationUnits()
            .byParentUid().eq(uid)

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
        orgUnitRepository

    private fun applySearchFilter(orgUnitRepository: OrganisationUnitCollectionRepository) =
        orgUnitRepository.byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
}
