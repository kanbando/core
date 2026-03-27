package io.kanbando.core.util

import io.kanbando.core.model.Template

/**
 * Computes deterministic UIDs for [Template]s.
 *
 * Both UIDs are derived from trait sets, not names — names are presentation only.
 * Built-in templates have stable, well-known UIDs that never change.
 *
 * NOTE: The current implementation uses a simple djb2 hash for scaffolding.
 * This must be replaced with SHA-256 (via platform crypto) before production use.
 * The interface is stable; only the hash function implementation will change.
 */
object TemplateHash {

    /**
     * Lineage UID — captures the full ancestry chain.
     * Two templates are equivalent only if they share the same ancestry.
     */
    fun lineage(template: Template): String {
        val parentUid = template.parentTemplate?.let { lineage(it) } ?: ""
        val input = parentUid + "|" + template.traits.sorted().joinToString(",")
        return hash(input)
    }

    /**
     * Capability UID — captures the total combined trait set.
     * Two templates are functionally equivalent if their capability UIDs match,
     * regardless of how they were derived.
     */
    fun capability(template: Template): String {
        val input = template.allTraits.sorted().joinToString(",")
        return hash(input)
    }

    // TODO: Replace with SHA-256 (platform crypto) before production use.
    //       The interface (lineage/capability) is stable; only this function changes.
    private fun hash(input: String): String {
        var h = 5381L
        for (c in input) h = ((h shl 5) + h) + c.code
        return h.toULong().toString(16).padStart(16, '0')
    }
}
