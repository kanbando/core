package io.kanbando.core.action.trait

import io.kanbando.core.action.Action
import io.kanbando.core.action.ActionResult
import io.kanbando.core.action.Failure
import io.kanbando.core.action.Success
import io.kanbando.core.event.Event
import io.kanbando.core.event.EventLog
import io.kanbando.core.event.EventType
import io.kanbando.core.model.BandoTrait
import io.kanbando.core.model.ListSortOrder
import io.kanbando.core.model.ListTrait
import io.kanbando.core.model.ClientId
import io.kanbando.core.model.Node
import io.kanbando.core.model.NodeId
import io.kanbando.core.model.ROOT_NODE_ID
import io.kanbando.core.model.identity.UserId
import io.kanbando.core.repository.NodeRepository
import io.kanbando.core.util.UuidGenerator
import kotlin.time.Clock

/**
 * Creates a new bando as a direct child of the Root node.
 *
 * The resulting node carries [BandoTrait] and has [ROOT_NODE_ID] as its parent.
 */
data class CreateBando(
    val title: String,
    val icon: String? = null,
    val color: String? = null,
    val description: String? = null,
    val templateId: String? = null,
    val actingUserId: UserId,
    val clientId: ClientId,
) : Action {
    override suspend fun execute(nodes: NodeRepository, events: EventLog): ActionResult {
        val now = Clock.System.now()
        val node = Node(
            id = UuidGenerator.generate(),
            title = title,
            parentId = ROOT_NODE_ID,
            createdAt = now,
            modifiedAt = now,
            clientId = clientId,
            version = 1L,
            traits = setOf(
                BandoTrait(
                    icon = icon,
                    color = color,
                    description = description,
                    templateId = templateId,
                ),
                ListTrait(),
            ),
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

/**
 * Updates an existing bando's display properties.
 */
data class UpdateBando(
    val bandoId: NodeId,
    val title: String,
    val icon: String?,
    val iconAsBackground: Boolean,
    val color: String?,
    val actingUserId: UserId,
    val clientId: ClientId,
) : Action {
    override suspend fun execute(nodes: NodeRepository, events: EventLog): ActionResult {
        val existing = nodes.get(bandoId)
            ?: return Failure(
                Event(
                    id = UuidGenerator.generate(),
                    type = EventType.NODE_MODIFIED,
                    nodeId = bandoId,
                    userId = actingUserId,
                    clientId = clientId,
                    timestamp = Clock.System.now(),
                ),
                IllegalArgumentException("Bando $bandoId not found"),
            )
        val oldTrait = existing.traits.filterIsInstance<BandoTrait>().firstOrNull() ?: BandoTrait()
        val now = Clock.System.now()
        val updated = existing.copy(
            title = title,
            modifiedAt = now,
            clientId = clientId,
            version = existing.version + 1,
            traits = (existing.traits - oldTrait) + oldTrait.copy(
                icon = icon,
                iconAsBackground = iconAsBackground,
                color = color,
            ),
        )
        nodes.save(updated)
        val event = Event(
            id = UuidGenerator.generate(),
            type = EventType.NODE_MODIFIED,
            nodeId = bandoId,
            userId = actingUserId,
            clientId = clientId,
            timestamp = now,
        )
        events.append(event)
        return Success(event)
    }
}

/**
 * Updates the [ListTrait] on an existing list/bando node.
 */
data class UpdateListTrait(
    val nodeId: NodeId,
    val sortOrder: ListSortOrder,
    val showCompleted: Boolean,
    val actingUserId: UserId,
    val clientId: ClientId,
) : Action {
    override suspend fun execute(nodes: NodeRepository, events: EventLog): ActionResult {
        val existing = nodes.get(nodeId)
            ?: return Failure(
                Event(
                    id = UuidGenerator.generate(),
                    type = EventType.NODE_MODIFIED,
                    nodeId = nodeId,
                    userId = actingUserId,
                    clientId = clientId,
                    timestamp = Clock.System.now(),
                ),
                IllegalArgumentException("Node $nodeId not found"),
            )
        val oldTrait = existing.traits.filterIsInstance<ListTrait>().firstOrNull() ?: ListTrait()
        val now = Clock.System.now()
        val updated = existing.copy(
            modifiedAt = now,
            clientId = clientId,
            version = existing.version + 1,
            traits = (existing.traits - oldTrait) + oldTrait.copy(
                sortOrder = sortOrder,
                showCompleted = showCompleted,
            ),
        )
        nodes.save(updated)
        val event = Event(
            id = UuidGenerator.generate(),
            type = EventType.NODE_MODIFIED,
            nodeId = nodeId,
            userId = actingUserId,
            clientId = clientId,
            timestamp = now,
        )
        events.append(event)
        return Success(event)
    }
}
