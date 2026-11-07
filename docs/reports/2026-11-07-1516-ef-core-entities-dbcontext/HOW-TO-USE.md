# How to Use — EF Core Entities, DbContext & Database

> Historical milestone record. Statements and verification below describe the original task, not a fresh run. For current setup and known limitations, use [the latest report index](../INDEX.md).

**Milestone:** 2 — Entity Framework Core Setup  
**Date:** 2026-11-07  
**For:** Developers building on the data layer

---

## What Was Built

The **data layer** for CampusLibrary is now complete:

1. **3 entity classes** (Book, Member, Loan)
2. **LibraryDbContext** with DbSets, configuration, and seed data
3. **SQL Server database** (`CampusLibraryDb`) with 3 tables, indexes, and relationships
4. **5 sample books** and **3 sample members** already in the database

**Note:** No API endpoints, repositories, or services yet. Those come in Milestone 3+.

---

## Prerequisites

Before you proceed, verify:

1. **.NET SDK 10.0** or higher
   - Check: `dotnet --version`

2. **SQL Server container running**
   ```bash
   docker ps --filter "name=campuslibrary-mssql"
   ```
   **Expected:** Status shows "Up" on port 14335

3. **Solution builds**
   ```bash
   dotnet build
   ```
   **Expected:** Build succeeded with 0 errors

---

## Step-by-Step: Verify the Database

### Step 1: Check the Database Exists

```bash
dotnet ef dbcontext info -p src/CampusLibrary.Data -s src/CampusLibrary.WebApi
```

**Expected output:**
```
Build succeeded.
Provider name: Microsoft.EntityFrameworkCore.SqlServer
Options: ProviderAssemblyVersion=10.0.14.0
```

---

### Step 2: Connect to the Database (Optional)

Use **DBeaver**, **Azure Data Studio**, or **SQL Server Management Studio**:

- **Server:** localhost,14335
- **Database:** CampusLibraryDb
- **User:** sa
- **Password:** <your-local-password>
- **Trust certificate:** Yes

---

### Step 3: Query the Seed Data

Run these SQL queries in your database client:

#### List all books:
```sql
SELECT * FROM Books;
```

**Expected:** 5 rows
```
Id | Title                          | Isbn            | Author                | YearPublished
1  | Clean Code                     | 978-0132350884  | Robert C. Martin      | 2008
2  | The Pragmatic Programmer       | 978-0201616224  | Andrew Hunt           | 1999
3  | Design Patterns                | 978-0201633610  | Erich Gamma           | 1994
4  | Introduction to Algorithms     | 978-0262033848  | Thomas H. Cormen      | 2009
5  | Code Complete                  | 978-0735619678  | Steve McConnell       | 2004
```

#### List all members:
```sql
SELECT * FROM Members;
```

**Expected:** 3 rows
```
Id | Name           | Email                    | StudentId | RegisteredDate
1  | Alice Johnson  | alice.johnson@campus.edu | S001      | 2026-01-15
2  | Bob Smith      | bob.smith@campus.edu     | S002      | 2026-02-20
3  | Carol White    | carol.white@campus.edu   | S003      | 2026-03-10
```

#### List all loans:
```sql
SELECT * FROM Loans;
```

**Expected:** 0 rows (no loans created yet — those come in Milestone 4)

---

### Step 4: Check Database Schema

```sql
-- List all tables
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'dbo';

-- List columns in Books table
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'Books' ORDER BY ORDINAL_POSITION;

-- List indexes
SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('Books');
```

---

## How to Add a New Migration

If you modify an entity (e.g., add a `Publisher` field to `Book`), create a new migration:

### Step 1: Update the entity

Edit `src/CampusLibrary.Data/Entities/Book.cs`:
```csharp
public class Book
{
    public int Id { get; set; }
    public required string Title { get; set; }
    public required string Isbn { get; set; }
    public string? Author { get; set; }
    public string? Publisher { get; set; }  // NEW
    public int? YearPublished { get; set; }

    public ICollection<Loan> Loans { get; set; } = new List<Loan>();
}
```

### Step 2: Create the migration

```bash
cd /Users/stephen/CampusLibrary
dotnet ef migrations add AddPublisherToBook -p src/CampusLibrary.Data -s src/CampusLibrary.WebApi
```

**Expected:** Migration `20261107HHMM_AddPublisherToBook.cs` created in `Migrations/` folder.

### Step 3: Apply the migration

```bash
dotnet ef database update -p src/CampusLibrary.Data -s src/CampusLibrary.WebApi
```

**Expected:** `ALTER TABLE [Books] ADD [Publisher] nvarchar(100) NULL;`

---

## How to Rollback a Migration

If you created a migration by mistake:

```bash
dotnet ef migrations remove -p src/CampusLibrary.Data -s src/CampusLibrary.WebApi
```

**Warning:** Do NOT use `remove` if you already applied the migration to production. Use `migrations add` with a new migration to undo changes instead.

---

## Connection String Reference

The connection string is in **two places**:

1. **`src/CampusLibrary.WebApi/appsettings.Development.json`** (used by WebApi)
   ```json
   "ConnectionStrings": {
     "DefaultConnection": "Server=localhost,14335;Database=CampusLibraryDb;User Id=sa;Password=<your-local-password>;TrustServerCertificate=True;"
   }
   ```

2. **`src/CampusLibrary.WebMvc/appsettings.Development.json`** (used by WebMvc)
   ```json
   "ConnectionStrings": {
     "DefaultConnection": "Server=localhost,14335;Database=CampusLibraryDb;User Id=sa;Password=<your-local-password>;TrustServerCertificate=True;"
   }
   ```

**Key points:**
- **Port:** 14335 (not 1433)
- **Server:** localhost (not 127.0.0.1)
- **Database:** CampusLibraryDb
- **User:** sa (SQL Server admin)
- **Password:** <your-local-password> (development only, never commit)
- **TrustServerCertificate:** True (for self-signed cert on development container)

---

## Troubleshooting

### Problem 1: "Cannot connect to server localhost,14335"

**Cause:** SQL Server container is not running or port 14335 is not accessible.

**Fix:**
```bash
# Check container status
docker ps --filter "name=campuslibrary-mssql"

# If not running, start it
docker start campuslibrary-mssql

# If container doesn't exist, create it
docker run -d \
  --name campuslibrary-mssql \
  -e 'ACCEPT_EULA=Y' \
  -e 'MSSQL_SA_PASSWORD=<your-local-password>' \
  -p 14335:1433 \
  mcr.microsoft.com/azure-sql-edge:latest
```

---

### Problem 2: "Login failed for user 'sa'"

**Cause:** Wrong password in connection string.

**Fix:**
1. Verify password is `<your-local-password>` in `appsettings.Development.json`
2. If container was created with a different password, recreate it with the correct one
3. Check for typos (no extra spaces)

---

### Problem 3: "The entity of type 'Book' requires a key to be defined"

**Cause:** Entity class is missing `[Key]` attribute or `Id` property.

**Fix:**
- Ensure `Book.cs` has an `Id` property
- Or use Fluent API: `modelBuilder.Entity<Book>().HasKey(e => e.Id);`

---

### Problem 4: "Unable to track an instance of type 'Book' because it is already being tracked"

**Cause:** Entity is being tracked by multiple DbContext instances or double-loaded.

**Fix:**
- Use `.AsNoTracking()` for read-only queries
- Dispose DbContext properly (dependency injection handles this)
- Avoid loading the same entity twice in one query

---

### Problem 5: Migration fails with "The ObjectId of index 'IX_...' cannot be used"

**Cause:** Schema conflicts or stale migration history.

**Fix:**
```bash
# Nuclear option: delete the database and re-migrate (DEV ONLY!)
dotnet ef database drop -p src/CampusLibrary.Data -s src/CampusLibrary.WebApi
dotnet ef database update -p src/CampusLibrary.Data -s src/CampusLibrary.WebApi
```

**Warning:** This deletes all data. Only use in development.

---

## What You Should See

### Build Output
```bash
$ dotnet build
Build succeeded.
0 Error(s)
```

### Migration Creation
```bash
$ dotnet ef migrations add InitialCreate ...
Done. To undo this action, use 'ef migrations remove'
```

### Database Update
```bash
$ dotnet ef database update ...
info: Microsoft.EntityFrameworkCore.Migrations[20402]
      Applying migration '20261107071528_InitialCreate'.
...
Done.
```

### SQL Query (5 Books)
```
SELECT COUNT(*) FROM Books;
-- Result: 5
```

---

## What Comes Next

**Milestone 3** will add:
- Book repository (`IBookRepository`, `BookRepository`)
- Book service (`IBookService`, `BookService`)
- Books API controller with CRUD endpoints
- Swagger testing
- Dependency injection registration

The DbContext you just created will be used by repositories to query and modify the database.

---

## Key Concepts

### DbContext
The bridge between your application and SQL Server. Manages entity state, tracks changes, and executes queries.

### Migrations
Version control for your database schema. Each migration is a snapshot of changes to apply.

### Seed Data
Initial data inserted into the database. Defined in `OnModelCreating()` with `HasData()`.

### Foreign Keys
Relationships between tables. `Loan.BookId` references `Book.Id`. `DeleteBehavior.Restrict` prevents deleting a book that has loans.

### Unique Indexes
Prevent duplicate values. `ISBN`, `Email`, and `StudentId` must be unique across all rows.

---

## Next Steps for Students

1. **Verify the database** using the queries above
2. **Study the entities** in `src/CampusLibrary.Data/Entities/`
3. **Study the DbContext** in `src/CampusLibrary.Data/LibraryDbContext.cs`
4. **Ready for Milestone 3** — Repositories, services, API controllers
