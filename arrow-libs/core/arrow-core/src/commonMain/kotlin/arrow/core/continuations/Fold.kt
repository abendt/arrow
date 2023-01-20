@file:JvmMultifileClass
@file:JvmName("Effect")
@file:OptIn(ExperimentalTypeInference::class)

package arrow.core.continuations

import arrow.atomic.Atomic
import arrow.core.nonFatalOrThrow
import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/**
 * `invoke` the [Effect] and [fold] the result:
 *  - _success_ [transform] result of [A] to a value of [B].
 *  - _raised_ [recover] from `raised` value of [R] to a value of [B].
 *  - _exception_ [error] from [Throwable] by transforming value into [B].
 *
 * This method should never be wrapped in `try`/`catch` as it will not throw any unexpected errors,
 * it will only result in [CancellationException], or fatal exceptions such as `OutOfMemoryError`.
 */
public suspend fun <R, A, B> Effect<R, A>.fold(
  error: suspend (error: Throwable) -> B,
  recover: suspend (raised: R) -> B,
  transform: suspend (value: A) -> B,
): B = fold({ invoke() }, { error(it) }, { recover(it) }, { transform(it) })

public suspend fun <R, A, B> Effect<R, A>.fold(
  recover: suspend (raised: R) -> B,
  transform: suspend (value: A) -> B,
): B = fold({ throw it }, recover, transform)

public fun <R, A, B> EagerEffect<R, A>.fold(recover: (R) -> B, transform: (A) -> B): B =
  fold({ throw it }, recover, transform)

public inline fun <R, A, B> EagerEffect<R, A>.fold(
  error: (error: Throwable) -> B,
  recover: (raised: R) -> B,
  transform: (value: A) -> B,
): B = fold({ invoke(this) }, error, recover, transform)

@JvmName("_foldOrThrow")
public inline fun <R, A, B> fold(
  @BuilderInference program: Raise<R>.() -> A,
  recover: (raised: R) -> B,
  transform: (value: A) -> B,
): B = fold(program, { throw it }, recover, transform)

@JvmName("_fold")
public inline fun <R, A, B> fold(
  @BuilderInference program: Raise<R>.() -> A,
  error: (error: Throwable) -> B,
  recover: (raised: R) -> B,
  transform: (value: A) -> B,
): B {
  val raise = DefaultRaise()
  return try {
    transform(program(raise)).also { raise.complete() }
  } catch (e: CancellationException) {
    raise.complete()
    recover(e.raisedOrRethrow(raise))
  } catch (e: Throwable) {
    raise.complete()
    error(e.nonFatalOrThrow())
  }
}

/** Returns the raised value, rethrows the CancellationException if not our scope */
@PublishedApi
internal fun <R> CancellationException.raisedOrRethrow(raise: DefaultRaise): R =
  if (this is RaiseCancellationException && this.raise === raise) _raised as R
  else throw this

/** Serves as both purposes of a scope-reference token, and a default implementation for Raise. */
@PublishedApi
internal class DefaultRaise : Raise<Any?> {
  private val isActive: Atomic<Boolean> = Atomic(true)

  @PublishedApi
  internal fun complete(): Boolean = isActive.getAndSet(false)

  override fun raise(r: Any?): Nothing =
    if (isActive.value) throw RaiseCancellationException(r, this)
    else throw ShiftLeakedException()
}

/** CancellationException is required to cancel coroutines when raising from within them. */
private class RaiseCancellationException(val _raised: Any?, val raise: Raise<Any?>) :
  CancellationException("Raised Continuation")

public class ShiftLeakedException : IllegalStateException(
  """
  
  shift or bind was called outside of its DSL scope, and the DSL Scoped operator was leaked
  This is kind of usage is incorrect, make sure all calls to shift or bind occur within the lifecycle of effect { }, either { } or similar builders.
 
  See: Effect KDoc for additional information.
  """.trimIndent()
)
