package com.splitmate.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.splitmate.android.data.local.dao.ExpenseDao
import com.splitmate.android.data.local.dao.GroupDao
import com.splitmate.android.data.local.dao.GroupMemberDao
import com.splitmate.android.data.local.dao.SettlementDao
import com.splitmate.android.data.local.entity.ExpenseEntity
import com.splitmate.android.data.local.entity.GroupEntity
import com.splitmate.android.data.local.entity.GroupMemberEntity
import com.splitmate.android.data.local.entity.SettlementEntity

@Database(
    entities = [
        GroupEntity::class,
        GroupMemberEntity::class,
        ExpenseEntity::class,
        SettlementEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun groupMemberDao(): GroupMemberDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun settlementDao(): SettlementDao
}
