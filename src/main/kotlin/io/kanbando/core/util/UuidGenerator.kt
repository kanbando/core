package io.kanbando.core.util

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Cross-platform UUID generation using the Kotlin stdlib [Uuid] API (Kotlin 2.0+).
 */
@OptIn(ExperimentalUuidApi::class)
object UuidGenerator {
    fun generate(): String = Uuid.random().toString()
}
