package com.campus.library.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campus.library.data.models.Book
import com.campus.library.data.repository.LibraryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BooksUiState {
    object Loading : BooksUiState()
    data class Success(val books: List<Book>) : BooksUiState()
    data class Error(val message: String) : BooksUiState()
}

class BooksViewModel(private val repository: LibraryRepository = LibraryRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow<BooksUiState>(BooksUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun loadBooks() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.value = BooksUiState.Loading
            repository.getBooks().fold(
                onSuccess = { _uiState.value = BooksUiState.Success(it) },
                onFailure = { _uiState.value = BooksUiState.Error("The catalog couldn’t be loaded. Check your connection and try again.") }
            )
        }
    }
}
