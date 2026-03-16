package io.kanbando.core.event

import io.kanbando.core.model.NodeId
import kotlinx.datetime.Instant

/**
 * Append-only log of [Event]s. Captured on-device always; optionally synced to server.
 *
 * Platform implementations provide the storage (SQLite, in-memory for tests).
 */
interface EventLog {
    suspend fun append(event: Event)
    suspend fun since(timestamp: Instant): List<Event>
    suspend fun forNode(nodeId: NodeId): List<Event>
}
