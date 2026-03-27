package io.kanbando.core.event

import io.kanbando.core.model.ClientId
import io.kanbando.core.model.NodeId
import io.kanbando.core.model.identity.UserId
import kotlinx.datetime.Instant

/**
 * An immutable record of something that happened.
 *
 * v1: action events only — "what was attempted on which node".
 * Future: mutation events — "which fields changed and to what values".
 */
data class Event(
    val id: String,
    val type: EventType,
    val nodeId: NodeId,
    val userId: UserId,
    val clientId: ClientId,
    val timestamp: Instant,
    /** Additional context — action name, trait type, etc. */
    val metadata: Map<String, String> = emptyMap(),
)

enum class EventType {
    // Node lifecycle
    NODE_CREATED,
    NODE_MODIFIED,
    NODE_MOVED,
    NODE_DELETED,
    NODE_SHARED,

    // Trait changes
    TRAIT_ADDED,
    TRAIT_REMOVED,

    // Task trait actions
    TASK_COMPLETED,
    TASK_REOPENED,
    TASK_DUE_DATE_SET,
    TASK_PRIORITY_SET,

    // Board trait actions
    CARD_MOVED,
    COLUMN_ADDED,
    COLUMN_ARCHIVED,

    // Sync
    SYNC_CONFLICT,
}
