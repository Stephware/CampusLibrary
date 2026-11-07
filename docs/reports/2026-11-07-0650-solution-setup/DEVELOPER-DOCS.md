# Developer Documentation — Solution Setup

> Historical milestone record. Statements and verification below describe the original task, not a fresh run. For current setup and known limitations, use [the latest report index](../INDEX.md).

**Milestone:** 1  
**Task:** Create CampusLibrary solution structure with 4 projects  
**Date:** 2026-11-07 06:50  
**Status:** ✅ Complete

---

## Summary

Created the foundational solution structure for CampusLibrary, a full-stack library management system. This milestone established:

1. **3 .NET 10 projects:** Data (class library), WebApi (REST API), WebMvc (admin site)
2. **1 Android Kotlin project:** Mobile app structure with Jetpack Compose
3. **SQL Server container:** Azure SQL Edge on port 14335 (Apple Silicon compatible)
4. **Repository setup:** Git branch switched to `main`, `.gitignore` configured, `docs/reports/` structure created

**No application code was implemented**—this milestone is infrastructure only. Entities, services, repositories, controllers, and UI come in future milestones.

---

## Projects Touched

- ✅ **EF CORE** — CampusLibrary.Data class library created (no entities yet)
- ✅ **WEB API** — CampusLibrary.WebApi project created (no controllers yet)
- ✅ **WEB MVC** — CampusLibrary.WebMvc project created (no views yet)
- ✅ **MSSQL** — Azure SQL Edge container running on port 14335
- ✅ **ANDROID KOTLIN** — Project structure created (requires Android Studio to complete Gradle wrapper)

---

## Files Changed

| Path | Status | Why |
|------|--------|-----|
| `CampusLibrary.sln` | Added | Solution file containing all 3 .NET projects |
| `src/CampusLibrary.Data/CampusLibrary.Data.csproj` | Added | Class library for EF Core entities, repositories, DbContext (future) |
| `src/CampusLibrary.Data/Class1.cs` | Added | Template default (will be removed in milestone 2) |
| `src/CampusLibrary.WebApi/CampusLibrary.WebApi.csproj` | Added | REST API project with reference to Data |
| `src/CampusLibrary.WebApi/Program.cs` | Added | WebApi entry point (template default) |
| `src/CampusLibrary.WebApi/appsettings.json` | Added | WebApi configuration |
| `src/CampusLibrary.WebApi/appsettings.Development.json` | Added | Development-specific settings (connection string goes here in milestone 2) |
| `src/CampusLibrary.WebMvc/CampusLibrary.WebMvc.csproj` | Added | MVC project with reference to Data |
| `src/CampusLibrary.WebMvc/Program.cs` | Added | WebMvc entry point (template default) |
| `src/CampusLibrary.WebMvc/appsettings.json` | Added | WebMvc configuration |
| `src/CampusLibrary.WebMvc/appsettings.Development.json` | Added | Development-specific settings |
| `src/CampusLibrary.WebMvc/Controllers/HomeController.cs` | Added | Default MVC home controller (template) |
| `src/CampusLibrary.WebMvc/Views/` | Added | Default Razor views (template) |
| `android/CampusLibraryApp/build.gradle.kts` | Added | Root Android build file |
| `android/CampusLibraryApp/settings.gradle.kts` | Added | Gradle settings |
| `android/CampusLibraryApp/gradle.properties` | Added | Android Gradle properties |
| `android/CampusLibraryApp/gradle/libs.versions.toml` | Added | Version catalog for Android dependencies |
| `android/CampusLibraryApp/app/build.gradle.kts` | Added | App module build file (minSdk 26, targetSdk 37, Compose) |
| `android/CampusLibraryApp/app/src/main/AndroidManifest.xml` | Added | Android manifest with INTERNET permission |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/MainActivity.kt` | Added | Placeholder Compose activity |
| `android/CampusLibraryApp/app/src/main/res/values/strings.xml` | Added | App strings resource |
| `.gitignore` | Added | Excludes bin/, obj/, build/, .vs/, .idea/, appsettings.Development.json |
| `docs/reports/INDEX.md` | Added | Reports index |
| `docs/reports/2026-11-07-0650-solution-setup/` | Added | This report folder |
| `CLAUDE.md` | Modified | Added mandatory reference to AI_DEVELOPMENT_GUIDE.md |
| `SPEC.md` | Modified | Integrated Service-Repository architecture, DI, async patterns |
| `tasks/plan.md` | Added | Implementation plan |
| `tasks/todo.md` | Added | Task breakdown (22 tasks) |

---

## Key Code

### Solution Structure

```bash
CampusLibrary/
├── CampusLibrary.sln                          # 3 projects
├── src/
│   ├── CampusLibrary.Data/                    # net10.0 class library
│   ├── CampusLibrary.WebApi/                  # net10.0 Web API
│   └── CampusLibrary.WebMvc/                  # net10.0 MVC
└── android/CampusLibraryApp/                  # Android Kotlin
```

### Project References

```xml
<!-- WebApi.csproj and WebMvc.csproj both have: -->
<ItemGroup>
  <ProjectReference Include="..\CampusLibrary.Data\CampusLibrary.Data.csproj" />
</ItemGroup>
```

### Android Configuration

**Package:** `com.campus.library`  
**Min SDK:** 26 (Android 8.0)  
**Target SDK:** 37  
**Compose BOM:** 2024.10.00  
**Key dependencies:** Retrofit 2.9.0, Jetpack Compose, Material3

### SQL Server Container

**Image:** `mcr.microsoft.com/azure-sql-edge:latest` (Apple Silicon compatible)  
**Container name:** `campuslibrary-mssql`  
**Port mapping:** 14335 → 1433 (changed from default 1433 due to existing container)  
**SA password:** `<your-local-password>` (development only, never commit)

**Connection string format:**
```
Server=localhost,14335;Database=CampusLibraryDb;User Id=sa;Password=<your-local-password>;TrustServerCertificate=True;
```

---

## Database Changes

**None yet.** The database container is running, but no database, tables, or migrations exist.

**Next milestone (2):** Create `CampusLibraryDb` database, entities (Book, Member, Loan), DbContext, seed data, and first EF Core migration.

---

## How It Was Verified

### .NET Build

```bash
$ dotnet build
# Result: Build succeeded. 0 Warning(s), 0 Error(s)
# Time: 00:00:01.97
```

**All 3 projects built successfully:**
- CampusLibrary.Data → `bin/Debug/net10.0/CampusLibrary.Data.dll`
- CampusLibrary.WebApi → `bin/Debug/net10.0/CampusLibrary.WebApi.dll`
- CampusLibrary.WebMvc → `bin/Debug/net10.0/CampusLibrary.WebMvc.dll`

### Solution Verification

```bash
$ dotnet sln list
# Result: Lists Data, WebApi, WebMvc
```

### SQL Server Container

```bash
$ docker ps --filter "name=campuslibrary-mssql"
# Result: Container running on port 14335, status "Up"
```

### Git Branch

```bash
$ git branch --show-current
# Result: main
```

### Android Project

**Manual verification required:** Open `android/CampusLibraryApp/` in Android Studio. Studio will:
1. Generate Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/`)
2. Download dependencies
3. Sync project

Then run:
```bash
$ ./gradlew assembleDebug
```

---

## Known Limits / TODO

### Milestone 1 (✅ Complete)

- ✅ Solution structure created
- ✅ SQL Server container running
- ✅ Android project structure created
- ✅ Git on `main` branch
- ✅ `.gitignore` configured
- ✅ Reports structure created

### Milestone 2 (Next)

- ❌ **Entities:** Book, Member, Loan classes in `CampusLibrary.Data/Entities/`
- ❌ **DbContext:** `LibraryDbContext` with `DbSet<Book>`, `DbSet<Member>`, `DbSet<Loan>`
- ❌ **Seed data:** 5 books, 3 members using `HasData()`
- ❌ **EF Core packages:** Install `Microsoft.EntityFrameworkCore.SqlServer`, `Microsoft.EntityFrameworkCore.Design`
- ❌ **First migration:** Create and apply initial migration to create tables
- ❌ **Connection string:** Add to `appsettings.Development.json` pointing to port 14335

### Future Milestones (3-7)

- ❌ Repositories and services (Controller → Service → Repository → EF Core architecture per AI_DEVELOPMENT_GUIDE.md)
- ❌ Web API endpoints (Books, Members, Loans CRUD + borrow/return)
- ❌ Web MVC admin pages (Razor views)
- ❌ Android screens (Compose UI calling the API)

---

## Architecture Notes (per AI_DEVELOPMENT_GUIDE.md)

**For future milestones, follow this pattern:**

```
Controller (WebApi/WebMvc)
    ↓
Service (business logic, interfaces)
    ↓
Repository (database access, interfaces)
    ↓
Entity Framework Core / DbContext
    ↓
SQL Server
```

**Key rules:**
- Use interfaces for all services and repositories
- Dependency injection for everything
- Async database operations (`ToListAsync()`, `SaveChangesAsync()`)
- Structured logging with `ILogger<T>`
- `AsNoTracking()` for read-only queries
- No MediatR, CQRS, generic repositories, Unit of Work unless explicitly requested
- No direct DbContext access from controllers

---

## Shortcuts Taken (ponytail debt)

None. This is infrastructure setup—no shortcuts needed or taken.

---

## Environment Details

- **.NET SDK:** 10.0.400
- **Platform:** macOS (Apple Silicon / arm64)
- **Docker:** 29.8.2
- **Java:** OpenJDK 26.0.2
- **Android SDK:** Platforms 36, 37.0, 37.1
- **SQL Server:** Azure SQL Edge (container ID: 970abab59164)

---

## Student Action Required

**Before milestone 2:**
1. Fill in name, section, and date at the top of `CLAUDE.md`
2. Open `android/CampusLibraryApp/` in Android Studio to generate Gradle wrapper
3. Verify Android project builds: `./gradlew assembleDebug`

**Port 14335 note:** The SQL Server container uses port 14335 instead of the default 1433 because port 1433 was already in use by another container (`mssql_container`). Update connection strings accordingly in milestone 2.
