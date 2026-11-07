package com.campus.library.data.repository

import com.campus.library.data.api.RetrofitClient
import com.campus.library.data.api.LibraryApi
import com.campus.library.data.models.Book
import com.campus.library.data.models.CreateLoanRequest
import com.campus.library.data.models.Loan
import retrofit2.HttpException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryRepository(
    private val api: LibraryApi = RetrofitClient.api,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getBooks(): Result<List<Book>> = withContext(dispatcher) {
        try {
            val books = api.getBooks()
            Result.success(books)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBook(id: Int): Result<Book> = withContext(dispatcher) {
        try {
            val book = api.getBook(id)
            Result.success(book)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMemberLoans(memberId: Int): Result<List<Loan>> = withContext(dispatcher) {
        try {
            val loans = api.getMemberLoans(memberId)
            Result.success(loans)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun borrowBook(bookId: Int, memberId: Int): Result<Loan> = withContext(dispatcher) {
        try {
            val request = CreateLoanRequest(bookId, memberId)
            val loan = api.borrowBook(request)
            Result.success(loan)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun returnBook(loanId: Int): Result<Unit> = withContext(dispatcher) {
        try {
            val response = api.returnBook(loanId)
            if (!response.isSuccessful) throw HttpException(response)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
