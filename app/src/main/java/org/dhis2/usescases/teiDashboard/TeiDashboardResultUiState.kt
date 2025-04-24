package org.dhis2.usescases.teiDashboard

sealed class TeiDashboardResultUiState<out T> {
  object Loading : TeiDashboardResultUiState<Nothing>()
  data class Success<out T>(val data: T) : TeiDashboardResultUiState<T>()
  data class Error(val message: String, val throwable: Throwable? = null) : TeiDashboardResultUiState<Nothing>()
}