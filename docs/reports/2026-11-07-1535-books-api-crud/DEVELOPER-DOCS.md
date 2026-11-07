# Developer Documentation — Books API CRUD

> Historical milestone record. Statements and verification below describe the original task, not a fresh run. For current setup and known limitations, use [the latest report index](../INDEX.md).

**Milestone:** 3  
**Task:** Books CRUD endpoints with Service-Repository pattern  
**Date:** 2026-11-07 15:35  
**Status:** ✅ Complete

---

## Summary

Implemented the complete Books CRUD API following the Service-Repository pattern from AI_DEVELOPMENT_GUIDE.md:

1. **Repository layer** — `IBookRepository`, `BookRepository` (database access)
2. **Service layer** — `IBookService`, `BookService` (business logic)
3. **DTOs** — `BookDto`, `CreateBookDto`, `UpdateBookDto` (data transfer)
4. **Controller** — `BooksController` with 5 endpoints (GET all, GET by ID, POST, PUT, DELETE)
5. **Dependency injection** — All services and repositories registered in `Program.cs`
6. **Testing** — All CRUD operations verified via curl

**Architecture:** Controller → Service → Repository → DbContext → SQL Server (per AI_DEVELOPMENT_GUIDE.md)

---

## Projects Touched

- ✅ **EF CORE** — Repository layer added (IBookRepository, BookRepository)
- ✅ **WEB API** — Service layer, DTOs, controller, DI registration
- ✅ **MSSQL** — Database queried via repositories
- ⏭️ **WEB MVC** — Not touched (Milestone 5)
- ⏭️ **ANDROID KOTLIN** — Not touched (Milestones 6-7)

---

## Files Changed

| Path | Status | Why |
|------|--------|-----|
| `src/CampusLibrary.Data/Repositories/Interfaces/IBookRepository.cs` | Added | Repository interface with GetAll, GetById, Add, Update, Delete, ExistsByIsbn |
| `src/CampusLibrary.Data/Repositories/BookRepository.cs` | Added | Repository implementation using LibraryDbContext |
| `src/CampusLibrary.WebApi/Services/Interfaces/IBookService.cs` | Added | Service interface with GetAllBooks, GetBookById, CreateBook, UpdateBook, DeleteBook |
| `src/CampusLibrary.WebApi/Services/BookService.cs` | Added | Service implementation with business logic (duplicate ISBN check, logging) |
| `src/CampusLibrary.WebApi/DTOs/BookDtos.cs` | Added | BookDto, CreateBookDto, UpdateBookDto |
| `src/CampusLibrary.WebApi/Controllers/BooksController.cs` | Added | API controller with 5 endpoints |
| `src/CampusLibrary.WebApi/Program.cs` | Modified | Added DI registration for repositories, services, controllers |

---

## Key Code

### Repository Interface

```csharp
public interface IBookRepository
{
    Task<List<Book>> GetAllAsync();
    Task<Book?> GetByIdAsync(int id);
    Task<Book> AddAsync(Book book);
    Task UpdateAsync(Book book);
    Task DeleteAsync(int id);
    Task<bool> ExistsByIsbnAsync(string isbn);
}
```

### Repository Implementation (Database Access)

```csharp
public class BookRepository : IBookRepository
{
    private readonly LibraryDbContext _context;

    public async Task<List<Book>> GetAllAsync()
    {
        return await _context.Books
            .AsNoTracking()  // Read-only query optimization
            .ToListAsync();
    }

    public async Task<Book> AddAsync(Book book)
    {
        _context.Books.Add(book);
        await _context.SaveChangesAsync();
        return book;
    }
}
```

**Key points:**
- Uses `AsNoTracking()` for read-only queries (performance)
- All async methods (`ToListAsync()`, `SaveChangesAsync()`)
- Direct DbContext access (repository layer only)

### Service Interface

```csharp
public interface IBookService
{
    Task<List<BookDto>> GetAllBooksAsync();
    Task<BookDto?> GetBookByIdAsync(int id);
    Task<BookDto> CreateBookAsync(CreateBookDto createDto);
    Task UpdateBookAsync(int id, UpdateBookDto updateDto);
    Task DeleteBookAsync(int id);
}
```

### Service Implementation (Business Logic)

```csharp
public class BookService : IBookService
{
    private readonly IBookRepository _repository;
    private readonly ILogger<BookService> _logger;

    public async Task<BookDto> CreateBookAsync(CreateBookDto createDto)
    {
        // Business rule: Check for duplicate ISBN
        if (await _repository.ExistsByIsbnAsync(createDto.Isbn))
        {
            _logger.LogWarning("Attempted to create book with duplicate ISBN: {Isbn}", createDto.Isbn);
            throw new InvalidOperationException($"A book with ISBN {createDto.Isbn} already exists.");
        }

        // Map DTO to entity
        var book = new Book
        {
            Title = createDto.Title,
            Isbn = createDto.Isbn,
            Author = createDto.Author,
            YearPublished = createDto.YearPublished
        };

        var created = await _repository.AddAsync(book);
        _logger.LogInformation("Created book with ID {Id}", created.Id);

        // Map entity to DTO
        return new BookDto { /* ... */ };
    }
}
```

**Key points:**
- Business logic: duplicate ISBN validation
- Structured logging with `ILogger<T>`
- Throws exceptions for service layer errors (caught by controller)
- Maps between entities and DTOs

### DTOs

```csharp
public class BookDto
{
    public int Id { get; set; }
    public required string Title { get; set; }
    public required string Isbn { get; set; }
    public string? Author { get; set; }
    public int? YearPublished { get; set; }
}

public class CreateBookDto
{
    public required string Title { get; set; }
    public required string Isbn { get; set; }
    public string? Author { get; set; }
    public int? YearPublished { get; set; }
}

public class UpdateBookDto
{
    public required string Title { get; set; }
    public required string Isbn { get; set; }
    public string? Author { get; set; }
    public int? YearPublished { get; set; }
}
```

**Why separate DTOs:**
- `BookDto` includes `Id` (for responses)
- `CreateBookDto` excludes `Id` (auto-generated by DB)
- `UpdateBookDto` includes all fields (for full updates)

### Controller

```csharp
[ApiController]
[Route("api/[controller]")]
public class BooksController : ControllerBase
{
    private readonly IBookService _bookService;
    private readonly ILogger<BooksController> _logger;

    [HttpGet]
    public async Task<ActionResult<List<BookDto>>> GetAll()
    {
        var books = await _bookService.GetAllBooksAsync();
        return Ok(books);
    }

    [HttpPost]
    public async Task<ActionResult<BookDto>> Create([FromBody] CreateBookDto createDto)
    {
        try
        {
            var book = await _bookService.CreateBookAsync(createDto);
            return CreatedAtAction(nameof(GetById), new { id = book.Id }, book);
        }
        catch (InvalidOperationException ex)
        {
            return Conflict(new { error = ex.Message });
        }
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> Update(int id, [FromBody] UpdateBookDto updateDto)
    {
        try
        {
            await _bookService.UpdateBookAsync(id, updateDto);
            return NoContent();
        }
        catch (KeyNotFoundException)
        {
            return NotFound();
        }
        catch (InvalidOperationException ex)
        {
            return Conflict(new { error = ex.Message });
        }
    }
}
```

**Key points:**
- Returns proper HTTP status codes (200 OK, 201 Created, 204 No Content, 404 Not Found, 409 Conflict)
- Uses `CreatedAtAction()` for POST (returns location header)
- Catches service exceptions and maps to HTTP responses
- No direct DbContext or repository access

### Dependency Injection (Program.cs)

```csharp
// Register repositories
builder.Services.AddScoped<IBookRepository, BookRepository>();

// Register services
builder.Services.AddScoped<IBookService, BookService>();

// Add controllers
builder.Services.AddControllers();

// Map controllers
app.MapControllers();
```

---

## Database Changes

**None.** This milestone uses the existing database from Milestone 2. No migrations or schema changes.

---

## API Endpoints

### 1. GET /api/books
**Get all books**

**Request:**
```bash
curl http://localhost:5143/api/books
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "title": "Clean Code",
    "isbn": "978-0132350884",
    "author": "Robert C. Martin",
    "yearPublished": 2008
  },
  ...
]
```

---

### 2. GET /api/books/{id}
**Get book by ID**

**Request:**
```bash
curl http://localhost:5143/api/books/1
```

**Response:** `200 OK` or `404 Not Found`
```json
{
  "id": 1,
  "title": "Clean Code",
  "isbn": "978-0132350884",
  "author": "Robert C. Martin",
  "yearPublished": 2008
}
```

---

### 3. POST /api/books
**Create new book**

**Request:**
```bash
curl -X POST http://localhost:5143/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Book",
    "isbn": "978-1234567890",
    "author": "Test Author",
    "yearPublished": 2024
  }'
```

**Response:** `201 Created` with `Location` header
```json
{
  "id": 6,
  "title": "Test Book",
  "isbn": "978-1234567890",
  "author": "Test Author",
  "yearPublished": 2024
}
```

**Error (duplicate ISBN):** `409 Conflict`
```json
{
  "error": "A book with ISBN 978-1234567890 already exists."
}
```

---

### 4. PUT /api/books/{id}
**Update existing book**

**Request:**
```bash
curl -X PUT http://localhost:5143/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Clean Code (Updated)",
    "isbn": "978-0132350884",
    "author": "Robert C. Martin",
    "yearPublished": 2008
  }'
```

**Response:** `204 No Content` (success) or `404 Not Found` (book doesn't exist) or `409 Conflict` (duplicate ISBN)

---

### 5. DELETE /api/books/{id}
**Delete book**

**Request:**
```bash
curl -X DELETE http://localhost:5143/api/books/1
```

**Response:** `204 No Content` (success) or `404 Not Found` (book doesn't exist)

---

## How It Was Verified

### 1. Build
```bash
$ dotnet build
Build succeeded. 0 Error(s). Time: 1.55s
```

### 2. Run API
```bash
$ cd src/CampusLibrary.WebApi && dotnet run
Now listening on: http://localhost:5143
```

### 3. Test GET All Books
```bash
$ curl http://localhost:5143/api/books
[{"id":1,"title":"Clean Code",...}, ...]
```
**Result:** ✅ Returns 5 seeded books

### 4. Test GET Book by ID
```bash
$ curl http://localhost:5143/api/books/1
{"id":1,"title":"Clean Code","isbn":"978-0132350884",...}
```
**Result:** ✅ Returns single book

### 5. Test POST (Create)
```bash
$ curl -X POST http://localhost:5143/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Book","isbn":"978-1234567890","author":"Test Author","yearPublished":2024}'
{"id":6,"title":"Test Book",...}
```
**Result:** ✅ Created book with ID 6

### 6. Test PUT (Update)
```bash
$ curl -X PUT http://localhost:5143/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code (Updated)","isbn":"978-0132350884","author":"Robert C. Martin","yearPublished":2008}'
# HTTP 204 No Content
```
**Result:** ✅ Book updated

### 7. Test DELETE
```bash
$ curl -X DELETE http://localhost:5143/api/books/6
# HTTP 204 No Content
```
**Result:** ✅ Book deleted

---

## Architecture (per AI_DEVELOPMENT_GUIDE.md)

```
BooksController (Presentation)
    ↓ calls
IBookService → BookService (Business Logic)
    ↓ calls
IBookRepository → BookRepository (Data Access)
    ↓ calls
LibraryDbContext (EF Core)
    ↓ queries
SQL Server (CampusLibraryDb)
```

**✅ Follows all rules:**
- Controllers never touch DbContext directly
- Services contain business logic (duplicate ISBN check)
- Repositories contain database queries only
- All async operations
- Interfaces for services and repositories
- Dependency injection throughout
- Structured logging
- `AsNoTracking()` for read-only queries
- Proper HTTP status codes

---

## Known Limits / TODO

### Milestone 3 (✅ Complete)

- ✅ Repository layer (IBookRepository, BookRepository)
- ✅ Service layer (IBookService, BookService)
- ✅ DTOs (BookDto, CreateBookDto, UpdateBookDto)
- ✅ Controller (BooksController with 5 endpoints)
- ✅ DI registration
- ✅ CRUD operations tested

### Milestone 4 (Next — Members + Loans)

- ❌ **Members CRUD** — IMemberRepository, IMemberService, MembersController
- ❌ **Loans endpoints** — ILoanRepository, ILoanService, LoansController
- ❌ **POST /api/loans** (borrow book) — Business rule: one book = max one open loan
- ❌ **POST /api/loans/{id}/return** (return book) — Sets `ReturnDate`

### Milestone 5 (Web MVC Admin Pages)

- ❌ MVC controllers, views, ViewModels

### Milestones 6-7 (Android)

- ❌ Android screens calling these API endpoints

---

## Shortcuts Taken (ponytail debt)

None. Followed AI_DEVELOPMENT_GUIDE.md exactly with no shortcuts.

---

## What Was NOT Implemented (As Planned)

Per ponytail principles and milestone scope:

❌ Members CRUD — **Milestone 4**  
❌ Loans endpoints — **Milestone 4**  
❌ Business rule enforcement (one open loan per book) — **Milestone 4**  
❌ Input validation attributes — **Milestone 4** (validation works, but no `[Required]`, `[MaxLength]` on DTOs yet)  
❌ Global exception handling — **Milestone 4** (exceptions caught per-action)  
❌ API versioning — Not required  
❌ Rate limiting — Not required  
❌ Authentication/Authorization — Not required  
❌ CORS for Android — **Milestone 6** (when Android app is built)  

---

## Testing with Swagger

**Swagger UI:** http://localhost:5143/openapi/v1.json (OpenAPI spec)

**Note:** .NET 10 uses OpenAPI by default, not Swashbuckle. The OpenAPI document is available at `/openapi/v1.json`.

To view in Swagger UI:
1. Run the API: `dotnet run`
2. Open: https://editor.swagger.io/
3. Paste the OpenAPI JSON from http://localhost:5143/openapi/v1.json

Or use curl/Postman for testing (as verified above).

---

## Environment Details

- **.NET SDK:** 10.0.400
- **EF Core:** 10.0.14
- **API Port:** 5143 (auto-assigned, may vary)
- **Database:** CampusLibraryDb (port 14335)
- **Platform:** macOS (Apple Silicon / arm64)

---

## Student Action Required

**Before Milestone 4:**

1. ✅ All Books CRUD endpoints working
2. ✅ Service-Repository pattern implemented
3. ⏭️ Ready for Members and Loans endpoints

**Test the API yourself:**

```bash
cd src/CampusLibrary.WebApi
dotnet run
# In another terminal:
curl http://localhost:5143/api/books
```
