package io.gitlab.jfronny.kirchenfuehrung.client.ui.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.gitlab.jfronny.kirchenfuehrung.client.data.ToursRepository
import io.gitlab.jfronny.kirchenfuehrung.client.model.Tour
import io.gitlab.jfronny.kirchenfuehrung.client.ui.overview.OverviewViewModel

// id == null is equivalent to the highlighted tour
class ViewerViewModel(private val toursRepository: ToursRepository, private val id: String?): ViewModel() {
    fun getTourId(): String = id ?: "null" //TODO remove

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