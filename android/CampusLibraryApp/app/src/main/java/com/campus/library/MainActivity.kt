package com.campus.library

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.campus.library.ui.screens.BookDetailScreen
import com.campus.library.ui.screens.BookListScreen
import com.campus.library.ui.screens.MyLoansScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusLibraryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CampusLibraryApp()
                }
            }
        }
    }
}

@Composable
fun CampusLibraryApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.BookList) }

    BackHandler(enabled = currentScreen != Screen.BookList) {
        currentScreen = Screen.BookList
    }

    when (val screen = currentScreen) {
        is Screen.BookList -> {
            BookListScreen(
                onBookClick = { bookId ->
                    currentScreen = Screen.BookDetail(bookId)
                },
                onMyLoansClick = {
                    currentScreen = Screen.MyLoans
                }
            )
        }
        is Screen.BookDetail -> {
            BookDetailScreen(
                bookId = screen.bookId,
                onBackClick = {
                    currentScreen = Screen.BookList
                },
                onBorrowSuccess = {
                    currentScreen = Screen.MyLoans
                }
            )
        }
        is Screen.MyLoans -> {
            MyLoansScreen(
                onBackClick = {
                    currentScreen = Screen.BookList
                }
            )
        }
    }
}

sealed class Screen {
    object BookList : Screen()
    data class BookDetail(val bookId: Int) : Screen()
    object MyLoans : Screen()
}

@Composable
fun CampusLibraryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content
    )
}
