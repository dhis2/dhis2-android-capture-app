package org.dhis2.usescases.orgunitselector

import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class OUTreeRepository(private val d2: D2) {

    fun orgUnits(
        parentUid: String? = null,
        name: String? = null
    ): Single<MutableList<OrganisationUnit>> {
        var orgUnitRepository = d2.organisationUnitModule().organisationUnits()
            .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)

        orgUnitRepository = when {
            parentUid != null -> orgUnitRepository.byParentUid().eq(parentUid)
            name != null -> orgUnitRepository.byDisplayName().like("%$name%")
            else -> orgUnitRepository.byRootOrganisationUnit(true)
        }

        return when {
            orgUnitRepository
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                .blockingCount() > 0 ->
                orgUnitRepository
                    .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                    .get()
            else ->
                orgUnitRepository
                    .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                    .get()
        }
    }
    fun orgUnit(uid: String): OrganisationUnit? =
        d2.organisationUnitModule().organisationUnits().uid(uid).blockingGet()

    fun orgUnitHasChildren(uid: String): Boolean =
        !d2.organisationUnitModule().organisationUnits().byParentUid().eq(uid).blockingIsEmpty()
}
