package org.dhis2.android.rtsm.services

import io.reactivex.Single
import org.dhis2.android.rtsm.exceptions.InitializationException
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import javax.inject.Inject

class MetadataManagerImpl @Inject constructor(
    private val d2: D2,
) : MetadataManager {

    override fun stockManagementProgram(programUid: String): Single<Program?> {
        if (programUid.isBlank()) {
            throw InitializationException(
                "The program config has not been set in the configuration file",
            )
        }

        return d2.programModule()
            .programs()
            .byUid()
            .eq(programUid)
            .one()
            .get()
    }

    override fun transactionType(dataSetUid: String): Single<DataElement> {
        return Single.defer {
            d2.dataElementModule().dataElements().uid(dataSetUid).get()
        }
    }

    /**
     * Get the program OUs which the user has access to and also
     * set as the user's the data capture OU. This is simply the
     * intersection of the program OUs (without DESCENDANTS) and
     * the user data capture OUs (with DESCENDANTS)
     */
    override fun facilities(programUid: String): Single<List<OrganisationUnit>> {
        return Single.defer {
            stockManagementProgram(programUid).map { program ->
                // TODO: Flag situations where the intersection is nil (i.e. no facility obtained)
                d2.organisationUnitModule()
                    .organisationUnits()
                    .byOrganisationUnitScope(
                        OrganisationUnit.Scope.SCOPE_DATA_CAPTURE,
                    )
                    .byProgramUids(listOf(program.uid()))
                    .blockingGet()
            }
        }
    }

    override fun destinations(distributedTo: String): Single<List<Option>> {
        return Single.defer {
            d2.dataElementModule()
                .dataElements()
                .uid(distributedTo)
                .get()
                .flatMap {
                    d2.optionModule()
                        .options()
                        .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                        .byOptionSetUid()
                        .eq(it.optionSetUid())
                        .get()
                }
        }
    }
}
