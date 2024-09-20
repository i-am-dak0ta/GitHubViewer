package com.icerock.githubviewer.domain.repository

import com.icerock.githubviewer.domain.entity.Repo
import com.icerock.githubviewer.domain.entity.RepoDetails
import com.icerock.githubviewer.domain.entity.RepoReadme
import com.icerock.githubviewer.domain.entity.UserInfo

interface AppRepository {
    suspend fun getRepositories(): List<Repo>

    suspend fun getRepository(repoId: String): RepoDetails

    suspend fun getRepositoryReadme(
        ownerName: String,
        repositoryName: String,
        branchName: String
    ): RepoReadme

    suspend fun signIn(token: String): UserInfo

    fun logOut()

    fun isAuthorized(): Boolean
}