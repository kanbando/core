package io.kanbando.core.action

import io.kanbando.core.event.EventLog
import io.kanbando.core.repository.NodeRepository

/**
 * Every interaction with the [io.kanbando.core.model.Node] model flows through a typed Action.
 * Actions define the complete vocabulary of what the app can do.
 *
 * Actions are defined and executed in core — business logic never leaks into platforms.
 * Actions call core-defined repository/service interfaces; UI and server provide implementations.
 */
interface Action {
    suspend fun execute(
        nodes: NodeRepository,
        events: EventLog,
    ): ActionResult
}
