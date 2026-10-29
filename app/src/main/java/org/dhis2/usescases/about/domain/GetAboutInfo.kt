package org.dhis2.usescases.about.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.usescases.about.model.AboutInfo

class GetAboutInfo(
    private val repository: AboutRepository,
) : UseCase<Unit, AboutInfo> {
    override suspend fun invoke(input: Unit): Result<AboutInfo> =
        try {
            Result.success(repository.aboutInfo())
        } catch (e: Exception) {
            Result.failure(e)
        }
}
