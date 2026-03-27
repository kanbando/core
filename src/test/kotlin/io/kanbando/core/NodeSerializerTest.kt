package io.kanbando.core

import io.kanbando.core.model.Node
import io.kanbando.core.model.TaskTrait
import io.kanbando.core.serialization.NodeSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NodeSerializerTest {

    private val json = Json { prettyPrint = false }

    private val sampleNode = Node(
        id = "test-id-1",
        title = "My Task",
        parentId = null,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        modifiedAt = Instant.parse("2026-01-02T00:00:00Z"),
        clientId = "client-key-abc",
        version = 1L,
        traits = setOf(TaskTrait()),
    )

    @Test
    fun roundTrip_knownNode() {
        val encoded = json.encodeToString(NodeSerializer, sampleNode)
        val decoded = json.decodeFromString(NodeSerializer, encoded)
        assertEquals(sampleNode, decoded)
    }

    @Test
    fun unknownFields_preserved() {
        val withExtra = """
            {
              "id": "test-id-2",
              "title": "Future Node",
              "position": "n",
              "createdAt": "2026-01-01T00:00:00Z",
              "modifiedAt": "2026-01-01T00:00:00Z",
              "clientId": "client-key-xyz",
              "version": 1,
              "futureField": "some-value",
              "traits": []
            }
        """.trimIndent()

        val decoded = json.decodeFromString(NodeSerializer, withExtra)
        assertEquals("some-value", (decoded.unknownFields["futureField"] as? JsonPrimitive)?.content)

        // Re-encode and verify the unknown field survived
        val reEncoded = json.encodeToString(NodeSerializer, decoded)
        val reDecoded = json.decodeFromString(NodeSerializer, reEncoded)
        assertEquals(decoded.unknownFields, reDecoded.unknownFields)
    }

    @Test
    fun unknownTrait_preserved() {
        val withUnknownTrait = """
            {
              "id": "test-id-3",
              "title": "Node with future trait",
              "position": "o",
              "createdAt": "2026-01-01T00:00:00Z",
              "modifiedAt": "2026-01-01T00:00:00Z",
              "clientId": "client-key-xyz",
              "version": 1,
              "traits": [
                { "type": "futureTrait", "someData": 42 }
              ]
            }
        """.trimIndent()

        val decoded = json.decodeFromString(NodeSerializer, withUnknownTrait)
        assertNotNull(decoded.traits.firstOrNull())

        val reEncoded = json.encodeToString(NodeSerializer, decoded)
        val reDecoded = json.decodeFromString(NodeSerializer, reEncoded)
        assertEquals(decoded.traits, reDecoded.traits)
    }
}
