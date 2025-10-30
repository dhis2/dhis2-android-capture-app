package org.dhis2.usescases.main.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.data.HomeRepository

class GetUserName(
    private val homeRepository: HomeRepository,
) : UseCase<Unit, String> {
    override suspend operator fun invoke(input: Unit) =
        try {
            val user = homeRepository.user()
            val userName = "${user?.firstName()} ${user?.surname()}"
            Result.success(userName)
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }
}
