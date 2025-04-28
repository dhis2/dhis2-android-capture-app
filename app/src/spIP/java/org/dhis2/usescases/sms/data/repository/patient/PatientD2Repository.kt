package org.dhis2.usescases.sms.data.repository.patient

import org.dhis2.usescases.sms.domain.model.patient.Patient
import org.dhis2.usescases.sms.domain.repository.patient.PatientRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

const val numberAtt = "Pntz2rubsPu"
const val firstNameAtt = "SinKvMFe2mD"
const val lastNameAtt = "nOguXiyCUSv"
const val phoneAtt = "yXS3uuFF5ul"
const val preferredLanguageAtt = "VnOpPm1uZJR"

class PatientD2Repository(
  private val d2: D2
) : PatientRepository {

  /**
   * This repository is responsible for fetching patient data from the D2 database.
   * It uses the D2 API to retrieve the tracked entity instance (TEI) associated with a given UID.
   * The TEI contains various attributes that represent patient information.
   *
   * @param d2 The D2 instance used to interact with the DHIS2 database.
   */
  override fun getByUid(uid: String): Patient {
    val tei = d2.trackedEntityModule().trackedEntityInstances()
      .withTrackedEntityAttributeValues().uid(uid).blockingGet()
      ?: throw IllegalArgumentException("No TEI found with uid: $uid")
    return buildPatient(tei)
  }

  /**
   * This function constructs a Patient object from the provided TrackedEntityInstance (TEI).
   * It retrieves various attributes from the TEI and uses them to populate the Patient object.
   *
   * @param tei The TrackedEntityInstance object containing patient data.
   * @return A Patient object populated with data from the TEI.
   */
  private fun buildPatient(
    tei: TrackedEntityInstance
  ): Patient {
    val number = getAttributeValue(tei, numberAtt)
    val firstName = getAttributeValue(tei, firstNameAtt)
    val lastName = getAttributeValue(tei, lastNameAtt)
    val phone = getAttributeValue(tei, phoneAtt)
    val preferredLanguage = getAttributeValue(tei, preferredLanguageAtt)

    return Patient(
      uid = tei.uid(),
      number = number,
      name = "$firstName $lastName",
      phone = phone,
      preferredLanguage = preferredLanguage
    )
  }

  /**
   * This function retrieves the value of a specific attribute from the TrackedEntityInstance (TEI).
   * It searches for the attribute by its UID and returns its value.
   *
   * @param tei The TrackedEntityInstance object containing patient data.
   * @param attribute The UID of the attribute to retrieve.
   * @return The value of the specified attribute, or an empty string if not found.
   */
  private fun getAttributeValue(
    tei: TrackedEntityInstance,
    attribute: String
  ) = tei.trackedEntityAttributeValues()
    ?.find { it.trackedEntityAttribute() == attribute }
    ?.value()
    ?: ""

}