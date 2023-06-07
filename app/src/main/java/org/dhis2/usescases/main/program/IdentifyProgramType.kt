package org.dhis2.usescases.main.program

internal class IdentifyProgramType(
    val repository: ProgramThemeRepository
) {

    operator fun invoke(programUid: String) = if (repository.isStockTheme(programUid)) {
        HomeItemType.PROGRAM_STOCK
    } else {
        HomeItemType.PROGRAM
    }
}
