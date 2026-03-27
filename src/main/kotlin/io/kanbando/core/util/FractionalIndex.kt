package io.kanbando.core.util

import io.kanbando.core.model.ClientId

/**
 * Utilities for fractional indexing — string-based positions that allow insertion
 * anywhere in a list without renumbering siblings.
 *
 * Positions are lexicographically ordered strings. Concurrent position conflicts
 * are broken by appending the [ClientId] as a tiebreaker.
 *
 * Format: a sequence of base-62 characters. Midpoint between two positions is
 * computed by averaging their character codes at each position.
 */
object FractionalIndex {
    private const val MIN = "0"
    private const val MAX = "z"
    private val CHARS = ('0'..'9') + ('a'..'z')

    /** Generate a position before [before]. */
    fun before(before: String): String = between(MIN, before)

    /** Generate a position after [after]. */
    fun after(after: String): String = between(after, MAX)

    /** Generate a position between [lower] and [upper]. */
    fun between(lower: String, upper: String): String {
        require(lower < upper) { "lower ($lower) must be less than upper ($upper)" }
        val mid = midpoint(lower, upper)
        return if (mid != lower && mid != upper) mid else lower + CHARS[CHARS.size / 2]
    }

    /**
     * Resolve a tie between two identical positions from different clients.
     * Appends the [clientId] to make the position unique and deterministic.
     */
    fun tiebreak(position: String, clientId: ClientId): String =
        "${position}:${clientId}"

    private fun midpoint(lower: String, upper: String): String {
        val n = CHARS.size
        val result = StringBuilder()
        val maxLen = maxOf(lower.length, upper.length) + 1
        var inGap = false // true once we've consumed a position where hi == lo + 1

        for (i in 0 until maxLen) {
            val lo = if (i < lower.length) CHARS.indexOf(lower[i]).coerceAtLeast(0) else 0
            val hi = if (inGap || i >= upper.length) n else CHARS.indexOf(upper[i]).coerceAtLeast(0)

            when {
                hi - lo > 1 -> {
                    result.append(CHARS[(lo + hi) / 2])
                    return result.toString()
                }
                hi == lo + 1 -> {
                    // Can't split here; take lo's digit and recurse into the gap
                    result.append(CHARS[lo])
                    inGap = true
                }
                else -> { // lo == hi: common prefix digit, keep going
                    result.append(CHARS[lo])
                }
            }
        }
        return result.toString()
    }
}
