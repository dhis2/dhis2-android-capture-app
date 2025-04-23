package org.dhis2.usescases.sms.domain.repository.patient

import org.dhis2.usescases.sms.domain.model.patient.Patient

interface PatientRepository {
  fun getByUid(uid: String): Patient
}