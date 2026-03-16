package io.kanbando.core.model

import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject

/**
 * Traits define capabilities of a [Node]. Multiple traits can be applied to a single node.
 * Traits are sealed — fixed at compile time in v1 for type safety.
 *
 * Every known trait has a [traitType] string used as a discriminator in JSON.
 * Unknown trait types are preserved as [UnknownTrait].
 */
sealed interface Trait {
    val traitType: String
}

/**
 * The node represents a task — something that can be completed, scheduled, and prioritised.
 * Classic uses: to-do item, ticket, card.
 */
data class TaskTrait(
    val completedAt: Instant? = null,
    val dueDate: Instant? = null,
    val priority: Priority? = null,
    val reminderAt: Instant? = null,
    val unknownFields: JsonObject = JsonObject(emptyMap()),
) : Trait {
    override val traitType: String get() = TYPE
    companion object {
        const val TYPE = "task"
        val KNOWN_KEYS = setOf("type", "completedAt", "dueDate", "priority", "reminderAt")
    }
}

/**
 * The node renders as a Kanban-style board — its children are columns,
 * their children are cards.
 */
data class BoardTrait(
    val defaultColumnOrder: ColumnOrder = ColumnOrder.MANUAL,
    val unknownFields: JsonObject = JsonObject(emptyMap()),
) : Trait {
    override val traitType: String get() = TYPE
    companion object {
        const val TYPE = "board"
        val KNOWN_KEYS = setOf("type", "defaultColumnOrder")
    }
}

/**
 * The node acts as a workspace — manages membership and access for its subtree.
 */
data class WorkspaceTrait(
    val unknownFields: JsonObject = JsonObject(emptyMap()),
) : Trait {
    override val traitType: String get() = TYPE
    companion object {
        const val TYPE = "workspace"
        val KNOWN_KEYS = setOf("type")
    }
}

/**
 * Preserves a trait type from a newer app version that this client does not understand.
 * The full JSON of the trait is kept intact for round-trip fidelity.
 */
data class UnknownTrait(
    override val traitType: String,
    val data: JsonObject,
) : Trait

enum class Priority { LOW, MEDIUM, HIGH, URGENT }

enum class ColumnOrder { MANUAL, ALPHABETICAL, DUE_DATE }
