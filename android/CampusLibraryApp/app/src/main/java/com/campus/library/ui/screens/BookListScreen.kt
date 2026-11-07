package com.campus.library.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campus.library.data.models.Book
import com.campus.library.ui.viewmodels.*

@Composable
fun BookListScreen(onBookClick: (Int) -> Unit, onMyLoansClick: () -> Unit, viewModel: BooksViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume { viewModel.loadBooks() }
    LibraryScaffold("catalog", viewModel::loadBooks, actions = {
        TextButton(onClick = onMyLoansClick, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSecondaryContainer), modifier = Modifier.heightIn(min = 48.dp)) { Text("My loans") }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                BooksUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is BooksUiState.Error -> StateMessage("Catalog unavailable", state.message, "Try again", viewModel::loadBooks)
                is BooksUiState.Success -> {
                    if (state.books.isEmpty()) StateMessage("The shelves are quiet", "There are no titles in the catalog yet. Refresh after your librarian adds a book.", "Refresh catalog", viewModel::loadBooks)
                    else LazyColumn {
                        item { PageHeading("The catalog", "${state.books.size} titles · Find your next read.") }
                        items(state.books, key = { it.id }) { book -> BookListItem(book) { onBookClick(book.id) } }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun BookListItem(book: Book, onClick: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().clickable(role = Role.Button, onClickLabel = "View book details", onClick = onClick).padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text(book.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(book.author ?: "Author not recorded", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Metadata("ISBN", book.isbn)
            Metadata("Published", book.yearPublished?.toString() ?: "Not recorded")
            Spacer(Modifier.height(12.dp))
            CirculationStatus(book.isAvailable)
        }
    }
    HorizontalDivider(Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.outlineVariant)
}
