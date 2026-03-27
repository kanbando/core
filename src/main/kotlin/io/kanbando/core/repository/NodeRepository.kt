package io.kanbando.core.repository

import io.kanbando.core.model.Node
import io.kanbando.core.model.NodeId
import kotlinx.datetime.Instant

/**
 * Port (interface) for node storage. Platform packages provide the adapter (implementation).
 *
 * - App: SQLDelight
 * - Server: PostgreSQL via Exposed
 * - Tests: in-memory implementation
 */
interface NodeRepository {
    suspend fun get(id: NodeId): Node?
    suspend fun save(node: Node)
    suspend fun delete(id: NodeId)
    suspend fun children(parentId: NodeId): List<Node>
    suspend fun roots(): List<Node>
    /** Returns all nodes modified after [since] — used for sync. */
    suspend fun modifiedSince(since: Instant): List<Node>
}
