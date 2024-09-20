package com.icerock.githubviewer.domain.usecase

import com.icerock.githubviewer.domain.repository.AppRepository
import javax.inject.Inject

class CheckAuthorizationUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    operator fun invoke(): Boolean {
        return appRepository.isAuthorized()
    }
}