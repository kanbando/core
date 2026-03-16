package io.kanbando.core.model.identity

/** Globally unique organisation identifier. */
typealias OrgId = String

/**
 * An organisation — the top-level governance container for shared workspaces and teams.
 *
 * Members can belong via direct membership or via a [Group].
 * An org defines the default conflict resolution policy for its [Ownership.Organisational] nodes.
 */
data class Organisation(
    val id: OrgId,
    val name: String,
    val groupIds: Set<GroupId> = emptySet(),
    val directMemberIds: Set<UserId> = emptySet(),
)
