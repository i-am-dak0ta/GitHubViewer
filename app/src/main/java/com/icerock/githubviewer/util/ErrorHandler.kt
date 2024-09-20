package com.icerock.githubviewer.util

import com.icerock.githubviewer.R
import retrofit2.HttpException
import java.io.IOException

object ErrorHandler {

    sealed class ErrorType {
        object ConnectionError : ErrorType()
        object EmptyError : ErrorType()
        object HttpError : ErrorType()
        object InvalidInput : ErrorType()
        object UnknownError : ErrorType()
    }

    fun getErrorTypeAndMessageResId(error: Throwable): Pair<ErrorType, Int> {
        return when (error) {
            is IllegalArgumentException -> ErrorType.InvalidInput to R.string.error_invalid_characters_in_token
            is HttpException -> {
                val errorType = ErrorType.HttpError
                val errorMessageResId = when (error.code()) {
                    301 -> R.string.error_moved_permanently
                    400 -> R.string.error_bad_request
                    401 -> R.string.error_unauthorized
                    403 -> R.string.error_forbidden
                    404 -> R.string.error_not_found
                    500 -> R.string.error_server
                    else -> R.string.error_unknown
                }
                errorType to errorMessageResId
            }

            is IOException -> ErrorType.ConnectionError to R.string.error_network
            else -> ErrorType.UnknownError to R.string.error_unknown
        }
    }

    fun getErrorMessageResId(error: Throwable): Int {
        return getErrorTypeAndMessageResId(error).second
    }
}
