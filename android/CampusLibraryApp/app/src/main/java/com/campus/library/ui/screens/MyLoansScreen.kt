package com.campus.library.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campus.library.data.models.Loan
import com.campus.library.ui.viewmodels.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun MyLoansScreen(
    onBackClick: () -> Unit,
    borrowConfirmation: String? = null,
    onBorrowConfirmationShown: () -> Unit = {},
    viewModel: LoansViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    RefreshOnResume { viewModel.loadLoans() }
    // Run once for this route entry; clearing the message must not cancel the snackbar.
    LaunchedEffect(Unit) {
        if (borrowConfirmation != null) {
            onBorrowConfirmationShown()
            snackbar.showSnackbar(borrowConfirmation)
        }
    }
    val message = (uiState as? LoansUiState.Success)?.message
    LaunchedEffect(message) {
        if (message != null) { snackbar.showSnackbar(message); viewModel.clearMessage() }
    }
    LibraryScaffold("loans", { viewModel.loadLoans() }, onBackClick, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                LoansUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is LoansUiState.Error -> StateMessage("Loans unavailable", state.message, "Try again", { viewModel.loadLoans() })
                is LoansUiState.Success -> {
                    if (state.loans.isEmpty()) StateMessage("Nothing on loan", "You’re all caught up. Browse the catalog to find your next book.", "Browse catalog", onBackClick)
                    else LazyColumn {
                        item { PageHeading("My loans", "${state.loans.size} active ${if (state.loans.size == 1) "loan" else "loans"} · Keep track of your reading.") }
                        items(state.loans, key = { it.id }) { loan ->
                            LoanCard(loan, loan.id in state.isReturning, { viewModel.returnBook(loan.id) }, state.isReturning.isEmpty())
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun LoanCard(loan: Loan, isReturning: Boolean, onReturnClick: () -> Unit, canReturn: Boolean = true) {
    val overdue = runCatching { LocalDate.parse(loan.dueDate.substringBefore("T")).isBefore(LocalDate.now()) }.getOrDefault(false)
    val title = loan.bookTitle?.takeIf { it.isNotBlank() }
        ?: loan.book?.title?.takeIf { it.isNotBlank() }
        ?: "Book #${loan.bookId}"
    val author = loan.bookAuthor?.takeIf { it.isNotBlank() }
        ?: loan.book?.author?.takeIf { it.isNotBlank() }
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            author?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(16.dp))
            Text(if (overdue) "Overdue · Please return this book" else "On loan", style = MaterialTheme.typography.labelLarge, color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Metadata("Borrowed", formatDate(loan.borrowDate))
            Metadata("Due", formatDate(loan.dueDate))
            Spacer(Modifier.height(20.dp))
            OutlinedButton(onClick = onReturnClick, enabled = canReturn && !isReturning, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                if (isReturning) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                }
                Text(if (isReturning) "Returning…" else "Return book")
            }
        }
    }
    HorizontalDivider(Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

internal fun formatDate(value: String): String = runCatching {
    LocalDate.parse(value.substringBefore("T")).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
}.getOrDefault(value.substringBefore("T"))
