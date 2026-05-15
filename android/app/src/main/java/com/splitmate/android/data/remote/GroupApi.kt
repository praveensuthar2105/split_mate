package com.splitmate.android.data.remote

import com.splitmate.android.data.remote.dto.GroupDetailResponse
import com.splitmate.android.data.remote.dto.GroupResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface GroupApi {
    @GET("groups")
    suspend fun getGroups(): List<GroupResponse>

    @GET("groups/{id}")
    suspend fun getGroupDetail(@Path("id") groupId: String): GroupDetailResponse
}
