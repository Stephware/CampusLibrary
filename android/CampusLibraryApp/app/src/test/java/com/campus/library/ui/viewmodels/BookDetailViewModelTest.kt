package com.campus.library.ui.viewmodels

import com.campus.library.data.api.LibraryApi
import com.campus.library.data.models.Book
import com.campus.library.data.models.CreateLoanRequest
import com.campus.library.data.models.Loan
import com.campus.library.data.repository.LibraryRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class BookDetailViewModelTest {
    @Test
    fun latePreviousBookDoesNotReplaceCurrentDetail() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val api = FakeApi()
            val viewModel = BookDetailViewModel(LibraryRepository(api, dispatcher))
            viewModel.loadBook(1)
            runCurrent()
            viewModel.loadBook(2)
            runCurrent()
            api.slowBook.complete(book(1))
            runCurrent()
            assertEquals(2, (viewModel.uiState.value as BookDetailUiState.Success).book.id)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun borrowMustMatchDisplayedBook() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val api = FakeApi()
            val viewModel = BookDetailViewModel(LibraryRepository(api, dispatcher))
            viewModel.loadBook(2)
            runCurrent()
            viewModel.borrowBook(1)
            runCurrent()
            assertTrue(api.borrowedBookIds.isEmpty())
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class FakeApi : LibraryApi {
        val slowBook = CompletableDeferred<Book>()
        val borrowedBookIds = mutableListOf<Int>()
        override suspend fun getBook(id: Int): Book = if (id == 1) slowBook.await() else book(id)
        override suspend fun getBooks(): List<Book> = emptyList()
        override suspend fun getMemberLoans(memberId: Int, openOnly: Boolean): List<Loan> = emptyList()
        override suspend fun borrowBook(request: CreateLoanRequest): Loan {
            borrowedBookIds.add(request.bookId)
            return Loan(1, request.bookId, request.memberId, "2026-11-07T00:00:00", "2026-11-21T00:00:00", null)
        }
        override suspend fun returnBook(loanId: Int): Response<Unit> = Response.success(null)
    }

    companion object {
        private fun book(id: Int) = Book(id, "Book $id", "isbn$id", null, null)
    }
}
