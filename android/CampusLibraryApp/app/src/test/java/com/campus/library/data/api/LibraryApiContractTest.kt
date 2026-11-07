package com.campus.library.data.api

import java.net.ServerSocket
import kotlin.concurrent.thread
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LibraryApiContractTest {
    @Test
    fun availabilityIsReadFromAdditiveJsonAndMissingValueFailsClosed() {
        val gson = com.google.gson.Gson()
        val available = gson.fromJson("""{"id":2,"title":"Book","isbn":"123","isAvailable":true}""", com.campus.library.data.models.Book::class.java)
        val onLoan = gson.fromJson("""{"id":2,"title":"Book","isbn":"123","isAvailable":false}""", com.campus.library.data.models.Book::class.java)
        val legacy = gson.fromJson("""{"id":2,"title":"Book","isbn":"123"}""", com.campus.library.data.models.Book::class.java)
        assertTrue(available.isAvailable)
        org.junit.Assert.assertFalse(onLoan.isAvailable)
        org.junit.Assert.assertFalse(legacy.isAvailable)
    }

    @Test
    fun memberLoansUseQueryAndReturnAcceptsNoContent() = runBlocking {
        val requests = mutableListOf<String>()
        val server = ServerSocket(0)
        val worker = thread(isDaemon = true) {
            try {
                repeat(2) {
                    server.accept().use { socket ->
                        val reader = socket.getInputStream().bufferedReader()
                        val request = reader.readLine().substringBefore(" HTTP/")
                        requests.add(request)
                        while (!reader.readLine().isNullOrEmpty()) { }
                        val body = """[{"id":7,"bookId":2,"memberId":1,"borrowDate":"2026-11-07T00:00:00","dueDate":"2026-11-21T00:00:00","returnDate":null}]"""
                        val response = when (request) {
                            "GET /api/loans?memberId=1&openOnly=true" ->
                                "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: ${body.toByteArray().size}\r\nConnection: close\r\n\r\n$body"
                            "POST /api/loans/7/return" -> "HTTP/1.1 204 No Content\r\nConnection: close\r\n\r\n"
                            else -> "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
                        }
                        socket.getOutputStream().write(response.toByteArray())
                    }
                }
            } catch (e: java.net.SocketException) {
                if (!server.isClosed) throw e
            }
        }
        try {
            val api = Retrofit.Builder()
                .baseUrl("http://127.0.0.1:${server.localPort}/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(LibraryApi::class.java)
            val loans = api.getMemberLoans(1)
            assertEquals(7, loans.single().id)
            assertTrue(api.returnBook(7).isSuccessful)
            assertEquals(listOf("GET /api/loans?memberId=1&openOnly=true", "POST /api/loans/7/return"), requests)
        } finally {
            server.close()
            worker.join(1000)
        }
    }
}
