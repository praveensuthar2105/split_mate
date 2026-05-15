package com.splitmate.android.data.repository

import com.splitmate.android.data.local.dao.GroupDao
import com.splitmate.android.data.local.entity.GroupEntity
import com.splitmate.android.data.remote.GroupApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val groupApi: GroupApi,
    private val groupDao: GroupDao
) {
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
                    isArchived = it.isArchived,
                    budgetAmount = it.budgetAmount,
                    lastActivity = it.lastActivity,
                    balance = it.balance
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
}
