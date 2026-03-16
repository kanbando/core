package io.kanbando.core.preferences

import io.kanbando.core.model.NodeId
import io.kanbando.core.model.SortMode

/**
 * All user preferences stored as a single JSON file.
 *
 * Forward-compatible: unknown keys are preserved on load and written back unchanged.
 * Load strategy: effectivePreferences = deepMerge(appDefaults, userPreferences)
 *
 * Two scopes:
 * - [user]: syncs across all of the user's devices
 * - [device]: stays local to this device only
 */
data class UserPreferences(
    val user: UserScope = UserScope(),
    val device: DeviceScope = DeviceScope(),
)

data class UserScope(
    val theme: Theme = Theme.SYSTEM,
    val language: String = "en",
    val syncServers: List<SyncServer> = emptyList(),
    /** Per-node instance preferences, keyed by nodeId. */
    val nodes: Map<NodeId, NodeUserPrefs> = emptyMap(),
)

data class DeviceScope(
    val notificationsEnabled: Boolean = true,
    val localStoragePath: String? = null,
    /** Per-node instance preferences, keyed by nodeId. */
    val nodes: Map<NodeId, NodeDevicePrefs> = emptyMap(),
)

data class NodeUserPrefs(
    val sortMode: SortMode? = null,
    val sync: SyncPrefs? = null,
)

data class NodeDevicePrefs(
    val viewType: ViewType? = null,
)

data class SyncPrefs(
    val mode: SyncMode = SyncMode.AUTO,
    val intervalMinutes: Int = 15,
)

data class SyncServer(
    val url: String,
    val label: String? = null,
)

enum class Theme { LIGHT, DARK, SYSTEM }

enum class SyncMode { AUTO, MANUAL, DISABLED }

enum class ViewType { LIST, BOARD, CALENDAR }
