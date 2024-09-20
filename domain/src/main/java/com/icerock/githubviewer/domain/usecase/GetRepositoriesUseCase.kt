package com.icerock.githubviewer.domain.usecase

import com.icerock.githubviewer.domain.entity.Repo
import com.icerock.githubviewer.domain.repository.AppRepository
import javax.inject.Inject

class GetRepositoriesUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(): Result<List<Repo>> {
        return try {
            val response = appRepository.getRepositories()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}