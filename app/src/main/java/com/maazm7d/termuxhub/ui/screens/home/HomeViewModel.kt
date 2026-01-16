package com.maazm7d.termuxhub.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maazm7d.termuxhub.data.repository.ToolRepository
import com.maazm7d.termuxhub.domain.mapper.toDomain
import com.maazm7d.termuxhub.domain.model.Tool
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val tools: List<Tool> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ToolRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        repository.observeAll()
            .map { entities ->
                HomeUiState(
                    tools = entities.map { it.toDomain() }
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState()
            )

    private val _starsMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val starsMap: StateFlow<Map<String, Int>> = _starsMap.asStateFlow()

    init {
        fetchStarsOnce()
    }

    fun toggleFavorite(toolId: String) {
        viewModelScope.launch {
            val tool = repository.getToolById(toolId) ?: return@launch
            repository.setFavorite(toolId, !tool.isFavorite)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshFromRemote()
        }
    }

    private fun fetchStarsOnce() {
        viewModelScope.launch {
            try {
                val stars = repository.fetchStars()
                if (stars.isNotEmpty()) {
                    _starsMap.value = stars
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
