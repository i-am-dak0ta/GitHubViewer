package com.icerock.githubviewer.di

import com.icerock.githubviewer.data.api.GitHubApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideGitHubApi(): GitHubApi {
        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit.create(GitHubApi::class.java)
    }
}
