package com.icerock.githubviewer.di

import android.content.Context
import com.icerock.githubviewer.data.storage.KeyValueStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideKeyValueStorage(@ApplicationContext context: Context): KeyValueStorage {
        return KeyValueStorage(context)
    }
}
