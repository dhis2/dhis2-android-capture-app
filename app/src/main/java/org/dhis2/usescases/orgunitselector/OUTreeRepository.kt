package org.dhis2.usescases.orgunitselector

import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class OUTreeRepository(private val d2: D2) {

    fun orgUnits(parentUid: String? = null, name: String? = null): Single<MutableList<OrganisationUnit>> {

        val orgUnitRepository = d2.organisationUnitModule().organisationUnits()
            .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
            .byRootOrganisationUnit(true)

        parentUid?.let {
            orgUnitRepository.byParentUid().eq(it)
        }

        name?.let{
            orgUnitRepository.byDisplayName().like("%$name%")

        }

        return when {
            orgUnitRepository
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                .blockingCount() > 0 -> orgUnitRepository
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                .get()
            else -> orgUnitRepository
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .get()
        }
    }

    fun orgUnitHasChildren(uid: String): Boolean =
        !d2.organisationUnitModule().organisationUnits().byParentUid().eq(uid).blockingIsEmpty()

}
