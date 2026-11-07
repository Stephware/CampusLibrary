package com.campus.library.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campus.library.data.models.DemoMember
import com.campus.library.data.models.Loan
import com.campus.library.data.repository.LibraryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoansUiState {
    object Loading : LoansUiState()
    data class Success(val loans: List<Loan>, val isReturning: Set<Int> = emptySet(), val message: String? = null) : LoansUiState()
    data class Error(val message: String) : LoansUiState()
}

class LoansViewModel(private val repository: LibraryRepository = LibraryRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow<LoansUiState>(LoansUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun loadLoans(message: String? = null) {
        if (loadJob?.isActive == true || (_uiState.value as? LoansUiState.Success)?.isReturning?.isNotEmpty() == true) return
        loadJob = viewModelScope.launch {
            _uiState.value = LoansUiState.Loading
            repository.getMemberLoans(DemoMember.ID).fold(
                onSuccess = { _uiState.value = LoansUiState.Success(it.filter { loan -> loan.returnDate == null }, message = message) },
                onFailure = { _uiState.value = LoansUiState.Error("Your loans couldn’t be loaded. Check your connection and try again.") }
            )
        }
    }

    fun returnBook(loanId: Int) {
        val state = _uiState.value as? LoansUiState.Success ?: return
        if (state.isReturning.isNotEmpty() || state.loans.none { it.id == loanId }) return
        _uiState.value = state.copy(isReturning = setOf(loanId), message = null)
        viewModelScope.launch {
            repository.returnBook(loanId).fold(
                onSuccess = {
                    _uiState.value = state.copy(loans = state.loans.filterNot { it.id == loanId })
                    loadLoans("Book returned. Thank you.")
                },
                onFailure = { _uiState.value = state.copy(message = "The return wasn’t confirmed. Try again; your loan is still shown here.") }
            )
        }
    }
    fun clearMessage() {
        val state = _uiState.value as? LoansUiState.Success ?: return
        _uiState.value = state.copy(message = null)
    }
}
