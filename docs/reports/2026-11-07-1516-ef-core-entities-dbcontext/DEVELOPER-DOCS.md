# Developer Documentation — EF Core Entities, DbContext & Migration

**Milestone:** 2  
**Task:** Create EF Core entities, DbContext, seed data, first migration, and database  
**Date:** 2026-11-07 15:16  
**Status:** ✅ Complete

---

## Summary

Implemented the data layer for CampusLibrary with Entity Framework Core:

1. **3 entity classes** (Book, Member, Loan) in `CampusLibrary.Data/Entities/`
2. **LibraryDbContext** with DbSets, Fluent API configuration, unique indexes
3. **Seed data** (5 books, 3 members)
4. **EF Core packages** installed (SqlServer 10.0.14, Design 10.0.14)
5. **Connection strings** added to WebApi and WebMvc `appsettings.Development.json`
6. **DbContext registration** in WebApi `Program.cs`
7. **First migration** created and applied
8. **Database `CampusLibraryDb`** created on SQL Server with 3 tables, indexes, foreign keys, and seed data

**No repositories, services, or controllers yet** — those come in Milestone 3+.

---

## Projects Touched

- ✅ **EF CORE** — Entities, DbContext, migration created
- ✅ **WEB API** — DbContext registered, connection string added, Design package installed
- ✅ **WEB MVC** — Connection string added
- ✅ **MSSQL** — Database created, tables populated
- ⏭️ **ANDROID KOTLIN** — Not touched (will consume API in Milestones 6-7)

---

## Files Changed

| Path | Status | Why |
|------|--------|-----|
| `src/CampusLibrary.Data/Entities/Book.cs` | Added | Book entity with Id, Title, Isbn (unique), Author, YearPublished |
| `src/CampusLibrary.Data/Entities/Member.cs` | Added | Member entity with Id, Name, Email (unique), StudentId (unique), RegisteredDate |
| `src/CampusLibrary.Data/Entities/Loan.cs` | Added | Loan entity with Id, BookId (FK), MemberId (FK), BorrowDate, DueDate, ReturnDate |
| `src/CampusLibrary.Data/LibraryDbContext.cs` | Added | DbContext with DbSets, Fluent API configuration, seed data |
| `src/CampusLibrary.Data/CampusLibrary.Data.csproj` | Modified | Added EF Core SqlServer 10.0.14 and Design 10.0.14 packages |
| `src/CampusLibrary.Data/Class1.cs` | Deleted | Template file removed |
| `src/CampusLibrary.Data/Migrations/20261107071528_InitialCreate.cs` | Added | Initial migration (generated) |
| `src/CampusLibrary.Data/Migrations/20261107071528_InitialCreate.Designer.cs` | Added | Migration designer (generated) |
| `src/CampusLibrary.Data/Migrations/LibraryDbContextModelSnapshot.cs` | Added | EF Core model snapshot (generated) |
| `src/CampusLibrary.WebApi/Program.cs` | Modified | Added DbContext registration with SQL Server connection |
| `src/CampusLibrary.WebApi/appsettings.Development.json` | Modified | Added connection string (port 14335) |
| `src/CampusLibrary.WebApi/CampusLibrary.WebApi.csproj` | Modified | Added EF Core Design 10.0.14 package |
| `src/CampusLibrary.WebMvc/appsettings.Development.json` | Modified | Added connection string (port 14335) |

---

## Key Code

### Entity: Book

```csharp
namespace CampusLibrary.Data.Entities;

public class Book
{
    public int Id { get; set; }
    public required string Title { get; set; }
    public required string Isbn { get; set; }
    public string? Author { get; set; }
    public int? YearPublished { get; set; }

    public ICollection<Loan> Loans { get; set; } = new List<Loan>();
}
```

### Entity: Member

```csharp
namespace CampusLibrary.Data.Entities;

public class Member
{
    public int Id { get; set; }
    public required string Name { get; set; }
    public required string Email { get; set; }
    public required string StudentId { get; set; }
    public DateTime RegisteredDate { get; set; }

    public ICollection<Loan> Loans { get; set; } = new List<Loan>();
}
```

### Entity: Loan

```csharp
namespace CampusLibrary.Data.Entities;

public class Loan
{
    public int Id { get; set; }
    public int BookId { get; set; }
    public int MemberId { get; set; }
    public DateTime BorrowDate { get; set; }
    public DateTime DueDate { get; set; }
    public DateTime? ReturnDate { get; set; }

    public Book Book { get; set; } = null!;
    public Member Member { get; set; } = null!;
}
```

### DbContext Configuration

```csharp
// Book configuration
modelBuilder.Entity<Book>(entity =>
{
    entity.HasKey(e => e.Id);
    entity.Property(e => e.Title).IsRequired().HasMaxLength(200);
    entity.Property(e => e.Isbn).IsRequired().HasMaxLength(20);
    entity.HasIndex(e => e.Isbn).IsUnique(); // Unique index
    entity.Property(e => e.Author).HasMaxLength(100);
});

// Member configuration
modelBuilder.Entity<Member>(entity =>
{
    entity.HasKey(e => e.Id);
    entity.Property(e => e.Name).IsRequired().HasMaxLength(100);
    entity.Property(e => e.Email).IsRequired().HasMaxLength(100);
    entity.HasIndex(e => e.Email).IsUnique(); // Unique index
    entity.Property(e => e.StudentId).IsRequired().HasMaxLength(20);
    entity.HasIndex(e => e.StudentId).IsUnique(); // Unique index
});

// Loan configuration
modelBuilder.Entity<Loan>(entity =>
{
    entity.HasKey(e => e.Id);
    entity.HasOne(e => e.Book)
          .WithMany(b => b.Loans)
          .HasForeignKey(e => e.BookId)
          .OnDelete(DeleteBehavior.Restrict); // Prevent cascade delete
    entity.HasOne(e => e.Member)
          .WithMany(m => m.Loans)
          .HasForeignKey(e => e.MemberId)
          .OnDelete(DeleteBehavior.Restrict); // Prevent cascade delete
});
```

### DbContext Registration (WebApi Program.cs)

```csharp
using CampusLibrary.Data;
using Microsoft.EntityFrameworkCore;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddDbContext<LibraryDbContext>(options =>
    options.UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection")));
```

### Connection String

```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Server=localhost,14335;Database=CampusLibraryDb;User Id=sa;Password=<your-local-password>;TrustServerCertificate=True;"
  }
}
```

**Port 14335** is used (not 1433) per Milestone 1 setup.

---

## Database Changes

**Migration:** `20261107071528_InitialCreate`

**Tables created:**

1. **Books**
   - `Id` (int, PK, IDENTITY)
   - `Title` (nvarchar(200), NOT NULL)
   - `Isbn` (nvarchar(20), NOT NULL, UNIQUE INDEX)
   - `Author` (nvarchar(100), NULL)
   - `YearPublished` (int, NULL)

2. **Members**
   - `Id` (int, PK, IDENTITY)
   - `Name` (nvarchar(100), NOT NULL)
   - `Email` (nvarchar(100), NOT NULL, UNIQUE INDEX)
   - `StudentId` (nvarchar(20), NOT NULL, UNIQUE INDEX)
   - `RegisteredDate` (datetime2, NOT NULL)

3. **Loans**
   - `Id` (int, PK, IDENTITY)
   - `BookId` (int, FK to Books, NOT NULL)
   - `MemberId` (int, FK to Members, NOT NULL)
   - `BorrowDate` (datetime2, NOT NULL)
   - `DueDate` (datetime2, NOT NULL)
   - `ReturnDate` (datetime2, NULL)

**Foreign keys:**
- `Loans.BookId` → `Books.Id` (ON DELETE NO ACTION)
- `Loans.MemberId` → `Members.Id` (ON DELETE NO ACTION)

**Seed data inserted:**

**Books (5):**
1. Clean Code — Robert C. Martin (2008) — ISBN 978-0132350884
2. The Pragmatic Programmer — Andrew Hunt (1999) — ISBN 978-0201616224
3. Design Patterns — Erich Gamma (1994) — ISBN 978-0201633610
4. Introduction to Algorithms — Thomas H. Cormen (2009) — ISBN 978-0262033848
5. Code Complete — Steve McConnell (2004) — ISBN 978-0735619678

**Members (3):**
1. Alice Johnson — alice.johnson@campus.edu — S001 — Registered 2026-01-15
2. Bob Smith — bob.smith@campus.edu — S002 — Registered 2026-02-20
3. Carol White — carol.white@campus.edu — S003 — Registered 2026-03-10

---

## How It Was Verified

### 1. EF Core Packages Installed

```bash
$ cd src/CampusLibrary.Data && dotnet add package Microsoft.EntityFrameworkCore.SqlServer
$ dotnet add package Microsoft.EntityFrameworkCore.Design
```

**Result:** Packages version 10.0.14 installed successfully.

### 2. Migration Created

```bash
$ dotnet ef migrations add InitialCreate -p src/CampusLibrary.Data -s src/CampusLibrary.WebApi
```

**Result:** Migration `20261107071528_InitialCreate` created in `src/CampusLibrary.Data/Migrations/`.

### 3. Database Updated

```bash
$ dotnet ef database update -p src/CampusLibrary.Data -s src/CampusLibrary.WebApi
```

**Result:**
- Database `CampusLibraryDb` created
- 3 tables created (Books, Members, Loans)
- 5 books and 3 members inserted
- 6 indexes created (1 PK per table + 3 unique indexes)

**Output excerpt:**
```
info: Microsoft.EntityFrameworkCore.Database.Command[20101]
      CREATE DATABASE [CampusLibraryDb];
...
info: Microsoft.EntityFrameworkCore.Migrations[20402]
      Applying migration '20261107071528_InitialCreate'.
...
info: Executed DbCommand INSERT INTO [Books] ... (5 rows)
info: Executed DbCommand INSERT INTO [Members] ... (3 rows)
...
Done.
```

### 4. Solution Build

```bash
$ dotnet build
```

**Result:** Build succeeded (0 errors, 12 warnings about System.Security.Cryptography.Pkcs vulnerabilities — transitive dependency, not actionable).

**Time elapsed:** 1.10 seconds

---

## Known Limits / TODO

### Milestone 2 (✅ Complete)

- ✅ 3 entity classes (Book, Member, Loan)
- ✅ LibraryDbContext with DbSets
- ✅ Fluent API configuration (max lengths, unique indexes, FK relationships)
- ✅ Seed data (5 books, 3 members)
- ✅ EF Core packages installed
- ✅ Connection strings added
- ✅ DbContext registered in WebApi
- ✅ Initial migration created and applied
- ✅ Database and tables created

### Milestone 3 (Next — Web API Books CRUD)

- ❌ **Repositories:** `IBookRepository`, `BookRepository` (database access layer)
- ❌ **Services:** `IBookService`, `BookService` (business logic layer)
- ❌ **DTOs:** `BookDto`, `CreateBookDto` (data transfer objects)
- ❌ **Controller:** `BooksController` with GET/POST/PUT/DELETE endpoints
- ❌ **DI registration:** Register repositories and services in `Program.cs`
- ❌ **Swagger testing:** Verify CRUD operations via Swagger UI

### Milestone 4 (Web API Members + Loans)

- ❌ Members CRUD endpoints
- ❌ Loans endpoints (borrow, return)
- ❌ Business rules: one book = max one open loan

### Future Milestones (5-7)

- ❌ Web MVC admin pages (Milestone 5)
- ❌ Android screens (Milestones 6-7)

---

## Architecture Notes

**Current state:** Data layer only (entities + DbContext).

**Next milestone (3) will implement the Service-Repository pattern per AI_DEVELOPMENT_GUIDE.md:**

```
BooksController (WebApi)
    ↓
IBookService → BookService (business logic)
    ↓
IBookRepository → BookRepository (database access)
    ↓
LibraryDbContext (EF Core)
    ↓
SQL Server (CampusLibraryDb)
```

**Key rules to follow in Milestone 3:**
- Controllers call services, not repositories or DbContext
- Services contain business logic
- Repositories contain database queries
- All async operations (`ToListAsync()`, `SaveChangesAsync()`)
- Interfaces for services and repositories
- Dependency injection for everything
- Structured logging with `ILogger<T>`
- `AsNoTracking()` for read-only queries

---

## Shortcuts Taken (ponytail debt)

None. This milestone follows the specification exactly with no shortcuts.

---

## Security Vulnerabilities (NuGet Warnings)

**Warning:** `System.Security.Cryptography.Pkcs` 9.0.11 has known vulnerabilities (GHSA-2fmv-rv2c-59x7, GHSA-846f-f764-76mx).

**Status:** Transitive dependency from EF Core 10.0.14. No direct action needed — wait for EF Core to update its dependencies.

**Risk assessment:** Low for local development. Production deployment should monitor for EF Core updates.

---

## Environment Details

- **.NET SDK:** 10.0.400
- **EF Core:** 10.0.14 (SqlServer + Design)
- **EF Tools:** 10.0.12 (older than runtime 10.0.14 — warning shown, not blocking)
- **SQL Server:** Azure SQL Edge (container ID: 970abab59164, port 14335)
- **Database:** `CampusLibraryDb` (newly created)
- **Platform:** macOS (Apple Silicon / arm64)

---

## Student Action Required

**Before Milestone 3:**

1. ✅ Database is ready with seed data
2. ✅ Solution builds successfully
3. ⏭️ Ready to implement repositories, services, and API controllers

**Optional verification:**

Use a database client (DBeaver, Azure Data Studio, or SQL Server Management Studio) to connect and verify:

```
Server: localhost,14335
Database: CampusLibraryDb
User: sa
Password: <your-local-password>
```

Then run:
```sql
SELECT * FROM Books;
SELECT * FROM Members;
SELECT * FROM Loans; -- Should be empty (no loans yet)
```

**Expected:** 5 books, 3 members, 0 loans.

---

## What Was NOT Implemented (As Planned)

Per ponytail principles and milestone scope:

❌ Repositories — **Milestone 3**  
❌ Services — **Milestone 3**  
❌ Controllers (beyond template) — **Milestone 3**  
❌ DTOs — **Milestone 3**  
❌ API endpoints — **Milestone 3**  
❌ Dependency injection registration (beyond DbContext) — **Milestone 3**  
❌ Swagger configuration (beyond template) — **Milestone 3**  
❌ CORS configuration — **Milestone 3**  
❌ Validation — **Milestone 3**  
❌ Error handling — **Milestone 3**  

This is expected — Milestone 2 is data layer only.
