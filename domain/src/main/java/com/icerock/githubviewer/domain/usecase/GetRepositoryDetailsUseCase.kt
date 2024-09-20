package com.icerock.githubviewer.domain.usecase

import com.icerock.githubviewer.domain.entity.RepoDetails
import com.icerock.githubviewer.domain.repository.AppRepository
import javax.inject.Inject

class GetRepositoryDetailsUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(repoId: String): Result<RepoDetails> {
        return try {
            val response = appRepository.getRepository(repoId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}