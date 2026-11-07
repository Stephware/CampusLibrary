# Flowcharts — EF Core Entities, DbContext & Migration

> Historical milestone record. Statements and verification below describe the original task, not a fresh run. For current setup and known limitations, use [the latest report index](../INDEX.md).

**Milestone:** 2  
**Date:** 2026-11-07  

---

## 1. Entity Relationship Diagram (ERD)

This diagram shows the database schema with relationships, keys, and constraints.

```mermaid
erDiagram
    BOOK ||--o{ LOAN : "has"
    MEMBER ||--o{ LOAN : "borrows"
    
    BOOK {
        int Id PK "Primary Key, IDENTITY"
        nvarchar(200) Title "Required"
        nvarchar(20) Isbn UK "Unique"
        nvarchar(100) Author "Nullable"
        int YearPublished "Nullable"
    }
    
    MEMBER {
        int Id PK "Primary Key, IDENTITY"
        nvarchar(100) Name "Required"
        nvarchar(100) Email UK "Unique"
        nvarchar(20) StudentId UK "Unique"
        datetime2 RegisteredDate "Required"
    }
    
    LOAN {
        int Id PK "Primary Key, IDENTITY"
        int BookId FK "Foreign Key to Book"
        int MemberId FK "Foreign Key to Member"
        datetime2 BorrowDate "Required"
        datetime2 DueDate "Required"
        datetime2 ReturnDate "Nullable (NULL = not returned)"
    }
```

**Key constraints:**
- **One Book → Many Loans** (a book can be borrowed multiple times over its lifetime)
- **One Member → Many Loans** (a member can borrow multiple books)
- **Delete Behavior:** `RESTRICT` (cannot delete a book or member with existing loan records)
- **Business rule (not enforced by DB):** A book can have at most one open loan (where `ReturnDate IS NULL`) — will be enforced in service layer in Milestone 4

---

## 2. Migration Flow

This sequence shows how the migration was created and applied to the database.

```mermaid
sequenceDiagram
    participant Dev as Developer
    participant CLI as dotnet CLI
    participant EF as EF Core Tools
    participant Code as LibraryDbContext
    participant DB as SQL Server (port 14335)
    
    Dev->>CLI: dotnet ef migrations add InitialCreate
    CLI->>EF: Parse DbContext
    EF->>Code: Read OnModelCreating()
    Code-->>EF: Entity configurations + seed data
    EF->>EF: Generate migration file
    EF-->>Dev: Migration created: 20261107071528_InitialCreate.cs
    
    Dev->>CLI: dotnet ef database update
    CLI->>EF: Apply pending migrations
    EF->>DB: CREATE DATABASE [CampusLibraryDb]
    DB-->>EF: Database created
    
    EF->>DB: CREATE TABLE [Books] (Id, Title, Isbn, Author, YearPublished)
    EF->>DB: CREATE TABLE [Members] (Id, Name, Email, StudentId, RegisteredDate)
    EF->>DB: CREATE TABLE [Loans] (Id, BookId, MemberId, BorrowDate, DueDate, ReturnDate)
    
    EF->>DB: INSERT INTO [Books] VALUES (5 rows)
    EF->>DB: INSERT INTO [Members] VALUES (3 rows)
    
    EF->>DB: CREATE UNIQUE INDEX [IX_Books_Isbn] ON [Books] ([Isbn])
    EF->>DB: CREATE UNIQUE INDEX [IX_Members_Email] ON [Members] ([Email])
    EF->>DB: CREATE UNIQUE INDEX [IX_Members_StudentId] ON [Members] ([StudentId])
    EF->>DB: CREATE INDEX [IX_Loans_BookId] ON [Loans] ([BookId])
    EF->>DB: CREATE INDEX [IX_Loans_MemberId] ON [Loans] ([MemberId])
    
    EF->>DB: INSERT INTO [__EFMigrationsHistory] (MigrationId, ProductVersion)
    
    DB-->>EF: Migration applied successfully
    EF-->>CLI: Done.
    CLI-->>Dev: Database updated
```

---

## 3. DbContext Structure

This diagram shows how the DbContext is organized with entities, DbSets, and configuration.

```mermaid
graph TD
    CTX[LibraryDbContext<br/>DbContext]
    
    CTX --> DBSETS[DbSets]
    CTX --> CONFIG[OnModelCreating]
    CTX --> SEED[Seed Data]
    
    DBSETS --> BOOKS_SET["DbSet&lt;Book&gt; Books"]
    DBSETS --> MEMBERS_SET["DbSet&lt;Member&gt; Members"]
    DBSETS --> LOANS_SET["DbSet&lt;Loan&gt; Loans"]
    
    CONFIG --> BOOK_CFG[Book Configuration<br/>• MaxLength: Title 200, Isbn 20<br/>• Unique Index: Isbn]
    CONFIG --> MEMBER_CFG[Member Configuration<br/>• MaxLength: Name 100, Email 100<br/>• Unique Index: Email, StudentId]
    CONFIG --> LOAN_CFG[Loan Configuration<br/>• FK: BookId → Books.Id<br/>• FK: MemberId → Members.Id<br/>• DeleteBehavior: Restrict]
    
    SEED --> BOOKS_SEED["5 Books<br/>Clean Code, Pragmatic Programmer,<br/>Design Patterns, Algorithms, Code Complete"]
    SEED --> MEMBERS_SEED["3 Members<br/>Alice Johnson S001<br/>Bob Smith S002<br/>Carol White S003"]
    
    style CTX fill:#e1f5ff
    style DBSETS fill:#fff4e1
    style CONFIG fill:#fff4e1
    style SEED fill:#e8f5e9
```

---

## 4. EF Core Query Flow (Future — Milestone 3+)

This shows how queries will flow from controllers through services and repositories to the database.

```mermaid
sequenceDiagram
    participant CTRL as BooksController
    participant SVC as BookService
    participant REPO as BookRepository
    participant CTX as LibraryDbContext
    participant DB as SQL Server
    
    Note over CTRL,DB: Example: GET /api/books
    
    CTRL->>SVC: GetAllBooksAsync()
    SVC->>REPO: GetAllAsync()
    REPO->>CTX: Books.AsNoTracking().ToListAsync()
    CTX->>DB: SELECT * FROM Books
    DB-->>CTX: 5 rows
    CTX-->>REPO: List<Book>
    REPO-->>SVC: List<Book>
    SVC->>SVC: Map to BookDto
    SVC-->>CTRL: List<BookDto>
    CTRL-->>CTRL: return Ok(books)
    
    Note over CTRL,DB: Not yet implemented (Milestone 3)
```

**Key points (per AI_DEVELOPMENT_GUIDE.md):**
- Controllers call services (never DbContext directly)
- Services call repositories (business logic layer)
- Repositories call DbContext (data access layer)
- All async: `ToListAsync()`, `SaveChangesAsync()`
- Read-only queries use `AsNoTracking()`

---

## 5. Seed Data Distribution

```mermaid
pie title Seed Data: Books by Publication Decade
    "1990s" : 2
    "2000s" : 3
```

```mermaid
pie title Seed Data: Members by Registration Month
    "January" : 1
    "February" : 1
    "March" : 1
```

---

## 6. Database Schema Creation Flow

```mermaid
flowchart TD
    START([Migration Applied])
    
    CREATE_DB[Create Database<br/>CampusLibraryDb]
    CREATE_BOOKS[Create Table: Books<br/>Id, Title, Isbn, Author, YearPublished]
    CREATE_MEMBERS[Create Table: Members<br/>Id, Name, Email, StudentId, RegisteredDate]
    CREATE_LOANS[Create Table: Loans<br/>Id, BookId, MemberId, BorrowDate, DueDate, ReturnDate]
    
    INSERT_BOOKS[Insert 5 Books]
    INSERT_MEMBERS[Insert 3 Members]
    
    INDEX_BOOKS[Create Unique Index on Isbn]
    INDEX_MEMBERS_EMAIL[Create Unique Index on Email]
    INDEX_MEMBERS_STUDENT[Create Unique Index on StudentId]
    INDEX_LOANS_BOOK[Create Index on BookId]
    INDEX_LOANS_MEMBER[Create Index on MemberId]
    
    HISTORY[Record Migration in __EFMigrationsHistory]
    
    DONE([Migration Complete])
    
    START --> CREATE_DB
    CREATE_DB --> CREATE_BOOKS
    CREATE_BOOKS --> CREATE_MEMBERS
    CREATE_MEMBERS --> CREATE_LOANS
    CREATE_LOANS --> INSERT_BOOKS
    INSERT_BOOKS --> INSERT_MEMBERS
    INSERT_MEMBERS --> INDEX_BOOKS
    INDEX_BOOKS --> INDEX_MEMBERS_EMAIL
    INDEX_MEMBERS_EMAIL --> INDEX_MEMBERS_STUDENT
    INDEX_MEMBERS_STUDENT --> INDEX_LOANS_BOOK
    INDEX_LOANS_BOOK --> INDEX_LOANS_MEMBER
    INDEX_LOANS_MEMBER --> HISTORY
    HISTORY --> DONE
    
    style CREATE_DB fill:#e1f5ff
    style INSERT_BOOKS fill:#e8f5e9
    style INSERT_MEMBERS fill:#e8f5e9
    style DONE fill:#c8e6c9
```

---

## 7. Future Architecture (Milestone 3+)

This shows where the DbContext fits in the complete application architecture.

```mermaid
graph TB
    subgraph "Presentation Layer"
        API[Web API Controller<br/>BooksController]
        MVC[MVC Controller<br/>BooksController]
        ANDROID[Android App<br/>Retrofit Client]
    end
    
    subgraph "Business Logic Layer"
        SVC[Service<br/>IBookService<br/>BookService]
    end
    
    subgraph "Data Access Layer (Milestone 2 ✅)"
        REPO[Repository<br/>IBookRepository<br/>BookRepository]
        CTX[LibraryDbContext<br/>DbSet&lt;Book&gt;<br/>DbSet&lt;Member&gt;<br/>DbSet&lt;Loan&gt;]
    end
    
    subgraph "Database (Milestone 2 ✅)"
        DB[(SQL Server<br/>CampusLibraryDb<br/>Port 14335)]
    end
    
    ANDROID -->|HTTP/JSON| API
    MVC --> SVC
    API --> SVC
    SVC --> REPO
    REPO --> CTX
    CTX -->|EF Core SQL| DB
    
    style CTX fill:#e1f5ff
    style DB fill:#fce4ec
    style API fill:#fff4e1
    style MVC fill:#fff4e1
    style SVC fill:#fff4e1
    style REPO fill:#e1f5ff
    style ANDROID fill:#e8f5e9
```

**Legend:**
- ✅ Blue = Implemented in Milestone 2
- ⏭️ Yellow/Green = Coming in Milestones 3-7

---

## 8. EF Core Entity Navigation Properties

```mermaid
graph LR
    BOOK[Book Entity<br/>Id, Title, Isbn, Author]
    MEMBER[Member Entity<br/>Id, Name, Email, StudentId]
    LOAN[Loan Entity<br/>Id, BorrowDate, DueDate, ReturnDate]
    
    BOOK -->|"Loans<br/>ICollection&lt;Loan&gt;"| LOAN
    MEMBER -->|"Loans<br/>ICollection&lt;Loan&gt;"| LOAN
    LOAN -->|"Book<br/>Navigation Property"| BOOK
    LOAN -->|"Member<br/>Navigation Property"| MEMBER
    
    style BOOK fill:#e1f5ff
    style MEMBER fill:#e8f5e9
    style LOAN fill:#fff4e1
```

**How it works:**
- `Book.Loans` gives all loans for that book
- `Member.Loans` gives all loans for that member
- `Loan.Book` navigates back to the book
- `Loan.Member` navigates back to the member

**Example query (Milestone 3+):**
```csharp
var book = await _context.Books
    .Include(b => b.Loans)
    .ThenInclude(l => l.Member)
    .FirstOrDefaultAsync(b => b.Id == 1);

// book.Loans[0].Member.Name → "Alice Johnson"
```

---

## Summary

**Milestone 2 completed:**
- ✅ 3 entities with navigation properties
- ✅ LibraryDbContext with configuration and seed data
- ✅ Database created with 3 tables, 6 indexes, 2 foreign keys
- ✅ 5 books and 3 members seeded

**Next milestone (3):**
- Implement repositories (IBookRepository, BookRepository)
- Implement services (IBookService, BookService)
- Add Books API controller with CRUD endpoints
- Test with Swagger

The DbContext created in this milestone is the foundation for all future database operations.
