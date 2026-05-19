package com.splitmate.android.data.repository

import com.splitmate.android.data.local.dao.GroupDao
import com.splitmate.android.data.local.dao.GroupMemberDao
import com.splitmate.android.data.local.entity.GroupEntity
import com.splitmate.android.data.local.entity.GroupMemberEntity
import com.splitmate.android.data.remote.GroupApi
import com.splitmate.android.data.remote.dto.CreateGroupRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val groupApi: GroupApi,
    private val groupDao: GroupDao,
    private val groupMemberDao: GroupMemberDao
) {
    suspend fun createGroup(name: String, groupType: String): GroupEntity {
        val created = groupApi.createGroup(
            CreateGroupRequest(
                name = name,
                groupType = groupType
            )
        )
        val entity = created.toEntity()
        groupDao.insert(entity)
        return entity
    }

    suspend fun generateInviteLink(groupId: String): String {
        return groupApi.generateInviteLink(groupId).inviteLink
    }

    suspend fun joinGroup(groupIdOrInviteLink: String): GroupEntity {
        val groupId = groupIdOrInviteLink
            .trim()
            .substringAfterLast("/")
            .substringAfterLast("join:")
        groupApi.joinGroup(groupId)
        val detail = groupApi.getGroupDetail(groupId)
        val entity = detail.group.toEntity()
        groupDao.insert(entity)
        groupMemberDao.insertAll(detail.members.map { member ->
            GroupMemberEntity(
                id = member.id ?: "${member.groupId}_${member.userId}",
                groupId = member.groupId,
                userId = member.userId,
                role = member.role,
                name = member.name ?: member.userId.take(8),
                upiId = member.upiId
            )
        })
        return entity
    }

    /**
     * Offline-first logic:
     * 1. Emit data from the local Room database immediately.
     * 2. Fetch fresh data from the network in the background.
     * 3. Save network data to Room (which automatically updates the UI).
     */
    fun getGroups(): Flow<List<GroupEntity>> = flow {
        // 1. Emit local cached data instantly
        val cached = groupDao.getAllGroups().first()
        if (cached.isNotEmpty()) {
            emit(cached)
        }

        // 2. Fetch from network
        try {
            val remoteGroups = groupApi.getGroups()
            val entities = remoteGroups.map {
                GroupEntity(
                    id = it.id,
                    name = it.name,
                    photoUrl = it.photoUrl,
                    createdBy = it.createdBy,
                    groupType = it.groupType ?: "friends",
                    isArchived = it.isArchived,
                    budgetAmount = it.budgetAmount,
                    lastActivity = it.lastActivity.orEmpty(),
                    balance = it.balance ?: 0.0
                )
            }
            
            // 3. Save to database
            groupDao.insertAll(entities)
        } catch (e: Exception) {
            // Ignore network errors if we are offline; the UI will just show cached data
        }

        // 4. Continue emitting from the database (Single Source of Truth)
        emitAll(groupDao.getAllGroups())
    }

    fun getGroup(groupId: String): Flow<GroupEntity?> = flow {
        try {
            val remote = groupApi.getGroupDetail(groupId)
            groupDao.insert(remote.group.toEntity())
            groupMemberDao.insertAll(remote.members.map { member ->
                GroupMemberEntity(
                    id = member.id ?: "${member.groupId}_${member.userId}",
                    groupId = member.groupId,
                    userId = member.userId,
                    role = member.role,
                    name = member.name ?: member.userId.take(8),
                    upiId = member.upiId
                )
            })
        } catch (e: Exception) {
            // Keep showing whatever is cached.
        }

        emitAll(groupDao.getGroupById(groupId))
    }

    fun getGroupMembers(groupId: String): Flow<List<GroupMemberEntity>> = flow {
        try {
            val remote = groupApi.getGroupDetail(groupId)
            groupMemberDao.insertAll(remote.members.map { member ->
                GroupMemberEntity(
                    id = member.id ?: "${member.groupId}_${member.userId}",
                    groupId = member.groupId,
                    userId = member.userId,
                    role = member.role,
                    name = member.name ?: member.userId.take(8),
                    upiId = member.upiId
                )
            })
        } catch (e: Exception) {
            // Keep showing whatever is cached.
        }

        emitAll(groupMemberDao.getMembers(groupId))
    }

    private fun com.splitmate.android.data.remote.dto.GroupResponse.toEntity(): GroupEntity {
        return GroupEntity(
            id = id,
            name = name,
            photoUrl = photoUrl,
            createdBy = createdBy,
            groupType = groupType ?: "friends",
            isArchived = isArchived,
            budgetAmount = budgetAmount,
            lastActivity = lastActivity.orEmpty(),
            balance = balance ?: 0.0
        )
    }
}
