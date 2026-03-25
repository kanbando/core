package io.kanbando.core.serialization

import io.kanbando.core.model.BandoTrait
import io.kanbando.core.model.BoardTrait
import io.kanbando.core.model.ColumnOrder
import io.kanbando.core.model.ListSortOrder
import io.kanbando.core.model.ListTrait
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
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
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
            ListTrait.TYPE -> ListTrait(
                sortOrder = json["sortOrder"]?.jsonPrimitive?.content
                    ?.let { ListSortOrder.valueOf(it) } ?: ListSortOrder.MANUAL,
                showCompleted = json["showCompleted"]?.jsonPrimitive?.booleanOrNull ?: true,
                unknownFields = json.unknownFields(ListTrait.KNOWN_KEYS),
            )
            WorkspaceTrait.TYPE -> WorkspaceTrait(
                unknownFields = json.unknownFields(WorkspaceTrait.KNOWN_KEYS),
            )
            BandoTrait.TYPE -> BandoTrait(
                icon = json["icon"]?.jsonPrimitive?.content,
                templateId = json["templateId"]?.jsonPrimitive?.content,
                color = json["color"]?.jsonPrimitive?.content,
                description = json["description"]?.jsonPrimitive?.content,
                iconAsBackground = json["iconAsBackground"]?.jsonPrimitive?.booleanOrNull ?: false,
                unknownFields = json.unknownFields(BandoTrait.KNOWN_KEYS),
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
            is ListTrait -> buildJsonObject {
                put("type", ListTrait.TYPE)
                put("sortOrder", value.sortOrder.name)
                if (!value.showCompleted) put("showCompleted", false)
            } + value.unknownFields
            is WorkspaceTrait -> buildJsonObject {
                put("type", WorkspaceTrait.TYPE)
            } + value.unknownFields
            is BandoTrait -> buildJsonObject {
                put("type", BandoTrait.TYPE)
                value.icon?.let { put("icon", it) }
                value.templateId?.let { put("templateId", it) }
                value.color?.let { put("color", it) }
                value.description?.let { put("description", it) }
                if (value.iconAsBackground) put("iconAsBackground", true)
            } + value.unknownFields
            is UnknownTrait -> value.data
        }
        jsonEncoder.encodeJsonElement(json)
    }
}
