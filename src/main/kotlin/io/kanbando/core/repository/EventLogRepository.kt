package io.kanbando.core.repository

import io.kanbando.core.event.Event
import io.kanbando.core.event.EventLog
import io.kanbando.core.model.NodeId
import kotlinx.datetime.Instant

/**
 * Storage-backed implementation of [EventLog]. Platform packages provide the adapter.
 */
interface EventLogRepository : EventLog {
    override suspend fun append(event: Event)
    override suspend fun since(timestamp: Instant): List<Event>
    override suspend fun forNode(nodeId: NodeId): List<Event>
    /** Prune events older than [before] — respects user retention preferences. */
    suspend fun pruneBefore(before: Instant)
}
