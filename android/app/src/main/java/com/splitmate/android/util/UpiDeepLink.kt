package com.splitmate.android.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object UpiDeepLink {

    fun build(
        receiverUpiId: String,
        receiverName: String,
        amount: Double,
        note: String = "SplitMate settlement"
    ): Uri {
        return Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", receiverUpiId)
            .appendQueryParameter("pn", receiverName)
            .appendQueryParameter("am", String.format("%.2f", amount))
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", note)
            .build()
    }

    fun isUpiAppInstalled(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("upi://pay"))
        return context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()
    }
}
