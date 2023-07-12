package io.gitlab.jfronny.kirchenfuehrung.client.ui.overview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.gitlab.jfronny.kirchenfuehrung.client.R
import io.gitlab.jfronny.kirchenfuehrung.client.data.ToursRepository
import io.gitlab.jfronny.kirchenfuehrung.client.model.Tour
import io.gitlab.jfronny.kirchenfuehrung.client.util.ErrorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface OverviewUiState {
    object Loading: OverviewUiState
    data class Error(val errorMessages: List<ErrorMessage>): OverviewUiState
    object Empty: OverviewUiState
    data class Tours(val highlighted: Tour, val other: List<Tour>): OverviewUiState
}

private data class OverviewViewModelState(
    val highlighted: Tour? = null,
    val other: List<Tour>? = null,
    val isLoading: Boolean = false,
    val errorMessages: List<ErrorMessage> = emptyList()
) {
    fun toUiState(): OverviewUiState =
        if (isLoading) OverviewUiState.Loading
        else if (errorMessages.isNotEmpty()) OverviewUiState.Error(errorMessages)
        else if (highlighted == null) OverviewUiState.Empty
        else OverviewUiState.Tours(highlighted, other ?: emptyList())
}

class OverviewViewModel(
    private val toursRepository: ToursRepository
): ViewModel() {
    private val viewModelState = MutableStateFlow(
        OverviewViewModelState(isLoading = true)
    )

    val uiState = viewModelState
        .map(OverviewViewModelState::toUiState)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )

    init {
        refreshTours()
    }

    fun refreshTours() {
        viewModelState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val tours = toursRepository.getTours()
            viewModelState.update { state ->
                tours.fold(onSuccess = {
                    state.copy(
                        highlighted = it.highlight,
                        other = it.secondary.values.toList(),
                        isLoading = false
                    )
                }, onFailure = {
                    Log.e("OverviewViewModel", "Could not load tours", it)
                    state.copy(
                        errorMessages = state.errorMessages + ErrorMessage(R.string.error_tours_load),
                        isLoading = false
                    )
                })
            }
        }
    }

    fun errorShown(errorId: Long) {
        viewModelState.update {
            it.copy(errorMessages = it.errorMessages.filterNot { it.id == errorId })
        }
    }

    companion object {
        fun provideFactory(
            toursRepository: ToursRepository
        ): ViewModelProvider.Factory = object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OverviewViewModel(toursRepository) as T
            }
        }
    }
}