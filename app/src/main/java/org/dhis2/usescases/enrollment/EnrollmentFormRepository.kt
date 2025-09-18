package org.dhis2.usescases.enrollment

import io.reactivex.Single

interface EnrollmentFormRepository {
    fun generateEvents(): Single<Pair<String, String?>>

    fun getProfilePicture(): String

    fun getProgramStageUidFromEvent(eventUi: String): String?

    fun hasWriteAccess(): Boolean
}
