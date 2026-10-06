package org.dhis2.usescases.settings.domain

import com.google.gson.Gson
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.settings.models.ErrorViewModel

class GetSharedData : UseCase<List<ErrorViewModel>, String> {
    override suspend fun invoke(input: List<ErrorViewModel>): Result<String> =
        try {
            Result.success(
                Gson().toJson(input.filter { it.isSelected }),
            )
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }
}
