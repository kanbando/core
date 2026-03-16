package io.kanbando.core.model

import io.kanbando.core.model.identity.GroupId
import io.kanbando.core.model.identity.OrgId
import io.kanbando.core.model.identity.UserId
import kotlinx.serialization.json.JsonObject

/**
 * Governs sync behaviour, conflict resolution strategy, and access control for a [Node].
 *
 * Ownership is resolved by walking up the node tree until an explicit value is found.
 * [Delegate] is the default — it means "use my parent's ownership".
 */
sealed interface Ownership {

    /** Inherits ownership from the parent node. Default for all nodes. */
    data object Delegate : Ownership

    /** Personal node — owned by a single user, synced to their devices only. */
    data class Owned(val userId: UserId) : Ownership

    /** Shared with specific users or groups. Collaborative sync. */
    data class Shared(
        val ownerId: UserId,
        val sharedWith: Set<PrincipalRef> = emptySet(),
    ) : Ownership

    /** Belongs to an organisation. Governed by org-level sync and access policies. */
    data class Organisational(val orgId: OrgId) : Ownership

    /** Preserves unknown ownership types from newer app versions. */
    data class Unknown(val data: JsonObject) : Ownership
}

/** A reference to a user or group — used in [Ownership.Shared]. */
sealed interface PrincipalRef {
    data class UserRef(val userId: UserId) : PrincipalRef
    data class GroupRef(val groupId: GroupId) : PrincipalRef
}
