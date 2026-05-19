package com.splitmate.android.domain.model

data class GroupAnalytics(
    val groupId: String,
    val monthlyTotals: Map<String, Double>, // e.g., {"Jan" to 400.0, "Feb" to 1200.0}
    val categoryBreakdown: Map<String, Double>, // e.g., {"Food" to 800.0, "Transport" to 300.0}
    val totalSpend: Double
)