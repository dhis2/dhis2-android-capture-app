package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoenrollment.model

import com.google.gson.Gson
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoEnrollment.model.AutoEnrollments
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoEnrollment.model.Configurations
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoEnrollment.model.ConstraintsDataElement
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoEnrollment.model.TargetProgsItem

data class AutoEnrollmentConfig(
    val configurations: Configurations
) {
    companion object {
        fun createDefaultAutoEnrollmentConfigObject(): String {
            val disableProgrEnrollement = listOf("djdkNbk")
            val sourceProgram = "DEFAULT"
            val targetItem = TargetProgsItem(
                constraintsDataElements = listOf(
                    ConstraintsDataElement(
                        "urir",
                        "false"
                    )
                ), ids = listOf("HDJDj")
            )
            val autoEnrollments = AutoEnrollments(
                disableProgrEnrollement,
                sourceProgram,
                targetPrograms = arrayListOf(targetItem)
            )
            val configurations = Configurations(autoEnrollments)
            val defaultEnrollementConfig = AutoEnrollmentConfig(configurations)
            return Gson().toJson(defaultEnrollementConfig)
        }
    }
}