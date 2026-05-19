package com.splitmate.android.util

object VoiceExpenseParser {

    data class ParsedExpense(
        val payer: String?,
        val amount: Double?,
        val description: String?,
        val participants: List<String>
    )

    // Convert spoken numbers to digits
    private val NUMBER_WORDS = mapOf(
        "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
        "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10,
        "hundred" to 100, "thousand" to 1000, "k" to 1000,
        "fifteen hundred" to 1500, "five hundred" to 500
    )

    fun parse(speech: String, groupMembers: List<String>): ParsedExpense {
        val lower = speech.lowercase()

        // Extract amount — "five hundred", "800", "1.5k", "₹400"
        val amount = extractAmount(lower)

        // Extract payer — first member name mentioned before "paid"
        val payer = groupMembers.firstOrNull { name ->
            lower.contains(name.lowercase()) &&
            lower.indexOf(name.lowercase()) <
            (lower.indexOf("paid").takeIf { it >= 0 } ?: Int.MAX_VALUE)
        }

        // Extract participants — members mentioned after "between" or "split"
        val splitIndex = maxOf(
            lower.indexOf("between"), lower.indexOf("split with"), lower.indexOf("among")
        )
        val participants = if (splitIndex >= 0) {
            groupMembers.filter { name ->
                lower.substring(splitIndex).contains(name.lowercase())
            }
        } else groupMembers  // Default to all members

        // Extract description — text between payer and amount
        val description = extractDescription(lower, payer, amount)

        return ParsedExpense(payer, amount, description, participants)
    }

    private fun extractAmount(text: String): Double? {
        // Numeric: "800", "1200.50", "₹500"
        val numericPattern = Regex("""[₹Rs.]?\s*(\d+\.?\d*)""")
        numericPattern.find(text)?.let {
            return it.groupValues[1].toDoubleOrNull()
        }
        // Word-based: "five hundred", "1.5k"
        NUMBER_WORDS.entries.sortedByDescending { it.key.length }.forEach { (word, value) ->
            if (text.contains(word)) return value.toDouble()
        }
        return null
    }

    private fun extractDescription(text: String, payer: String?, amount: Double?): String? {
        // Simple heuristic: words between "for" and a number/member name
        val forIndex = text.indexOf(" for ")
        if (forIndex < 0) return null
        return text.substring(forIndex + 5)
            .split(Regex("\\d|split|between|paid"))
            .firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
    }
}