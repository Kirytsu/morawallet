package com.example.morawallet.core.util

/**
 * Cleans raw NewsAPI text for display. NewsAPI returns description/content that can
 * contain HTML tags, HTML entities, and a trailing truncation marker like
 * "… [+1234 chars]". This strips all of that down to plain, readable text.
 *
 * Pure (no Android dependencies) so it is unit-testable on the JVM.
 */
object HtmlText {

    private val truncationMarker = Regex("\\[\\+\\d+\\s*chars]")
    private val htmlTag = Regex("<[^>]*>")
    private val numericEntity = Regex("&#(x?[0-9a-fA-F]+);")
    private val whitespace = Regex("\\s+")

    private val namedEntities = mapOf(
        "amp" to "&", "lt" to "<", "gt" to ">", "quot" to "\"", "apos" to "'",
        "nbsp" to " ", "hellip" to "…", "mdash" to "—", "ndash" to "–",
        "rsquo" to "’", "lsquo" to "‘", "rdquo" to "”", "ldquo" to "“",
        "trade" to "™", "copy" to "©", "reg" to "®", "deg" to "°", "euro" to "€",
    )

    fun clean(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        var text = raw
        text = truncationMarker.replace(text, "")
        text = htmlTag.replace(text, " ")
        text = decodeEntities(text)
        return whitespace.replace(text, " ").trim()
    }

    private fun decodeEntities(input: String): String {
        var text = input
        text = numericEntity.replace(text) { match ->
            val body = match.groupValues[1]
            val code = if (body.startsWith("x") || body.startsWith("X")) {
                body.drop(1).toIntOrNull(16)
            } else {
                body.toIntOrNull()
            }
            code?.let { runCatching { String(Character.toChars(it)) }.getOrNull() } ?: match.value
        }
        for ((name, replacement) in namedEntities) {
            text = text.replace("&$name;", replacement)
        }
        return text
    }
}
