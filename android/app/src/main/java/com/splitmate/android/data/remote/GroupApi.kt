package com.splitmate.android.data.remote

import com.splitmate.android.data.remote.dto.CreateGroupRequest
import com.splitmate.android.data.remote.dto.GroupDetailResponse
import com.splitmate.android.data.remote.dto.GroupResponse
import com.splitmate.android.data.remote.dto.InviteLinkResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GroupApi {
    @POST("api/groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): GroupResponse

    @GET("api/groups")
    suspend fun getGroups(): List<GroupResponse>

    @GET("api/groups/{id}")
    suspend fun getGroupDetail(@Path("id") groupId: String): GroupDetailResponse

    @POST("api/groups/{id}/invite-link")
    suspend fun generateInviteLink(@Path("id") groupId: String): InviteLinkResponse

    @POST("api/groups/{id}/join")
    suspend fun joinGroup(@Path("id") groupId: String)
}
