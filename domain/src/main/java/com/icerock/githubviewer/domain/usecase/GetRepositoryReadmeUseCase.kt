package com.icerock.githubviewer.domain.usecase

import com.icerock.githubviewer.domain.entity.RepoReadme
import com.icerock.githubviewer.domain.repository.AppRepository
import javax.inject.Inject

class GetRepositoryReadmeUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(
        ownerName: String,
        repositoryName: String,
        branchName: String
    ): Result<RepoReadme> {
        return try {
            val response = appRepository.getRepositoryReadme(ownerName, repositoryName, branchName)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}