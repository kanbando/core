package io.kanbando.core.repository

import io.kanbando.core.preferences.UserPreferences

interface PreferencesRepository {
    suspend fun load(): UserPreferences
    suspend fun save(preferences: UserPreferences)
}
