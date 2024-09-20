package com.icerock.githubviewer.presentation.viewmodel

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icerock.githubviewer.R
import com.icerock.githubviewer.domain.entity.RepoDetails
import com.icerock.githubviewer.domain.usecase.GetRepositoryDetailsUseCase
import com.icerock.githubviewer.domain.usecase.GetRepositoryReadmeUseCase
import com.icerock.githubviewer.domain.usecase.LogOutUseCase
import com.icerock.githubviewer.util.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class RepositoryInfoViewModel @Inject constructor(
    private val getRepositoryDetailsUseCase: GetRepositoryDetailsUseCase,
    private val getRepositoryReadmeUseCase: GetRepositoryReadmeUseCase,
    private val logOutUseCase: LogOutUseCase
) : ViewModel() {
    private val _state = MutableLiveData<State>(State.Loading)
    val state: LiveData<State> get() = _state

    private val _actions = Channel<Action>(Channel.BUFFERED)
    val actions: Flow<Action> = _actions.receiveAsFlow()

    fun loadRepositoryDetails(repoId: String) {
        viewModelScope.launch {
            _state.value = State.Loading
            val result = getRepositoryDetailsUseCase(repoId)
            result.onSuccess { repoDetails ->
                _state.value = State.Loaded(repoDetails, ReadmeState.Loading)
                loadRepositoryReadme(repoDetails)
            }.onFailure { error ->
                val (errorType, errorMessageResId) = ErrorHandler.getErrorTypeAndMessageResId(error)
                _state.value = State.Error(errorType,errorMessageResId)
            }
        }
    }


    private fun loadRepositoryReadme(repoDetails: RepoDetails) {
        viewModelScope.launch {
            val result = getRepositoryReadmeUseCase(
                repoDetails.owner.login,
                repoDetails.name,
                repoDetails.defaultBranch
            )
            result.onSuccess { readme ->
                val decodedBytes = Base64.decode(readme.content, Base64.DEFAULT)
                val decodedString = String(decodedBytes)
                val readmeContent = if (decodedString.isBlank()) {
                    ReadmeState.Empty
                } else {
                    ReadmeState.Loaded(readme.content)
                }
                _state.value = State.Loaded(
                    repoDetails,
                    readmeContent
                )
            }.onFailure { error ->
                var (errorType, errorMessageResId) = ErrorHandler.getErrorTypeAndMessageResId(error)
                if (error is HttpException && error.code() == 404) errorMessageResId =
                    R.string.error_no_readme
                _state.value = State.Loaded(
                    repoDetails,
                    ReadmeState.Error(errorType, errorMessageResId)
                )
            }
        }
    }

    fun back() {
        viewModelScope.launch {
            _actions.send(Action.NavigateToRepositoriesList)
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
        data class Error(val errorType: ErrorHandler.ErrorType, val errorMessageResId: Int) : State
        data class Loaded(
            val githubRepo: RepoDetails,
            val readmeState: ReadmeState
        ) : State
    }

    sealed interface ReadmeState {
        object Loading : ReadmeState
        object Empty : ReadmeState
        data class Error(val errorType: ErrorHandler.ErrorType, val errorMessageResId: Int) :
            ReadmeState

        data class Loaded(val markdown: String) : ReadmeState
    }

    sealed interface Action {
        object NavigateToRepositoriesList : Action
        object NavigateToAuth : Action
    }
}