package io.kanbando.core.action

import io.kanbando.core.event.Event
import io.kanbando.core.event.EventLog
import io.kanbando.core.event.EventType
import io.kanbando.core.model.ClientId
import io.kanbando.core.model.Node
import io.kanbando.core.model.NodeId
import io.kanbando.core.model.Ownership
import io.kanbando.core.model.PrincipalRef
import io.kanbando.core.model.identity.UserId
import io.kanbando.core.repository.NodeRepository
import io.kanbando.core.util.UuidGenerator
import kotlinx.datetime.Clock

data class CreateChild(
    val parentId: NodeId?,
    val title: String,
    val position: String,
    val actingUserId: UserId,
    val clientId: ClientId,
) : Action {
    override suspend fun execute(nodes: NodeRepository, events: EventLog): ActionResult {
        val now = Clock.System.now()
        val node = Node(
            id = UuidGenerator.generate(),
            title = title,
            parentId = parentId,
            position = position,
            createdAt = now,
            modifiedAt = now,
            clientId = clientId,
            version = 1L,
        )
        nodes.save(node)
        val event = Event(
            id = UuidGenerator.generate(),
            type = EventType.NODE_CREATED,
            nodeId = node.id,
            userId = actingUserId,
            clientId = clientId,
            timestamp = now,
        )
        events.append(event)
        return Success(event)
    }
}

data class RenameNode(
    val nodeId: NodeId,
    val newTitle: String,
    val actingUserId: UserId,
    val clientId: ClientId,
) : Action {
    override suspend fun execute(nodes: NodeRepository, events: EventLog): ActionResult {
        val node = nodes.get(nodeId) ?: return Failure(
            event = errorEvent(nodeId, actingUserId, clientId, "Node not found: $nodeId"),
            cause = IllegalArgumentException("Node not found: $nodeId"),
        )
        val now = Clock.System.now()
        nodes.save(node.copy(title = newTitle, modifiedAt = now, version = node.version + 1, clientId = clientId))
        val event = Event(
            id = UuidGenerator.generate(),
            type = EventType.NODE_MODIFIED,
            nodeId = nodeId,
            userId = actingUserId,
            clientId = clientId,
            timestamp = now,
            metadata = mapOf("field" to "title"),
        )
        events.append(event)
        return Success(event)
    }
}

data class MoveNode(
    val nodeId: NodeId,
    val newParentId: NodeId?,
    val newPosition: String,
    val actingUserId: UserId,
    val clientId: ClientId,
) : Action {
    override suspend fun execute(nodes: NodeRepository, events: EventLog): ActionResult {
        val node = nodes.get(nodeId) ?: return Failure(
            event = errorEvent(nodeId, actingUserId, clientId, "Node not found: $nodeId"),
            cause = IllegalArgumentException("Node not found: $nodeId"),
        )
        val now = Clock.System.now()
        nodes.save(node.copy(parentId = newParentId, position = newPosition, modifiedAt = now, version = node.version + 1, clientId = clientId))
        val event = Event(
            id = UuidGenerator.generate(),
            type = EventType.NODE_MOVED,
            nodeId = nodeId,
            userId = actingUserId,
            clientId = clientId,
            timestamp = now,
        )
        events.append(event)
        return Success(event)
    }
}

data class DeleteNode(
    val nodeId: NodeId,
    val actingUserId: UserId,
    val clientId: ClientId,
) : Action {
    override suspend fun execute(nodes: NodeRepository, events: EventLog): ActionResult {
        nodes.delete(nodeId)
        val now = Clock.System.now()
        val event = Event(
            id = UuidGenerator.generate(),
            type = EventType.NODE_DELETED,
            nodeId = nodeId,
            userId = actingUserId,
            clientId = clientId,
            timestamp = now,
        )
        events.append(event)
        return Success(event)
    }
}

data class ShareNode(
    val nodeId: NodeId,
    val ownerId: UserId,
    val shareWith: Set<PrincipalRef>,
    val actingUserId: UserId,
    val clientId: ClientId,
) : Action {
    override suspend fun execute(nodes: NodeRepository, events: EventLog): ActionResult {
        val node = nodes.get(nodeId) ?: return Failure(
            event = errorEvent(nodeId, actingUserId, clientId, "Node not found: $nodeId"),
            cause = IllegalArgumentException("Node not found: $nodeId"),
        )
        val now = Clock.System.now()
        nodes.save(node.copy(
            ownership = Ownership.Shared(ownerId = ownerId, sharedWith = shareWith),
            modifiedAt = now,
            version = node.version + 1,
            clientId = clientId,
        ))
        val event = Event(
            id = UuidGenerator.generate(),
            type = EventType.NODE_SHARED,
            nodeId = nodeId,
            userId = actingUserId,
            clientId = clientId,
            timestamp = now,
        )
        events.append(event)
        return Success(event)
    }
}

private fun errorEvent(nodeId: NodeId, userId: UserId, clientId: ClientId, message: String) = Event(
    id = UuidGenerator.generate(),
    type = EventType.NODE_MODIFIED,
    nodeId = nodeId,
    userId = userId,
    clientId = clientId,
    timestamp = Clock.System.now(),
    metadata = mapOf("error" to message),
)
