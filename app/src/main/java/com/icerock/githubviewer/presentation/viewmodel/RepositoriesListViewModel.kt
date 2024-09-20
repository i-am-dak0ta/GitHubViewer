package com.icerock.githubviewer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icerock.githubviewer.domain.entity.Repo
import com.icerock.githubviewer.domain.usecase.GetRepositoriesUseCase
import com.icerock.githubviewer.domain.usecase.LogOutUseCase
import com.icerock.githubviewer.util.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepositoriesListViewModel @Inject constructor(
    private val getRepositoriesUseCase: GetRepositoriesUseCase,
    private val logOutUseCase: LogOutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _actions = Channel<Action>(Channel.BUFFERED)
    val actions: Flow<Action> = _actions.receiveAsFlow()

    fun loadRepositories() {
        _state.value = State.Loading
        viewModelScope.launch {
            val result = getRepositoriesUseCase()
            result.onSuccess { repos ->
                if (repos.isEmpty()) {
                    _state.value = State.Empty
                } else {
                    _state.value = State.Success(repos)
                }
            }.onFailure { error ->
                val (errorType, errorMessageResId) = ErrorHandler.getErrorTypeAndMessageResId(error)
                _state.value = State.Error(errorType, errorMessageResId)
            }
        }
    }

    fun onRepoSelected(repo: Repo) {
        viewModelScope.launch {
            _actions.send(Action.NavigateToDetails(repo.id.toString()))
        }
    }

    fun logout() {
        viewModelScope.launch {
            logOutUseCase()
            _actions.send(Action.NavigateToAuth)
        }
    }

    sealed interface State {
        object Loading : State
        data class Success(val repos: List<Repo>) : State
        data class Error(val errorType: ErrorHandler.ErrorType, val errorMessageResId: Int) : State
        object Empty : State
    }

    sealed interface Action {
        data class NavigateToDetails(val repoId: String) : Action
        object NavigateToAuth : Action
    }


}
