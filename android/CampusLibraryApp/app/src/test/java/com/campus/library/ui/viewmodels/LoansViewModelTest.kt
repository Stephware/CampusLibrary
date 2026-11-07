package com.campus.library.ui.viewmodels

import com.campus.library.data.api.LibraryApi
import com.campus.library.data.models.*
import com.campus.library.data.repository.LibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class LoansViewModelTest {
    @Test
    fun returnRequiresDisplayedLoanAndRefreshesOnceAfterSuccess() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val api = FakeApi()
            val model = LoansViewModel(LibraryRepository(api, dispatcher))
            model.loadLoans()
            model.loadLoans()
            runCurrent()
            assertEquals(1, api.loads)
            assertEquals(DemoMember.ID, api.memberId)
            assertEquals(1, (model.uiState.value as LoansUiState.Success).loans.size)
            model.returnBook(99)
            model.returnBook(7)
            model.returnBook(7)
            runCurrent()
            assertEquals(listOf(7), api.returns)
            assertEquals(2, api.loads)
            assertTrue((model.uiState.value as LoansUiState.Success).loans.isEmpty())
        } finally { Dispatchers.resetMain() }
    }

    @Test
    fun failedReturnKeepsLoanAndEnablesRetry() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val api = FakeApi().apply { failReturn = true }
            val model = LoansViewModel(LibraryRepository(api, dispatcher))
            model.loadLoans()
            runCurrent()
            model.returnBook(7)
            runCurrent()
            val state = model.uiState.value as LoansUiState.Success
            assertEquals(7, state.loans.single().id)
            assertTrue(state.isReturning.isEmpty())
            assertNotNull(state.message)
            assertEquals(1, api.loads)
        } finally { Dispatchers.resetMain() }
    }

    private class FakeApi : LibraryApi {
        var loads = 0
        var memberId = 0
        var failReturn = false
        val returns = mutableListOf<Int>()
        override suspend fun getMemberLoans(memberId: Int, openOnly: Boolean): List<Loan> {
            loads++
            this.memberId = memberId
            return if (returns.isEmpty() || failReturn) listOf(loan(7, null), loan(8, "2026-11-08")) else emptyList()
        }
        override suspend fun returnBook(loanId: Int): Response<Unit> {
            returns.add(loanId)
            if (failReturn) throw java.io.IOException("offline")
            return Response.success(null)
        }
        override suspend fun getBooks(): List<Book> = emptyList()
        override suspend fun getBook(id: Int): Book = error("unused")
        override suspend fun borrowBook(request: CreateLoanRequest): Loan = error("unused")
        private fun loan(id: Int, returned: String?) = Loan(id, 2, 1, "2026-11-07", "2026-11-21", returned)
    }
}
