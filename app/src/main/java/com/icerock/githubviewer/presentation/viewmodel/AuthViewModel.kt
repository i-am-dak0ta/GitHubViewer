package com.icerock.githubviewer.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icerock.githubviewer.R
import com.icerock.githubviewer.domain.usecase.CheckAuthorizationUseCase
import com.icerock.githubviewer.domain.usecase.SignInUseCase
import com.icerock.githubviewer.util.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val checkAuthorizationUseCase: CheckAuthorizationUseCase
) : ViewModel() {

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> = _token

    private val _state = MutableLiveData<State>(State.Idle)
    val state: LiveData<State> get() = _state

    private val _actions = Channel<Action>(Channel.BUFFERED)
    val actions: Flow<Action> = _actions.receiveAsFlow()

    private val tokenValidationPattern = "^[a-z_0-9]+$".toRegex(RegexOption.IGNORE_CASE)

    init {
        checkAuthorization()
    }

    private fun checkAuthorization() {
        viewModelScope.launch {
            if (checkAuthorizationUseCase()) {
                _actions.send(Action.NavigateToRepositoriesList)
            }
        }
    }

    fun onSignButtonPressed() {
        val currentToken = _token.value

        val validationErrorResId = validateToken(currentToken)
        if (validationErrorResId != null) {
            _state.value = State.InvalidInput(validationErrorResId)
            return
        }

        viewModelScope.launch {
            _state.value = State.Loading
            val result = signInUseCase(currentToken!!)
            result.onSuccess {
                _state.value = State.Idle
                _actions.send(Action.NavigateToRepositoriesList)
            }.onFailure { error ->
                handleError(error)
            }
        }
    }

    fun onTextChanged(newText: String) {
        _token.value = newText
        if (_state.value is State.InvalidInput) {
            _state.value = State.Idle
        }
    }

    private fun validateToken(token: String?): Int? {
        return when {
            token.isNullOrBlank() -> R.string.error_empty_token
            !token.matches(tokenValidationPattern) -> R.string.error_invalid_characters_in_token
            else -> null
        }
    }

    private suspend fun handleError(error: Throwable) {
        val errorMessageResId = ErrorHandler.getErrorMessageResId(error)

        if (error is IllegalArgumentException || error is HttpException) {
            _state.value = State.InvalidInput(errorMessageResId)
        } else {
            _state.value = State.InvalidInput(errorMessageResId)
            _actions.send(Action.ShowError(errorMessageResId))
        }
    }

    sealed interface State {
        object Idle : State
        object Loading : State
        data class InvalidInput(val reason: Int) : State
    }

    sealed interface Action {
        data class ShowError(val messageResId: Int) : Action
        object NavigateToRepositoriesList : Action
    }
}