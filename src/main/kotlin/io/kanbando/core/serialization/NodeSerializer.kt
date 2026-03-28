package io.kanbando.core.serialization

import io.kanbando.core.model.Node
import io.kanbando.core.model.Ownership
import io.kanbando.core.model.Trait
import kotlin.time.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Forward-compatible serialiser for [Node].
 *
 * All known fields are explicitly handled. Any field not in [KNOWN_KEYS] is preserved
 * in [Node.unknownFields] and written back unchanged on re-serialisation, ensuring
 * nodes round-trip safely through older clients.
 */
object NodeSerializer : KSerializer<Node> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Node")

    private val traitsSerializer = SetSerializer(TraitSerializer)
    private val ownershipSerializer = OwnershipSerializer

    private val KNOWN_KEYS = setOf(
        "id", "title", "description", "parentId",
        "createdAt", "modifiedAt", "clientId", "version",
        "ownership", "traits",
        "position", // legacy — still recognised so old JSON round-trips cleanly
    )

    override fun deserialize(decoder: Decoder): Node {
        val jsonDecoder = decoder as JsonDecoder
        val json = jsonDecoder.decodeJsonElement().jsonObject

        return Node(
            id = json["id"]!!.jsonPrimitive.content,
            title = json["title"]!!.jsonPrimitive.content,
            description = json["description"]?.jsonPrimitive?.content,
            parentId = json["parentId"]?.jsonPrimitive?.content,
            createdAt = Instant.parse(json["createdAt"]!!.jsonPrimitive.content),
            modifiedAt = Instant.parse(json["modifiedAt"]!!.jsonPrimitive.content),
            clientId = json["clientId"]!!.jsonPrimitive.content,
            version = json["version"]!!.jsonPrimitive.content.toLong(),
            ownership = json["ownership"]?.jsonObject?.let {
                jsonDecoder.json.decodeFromJsonElement(ownershipSerializer, it)
            } ?: Ownership.Delegate,
            traits = json["traits"]?.jsonArray?.let { arr ->
                jsonDecoder.json.decodeFromJsonElement(traitsSerializer, arr)
            } ?: emptySet(),
            unknownFields = json.unknownFields(KNOWN_KEYS),
        )
    }

    override fun serialize(encoder: Encoder, value: Node) {
        val jsonEncoder = encoder as JsonEncoder
        val known = buildJsonObject {
            put("id", value.id)
            put("title", value.title)
            value.description?.let { put("description", it) }
            value.parentId?.let { put("parentId", it) }
            put("createdAt", value.createdAt.toString())
            put("modifiedAt", value.modifiedAt.toString())
            put("clientId", value.clientId)
            put("version", value.version)
            put("ownership", jsonEncoder.json.encodeToJsonElement(ownershipSerializer, value.ownership))
            put("traits", jsonEncoder.json.encodeToJsonElement(traitsSerializer, value.traits))
        }
        jsonEncoder.encodeJsonElement(known + value.unknownFields)
    }
}
