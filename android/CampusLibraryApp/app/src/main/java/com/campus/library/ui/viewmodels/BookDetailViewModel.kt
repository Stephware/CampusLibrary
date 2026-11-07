package com.campus.library.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campus.library.data.models.Book
import com.campus.library.data.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

sealed class BookDetailUiState {
    object Loading : BookDetailUiState()
    data class Success(val book: Book, val isBorrowing: Boolean = false) : BookDetailUiState()
    data class Error(val message: String) : BookDetailUiState()
    data class BorrowSuccess(val message: String) : BookDetailUiState()
}

class BookDetailViewModel(
    private val repository: LibraryRepository = LibraryRepository()
) : ViewModel() {
    private var loadJob: Job? = null
    // Hardcoded member ID for demo (Alice Johnson - S001)
    private val currentMemberId = 1

    private val _uiState = MutableStateFlow<BookDetailUiState>(BookDetailUiState.Loading)
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    fun loadBook(bookId: Int) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = BookDetailUiState.Loading
            repository.getBook(bookId).fold(
                onSuccess = { book ->
                    _uiState.value = BookDetailUiState.Success(book)
                },
                onFailure = { error ->
                    _uiState.value = BookDetailUiState.Error(
                        error.message ?: "Failed to load book"
                    )
                }
            )
        }
    }

    fun borrowBook(bookId: Int) {
        val currentState = _uiState.value
        if (currentState is BookDetailUiState.Success && currentState.book.id == bookId && !currentState.isBorrowing) {
            viewModelScope.launch {
                _uiState.value = currentState.copy(isBorrowing = true)
                repository.borrowBook(bookId, currentMemberId).fold(
                    onSuccess = {
                        _uiState.value = BookDetailUiState.BorrowSuccess(
                            "Book borrowed successfully!"
                        )
                    },
                    onFailure = { error ->
                        val message = error.message ?: "Failed to borrow book"
                        _uiState.value = BookDetailUiState.Error(
                            if (message.contains("409") || message.contains("already borrowed"))
                                "This book is already on loan"
                            else message
                        )
                    }
                )
            }
        }
    }
}
