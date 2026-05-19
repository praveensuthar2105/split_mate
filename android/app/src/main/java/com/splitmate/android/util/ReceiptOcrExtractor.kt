package com.splitmate.android.util

import com.splitmate.android.domain.model.LineItem

object ReceiptOcrExtractor {

    // Matches Indian amount formats:
    // ₹ 1,200.00 / Rs. 800 / Total: 450 / TOTAL 2450.50
    private val AMOUNT_PATTERNS = listOf(
        Regex("""(?:total|amount|grand total|subtotal|net amount)[:\s]*[₹Rs.]*\s*([\d,]+\.?\d*)""",
            RegexOption.IGNORE_CASE),
        Regex("""[₹Rs.]\s*([\d,]+\.?\d*)"""),
        Regex("""([\d,]+\.?\d*)\s*(?:/-|only)""", RegexOption.IGNORE_CASE)
    )

    fun extractAmount(rawText: String): Double? {
        for (pattern in AMOUNT_PATTERNS) {
            val match = pattern.find(rawText) ?: continue
            val amountStr = match.groupValues[1]
                .replace(",", "")
                .trim()
            return amountStr.toDoubleOrNull()
        }
        return null
    }

    fun extractLineItems(rawText: String): List<LineItem> {
        // Each line: item name followed by a price
        val linePattern = Regex("""^(.+?)\s+([\d,]+\.?\d*)$""", RegexOption.MULTILINE)
        return linePattern.findAll(rawText).map { match ->
            LineItem(
                name = match.groupValues[1].trim(),
                amount = match.groupValues[2].replace(",", "").toDoubleOrNull() ?: 0.0
            )
        }.filter { it.amount > 0 && it.amount < 100_000 }.toList()
    }
}