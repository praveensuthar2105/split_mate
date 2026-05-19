package com.splitmate.android.di

import android.content.Context
import androidx.room.Room
import com.splitmate.android.data.local.AppDatabase
import com.splitmate.android.data.local.dao.ExpenseDao
import com.splitmate.android.data.local.dao.GroupDao
import com.splitmate.android.data.local.dao.GroupMemberDao
import com.splitmate.android.data.local.dao.SettlementDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "splitmate_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideGroupDao(db: AppDatabase): GroupDao = db.groupDao()

    @Provides
    fun provideGroupMemberDao(db: AppDatabase): GroupMemberDao = db.groupMemberDao()

    @Provides
    fun provideExpenseDao(db: AppDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideSettlementDao(db: AppDatabase): SettlementDao = db.settlementDao()
}
