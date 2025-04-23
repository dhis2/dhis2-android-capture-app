package org.dhis2.usescases.sms.domain.types

sealed class Maybe<out T> {

  data class Some<out T>(val value: T) : Maybe<T>()
  data object None : Maybe<Nothing>()

  fun getOrThrow(): T = when (this) {
    is Some -> value
    is None -> throw NoSuchElementException("No value present")
  }

  fun isSome(): Boolean = this is Some
  fun isNone(): Boolean = this is None

  companion object {
    fun <T> of(value: T?): Maybe<T> = if (value != null) Some(value) else None
  }
}