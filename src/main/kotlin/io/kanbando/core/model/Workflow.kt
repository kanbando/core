package io.kanbando.core.model

/**
 * A named, composable configuration of node structure, templates, actions, and default views.
 *
 * A workflow is the highest-level unit of customisation — a complete "app within an app".
 * The built-in Kanbando Kanban experience is itself a workflow expression.
 *
 * Workflows are JSON-native, shareable, and forward-compatible.
 * Built-in workflow definitions ship as JSON resource files alongside core.
 */
data class Workflow(
    val id: String,
    val name: String,
    val description: String? = null,
    /** The root node template for this workflow (e.g. Workspace). */
    val rootTemplate: Template,
    /** All templates defined or used by this workflow. */
    val templates: List<Template> = emptyList(),
    /** Ordered default hierarchy of template names (e.g. ["Workspace", "Board", "Column", "Card"]). */
    val defaultHierarchy: List<String> = emptyList(),
)
