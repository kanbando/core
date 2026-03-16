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
        val maxLen = maxOf(lower.length, upper.length)
        val a = lower.padEnd(maxLen, '0')
        val b = upper.padEnd(maxLen, '0')
        val result = StringBuilder()
        var carry = 0
        for (i in maxLen - 1 downTo 0) {
            val ai = CHARS.indexOf(a[i]).coerceAtLeast(0)
            val bi = CHARS.indexOf(b[i]).coerceAtLeast(0)
            val sum = ai + bi + carry * CHARS.size
            result.insert(0, CHARS[sum / 2 % CHARS.size])
            carry = sum / 2 / CHARS.size
        }
        return result.toString().trimEnd('0').ifEmpty { "0" }
    }
}
