package org.dhis2.android.rtsm.data

sealed class OperationState<out T: Any> {
    object Loading: OperationState<Nothing>()
    object Empty: OperationState<Nothing>()
    object NotFound: OperationState<Nothing>()
    object Completed: OperationState<Nothing>()
    data class Success<out T: Any>(val result: T): OperationState<T>()
    data class Error(val errorStringRes: Int): OperationState<Nothing>()
}
