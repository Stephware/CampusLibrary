package com.campus.library.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campus.library.ui.viewmodels.BookDetailUiState
import com.campus.library.ui.viewmodels.BookDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Int,
    onBackClick: () -> Unit,
    onBorrowSuccess: () -> Unit,
    viewModel: BookDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is BookDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is BookDetailUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .heightIn(min = maxHeight)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = state.book.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (state.book.author != null) {
                            Text(
                                text = "Author: ${state.book.author}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Text(
                            text = "ISBN: ${state.book.isbn}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (state.book.yearPublished != null) {
                            Text(
                                text = "Published: ${state.book.yearPublished}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { viewModel.borrowBook(bookId) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isBorrowing
                        ) {
                            if (state.isBorrowing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (state.isBorrowing) "Borrowing..." else "Borrow This Book")
                        }
                    }
                }
                is BookDetailUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadBook(bookId) }) {
                            Text("Retry")
                        }
                    }
                }
                is BookDetailUiState.BorrowSuccess -> {
                    // Will show dialog instead
                }
            }
        }
    }

    if (uiState is BookDetailUiState.BorrowSuccess) {
        AlertDialog(
            onDismissRequest = {
                onBorrowSuccess()
            },
            title = { Text("Success") },
            text = { Text((uiState as? BookDetailUiState.BorrowSuccess)?.message ?: "Book borrowed successfully!") },
            confirmButton = {
                TextButton(onClick = {
                    onBorrowSuccess()
                }) {
                    Text("OK")
                }
            }
        )
    }
}
