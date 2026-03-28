package io.kanbando.core.action.trait

import io.kanbando.core.action.Action
import io.kanbando.core.action.ActionResult
import io.kanbando.core.action.Failure
import io.kanbando.core.action.Success
import io.kanbando.core.event.Event
import io.kanbando.core.event.EventLog
import io.kanbando.core.event.EventType
import io.kanbando.core.model.ClientId
import io.kanbando.core.model.NodeId
import io.kanbando.core.model.identity.UserId
import io.kanbando.core.repository.NodeRepository
import io.kanbando.core.util.UuidGenerator
import kotlin.time.Clock
import kotlin.time.Instant

/** Move a card (node) from one column to another, appending it at the end. */
data class MoveCard(
    val cardId: NodeId,
    val targetColumnId: NodeId,
    val actingUserId: UserId,
    val clientId: ClientId,
) : Action {
    override suspend fun execute(nodes: NodeRepository, events: EventLog): ActionResult {
        val card = nodes.get(cardId) ?: return notFound(cardId, actingUserId, clientId)
        val now = Clock.System.now()
        nodes.save(card.copy(parentId = targetColumnId, modifiedAt = now, version = card.version + 1, clientId = clientId))
        val event = event(EventType.CARD_MOVED, cardId, actingUserId, clientId, now,
            metadata = mapOf("targetColumn" to targetColumnId))
        events.append(event)
        return Success(event)
    }
}

/** Add a new column (child node) to a board. */
data class AddColumn(
    val boardId: NodeId,
    val title: String,
    val actingUserId: UserId,
    val clientId: ClientId,
) : Action {
    override suspend fun execute(nodes: NodeRepository, events: EventLog): ActionResult {
        val now = Clock.System.now()
        val column = io.kanbando.core.model.Node(
            id = UuidGenerator.generate(),
            title = title,
            parentId = boardId,
            createdAt = now,
            modifiedAt = now,
            clientId = clientId,
            version = 1L,
        )
        nodes.save(column)
        val event = event(EventType.COLUMN_ADDED, column.id, actingUserId, clientId, now,
            metadata = mapOf("boardId" to boardId))
        events.append(event)
        return Success(event)
    }
}

/** Archive a column — soft delete, preserving its cards. */
data class ArchiveColumn(
    val columnId: NodeId,
    val actingUserId: UserId,
    val clientId: ClientId,
) : Action {
    override suspend fun execute(nodes: NodeRepository, events: EventLog): ActionResult {
        val column = nodes.get(columnId) ?: return notFound(columnId, actingUserId, clientId)
        val now = Clock.System.now()
        nodes.save(column.copy(
            modifiedAt = now,
            version = column.version + 1,
            clientId = clientId,
        ))
        val event = event(EventType.COLUMN_ARCHIVED, columnId, actingUserId, clientId, now)
        events.append(event)
        return Success(event)
    }
}

private fun event(type: EventType, nodeId: NodeId, userId: UserId, clientId: ClientId, timestamp: Instant, metadata: Map<String, String> = emptyMap()) =
    Event(id = UuidGenerator.generate(), type = type, nodeId = nodeId, userId = userId, clientId = clientId, timestamp = timestamp, metadata = metadata)

private fun notFound(nodeId: NodeId, userId: UserId, clientId: ClientId): Failure =
    Failure(event(EventType.NODE_MODIFIED, nodeId, userId, clientId, Clock.System.now(), mapOf("error" to "not found")),
        IllegalArgumentException("Node not found: $nodeId"))
