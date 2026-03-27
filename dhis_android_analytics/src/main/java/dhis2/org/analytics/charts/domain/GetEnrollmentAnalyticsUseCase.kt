package dhis2.org.analytics.charts.domain

import dhis2.org.analytics.charts.ChartsRepository
import dhis2.org.analytics.charts.data.Graph
import kotlinx.coroutines.withContext
import org.dhis2.commons.viewmodel.DispatcherProvider

class GetEnrollmentAnalyticsUseCase(
    private val chartsRepository: ChartsRepository,
    private val dispatchers: DispatcherProvider,
) {
    suspend operator fun invoke(enrollmentUid: String): Result<List<Graph>> =
        withContext(dispatchers.io()) {
            try {
                Result.success(chartsRepository.getAnalyticsForEnrollment(enrollmentUid))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
