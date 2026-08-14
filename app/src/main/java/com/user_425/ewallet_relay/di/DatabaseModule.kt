package com.user_425.ewallet_relay.di

import android.content.Context
import androidx.room.Room
import com.user_425.ewallet_relay.data.database.NotificationDatabase
import com.user_425.ewallet_relay.data.database.dao.NotificationLogDao
import com.user_425.ewallet_relay.data.database.dao.PendingNotificationDao
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
    fun provideNotificationDatabase(@ApplicationContext context: Context): NotificationDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            NotificationDatabase::class.java,
            NotificationDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideNotificationLogDao(database: NotificationDatabase): NotificationLogDao {
        return database.notificationLogDao()
    }
    
    @Provides
    fun providePendingNotificationDao(database: NotificationDatabase): PendingNotificationDao {
        return database.pendingNotificationDao()
    }
}