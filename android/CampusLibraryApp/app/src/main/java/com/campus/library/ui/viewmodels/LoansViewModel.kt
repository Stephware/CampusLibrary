package com.campus.library.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campus.library.data.models.Loan
import com.campus.library.data.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoansUiState {
    object Loading : LoansUiState()
    data class Success(val loans: List<Loan>, val isReturning: Set<Int> = emptySet()) : LoansUiState()
    data class Error(val message: String) : LoansUiState()
}

class LoansViewModel : ViewModel() {
    private val repository = LibraryRepository()
    // Hardcoded member ID for demo (Alice Johnson - S001)
    private val currentMemberId = 1

    private val _uiState = MutableStateFlow<LoansUiState>(LoansUiState.Loading)
    val uiState: StateFlow<LoansUiState> = _uiState.asStateFlow()

    fun loadLoans() {
        viewModelScope.launch {
            _uiState.value = LoansUiState.Loading
            repository.getMemberLoans(currentMemberId).fold(
                onSuccess = { loans ->
                    val activeLoans = loans.filter { it.returnDate == null }
                    _uiState.value = LoansUiState.Success(activeLoans)
                },
                onFailure = { error ->
                    _uiState.value = LoansUiState.Error(
                        error.message ?: "Failed to load loans"
                    )
                }
            )
        }
    }

    fun returnBook(loanId: Int) {
        val currentState = _uiState.value
        if (currentState is LoansUiState.Success && loanId !in currentState.isReturning) {
            viewModelScope.launch {
                _uiState.value = currentState.copy(
                    isReturning = currentState.isReturning + loanId
                )
                repository.returnBook(loanId).fold(
                    onSuccess = {
                        loadLoans() // Reload to update list
                    },
                    onFailure = { error ->
                        _uiState.value = LoansUiState.Error(
                            error.message ?: "Failed to return book"
                        )
                    }
                )
            }
        }
    }
}
