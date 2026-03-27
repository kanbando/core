package io.kanbando.core.model

import io.kanbando.core.util.TemplateHash

/**
 * A named recipe for creating a [Node] — a display name plus a set of traits.
 *
 * Templates are not nodes themselves. They live in the presentation/configuration layer.
 * Templates support inheritance: a child template extends a parent and cannot remove
 * inherited traits.
 */
data class Template(
    val name: String,
    /** Trait type strings contributed by this template (not including inherited traits). */
    val traits: Set<String>,
    val parentTemplate: Template? = null,
    /**
     * Which action types are exposed in the UI for nodes of this template.
     * Null means all actions are exposed. A subset hides the rest.
     */
    val exposedActions: Set<String>? = null,
    val defaultChildSortMode: SortMode = SortMode.NATURAL,
) {
    /** All traits including those inherited from the parent chain. */
    val allTraits: Set<String>
        get() = (parentTemplate?.allTraits ?: emptySet()) + traits

    /**
     * Deterministic UID capturing the full ancestry chain.
     * Two templates are equivalent only if they share the same ancestry.
     */
    val lineageUid: String get() = TemplateHash.lineage(this)

    /**
     * Deterministic UID capturing the total combined trait set.
     * Two templates are functionally equivalent if their capability UIDs match,
     * regardless of how they were derived.
     */
    val capabilityUid: String get() = TemplateHash.capability(this)
}

@kotlinx.serialization.Serializable
enum class SortMode { NATURAL, DEFAULT, USER }

/** Built-in templates shipped with core. Their UIDs are stable and well-known. */
object BuiltInTemplates {
    val Task      = Template(name = "Task",      traits = setOf(TaskTrait.TYPE))
    val Board     = Template(name = "Board",     traits = setOf(BoardTrait.TYPE))
    val Workspace = Template(name = "Workspace", traits = setOf(WorkspaceTrait.TYPE))
    val Epic      = Template(name = "Epic",      traits = setOf(TaskTrait.TYPE), parentTemplate = Task)
    val Card      = Template(name = "Card",      traits = setOf(TaskTrait.TYPE), parentTemplate = Task)

    val all: List<Template> = listOf(Task, Board, Workspace, Epic, Card)
}
