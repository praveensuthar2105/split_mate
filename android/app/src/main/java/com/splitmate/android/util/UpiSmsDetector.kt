package com.splitmate.android.util

import android.content.Context
import android.net.Uri
import com.splitmate.android.ui.settle.SettlementTransaction

object UpiSmsDetector {

    // Matches UPI credit messages from all major Indian banks
    private val UPI_CREDIT_PATTERNS = listOf(
        Regex("""credited.*?Rs\.?\s*([\d,]+\.?\d*).*?UPI""", RegexOption.IGNORE_CASE),
        Regex("""received.*?Rs\.?\s*([\d,]+\.?\d*).*?(?:GPay|PhonePe|Paytm|BHIM|UPI)""", RegexOption.IGNORE_CASE),
        Regex("""A\/c.*?credited.*?([\d,]+\.?\d*).*?UPI""", RegexOption.IGNORE_CASE),
        Regex("""UPI.*?credit.*?INR\s*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)
    )

    data class DetectedPayment(val amount: Double, val rawSms: String)

    fun scanRecentSms(context: Context, lookbackHours: Int = 24): List<DetectedPayment> {
        val results = mutableListOf<DetectedPayment>()
        val cutoff = System.currentTimeMillis() - (lookbackHours * 3600 * 1000)

        val cursor = context.contentResolver.query(
            Uri.parse("content://sms/inbox"),
            arrayOf("body", "date"),
            "date > ?",
            arrayOf(cutoff.toString()),
            "date DESC"
        ) ?: return results

        cursor.use {
            while (it.moveToNext()) {
                val body = it.getString(0) ?: continue
                for (pattern in UPI_CREDIT_PATTERNS) {
                    val match = pattern.find(body) ?: continue
                    val amount = match.groupValues[1]
                        .replace(",", "").toDoubleOrNull() ?: continue
                    results.add(DetectedPayment(amount, body))
                    break
                }
            }
        }
        return results
    }

    fun findMatchingSettlement(
        detectedPayments: List<DetectedPayment>,
        pendingSettlements: List<SettlementTransaction>,
        toleranceRupees: Double = 1.0
    ): List<Pair<DetectedPayment, SettlementTransaction>> {
        return detectedPayments.flatMap { payment ->
            pendingSettlements
                .filter { s -> Math.abs(s.amount - payment.amount) <= toleranceRupees }
                .map { Pair(payment, it) }
        }
    }
}