# How to Use — Books API CRUD

**Milestone:** 3 — Books API with Service-Repository Pattern  
**Date:** 2026-11-07  
**For:** Developers testing the API

---

## What Was Built

The **Books CRUD API** is now complete with:

1. **5 REST endpoints** (GET all, GET by ID, POST, PUT, DELETE)
2. **Service-Repository pattern** (Controller → Service → Repository → DbContext → SQL Server)
3. **Business logic** (duplicate ISBN validation)
4. **Structured logging** (all operations logged)
5. **Proper HTTP status codes** (200, 201, 204, 404, 409)

**Note:** Members and Loans endpoints come in Milestone 4.

---

## Prerequisites

Before you proceed, verify:

1. **SQL Server container running**
   ```bash
   docker ps --filter "name=campuslibrary-mssql"
   ```
   **Expected:** Status shows "Up" on port 14335

2. **Database has seed data**
   ```bash
   # From SQL client or check previous milestone
   ```
   **Expected:** 5 books in the Books table

3. **Solution builds**
   ```bash
   dotnet build
   ```
   **Expected:** Build succeeded with 0 errors

---

## Step-by-Step: Running the API

### Step 1: Start the API

```bash
cd /Users/stephen/CampusLibrary/src/CampusLibrary.WebApi
dotnet run
```

**Expected output:**
```
Now listening on: http://localhost:5143
Application started. Press Ctrl+C to shut down.
```

**Note:** Port 5143 is auto-assigned. Check the output for your actual port.

---

### Step 2: Test GET All Books

Open a new terminal and run:

```bash
curl http://localhost:5143/api/books
```

**Expected response:** JSON array with 5 books
```json
[
  {
    "id": 1,
    "title": "Clean Code",
    "isbn": "978-0132350884",
    "author": "Robert C. Martin",
    "yearPublished": 2008
  },
  {
    "id": 2,
    "title": "The Pragmatic Programmer",
    "isbn": "978-0201616224",
    "author": "Andrew Hunt",
    "yearPublished": 1999
  },
  ...
]
```

---

### Step 3: Test GET Book by ID

```bash
curl http://localhost:5143/api/books/1
```

**Expected response:** Single book object
```json
{
  "id": 1,
  "title": "Clean Code",
  "isbn": "978-0132350884",
  "author": "Robert C. Martin",
  "yearPublished": 2008
}
```

**Test non-existent book:**
```bash
curl http://localhost:5143/api/books/999
```
**Expected:** HTTP 404 Not Found

---

### Step 4: Test POST (Create New Book)

```bash
curl -X POST http://localhost:5143/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Refactoring",
    "isbn": "978-0201485677",
    "author": "Martin Fowler",
    "yearPublished": 1999
  }'
```

**Expected response:** HTTP 201 Created with `Location` header
```json
{
  "id": 6,
  "title": "Refactoring",
  "isbn": "978-0201485677",
  "author": "Martin Fowler",
  "yearPublished": 1999
}
```

**Verify it was created:**
```bash
curl http://localhost:5143/api/books/6
```

---

### Step 5: Test Duplicate ISBN Validation

Try to create the same book again:

```bash
curl -X POST http://localhost:5143/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Refactoring (Duplicate)",
    "isbn": "978-0201485677",
    "author": "Martin Fowler",
    "yearPublished": 1999
  }'
```

**Expected response:** HTTP 409 Conflict
```json
{
  "error": "A book with ISBN 978-0201485677 already exists."
}
```

---

### Step 6: Test PUT (Update Book)

```bash
curl -X PUT http://localhost:5143/api/books/6 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Refactoring: Improving the Design of Existing Code",
    "isbn": "978-0201485677",
    "author": "Martin Fowler",
    "yearPublished": 1999
  }'
```

**Expected response:** HTTP 204 No Content (success, no body)

**Verify the update:**
```bash
curl http://localhost:5143/api/books/6
```
**Expected:** Updated title

---

### Step 7: Test DELETE

```bash
curl -X DELETE http://localhost:5143/api/books/6
```

**Expected response:** HTTP 204 No Content

**Verify it was deleted:**
```bash
curl http://localhost:5143/api/books/6
```
**Expected:** HTTP 404 Not Found

---

## Using Postman or Insomnia

If you prefer a GUI:

### 1. Import Collection

Create requests manually or use this structure:

**Base URL:** `http://localhost:5143/api`

| Method | Endpoint | Body | Description |
|--------|----------|------|-------------|
| GET | `/books` | — | Get all books |
| GET | `/books/1` | — | Get book by ID |
| POST | `/books` | JSON (CreateBookDto) | Create new book |
| PUT | `/books/1` | JSON (UpdateBookDto) | Update book |
| DELETE | `/books/1` | — | Delete book |

### 2. Sample POST Body

```json
{
  "title": "Your Book Title",
  "isbn": "978-XXXXXXXXXX",
  "author": "Author Name",
  "yearPublished": 2024
}
```

---

## Using Swagger / OpenAPI

.NET 10 uses OpenAPI by default (not Swashbuckle).

### Option 1: Download OpenAPI Spec

```bash
curl http://localhost:5143/openapi/v1.json > openapi.json
```

### Option 2: View in Swagger Editor

1. Go to https://editor.swagger.io/
2. Copy the JSON from `http://localhost:5143/openapi/v1.json`
3. Paste it into the editor
4. Try out the endpoints

---

## All Endpoints Reference

### GET /api/books
**Get all books**
- **Response:** 200 OK + array of BookDto
- **Error:** None (returns empty array if no books)

### GET /api/books/{id}
**Get book by ID**
- **Response:** 200 OK + BookDto
- **Error:** 404 Not Found if book doesn't exist

### POST /api/books
**Create new book**
- **Request body:** CreateBookDto (title, isbn, author?, yearPublished?)
- **Response:** 201 Created + BookDto + Location header
- **Error:** 409 Conflict if ISBN already exists

### PUT /api/books/{id}
**Update existing book**
- **Request body:** UpdateBookDto (title, isbn, author?, yearPublished?)
- **Response:** 204 No Content
- **Error:** 404 Not Found if book doesn't exist, 409 Conflict if ISBN already exists (on another book)

### DELETE /api/books/{id}
**Delete book**
- **Response:** 204 No Content
- **Error:** 404 Not Found if book doesn't exist

---

## Troubleshooting

### Problem 1: "Connection refused" on localhost:5143

**Cause:** API is not running or using a different port.

**Fix:**
1. Check the `dotnet run` output for the actual port
2. Replace `5143` with the correct port in your curl commands
3. Or configure a fixed port in `launchSettings.json`

---

### Problem 2: 404 on all endpoints

**Cause:** Controllers not mapped or wrong base URL.

**Fix:**
- Verify `app.MapControllers()` is in `Program.cs`
- Check the route: should be `http://localhost:5143/api/books` (not `/Books` or `/book`)

---

### Problem 3: 500 Internal Server Error

**Cause:** Database connection failed or service/repository not registered.

**Fix:**
1. Check SQL Server container is running: `docker ps`
2. Check connection string in `appsettings.Development.json`
3. Check DI registration in `Program.cs`
4. Look at API console logs for errors

---

### Problem 4: POST returns 400 Bad Request

**Cause:** Invalid JSON or missing required fields.

**Fix:**
- Ensure `Content-Type: application/json` header is set
- Ensure `title` and `isbn` are provided (required)
- Check JSON syntax (no trailing commas, proper quotes)

Example valid JSON:
```json
{
  "title": "Test",
  "isbn": "978-1234567890"
}
```

---

### Problem 5: PUT/DELETE returns 404 but book exists

**Cause:** Wrong book ID or book was deleted.

**Fix:**
1. Do a GET request first to verify the book ID
2. Check the ID in the URL matches an existing book

---

## What You Should See

### Successful GET All
```
HTTP/1.1 200 OK
Content-Type: application/json

[{"id":1,"title":"Clean Code",...}, ...]
```

### Successful POST
```
HTTP/1.1 201 Created
Location: http://localhost:5143/api/books/6
Content-Type: application/json

{"id":6,"title":"Refactoring",...}
```

### Successful PUT/DELETE
```
HTTP/1.1 204 No Content
```

### Error: Book Not Found
```
HTTP/1.1 404 Not Found
```

### Error: Duplicate ISBN
```
HTTP/1.1 409 Conflict
Content-Type: application/json

{"error":"A book with ISBN 978-1234567890 already exists."}
```

---

## Testing Checklist

Use this checklist to verify all functionality:

- [ ] GET /api/books returns 5 seeded books
- [ ] GET /api/books/1 returns "Clean Code"
- [ ] GET /api/books/999 returns 404
- [ ] POST /api/books creates a new book with ID 6
- [ ] POST same ISBN again returns 409 Conflict
- [ ] PUT /api/books/6 updates the book
- [ ] DELETE /api/books/6 deletes the book
- [ ] GET /api/books/6 after delete returns 404

---

## Next Steps

**Milestone 4 will add:**
- Members CRUD endpoints (`/api/members`)
- Loans endpoints (`/api/loans`)
- Business rule: one book can have at most one open loan
- Borrow and return operations

The Books API you just tested will be called by:
- **Web MVC admin site** (Milestone 5) — indirectly via shared services
- **Android app** (Milestones 6-7) — directly via HTTP

---

## Architecture Overview

This API follows the **Service-Repository Pattern**:

```
HTTP Request
    ↓
BooksController (validates input, returns HTTP responses)
    ↓
IBookService → BookService (business logic, duplicate check, logging)
    ↓
IBookRepository → BookRepository (database queries)
    ↓
LibraryDbContext (EF Core)
    ↓
SQL Server (CampusLibraryDb, port 14335)
```

**Why this matters:**
- Controllers are lightweight (no business logic)
- Services are testable (don't depend on HTTP)
- Repositories are swappable (could switch to another DB)
- Each layer has one responsibility

---

## Summary

✅ **What works now:**
- Full Books CRUD via REST API
- Duplicate ISBN validation
- Structured logging
- Proper HTTP status codes

⏭️ **What doesn't work yet:**
- Members endpoints (Milestone 4)
- Loans endpoints (Milestone 4)
- MVC admin pages (Milestone 5)
- Android app (Milestones 6-7)

**This is expected!** Milestone 3 is Books API only.
