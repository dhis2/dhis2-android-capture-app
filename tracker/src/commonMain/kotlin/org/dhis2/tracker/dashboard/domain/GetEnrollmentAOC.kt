package org.dhis2.tracker.dashboard.domain

import kotlinx.coroutines.CancellationException
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.tracker.dashboard.data.DashboardRepository
import org.dhis2.tracker.dashboard.domain.model.EnrollmentAOC
import org.dhis2.tracker.dashboard.domain.model.EnrollmentData

/**
 * Returns the attribute option combo of an enrollment, labelled with its program's enrollment
 * category combo, or `null` when the program uses the default category combo.
 */
class GetEnrollmentAOC(
    private val dashboardRepository: DashboardRepository,
) : UseCase<EnrollmentData, EnrollmentAOC?> {
    override suspend fun invoke(input: EnrollmentData): Result<EnrollmentAOC?> =
        try {
            val enrollmentAOC =
                dashboardRepository
                    .enrollmentCategoryComboLabel(input.programUid)
                    ?.let { categoryComboLabel ->
                        EnrollmentAOC(
                            label = categoryComboLabel,
                            value = dashboardRepository.enrollmentCategoryOptionComboLabel(input.attributeOptionCombo),
                        )
                    }
            Result.success(enrollmentAOC)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Result.failure(error)
        }
}
