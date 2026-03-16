package io.kanbando.core.serialization

import io.kanbando.core.model.BoardTrait
import io.kanbando.core.model.ColumnOrder
import io.kanbando.core.model.Priority
import io.kanbando.core.model.TaskTrait
import io.kanbando.core.model.Trait
import io.kanbando.core.model.UnknownTrait
import io.kanbando.core.model.WorkspaceTrait
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Forward-compatible serialiser for [Trait].
 *
 * Dispatches on the "type" discriminator field. Known types are deserialised to their
 * concrete classes. Unknown types are preserved as [UnknownTrait] with the full JSON intact.
 */
object TraitSerializer : KSerializer<Trait> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Trait")

    override fun deserialize(decoder: Decoder): Trait {
        val jsonDecoder = decoder as JsonDecoder
        val json = jsonDecoder.decodeJsonElement().jsonObject
        return when (val type = json["type"]?.jsonPrimitive?.content) {
            TaskTrait.TYPE -> TaskTrait(
                completedAt = json["completedAt"]?.jsonPrimitive?.content?.let(Instant::parse),
                dueDate = json["dueDate"]?.jsonPrimitive?.content?.let(Instant::parse),
                priority = json["priority"]?.jsonPrimitive?.content?.let { Priority.valueOf(it) },
                reminderAt = json["reminderAt"]?.jsonPrimitive?.content?.let(Instant::parse),
                unknownFields = json.unknownFields(TaskTrait.KNOWN_KEYS),
            )
            BoardTrait.TYPE -> BoardTrait(
                defaultColumnOrder = json["defaultColumnOrder"]?.jsonPrimitive?.content
                    ?.let { ColumnOrder.valueOf(it) } ?: ColumnOrder.MANUAL,
                unknownFields = json.unknownFields(BoardTrait.KNOWN_KEYS),
            )
            WorkspaceTrait.TYPE -> WorkspaceTrait(
                unknownFields = json.unknownFields(WorkspaceTrait.KNOWN_KEYS),
            )
            else -> UnknownTrait(traitType = type ?: "unknown", data = json)
        }
    }

    override fun serialize(encoder: Encoder, value: Trait) {
        val jsonEncoder = encoder as JsonEncoder
        val json = when (value) {
            is TaskTrait -> buildJsonObject {
                put("type", TaskTrait.TYPE)
                value.completedAt?.let { put("completedAt", it.toString()) }
                value.dueDate?.let { put("dueDate", it.toString()) }
                value.priority?.let { put("priority", it.name) }
                value.reminderAt?.let { put("reminderAt", it.toString()) }
            } + value.unknownFields
            is BoardTrait -> buildJsonObject {
                put("type", BoardTrait.TYPE)
                put("defaultColumnOrder", value.defaultColumnOrder.name)
            } + value.unknownFields
            is WorkspaceTrait -> buildJsonObject {
                put("type", WorkspaceTrait.TYPE)
            } + value.unknownFields
            is UnknownTrait -> value.data
        }
        jsonEncoder.encodeJsonElement(json)
    }
}
