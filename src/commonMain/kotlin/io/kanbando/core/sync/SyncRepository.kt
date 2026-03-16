package io.kanbando.core.sync

import io.kanbando.core.model.Node
import kotlinx.datetime.Instant

/**
 * Port for sync operations between a client and a server.
 * Platform (app) provides the adapter using Ktor client.
 */
interface SyncRepository {
    /** Push locally modified nodes to the server. Returns outcomes per node. */
    suspend fun push(nodes: List<Node>): PushResult

    /** Pull nodes modified on the server since [since]. */
    suspend fun pull(since: Instant): List<Node>
}

data class PushResult(
    val accepted: List<String>,
    val conflicts: List<SyncConflict>,
)

data class SyncConflict(
    val nodeId: String,
    val serverVersion: Node,
    val strategy: String,
)
