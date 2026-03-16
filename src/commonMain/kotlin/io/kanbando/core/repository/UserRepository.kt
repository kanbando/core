package io.kanbando.core.repository

import io.kanbando.core.model.identity.User
import io.kanbando.core.model.identity.UserId

interface UserRepository {
    suspend fun get(id: UserId): User?
    suspend fun save(user: User)
    suspend fun currentUser(): User?
}
