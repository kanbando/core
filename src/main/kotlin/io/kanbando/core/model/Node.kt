package io.kanbando.core.model

import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject

/** Globally unique node identifier (UUID string). */
typealias NodeId = String

/**
 * The single hidden root node that is the sole parent of all [BandoTrait] nodes.
 * Never shown in any UI. Created automatically on first launch.
 */
const val ROOT_NODE_ID: NodeId = "__root__"

/** Client identity — Ed25519 public key, base64url encoded. */
typealias ClientId = String

/**
 * The single universal building block of the Kanbando data model.
 *
 * Every piece of user data is a Node. Hierarchy is expressed via [parentId].
 * Capabilities are expressed via [traits]. The classic Kanban hierarchy
 * (Organisation → Workspace → Board → List → Card) is just one configuration.
 *
 * Forward compatibility: unknown JSON fields are preserved in [unknownFields]
 * and written back out on serialisation. See [io.kanbando.core.serialization.NodeSerializer].
 */
data class Node(
    val id: NodeId,
    val title: String,
    val description: String? = null,
    val parentId: NodeId? = null,
    val createdAt: Instant,
    val modifiedAt: Instant,
    /** Public key of the client that last modified this node. */
    val clientId: ClientId,
    /** Monotonically increasing modification counter per client. */
    val version: Long,
    /** Ownership metadata — defaults to [Ownership.Delegate] (inherits from parent). */
    val ownership: Ownership = Ownership.Delegate,
    val traits: Set<Trait> = emptySet(),
    /** Preserves unknown JSON fields from newer versions. Never modify directly. */
    val unknownFields: JsonObject = JsonObject(emptyMap()),
)
