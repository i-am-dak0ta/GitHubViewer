package com.icerock.githubviewer.data.api

import com.icerock.githubviewer.domain.entity.Repo
import com.icerock.githubviewer.domain.entity.RepoDetails
import com.icerock.githubviewer.domain.entity.RepoReadme
import com.icerock.githubviewer.domain.entity.UserInfo
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApi {

    @GET("users/{userName}/repos")
    suspend fun getRepositories(
        @Header("Authorization") token: String,
        @Path("userName") userName: String,
        @Query("per_page") perPage: Int = 10
    ): List<Repo>

    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): RepoDetails

    @GET("repos/{owner}/{repo}/readme")
    suspend fun getReadme(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("ref") branchName: String
    ): RepoReadme

    @GET("user")
    suspend fun signIn(
        @Header("Authorization") token: String
    ): UserInfo
}