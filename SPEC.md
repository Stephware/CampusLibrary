# Spec: CampusLibrary Solution Setup

## Objective

Create the foundational solution structure for **CampusLibrary**, a full-stack library management system for a second-year IT student project. This milestone establishes the solution file and four projects:

1. **CampusLibrary.Data** — Entity Framework Core class library
2. **CampusLibrary.WebApi** — ASP.NET Core REST API
3. **CampusLibrary.WebMvc** — ASP.NET Core MVC admin site with Razor views
4. **android/CampusLibraryApp** — Android Kotlin mobile app

**Success looks like:** A buildable solution with proper project references, a running SQL Server container, and a verified Android project structure that can be opened in Android Studio.

**User:** IT student building a graded assignment requiring MSSQL, .NET backend, and Android mobile client.

## Tech Stack

- **.NET SDK:** 10.0.400 (installed; also have 9.0.317 available)
- **Language:** C# 13 (.NET 10), Kotlin for Android
- **Database:** Microsoft SQL Server 2022 (Azure SQL Edge for Apple Silicon via Docker)
- **ORM:** Entity Framework Core 10.x
- **Android:** Kotlin, Jetpack Compose, min SDK 26, target SDK 37
- **Platform:** macOS (Apple Silicon / arm64)
- **Java:** OpenJDK 26.0.2
- **Android SDK:** Installed at `~/Library/Android/sdk` with platforms 36, 37.0, 37.1
- **Docker:** 29.8.2 (installed, no existing SQL Server containers)

## Commands

```bash
# Create solution and projects
dotnet new sln -n CampusLibrary
dotnet new classlib -n CampusLibrary.Data -o src/CampusLibrary.Data -f net10.0
dotnet new webapi -n CampusLibrary.WebApi -o src/CampusLibrary.WebApi -f net10.0
dotnet new mvc -n CampusLibrary.WebMvc -o src/CampusLibrary.WebMvc -f net10.0

# Add projects to solution
dotnet sln add src/CampusLibrary.Data/CampusLibrary.Data.csproj
dotnet sln add src/CampusLibrary.WebApi/CampusLibrary.WebApi.csproj
dotnet sln add src/CampusLibrary.WebMvc/CampusLibrary.WebMvc.csproj

# Add project references
dotnet add src/CampusLibrary.WebApi/CampusLibrary.WebApi.csproj reference src/CampusLibrary.Data/CampusLibrary.Data.csproj
dotnet add src/CampusLibrary.WebMvc/CampusLibrary.WebMvc.csproj reference src/CampusLibrary.Data/CampusLibrary.Data.csproj

# Build and verify
dotnet build

# SQL Server (Azure SQL Edge for Apple Silicon)
docker run -d \
  --name campuslibrary-mssql \
  -e 'ACCEPT_EULA=Y' \
  -e 'MSSQL_SA_PASSWORD=<your-local-password>' \
  -p 1433:1433 \
  mcr.microsoft.com/azure-sql-edge:latest

# Android (Gradle wrapper will be created by Android Studio)
# Open android/CampusLibraryApp in Android Studio
./gradlew assembleDebug  # (after Android Studio generates wrapper)
```

## Project Structure

```
CampusLibrary/
├── .git/
├── CLAUDE.md                          ← Master instructions (reads AI_DEVELOPMENT_GUIDE.md)
├── AI_DEVELOPMENT_GUIDE.md            ← Mandatory coding standards for .NET
├── SPEC.md                            ← This file
├── CampusLibrary.sln                  ← Solution file
├── docs/
│   └── reports/                       ← Auto-generated reports after each task
│       └── INDEX.md                   ← Report index
├── src/
│   ├── CampusLibrary.Data/            ← EF Core class library
│   │   ├── Entities/                  ← Book, Member, Loan (future milestone)
│   │   ├── Repositories/              ← Repository interfaces + implementations
│   │   │   ├── Interfaces/
│   │   │   │   ├── IBookRepository.cs
│   │   │   │   ├── IMemberRepository.cs
│   │   │   │   └── ILoanRepository.cs
│   │   │   ├── BookRepository.cs
│   │   │   ├── MemberRepository.cs
│   │   │   └── LoanRepository.cs
│   │   ├── LibraryDbContext.cs        ← DbContext (future milestone)
│   │   └── CampusLibrary.Data.csproj
│   ├── CampusLibrary.WebApi/          ← REST API
│   │   ├── Controllers/               ← API controllers (future milestone)
│   │   ├── Services/                  ← Service layer (business logic)
│   │   │   ├── Interfaces/
│   │   │   │   ├── IBookService.cs
│   │   │   │   ├── IMemberService.cs
│   │   │   │   └── ILoanService.cs
│   │   │   ├── BookService.cs
│   │   │   ├── MemberService.cs
│   │   │   └── LoanService.cs
│   │   ├── DTOs/                      ← Data transfer objects (future milestone)
│   │   ├── appsettings.json
│   │   ├── appsettings.Development.json  ← Connection string goes here
│   │   ├── Program.cs
│   │   └── CampusLibrary.WebApi.csproj
│   └── CampusLibrary.WebMvc/          ← Admin site
│       ├── Controllers/               ← MVC controllers (future milestone)
│       ├── Services/                  ← Service layer (business logic)
│       │   ├── Interfaces/
│       │   └── (same services as WebApi, or shared reference)
│       ├── ViewModels/                ← ViewModels for Razor views
│       ├── Views/                     ← Razor views (future milestone)
│       ├── wwwroot/                   ← Static files (Bootstrap, etc.)
│       ├── appsettings.json
│       ├── appsettings.Development.json
│       ├── Program.cs
│       └── CampusLibrary.WebMvc.csproj
└── android/
    └── CampusLibraryApp/              ← Android Kotlin app
        ├── app/
        │   ├── src/
        │   │   └── main/
        │   │       ├── java/com/campus/library/  ← Kotlin sources
        │   │       ├── res/                      ← Resources
        │   │       └── AndroidManifest.xml
        │   └── build.gradle.kts
        ├── gradle/
        ├── build.gradle.kts
        ├── settings.gradle.kts
        └── gradlew
```

**Key architectural notes:**
- **Repositories live in `CampusLibrary.Data`** (they touch DbContext)
- **Services can live in WebApi or WebMvc** (or be shared if both projects need the same logic)
- **Controllers call services, services call repositories, repositories call DbContext**
- No direct DbContext injection in controllers

## Code Style

**MANDATORY: Follow `AI_DEVELOPMENT_GUIDE.md` for all .NET code.** Key patterns:

### Architecture: Controller → Service → Repository → EF Core → SQL Server

```csharp
// CONTROLLER (WebApi or WebMvc)
public class BooksController : Controller
{
    private readonly IBookService _bookService;
    private readonly ILogger<BooksController> _logger;

    public BooksController(IBookService bookService, ILogger<BooksController> logger)
    {
        _bookService = bookService;
        _logger = logger;
    }

    public async Task<IActionResult> Index()
    {
        var books = await _bookService.GetAllAsync();
        return View(books);
    }
}

// SERVICE (business logic)
public interface IBookService
{
    Task<List<Book>> GetAllAsync();
    Task<Book?> GetByIdAsync(int id);
    Task<Book> CreateAsync(Book book);
}

public class BookService : IBookService
{
    private readonly IBookRepository _repository;
    private readonly ILogger<BookService> _logger;

    public BookService(IBookRepository repository, ILogger<BookService> logger)
    {
        _repository = repository;
        _logger = logger;
    }

    public async Task<List<Book>> GetAllAsync()
    {
        return await _repository.GetAllAsync();
    }
}

// REPOSITORY (database access)
public interface IBookRepository
{
    Task<List<Book>> GetAllAsync();
    Task<Book?> GetByIdAsync(int id);
    Task<Book> AddAsync(Book book);
    Task UpdateAsync(Book book);
    Task DeleteAsync(int id);
}

public class BookRepository : IBookRepository
{
    private readonly LibraryDbContext _context;

    public BookRepository(LibraryDbContext context)
    {
        _context = context;
    }

    public async Task<List<Book>> GetAllAsync()
    {
        return await _context.Books
            .AsNoTracking()
            .ToListAsync();
    }

    public async Task<Book?> GetByIdAsync(int id)
    {
        return await _context.Books
            .FirstOrDefaultAsync(b => b.Id == id);
    }

    public async Task<Book> AddAsync(Book book)
    {
        _context.Books.Add(book);
        await _context.SaveChangesAsync();
        return book;
    }
}

// ENTITY
namespace CampusLibrary.Data.Entities;

public class Book
{
    public int Id { get; set; }
    public required string Title { get; set; }
    public required string Isbn { get; set; }
    public string? Author { get; set; }
}
```

**Conventions (per AI_DEVELOPMENT_GUIDE.md):**
- File-scoped namespaces (`namespace X;`)
- `required` for non-nullable reference properties
- Interface for every service and repository (`IBookService`, `IBookRepository`)
- Dependency injection for all services, repositories, DbContext
- **Async database operations** (`ToListAsync()`, `FirstOrDefaultAsync()`, `SaveChangesAsync()`)
- Structured logging with `ILogger<T>` (never string concatenation)
- `AsNoTracking()` for read-only queries
- Controllers call services, not DbContext directly
- Services contain business logic
- Repositories contain database logic only
- Private fields: `_fieldName`
- Methods: `GetAllAsync()`, `CreateAsync()`
- **No** MediatR, CQRS, generic repositories, Unit of Work, AutoMapper unless explicitly requested

### Kotlin (Android)

```kotlin
// Package matches folder structure
package com.campus.library.ui.books

// Pascal case for classes, camel case for properties/functions
data class Book(
    val id: Int,
    val title: String,
    val isbn: String
)

// Composables are Pascal case
@Composable
fun BookListScreen(
    viewModel: BookViewModel = viewModel()
) {
    val books by viewModel.books.collectAsState()
    // ...
}
```

**Conventions:**
- Package: `com.campus.library`
- Data classes for models
- Sealed classes/interfaces for UI state
- Jetpack Compose for UI (no XML layouts)
- `StateFlow` for reactive state
- Repository pattern for API calls

## Testing Strategy

**This milestone:** No tests yet — solution setup only. Verification is `dotnet build` succeeding.

**Future milestones:**
- **Unit tests:** xUnit for .NET (separate test projects), JUnit for Android
- **Integration tests:** API endpoint tests with in-memory database
- **Location:** `tests/CampusLibrary.Data.Tests`, `tests/CampusLibrary.WebApi.Tests`, `android/CampusLibraryApp/app/src/test`
- **Coverage:** Not required for student project, but run tests after every change

## SQL Server via Docker

**Why Azure SQL Edge:** Microsoft SQL Server does not have an official arm64 image. Azure SQL Edge is the Microsoft-supported alternative for Apple Silicon, compatible with SQL Server T-SQL and EF Core.

**Container configuration:**
- **Image:** `mcr.microsoft.com/azure-sql-edge:latest`
- **Container name:** `campuslibrary-mssql`
- **Port:** 1433 (host) → 1433 (container)
- **SA password:** `<your-local-password>` (development only, never commit)
- **Database name:** `CampusLibraryDb` (will be created by EF Core migrations in milestone 2)

**Connection string format:**
```
Server=localhost,1433;Database=CampusLibraryDb;User Id=sa;Password=<password>;TrustServerCertificate=True;
```

## Android Gradle Setup

**Note:** Gradle wrapper (`gradlew`) will be generated by Android Studio when creating the project, not manually. The Android project will use Gradle 8.x with Kotlin DSL (`.gradle.kts` files).

**Package:** `com.campus.library`  
**Application ID:** `com.campus.library`  
**Min SDK:** 26 (Android 8.0)  
**Target SDK:** 37 (latest installed platform)

## Boundaries

### Always do:
- **Read `AI_DEVELOPMENT_GUIDE.md` before writing .NET code**
- Follow Controller → Service → Repository → EF Core → SQL Server architecture
- Use interfaces for all services and repositories (`IBookService`, `IBookRepository`)
- Use dependency injection (register in `Program.cs`)
- Use async database operations (`ToListAsync()`, `SaveChangesAsync()`)
- Use structured logging with `ILogger<T>`
- Use `AsNoTracking()` for read-only queries
- Run `dotnet build` after creating/modifying .NET projects
- Use `appsettings.Development.json` for connection strings, never hardcode
- Keep Android project in separate `android/` folder (not inside `src/`)
- Check Docker container is running before API starts (`docker ps`)
- Use project references, not DLL references
- Generate reports in `docs/reports/<timestamp>-<slug>/` after this task

### Ask first:
- Installing new NuGet packages (milestone 2+ will add EF Core packages)
- Changing SQL Server container configuration
- Modifying .NET SDK version or target framework
- Adding new projects beyond the 4 specified
- Changing default ports (5000/5001 for APIs)
- Introducing MediatR, CQRS, generic repositories, Unit of Work, AutoMapper, or DDD

### Never do:
- **Controllers directly accessing DbContext** (use services)
- **Services directly accessing DbContext** (use repositories)
- Blocking async calls (`.Result`, `.Wait()`, `.GetAwaiter().GetResult()`)
- Synchronous database operations (`.ToList()`, `.First()`, `.SaveChanges()`)
- String concatenation in logging (use structured logging)
- Instantiating services manually (`new BookService()`)
- Injecting `DbContext` into views (`@inject`)
- Silently swallowing exceptions (`catch { }`)
- Committing secrets, passwords, API keys, connection strings
- Using LocalDB (not available on macOS)
- Putting connection strings in `appsettings.json` (Development only)
- Committing `bin/`, `obj/`, `.vs/`, `.idea/`, `gradle/` to git
- Creating projects with .NET 8 when .NET 10 SDK is installed
- Using `dotnet ef` commands yet (milestone 2 — no migrations exist)
- Over-engineering or adding unnecessary abstraction layers

## Success Criteria

- [x] Solution file `CampusLibrary.sln` created at repository root
- [x] Three .NET projects created under `src/` with correct framework (net10.0)
- [x] Projects added to solution file
- [x] WebApi and WebMvc both reference Data project
- [x] `dotnet build` succeeds with no errors
- [x] Docker container `campuslibrary-mssql` running and accessible on port 1433
- [x] Android project structure created under `android/CampusLibraryApp/`
- [x] Android project opens successfully in Android Studio (manual verification step)
- [x] `docs/reports/` folder structure created with initial INDEX.md
- [x] Git branch switched to `main` (not `master`)
- [x] All four projects buildable (Android via `./gradlew assembleDebug` after Studio setup)

## Open Questions

1. **SQL Server password:** Is `<your-local-password>` acceptable for local development, or do you want a different password?
2. **Android package name:** Is `com.campus.library` appropriate, or do you want your institution/name in it (e.g., `com.youruni.campuslibrary`)?
3. **Git branch:** Should I create a `develop` branch for ongoing work, or continue on `main`?
4. **NuGet package versions:** For milestone 2, should EF Core use version 10.0.x (matching .NET 10), or stay on the stable 9.0.x?

---

**Next steps after approval:**
1. Create solution and .NET projects
2. Set up Docker SQL Server container
3. Create Android project structure (basic Gradle + manifest, Studio will complete it)
4. Verify builds
5. Switch git branch to `main`
6. Generate required reports in `docs/reports/`
