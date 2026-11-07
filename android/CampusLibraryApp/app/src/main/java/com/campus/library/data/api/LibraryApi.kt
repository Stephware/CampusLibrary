package com.campus.library.data.api

import com.campus.library.data.models.Book
import com.campus.library.data.models.CreateLoanRequest
import com.campus.library.data.models.Loan
import retrofit2.Response
import retrofit2.http.*

interface LibraryApi {
    @GET("books")
    suspend fun getBooks(): List<Book>

    @GET("books/{id}")
    suspend fun getBook(@Path("id") id: Int): Book

    @GET("loans")
    suspend fun getMemberLoans(
        @Query("memberId") memberId: Int,
        @Query("openOnly") openOnly: Boolean = true
    ): List<Loan>

    @POST("loans")
    suspend fun borrowBook(@Body request: CreateLoanRequest): Loan

    @POST("loans/{id}/return")
    suspend fun returnBook(@Path("id") loanId: Int): Response<Unit>
}
