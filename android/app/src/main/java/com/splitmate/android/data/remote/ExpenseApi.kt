package com.splitmate.android.data.remote

import com.splitmate.android.data.remote.dto.CreateExpenseRequest
import com.splitmate.android.data.remote.dto.ExpenseResponse
import com.splitmate.android.data.remote.dto.MarkSettlementRequest
import com.splitmate.android.data.remote.dto.SettlementSummaryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ExpenseApi {
    @GET("groups/{id}/expenses")
    suspend fun getExpenses(@Path("id") groupId: String): List<ExpenseResponse>

    @POST("groups/{id}/expenses")
    suspend fun addExpense(
        @Path("id") groupId: String, 
        @Body request: CreateExpenseRequest
    ): ExpenseResponse

    @GET("groups/{id}/settlements")
    suspend fun getSettlements(@Path("id") groupId: String): SettlementSummaryResponse

    @POST("groups/{id}/settlements/mark")
    suspend fun markSettlement(
        @Path("id") groupId: String,
        @Body request: MarkSettlementRequest
    )
}
