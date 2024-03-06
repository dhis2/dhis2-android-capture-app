package org.dhis2.android.rtsm.services

import io.reactivex.Single
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program

interface MetadataManager {
    fun stockManagementProgram(programUid: String): Single<Program?>
    fun facilities(programUid: String): Single<MutableList<OrganisationUnit>>
    fun destinations(distributedTo: String): Single<List<Option>>
}
