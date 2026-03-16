package io.kanbando.core.action

import io.kanbando.core.event.Event

/**
 * Every [Action] produces a result — regardless of whether it succeeded or failed.
 * Both outcomes produce an [Event] for the log.
 */
sealed class ActionResult {
    abstract val event: Event
}

data class Success(override val event: Event) : ActionResult()

data class Failure(
    override val event: Event,
    val cause: Throwable,
) : ActionResult()
