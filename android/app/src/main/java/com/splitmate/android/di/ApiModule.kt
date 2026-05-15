package com.splitmate.android.di

import com.splitmate.android.data.remote.ExpenseApi
import com.splitmate.android.data.remote.GroupApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    fun provideGroupApi(retrofit: Retrofit): GroupApi {
        return retrofit.create(GroupApi::class.java)
    }

    @Provides
    @Singleton
    fun provideExpenseApi(retrofit: Retrofit): ExpenseApi {
        return retrofit.create(ExpenseApi::class.java)
    }
}
