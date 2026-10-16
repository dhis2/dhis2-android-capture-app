package org.dhis2.mobile.sync.data

import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.error.withDomainErrors
import org.dhis2.mobile.sync.domain.DataSetUid
import org.hisp.dhis.android.core.D2

internal class SyncDataSetRepositoryImpl(
    private val d2: D2,
    private val domainErrorMapper: DomainErrorMapper,
) : SyncDataSetRepository {
    override suspend fun uploadDataSet(dataSetUid: DataSetUid) {
        domainErrorMapper.withDomainErrors {
            d2
                .dataSetModule()
                .dataSetInstances()
                .byDataSetUid()
                .eq(dataSetUid)
                .blockingGet()
                .forEach { dataSetInstance ->
                    d2
                        .dataValueModule()
                        .dataValues()
                        .byOrganisationUnitUid()
                        .eq(dataSetInstance.organisationUnitUid())
                        .byPeriod()
                        .eq(dataSetInstance.period())
                        .byAttributeOptionComboUid()
                        .eq(dataSetInstance.attributeOptionComboUid())
                        .blockingUpload()
                }
        }
    }

    override suspend fun uploadCompleteRegistration(dataSetUid: DataSetUid) {
        domainErrorMapper.withDomainErrors {
            d2
                .dataSetModule()
                .dataSetCompleteRegistrations()
                .byDataSetUid()
                .eq(dataSetUid)
                .blockingUpload()
        }
    }

    // TODO: The sdk does not provide methods to download data values for a given dataset
    override suspend fun downloadDataSet(dataSetUid: DataSetUid) {
        // Do nothing
    }
}
