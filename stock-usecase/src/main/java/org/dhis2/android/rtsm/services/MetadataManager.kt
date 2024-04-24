package org.dhis2.android.rtsm.services

import io.reactivex.Single
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program

interface MetadataManager {
    fun stockManagementProgram(programUid: String): Single<Program?>
    fun facilities(programUid: String): Single<List<OrganisationUnit>>
    fun destinations(distributedTo: String): Single<List<Option>>
    fun transactionType(dataSetUid: String): Single<DataElement>
}
