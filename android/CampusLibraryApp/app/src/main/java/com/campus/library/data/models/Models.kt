package com.campus.library.data.models

data class Book(
    val id: Int,
    val title: String,
    val isbn: String,
    val author: String?,
    val yearPublished: Int?,
    val isAvailable: Boolean = false
)

object DemoMember {
    const val ID = 1
    const val LABEL = "Demo member · #1"
}

data class Member(
    val id: Int,
    val name: String,
    val email: String,
    val studentId: String,
    val registeredDate: String
)

data class Loan(
    val id: Int,
    val bookId: Int,
    val memberId: Int,
    val borrowDate: String,
    val dueDate: String,
    val returnDate: String?,
    val book: Book? = null,
    val member: Member? = null,
    val bookTitle: String? = null,
    val bookAuthor: String? = null
)

data class CreateLoanRequest(
    val bookId: Int,
    val memberId: Int
)
