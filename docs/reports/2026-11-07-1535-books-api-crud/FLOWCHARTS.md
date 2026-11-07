# Flowcharts — Books API CRUD

**Milestone:** 3  
**Date:** 2026-11-07  

---

## 1. Service-Repository Architecture

This diagram shows the complete request flow from HTTP to database.

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    participant Ctrl as BooksController
    participant Svc as BookService
    participant Repo as BookRepository
    participant Ctx as LibraryDbContext
    participant DB as SQL Server

    Note over Client,DB: Example: GET /api/books
    
    Client->>Ctrl: GET /api/books
    Ctrl->>Svc: GetAllBooksAsync()
    Svc->>Repo: GetAllAsync()
    Repo->>Ctx: Books.AsNoTracking().ToListAsync()
    Ctx->>DB: SELECT * FROM Books
    DB-->>Ctx: 5 rows
    Ctx-->>Repo: List<Book>
    Repo-->>Svc: List<Book>
    Svc->>Svc: Map to List<BookDto>
    Svc-->>Ctrl: List<BookDto>
    Ctrl-->>Client: HTTP 200 OK + JSON

    Note over Client,DB: ✅ No direct DbContext access from Controller
```

---

## 2. POST (Create Book) Flow with Validation

This shows the business logic and duplicate ISBN check.

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    participant Ctrl as BooksController
    participant Svc as BookService
    participant Repo as BookRepository
    participant Ctx as LibraryDbContext
    participant DB as SQL Server
    participant Log as ILogger

    Client->>Ctrl: POST /api/books<br/>{title, isbn, author, year}
    Ctrl->>Svc: CreateBookAsync(CreateBookDto)
    
    Svc->>Repo: ExistsByIsbnAsync(isbn)
    Repo->>Ctx: Books.AnyAsync(b => b.Isbn == isbn)
    Ctx->>DB: SELECT COUNT(*) FROM Books WHERE Isbn = ?
    DB-->>Ctx: 0
    Ctx-->>Repo: false
    Repo-->>Svc: false
    
    alt ISBN already exists
        Svc->>Log: LogWarning("Duplicate ISBN")
        Svc-->>Ctrl: throw InvalidOperationException
        Ctrl-->>Client: HTTP 409 Conflict + error message
    else ISBN is unique
        Svc->>Svc: Map CreateBookDto → Book entity
        Svc->>Repo: AddAsync(book)
        Repo->>Ctx: Books.Add(book)
        Repo->>Ctx: SaveChangesAsync()
        Ctx->>DB: INSERT INTO Books VALUES (...)
        DB-->>Ctx: Id = 6
        Ctx-->>Repo: Book with Id = 6
        Repo-->>Svc: Book
        Svc->>Log: LogInformation("Created book ID 6")
        Svc->>Svc: Map Book → BookDto
        Svc-->>Ctrl: BookDto
        Ctrl-->>Client: HTTP 201 Created + Location header + JSON
    end
```

---

## 3. PUT (Update Book) Flow

```mermaid
flowchart TD
    START([PUT /api/books/1])
    
    CTRL[BooksController.Update]
    SVC[BookService.UpdateBookAsync]
    GET_BOOK[Repository.GetByIdAsync]
    CHECK_ISBN[Repository.ExistsByIsbnAsync]
    
    BOOK_EXISTS{Book<br/>exists?}
    ISBN_CHANGED{ISBN<br/>changed?}
    DUPLICATE{Duplicate<br/>ISBN?}
    
    UPDATE_ENTITY[Update entity properties]
    SAVE[Repository.UpdateAsync]
    LOG[Log: Updated book ID]
    
    RETURN_204[HTTP 204 No Content]
    RETURN_404[HTTP 404 Not Found]
    RETURN_409[HTTP 409 Conflict]
    
    START --> CTRL
    CTRL --> SVC
    SVC --> GET_BOOK
    GET_BOOK --> BOOK_EXISTS
    
    BOOK_EXISTS -->|No| RETURN_404
    BOOK_EXISTS -->|Yes| ISBN_CHANGED
    
    ISBN_CHANGED -->|No| UPDATE_ENTITY
    ISBN_CHANGED -->|Yes| CHECK_ISBN
    
    CHECK_ISBN --> DUPLICATE
    DUPLICATE -->|Yes| RETURN_409
    DUPLICATE -->|No| UPDATE_ENTITY
    
    UPDATE_ENTITY --> SAVE
    SAVE --> LOG
    LOG --> RETURN_204
    
    style BOOK_EXISTS fill:#fff4e1
    style ISBN_CHANGED fill:#fff4e1
    style DUPLICATE fill:#fff4e1
    style RETURN_404 fill:#fce4ec
    style RETURN_409 fill:#fce4ec
    style RETURN_204 fill:#c8e6c9
```

---

## 4. DELETE (Delete Book) Flow

```mermaid
flowchart TD
    START([DELETE /api/books/1])
    
    CTRL[BooksController.Delete]
    SVC[BookService.DeleteBookAsync]
    GET_BOOK[Repository.GetByIdAsync]
    
    BOOK_EXISTS{Book<br/>exists?}
    
    DELETE[Repository.DeleteAsync]
    REMOVE[Context.Books.Remove]
    SAVE[SaveChangesAsync]
    LOG[Log: Deleted book ID]
    
    RETURN_204[HTTP 204 No Content]
    RETURN_404[HTTP 404 Not Found]
    
    START --> CTRL
    CTRL --> SVC
    SVC --> GET_BOOK
    GET_BOOK --> BOOK_EXISTS
    
    BOOK_EXISTS -->|No| RETURN_404
    BOOK_EXISTS -->|Yes| DELETE
    
    DELETE --> REMOVE
    REMOVE --> SAVE
    SAVE --> LOG
    LOG --> RETURN_204
    
    style BOOK_EXISTS fill:#fff4e1
    style RETURN_404 fill:#fce4ec
    style RETURN_204 fill:#c8e6c9
```

---

## 5. Dependency Injection Graph

This shows how services and repositories are wired together.

```mermaid
graph TD
    PROGRAM[Program.cs<br/>DI Container]
    
    PROGRAM -->|AddDbContext| CTX[LibraryDbContext]
    PROGRAM -->|AddScoped| BOOK_REPO[IBookRepository →<br/>BookRepository]
    PROGRAM -->|AddScoped| BOOK_SVC[IBookService →<br/>BookService]
    PROGRAM -->|AddControllers| CTRL[BooksController]
    
    CTRL -->|Injects| BOOK_SVC
    CTRL -->|Injects| LOGGER_CTRL[ILogger&lt;BooksController&gt;]
    
    BOOK_SVC -->|Injects| BOOK_REPO
    BOOK_SVC -->|Injects| LOGGER_SVC[ILogger&lt;BookService&gt;]
    
    BOOK_REPO -->|Injects| CTX
    
    CTX -->|Connects to| DB[(SQL Server<br/>Port 14335)]
    
    style PROGRAM fill:#e1f5ff
    style CTX fill:#e1f5ff
    style DB fill:#fce4ec
    style CTRL fill:#fff4e1
    style BOOK_SVC fill:#fff4e1
    style BOOK_REPO fill:#e1f5ff
```

---

## 6. HTTP Status Codes Decision Tree

```mermaid
flowchart TD
    REQUEST([API Request])
    
    METHOD{HTTP<br/>Method?}
    
    GET_ALL[GET /api/books]
    GET_ONE[GET /api/books/id]
    POST[POST /api/books]
    PUT[PUT /api/books/id]
    DELETE[DELETE /api/books/id]
    
    EXISTS_GET{Exists?}
    EXISTS_PUT{Exists?}
    EXISTS_DEL{Exists?}
    
    DUPLICATE_POST{Duplicate<br/>ISBN?}
    DUPLICATE_PUT{Duplicate<br/>ISBN?}
    
    R200[200 OK<br/>+ JSON array]
    R200_ONE[200 OK<br/>+ JSON object]
    R201[201 Created<br/>+ Location header<br/>+ JSON]
    R204[204 No Content]
    R404[404 Not Found]
    R409[409 Conflict<br/>+ error message]
    
    REQUEST --> METHOD
    
    METHOD -->|GET all| GET_ALL
    METHOD -->|GET one| GET_ONE
    METHOD -->|POST| POST
    METHOD -->|PUT| PUT
    METHOD -->|DELETE| DELETE
    
    GET_ALL --> R200
    
    GET_ONE --> EXISTS_GET
    EXISTS_GET -->|Yes| R200_ONE
    EXISTS_GET -->|No| R404
    
    POST --> DUPLICATE_POST
    DUPLICATE_POST -->|Yes| R409
    DUPLICATE_POST -->|No| R201
    
    PUT --> EXISTS_PUT
    EXISTS_PUT -->|No| R404
    EXISTS_PUT -->|Yes| DUPLICATE_PUT
    DUPLICATE_PUT -->|Yes| R409
    DUPLICATE_PUT -->|No| R204
    
    DELETE --> EXISTS_DEL
    EXISTS_DEL -->|Yes| R204
    EXISTS_DEL -->|No| R404
    
    style R200 fill:#c8e6c9
    style R200_ONE fill:#c8e6c9
    style R201 fill:#c8e6c9
    style R204 fill:#c8e6c9
    style R404 fill:#fce4ec
    style R409 fill:#ffecb3
```

---

## 7. Data Mapping Flow

This shows how entities are mapped to DTOs at each layer.

```mermaid
graph LR
    subgraph "Database Layer"
        DB[(SQL Server<br/>Books Table)]
    end
    
    subgraph "Repository Layer"
        ENTITY[Book Entity<br/>Id, Title, Isbn, Author,<br/>YearPublished, Loans]
    end
    
    subgraph "Service Layer"
        DTO[BookDto<br/>Id, Title, Isbn, Author,<br/>YearPublished]
        CREATE_DTO[CreateBookDto<br/>Title, Isbn, Author,<br/>YearPublished]
        UPDATE_DTO[UpdateBookDto<br/>Title, Isbn, Author,<br/>YearPublished]
    end
    
    subgraph "Controller Layer"
        JSON[JSON Response<br/>or Request Body]
    end
    
    DB -->|EF Core<br/>ToListAsync| ENTITY
    ENTITY -->|Manual mapping<br/>in Service| DTO
    DTO -->|Serialization| JSON
    
    JSON -->|Deserialization| CREATE_DTO
    CREATE_DTO -->|Manual mapping<br/>in Service| ENTITY
    ENTITY -->|SaveChangesAsync| DB
    
    style DB fill:#fce4ec
    style ENTITY fill:#e1f5ff
    style DTO fill:#fff4e1
    style CREATE_DTO fill:#fff4e1
    style UPDATE_DTO fill:#fff4e1
    style JSON fill:#e8f5e9
```

**Why separate DTOs:**
- **Repository → Service:** Returns full `Book` entity (with navigation properties)
- **Service → Controller:** Returns `BookDto` (no navigation properties, clean API contract)
- **Controller → Service (POST):** Accepts `CreateBookDto` (no `Id`, auto-generated)
- **Controller → Service (PUT):** Accepts `UpdateBookDto` (all fields for update)

---

## 8. Logging Flow

```mermaid
sequenceDiagram
    participant Svc as BookService
    participant Log as ILogger<BookService>
    participant Console as Console Output
    
    Note over Svc,Console: Example: Create Book
    
    Svc->>Svc: CreateBookAsync(dto)
    Svc->>Svc: Check duplicate ISBN
    
    alt ISBN already exists
        Svc->>Log: LogWarning("Duplicate ISBN: {Isbn}", isbn)
        Log->>Console: warn: Attempted to create book with duplicate ISBN: 978-xxx
    else Success
        Svc->>Svc: Create book
        Svc->>Log: LogInformation("Created book with ID {Id}", id)
        Log->>Console: info: Created book with ID 6
    end
    
    Note over Svc,Console: Structured logging (not string concatenation)
```

**Key points:**
- Uses `ILogger<T>` for structured logging
- Logs warnings for business rule violations
- Logs information for successful operations
- Includes parameters like `{Id}`, `{Isbn}` for searchability

---

## 9. Complete CRUD Operations Overview

```mermaid
graph TB
    CLIENT[HTTP Client<br/>curl, Postman, Android App]
    
    API[BooksController<br/>/api/books]
    
    GET_ALL[GET /api/books<br/>→ List all books]
    GET_ONE[GET /api/books/id<br/>→ Get one book]
    POST[POST /api/books<br/>→ Create book]
    PUT[PUT /api/books/id<br/>→ Update book]
    DELETE[DELETE /api/books/id<br/>→ Delete book]
    
    SVC[BookService<br/>Business Logic]
    REPO[BookRepository<br/>Database Access]
    DB[(SQL Server<br/>Books Table)]
    
    CLIENT --> API
    
    API --> GET_ALL
    API --> GET_ONE
    API --> POST
    API --> PUT
    API --> DELETE
    
    GET_ALL --> SVC
    GET_ONE --> SVC
    POST --> SVC
    PUT --> SVC
    DELETE --> SVC
    
    SVC --> REPO
    REPO --> DB
    
    style CLIENT fill:#e8f5e9
    style API fill:#fff4e1
    style SVC fill:#fff4e1
    style REPO fill:#e1f5ff
    style DB fill:#fce4ec
```

---

## 10. Future Architecture (Complete System)

This shows where the Books API fits in the complete CampusLibrary system.

```mermaid
graph TB
    subgraph "Client Layer"
        BROWSER[Web Browser]
        ANDROID[Android App]
    end
    
    subgraph "Web API (Milestone 3 ✅)"
        API_CTRL[BooksController<br/>MembersController<br/>LoansController]
    end
    
    subgraph "Web MVC (Milestone 5)"
        MVC_CTRL[MVC Controllers]
    end
    
    subgraph "Service Layer (Milestone 3 ✅ Books only)"
        SERVICES[BookService<br/>MemberService<br/>LoanService]
    end
    
    subgraph "Repository Layer (Milestone 3 ✅ Books only)"
        REPOS[BookRepository<br/>MemberRepository<br/>LoanRepository]
    end
    
    subgraph "Data Layer (Milestone 2 ✅)"
        CTX[LibraryDbContext]
        DB[(SQL Server<br/>CampusLibraryDb)]
    end
    
    ANDROID -->|HTTP/JSON| API_CTRL
    BROWSER -->|HTTP| MVC_CTRL
    
    API_CTRL --> SERVICES
    MVC_CTRL --> SERVICES
    
    SERVICES --> REPOS
    REPOS --> CTX
    CTX --> DB
    
    style API_CTRL fill:#c8e6c9
    style SERVICES fill:#c8e6c9
    style REPOS fill:#c8e6c9
    style CTX fill:#c8e6c9
    style DB fill:#c8e6c9
    style MVC_CTRL fill:#ffecb3
    style ANDROID fill:#ffecb3
```

**Legend:**
- 🟢 Green = Implemented (Milestones 2-3)
- 🟡 Yellow = Coming soon (Milestones 4-7)

---

## Summary

**Milestone 3 completed:**
- ✅ Full CRUD API for Books
- ✅ Service-Repository pattern (3 layers: Controller, Service, Repository)
- ✅ Business logic (duplicate ISBN validation)
- ✅ Proper HTTP status codes (200, 201, 204, 404, 409)
- ✅ Structured logging
- ✅ Dependency injection
- ✅ DTO mapping

**Next milestone (4):**
- Add Members CRUD (`/api/members`)
- Add Loans endpoints (`/api/loans`, `/api/loans/{id}/return`)
- Implement business rule: one book = max one open loan

The architecture established in this milestone (Controller → Service → Repository) will be replicated for Members and Loans.
