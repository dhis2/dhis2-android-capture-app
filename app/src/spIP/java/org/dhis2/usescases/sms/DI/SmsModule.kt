package org.dhis2.usescases.sms.DI

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.dhis2.usescases.sms.data.api.ConstantApi
import org.dhis2.usescases.sms.data.api.ConstantApiImpl
import org.dhis2.usescases.sms.data.api.OutboundApi
import org.dhis2.usescases.sms.data.api.OutboundApiImpl
import org.dhis2.usescases.sms.data.repository.message.MessageTemplateD2Repository
import org.dhis2.usescases.sms.data.repository.patient.PatientD2Repository
import org.dhis2.usescases.sms.data.repository.preferred.PreferredLanguageD2Repository
import org.dhis2.usescases.sms.data.repository.sms.SmsApiRepository
import org.dhis2.usescases.sms.domain.repository.message.MessageTemplateRepository
import org.dhis2.usescases.sms.domain.repository.patient.PatientRepository
import org.dhis2.usescases.sms.domain.repository.preferred.PreferredLanguageRepository
import org.dhis2.usescases.sms.domain.repository.sms.SmsRepository
import org.dhis2.usescases.teiDashboard.TeiDashboardMenuCustomActionsManager
import org.dhis2.usescases.teiDashboard.ui.TeiDashboardMenuCustomActionsManagerImpl
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.arch.api.HttpServiceClient

@Module
@InstallIn(SingletonComponent::class)
abstract class SmsModule {

  @Binds
  abstract fun ZbindPatientRepository(impl: PatientD2Repository): PatientRepository

  @Binds
  abstract fun bindMessageTemplateRepository(impl: MessageTemplateD2Repository): MessageTemplateRepository

  @Binds
  abstract fun bindPreferredLanguageRepository(impl: PreferredLanguageD2Repository): PreferredLanguageRepository

  @Binds
  abstract fun bindSmsRepository(impl: SmsApiRepository): SmsRepository

  @Binds
  abstract fun bindOutboundApi(impl: OutboundApiImpl): OutboundApi

  @Binds
  abstract fun bindConstantApi(impl: ConstantApiImpl): ConstantApi

  @Binds
  abstract fun bindTeiDashboardMenuCustomActions(impl: TeiDashboardMenuCustomActionsManagerImpl): TeiDashboardMenuCustomActionsManager

  companion object {

    @Provides
    fun provideHttpServiceClient(): HttpServiceClient {
      return D2Manager.getD2().httpServiceClient()
    }
  }
}