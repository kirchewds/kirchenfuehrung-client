package de.kirchewds.kirchenfuehrung.client.ui.viewer

import android.util.Log
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.kirchewds.kirchenfuehrung.client.R
import de.kirchewds.kirchenfuehrung.client.data.ToursRepository
import de.kirchewds.kirchenfuehrung.client.model.Tour
import de.kirchewds.kirchenfuehrung.client.playback.PlayerConnection
import de.kirchewds.kirchenfuehrung.client.util.ErrorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ViewerUiState {
    object Loading: ViewerUiState
    data class Error(val errorMessages: List<ErrorMessage>): ViewerUiState
    data class Play(val tour: Tour): ViewerUiState
}

private data class ViewerViewModelState(
    val tour: Tour? = null,
    val isLoading: Boolean = false,
    val errorMessages: List<ErrorMessage> = emptyList()
) {
    fun toUiState(): ViewerUiState =
        if (isLoading) ViewerUiState.Loading
        else if (errorMessages.isNotEmpty()) ViewerUiState.Error(errorMessages)
        else if (tour == null) ViewerUiState.Error(listOf(ErrorMessage(R.string.error_not_found)))
        else ViewerUiState.Play(tour)
}

// id == null is equivalent to the highlighted tour
class ViewerViewModel(
    private val toursRepository: ToursRepository,
    private val id: String?
): ViewModel() {
    private val viewModelState = MutableStateFlow(
        ViewerViewModelState(isLoading = true)
    )

    val uiState = viewModelState
        .map(ViewerViewModelState::toUiState)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )

    init {
        refreshTour()
    }

    fun refreshTour() {
        viewModelState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val tour = toursRepository.getTour(id!!)
            viewModelState.update { state ->
                tour.fold(onSuccess = {
                    state.copy(
                        tour = it,
                        isLoading = false
                    )
                }, onFailure = {
                    Log.e("ViewerViewModel", "Could not load tour", it)
                    state.copy(
                        errorMessages = state.errorMessages + ErrorMessage(R.string.error_tour_load),
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
            toursRepository: ToursRepository,
            id: String?
        ): ViewModelProvider.Factory = object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ViewerViewModel(toursRepository, id) as T
            }
        }
    }
}

val LocalPlayerConnection = staticCompositionLocalOf<PlayerConnection?> { error("No PlayerConnection provided") }
var skipHeadphonesDialog = false // Yes, this is global mutable state. No, I will not change it.