package io.kanbando.core.model.identity

/** Globally unique group identifier. */
typealias GroupId = String

/**
 * A named group of users. Groups can belong to multiple organisations.
 * Membership is many-to-many: a user can belong to multiple groups.
 */
data class Group(
    val id: GroupId,
    val name: String,
    val memberIds: Set<UserId> = emptySet(),
)
