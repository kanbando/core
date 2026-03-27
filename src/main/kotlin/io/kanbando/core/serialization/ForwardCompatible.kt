package io.kanbando.core.serialization

import kotlinx.serialization.json.JsonObject

/**
 * Utilities for forward-compatible JSON serialisation.
 *
 * The guiding rule: if you don't understand a field, store it and pass it on.
 * Unknown fields are captured in a [JsonObject] and written back unchanged on re-serialisation.
 */

/** Returns a [JsonObject] containing only keys NOT in [knownKeys]. */
fun JsonObject.unknownFields(knownKeys: Set<String>): JsonObject =
    JsonObject(filterKeys { it !in knownKeys })

/** Merges two [JsonObject]s. Keys in [override] take precedence over [base]. */
operator fun JsonObject.plus(override: JsonObject): JsonObject =
    JsonObject(toMap() + override.toMap())
