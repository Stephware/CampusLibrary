package com.campus.library.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campus.library.data.models.Book
import com.campus.library.data.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BooksUiState {
    object Loading : BooksUiState()
    data class Success(val books: List<Book>) : BooksUiState()
    data class Error(val message: String) : BooksUiState()
}

class BooksViewModel : ViewModel() {
    private val repository = LibraryRepository()

    private val _uiState = MutableStateFlow<BooksUiState>(BooksUiState.Loading)
    val uiState: StateFlow<BooksUiState> = _uiState.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _uiState.value = BooksUiState.Loading
            repository.getBooks().fold(
                onSuccess = { books ->
                    _uiState.value = BooksUiState.Success(books)
                },
                onFailure = { error ->
                    _uiState.value = BooksUiState.Error(
                        error.message ?: "Failed to load books"
                    )
                }
            )
        }
    }
}
