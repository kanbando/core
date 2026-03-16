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
import io.kanbando.core.model.Priority
import io.kanbando.core.model.TaskTrait
import io.kanbando.core.model.identity.UserId
import io.kanbando.core.repository.NodeRepository
import io.kanbando.core.util.UuidGenerator
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class CompleteTask(
    val nodeId: NodeId,
    val actingUserId: UserId,
    val clientId: ClientId,
) : Action {
    override suspend fun execute(nodes: NodeRepository, events: EventLog): ActionResult {
        val node = nodes.get(nodeId) ?: return notFound(nodeId, actingUserId, clientId)
        val task = node.traits.filterIsInstance<TaskTrait>().firstOrNull()
            ?: return notATask(nodeId, actingUserId, clientId)
        val now = Clock.System.now()
        val updated = node.copy(
            traits = (node.traits - task) + task.copy(completedAt = now),
            modifiedAt = now,
            version = node.version + 1,
            clientId = clientId,
        )
        nodes.save(updated)
        val event = event(EventType.TASK_COMPLETED, nodeId, actingUserId, clientId, now)
        events.append(event)
        return Success(event)
    }
}

data class ReopenTask(
    val nodeId: NodeId,
    val actingUserId: UserId,
    val clientId: ClientId,
) : Action {
    override suspend fun execute(nodes: NodeRepository, events: EventLog): ActionResult {
        val node = nodes.get(nodeId) ?: return notFound(nodeId, actingUserId, clientId)
        val task = node.traits.filterIsInstance<TaskTrait>().firstOrNull()
            ?: return notATask(nodeId, actingUserId, clientId)
        val now = Clock.System.now()
        val updated = node.copy(
            traits = (node.traits - task) + task.copy(completedAt = null),
            modifiedAt = now,
            version = node.version + 1,
            clientId = clientId,
        )
        nodes.save(updated)
        val event = event(EventType.TASK_REOPENED, nodeId, actingUserId, clientId, now)
        events.append(event)
        return Success(event)
    }
}

data class SetDueDate(
    val nodeId: NodeId,
    val dueDate: Instant?,
    val actingUserId: UserId,
    val clientId: ClientId,
) : Action {
    override suspend fun execute(nodes: NodeRepository, events: EventLog): ActionResult {
        val node = nodes.get(nodeId) ?: return notFound(nodeId, actingUserId, clientId)
        val task = node.traits.filterIsInstance<TaskTrait>().firstOrNull()
            ?: return notATask(nodeId, actingUserId, clientId)
        val now = Clock.System.now()
        nodes.save(node.copy(
            traits = (node.traits - task) + task.copy(dueDate = dueDate),
            modifiedAt = now,
            version = node.version + 1,
            clientId = clientId,
        ))
        val event = event(EventType.TASK_DUE_DATE_SET, nodeId, actingUserId, clientId, now)
        events.append(event)
        return Success(event)
    }
}

data class SetPriority(
    val nodeId: NodeId,
    val priority: Priority?,
    val actingUserId: UserId,
    val clientId: ClientId,
) : Action {
    override suspend fun execute(nodes: NodeRepository, events: EventLog): ActionResult {
        val node = nodes.get(nodeId) ?: return notFound(nodeId, actingUserId, clientId)
        val task = node.traits.filterIsInstance<TaskTrait>().firstOrNull()
            ?: return notATask(nodeId, actingUserId, clientId)
        val now = Clock.System.now()
        nodes.save(node.copy(
            traits = (node.traits - task) + task.copy(priority = priority),
            modifiedAt = now,
            version = node.version + 1,
            clientId = clientId,
        ))
        val event = event(EventType.TASK_PRIORITY_SET, nodeId, actingUserId, clientId, now,
            metadata = mapOf("priority" to (priority?.name ?: "none")))
        events.append(event)
        return Success(event)
    }
}

private fun event(
    type: EventType,
    nodeId: NodeId,
    userId: UserId,
    clientId: ClientId,
    timestamp: Instant,
    metadata: Map<String, String> = emptyMap(),
) = Event(id = UuidGenerator.generate(), type = type, nodeId = nodeId, userId = userId, clientId = clientId, timestamp = timestamp, metadata = metadata)

private fun notFound(nodeId: NodeId, userId: UserId, clientId: ClientId): Failure =
    Failure(event(EventType.NODE_MODIFIED, nodeId, userId, clientId, Clock.System.now(), mapOf("error" to "not found")),
        IllegalArgumentException("Node not found: $nodeId"))

private fun notATask(nodeId: NodeId, userId: UserId, clientId: ClientId): Failure =
    Failure(event(EventType.NODE_MODIFIED, nodeId, userId, clientId, Clock.System.now(), mapOf("error" to "not a task")),
        IllegalStateException("Node $nodeId does not have TaskTrait"))
