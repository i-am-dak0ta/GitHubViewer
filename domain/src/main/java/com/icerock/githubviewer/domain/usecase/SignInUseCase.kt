package com.icerock.githubviewer.domain.usecase

import com.icerock.githubviewer.domain.entity.UserInfo
import com.icerock.githubviewer.domain.repository.AppRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(token: String): Result<UserInfo> {
        return try {
            val response = appRepository.signIn(token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}