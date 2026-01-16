package org.dhis2.mobile.commons.domain

/**
 * A generic use case interface representing an operation that takes input of type [R]
 * and returns a result of type [T] wrapped in a [Result] object.
 *
 * @param R The type of the input parameters.
 * @param T The type of the result.
 */
fun interface UseCase<in R, out T> {
    suspend operator fun invoke(input: R): Result<T>
}

// Extension for use it without parameters
suspend operator fun <T> UseCase<Unit, T>.invoke() = this(Unit)
