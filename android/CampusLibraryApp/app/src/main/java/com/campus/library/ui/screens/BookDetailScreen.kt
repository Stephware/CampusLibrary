package com.campus.library.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campus.library.ui.viewmodels.*

@Composable
fun BookDetailScreen(bookId: Int, onBackClick: () -> Unit, onBorrowSuccess: () -> Unit, viewModel: BookDetailViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume { viewModel.loadBook(bookId) }
    LaunchedEffect(uiState) {
        if (uiState is BookDetailUiState.BorrowSuccess) {
            viewModel.acknowledgeBorrowSuccess()
            onBorrowSuccess()
        }
    }
    LibraryScaffold("book details", { viewModel.loadBook(bookId) }, onBackClick) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                BookDetailUiState.Loading, is BookDetailUiState.BorrowSuccess -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is BookDetailUiState.Error -> StateMessage("Book unavailable", state.message, "Try again", { viewModel.loadBook(bookId) })
                is BookDetailUiState.Success -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    PageHeading(state.book.title, state.book.author ?: "Author not recorded")
                    Surface(color = MaterialTheme.colorScheme.surface) {
                        Column(Modifier.fillMaxWidth().padding(24.dp)) {
                            CirculationStatus(state.book.isAvailable)
                            Spacer(Modifier.height(24.dp))
                            Metadata("ISBN", state.book.isbn)
                            Metadata("Published", state.book.yearPublished?.toString() ?: "Not recorded")
                            Metadata("Catalog ID", "#${state.book.id}")
                        }
                    }
                    Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Borrowing this title", style = MaterialTheme.typography.titleLarge)
                        Text(if (state.book.isAvailable) "Borrow for demo member #1. Your due date will appear in My loans once confirmed." else "This copy is currently on loan. It remains in the catalog; refresh to check when it’s available again.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        state.message?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
                        Button(onClick = { viewModel.borrowBook(bookId) }, enabled = state.book.isAvailable && !state.isBorrowing, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                            if (state.isBorrowing) {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                Spacer(Modifier.width(12.dp))
                            }
                            Text(if (state.isBorrowing) "Borrowing…" else if (state.book.isAvailable) "Borrow book" else "On loan")
                        }
                    }
                }
            }
        }
    }
}
