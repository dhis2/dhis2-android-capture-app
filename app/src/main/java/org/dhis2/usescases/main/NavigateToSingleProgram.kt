package org.dhis2.usescases.main

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.usescases.main.program.ProgramRepository

class NavigateToSingleProgram(
    private val programRepository: ProgramRepository,
) : UseCase<Int, Boolean> {
    override suspend fun invoke(input: Int): Result<Boolean> =
        if (input == 1 && !programRepository.isSingleNavigationDone()) {
            programRepository.setSingleNavigationDone()
            Result.success(true)
        } else {
            Result.success(false)
        }
}
