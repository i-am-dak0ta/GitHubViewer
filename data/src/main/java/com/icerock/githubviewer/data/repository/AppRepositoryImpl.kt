package com.icerock.githubviewer.data.repository

import com.icerock.githubviewer.data.api.GitHubApi
import com.icerock.githubviewer.data.storage.KeyValueStorage
import com.icerock.githubviewer.domain.entity.Repo
import com.icerock.githubviewer.domain.entity.RepoDetails
import com.icerock.githubviewer.domain.entity.RepoReadme
import com.icerock.githubviewer.domain.entity.UserInfo
import com.icerock.githubviewer.domain.repository.AppRepository
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val gitHubApi: GitHubApi,
    private val keyValueStorage: KeyValueStorage
) : AppRepository {

    private fun getAuthToken(): String {
        return "Bearer ${keyValueStorage.authToken ?: throw Exception("No auth token found")}"
    }

    override suspend fun getRepositories(): List<Repo> {
        return gitHubApi.getRepositories(getAuthToken(), keyValueStorage.login!!)
    }

    override suspend fun getRepository(repoId: String): RepoDetails {
        val repository = getRepositories().find { it.id.toString() == repoId }
        return gitHubApi.getRepository(getAuthToken(), keyValueStorage.login!!, repository!!.name)
    }

    override suspend fun getRepositoryReadme(
        ownerName: String, repositoryName: String, branchName: String
    ): RepoReadme {
        return gitHubApi.getReadme(getAuthToken(), ownerName, repositoryName, branchName)
    }

    override suspend fun signIn(token: String): UserInfo {
        val authToken = "Bearer $token"
        val userInfo = gitHubApi.signIn(authToken)
        keyValueStorage.authToken = token
        keyValueStorage.isAuthorized = true
        keyValueStorage.login = userInfo.login
        return userInfo
    }

    override fun isAuthorized(): Boolean {
        return keyValueStorage.isAuthorized
    }

    override fun logOut() {
        keyValueStorage.authToken = null
        keyValueStorage.isAuthorized = false
        keyValueStorage.login = null
    }
}