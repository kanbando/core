package io.kanbando.core.preferences

import io.kanbando.core.model.NodeId
import io.kanbando.core.model.SortMode
import kotlinx.serialization.Serializable

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
@Serializable
data class UserPreferences(
    val user: UserScope = UserScope(),
    val device: DeviceScope = DeviceScope(),
)

@Serializable
data class UserScope(
    val theme: Theme = Theme.SYSTEM,
    val language: String = "en",
    val syncServers: List<SyncServer> = emptyList(),
    /** Per-node instance preferences, keyed by nodeId. */
    val nodes: Map<NodeId, NodeUserPrefs> = emptyMap(),
)

@Serializable
data class DeviceScope(
    val notificationsEnabled: Boolean = true,
    val localStoragePath: String? = null,
    /** Per-node instance preferences, keyed by nodeId. */
    val nodes: Map<NodeId, NodeDevicePrefs> = emptyMap(),
)

@Serializable
data class NodeUserPrefs(
    val sortMode: SortMode? = null,
    val sync: SyncPrefs? = null,
)

@Serializable
data class NodeDevicePrefs(
    val viewType: ViewType? = null,
)

@Serializable
data class SyncPrefs(
    val mode: SyncMode = SyncMode.AUTO,
    val intervalMinutes: Int = 15,
)

@Serializable
data class SyncServer(
    val url: String,
    val label: String? = null,
)

@Serializable
enum class Theme { LIGHT, DARK, SYSTEM }

@Serializable
enum class SyncMode { AUTO, MANUAL, DISABLED }

@Serializable
enum class ViewType { LIST, BOARD, CALENDAR }
