package io.kanbando.core.sync

import io.kanbando.core.model.Node

/**
 * Strategy interface for resolving conflicts between two versions of the same [Node].
 *
 * Implementations live on the server. The strategy is configured per workspace.
 * [core] defines the interface and built-in strategies only.
 */
interface ConflictResolver {
    /**
     * Resolve a conflict between [local] (client-submitted) and [remote] (server-stored).
     * Returns the [Node] that should be stored as the winner, or null if the conflict
     * cannot be resolved automatically and requires human intervention.
     */
    fun resolve(local: Node, remote: Node): Node?
}

/** Last-write-wins: the node with the later [Node.modifiedAt] wins. Default for owned nodes. */
object LastWriteWins : ConflictResolver {
    override fun resolve(local: Node, remote: Node): Node =
        if (local.modifiedAt >= remote.modifiedAt) local else remote
}

/**
 * Version vector merge: the node with the higher [Node.version] for the same [Node.clientId] wins.
 * Default for shared nodes.
 */
object VersionVectorMerge : ConflictResolver {
    override fun resolve(local: Node, remote: Node): Node? {
        // Same client — higher version wins
        if (local.clientId == remote.clientId) {
            return if (local.version >= remote.version) local else remote
        }
        // Different clients — cannot auto-resolve; return null to flag for human review
        return null
    }
}
