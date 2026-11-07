package com.campus.library.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campus.library.data.models.Book
import com.campus.library.data.models.DemoMember
import com.campus.library.data.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class BookDetailUiState {
    object Loading : BookDetailUiState()
    data class Success(val book: Book, val isBorrowing: Boolean = false, val message: String? = null) : BookDetailUiState()
    data class Error(val message: String) : BookDetailUiState()
    data class BorrowSuccess(val message: String) : BookDetailUiState()
}

class BookDetailViewModel(private val repository: LibraryRepository = LibraryRepository()) : ViewModel() {
    private var loadJob: Job? = null
    private var loadingBookId: Int? = null
    private val _uiState = MutableStateFlow<BookDetailUiState>(BookDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadBook(bookId: Int) {
        if ((_uiState.value as? BookDetailUiState.Success)?.isBorrowing == true) return
        if (loadJob?.isActive == true && loadingBookId == bookId) return
        loadJob?.cancel()
        loadingBookId = bookId
        loadJob = viewModelScope.launch {
            _uiState.value = BookDetailUiState.Loading
            repository.getBook(bookId).fold(
                onSuccess = { _uiState.value = BookDetailUiState.Success(it) },
                onFailure = { _uiState.value = BookDetailUiState.Error("This book couldn’t be loaded. Check your connection and try again.") }
            )
        }
    }

    /** Consume navigation feedback so reopening this screen does not navigate again. */
    fun acknowledgeBorrowSuccess() {
        if (_uiState.value is BookDetailUiState.BorrowSuccess)
            _uiState.value = BookDetailUiState.Loading
    }

    fun borrowBook(bookId: Int) {
        val state = _uiState.value as? BookDetailUiState.Success ?: return
        if (state.book.id != bookId || state.isBorrowing || !state.book.isAvailable) return
        _uiState.value = state.copy(isBorrowing = true, message = null)
        viewModelScope.launch {
            repository.borrowBook(bookId, DemoMember.ID).fold(
                onSuccess = { _uiState.value = BookDetailUiState.BorrowSuccess("Book borrowed. You’ll find it in My loans.") },
                onFailure = { error ->
                    val conflict = (error as? HttpException)?.code() == 409
                    _uiState.value = state.copy(
                        book = if (conflict) state.book.copy(isAvailable = false) else state.book,
                        message = if (conflict) "This book is now on loan. Please choose another title." else "Borrowing wasn’t confirmed. Check your connection, refresh the book and try again."
                    )
                }
            )
        }
    }
}
