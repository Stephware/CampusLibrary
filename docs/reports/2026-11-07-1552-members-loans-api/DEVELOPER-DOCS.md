# Milestone 4 — Developer documentation

## Summary

Implemented Members CRUD and Loans read/borrow/return endpoints using the existing layers.
Borrowing allows at most one open loan per book, including concurrent requests.
Returns are atomic, and loan history prevents member/book deletion.
Verified on macOS arm64 with the existing .NET 10, EF Core 10 and MSSQL container.
Milestones 1–3 were retained; Milestone 5 and Android were not changed.

## Projects touched

WEB API, EF CORE/Data and MSSQL. No new NuGet packages, solution projects, framework versions, container settings or default ports.

## Files changed

Statuses are relative to the start of this task. The repository has no commits and all prior files were already untracked; no commits, staging or resets were performed.

| Path | Status | Purpose |
|---|---|---|
| `tasks/milestone-4-plan.md` | added | Records the milestone scope, decisions, checks and completion. |
| `src/CampusLibrary.Data/Repositories/Interfaces/IMemberRepository.cs` | added | Defines asynchronous member persistence operations. |
| `src/CampusLibrary.Data/Repositories/MemberRepository.cs` | added | Queries and persists members, translating uniqueness, history and concurrent deletion conflicts. |
| `src/CampusLibrary.Data/Repositories/Interfaces/ILoanRepository.cs` | added | Defines loan queries, insertion and conditional return persistence. |
| `src/CampusLibrary.Data/Repositories/LoanRepository.cs` | added | Filters loans in SQL and persists borrow/return changes with concurrency protection. |
| `src/CampusLibrary.Data/Repositories/BookRepository.cs` | modified | Maps loan-history foreign-key deletion failures to a safe conflict response. |
| `src/CampusLibrary.Data/LibraryDbContext.cs` | modified | Configures the unique filtered open-loan index. |
| `src/CampusLibrary.Data/Migrations/20261107074756_EnforceOneOpenLoanPerBook.cs` | added | Replaces the Loans.BookId index with a unique index filtered to open loans and provides rollback. |
| `src/CampusLibrary.Data/Migrations/20261107074756_EnforceOneOpenLoanPerBook.Designer.cs` | added | Stores EF metadata for the new migration. |
| `src/CampusLibrary.Data/Migrations/LibraryDbContextModelSnapshot.cs` | modified | Tracks the migrated EF model. |
| `src/CampusLibrary.WebApi/DTOs/MemberDtos.cs` | added | Defines member request validation and response DTOs. |
| `src/CampusLibrary.WebApi/DTOs/LoanDtos.cs` | added | Defines positive-ID borrow input and loan response DTOs. |
| `src/CampusLibrary.WebApi/Services/Interfaces/IMemberService.cs` | added | Defines member use cases for controllers. |
| `src/CampusLibrary.WebApi/Services/MemberService.cs` | added | Applies member uniqueness, normalization, registration dates and deletion rules. |
| `src/CampusLibrary.WebApi/Services/Interfaces/ILoanService.cs` | added | Defines loan read, borrow and return use cases. |
| `src/CampusLibrary.WebApi/Services/LoanService.cs` | added | Validates borrowing eligibility, assigns UTC dates and enforces return rules. |
| `src/CampusLibrary.WebApi/Controllers/MembersController.cs` | added | Exposes Members CRUD through the member service. |
| `src/CampusLibrary.WebApi/Controllers/LoansController.cs` | added | Exposes loan list/detail, borrowing and returning through the loan service. |
| `src/CampusLibrary.WebApi/Infrastructure/ApiExceptionHandler.cs` | added | Produces safe problem details for service errors and logs unexpected failures. |
| `src/CampusLibrary.WebApi/Program.cs` | modified | Registers new services, repositories and exception handling. |
| `tests/test_milestone4.py` | added | Exercises real HTTP and MSSQL contracts, concurrency and Books regression with standard-library unittest. |
| `tests/run_milestone4.py` | added | Builds and migrates a disposable SQL database, starts a temporary API, runs tests and removes its database. |
| `docs/reports/INDEX.md` | modified | Links this milestone report. |
| `docs/reports/2026-11-07-1552-members-loans-api/DEVELOPER-DOCS.md` | added | Documents implementation, files, migration and verification. |
| `docs/reports/2026-11-07-1552-members-loans-api/HOW-TO-USE.md` | added | Explains running and using the endpoints with examples and troubleshooting. |
| `docs/reports/2026-11-07-1552-members-loans-api/FLOWCHARTS.md` | added | Shows feature logic, layer interactions and affected database relationships. |

## API contract

| Method | Path | Success | Rules |
|---|---|---|---|
| GET | `/api/members` | 200 | Member DTO list |
| GET | `/api/members/{id}` | 200 | 404 if missing |
| POST | `/api/members` | 201 + Location | Unique email and student ID; server registration date |
| PUT | `/api/members/{id}` | 204 | Full update; preserves registration date |
| DELETE | `/api/members/{id}` | 204 | 409 if any loan history; 404 if missing |
| GET | `/api/loans` | 200 | Optional `memberId` and `openOnly=true`; default all history |
| GET | `/api/loans/{id}` | 200 | Loan DTO; 404 if missing |
| POST | `/api/loans` | 201 + Location | Existing book/member, no open loan for that book |
| POST | `/api/loans/{id}/return` | 204 | 404 missing; 409 already returned |

Members require name/email/student ID, lengths 100/100/20, and a valid email. Required strings reject whitespace; stored strings are trimmed. SQL collation controls uniqueness comparisons. Positive route/filter/borrow IDs and valid query types are required. Input errors return 400 ValidationProblemDetails; new domain errors use 404/409 ProblemDetails. An unknown member filter returns an empty list. Unexpected failures log server-side and return generic 500 details.

Book DELETE now returns 409 when existing foreign keys protect loan history (including returned loans). Other Books behavior is retained. Controllers only call services, services apply business rules, repositories own EF queries. Read-only lists use AsNoTracking; member edits use tracked entities.

## Key code

```csharp
// LibraryDbContext: unique only among open loans.
entity.HasIndex(e => e.BookId).IsUnique().HasFilter("[ReturnDate] IS NULL");

// LoanService: dates are never supplied by the client.
var now = DateTime.UtcNow;
// BorrowDate = now, DueDate = now.AddDays(14)

// LoanRepository: exactly one concurrent return succeeds.
var updated = await _context.Loans
    .Where(l => l.Id == id && l.ReturnDate == null)
    .ExecuteUpdateAsync(setters => setters.SetProperty(l => l.ReturnDate, returnDate));
```

The 14-day loan period is an implementation choice because earlier requirements specify no duration. No fines, renewals or member loan caps were introduced. Date responses explicitly carry UTC (`Z`) because SQL datetime2 does not retain DateTime.Kind.

The service availability check gives an early conflict; the filtered unique index closes the race between check and insert. Repository translations handle SQL unique-index errors 2601/2627 and foreign-key errors 547. Member DbUpdateConcurrencyException becomes 404 when a concurrent request deletes the row. Return requests cannot overwrite the original return timestamp.

## Database changes

Migration: `20261107074756_EnforceOneOpenLoanPerBook`.

- Replaces `IX_Loans_BookId` with a unique filtered index (`ReturnDate IS NULL`).
- No tables, columns, seed data or existing records are removed/rewritten.
- Applied successfully to the isolated test databases and existing `CampusLibraryDb` at port 14335.
- Down restores the original non-unique index. Future databases containing duplicate open loans require resolving those records before applying this migration; the migration does not delete them.

## Verification

1. Baseline `dotnet build --no-restore`: passed before changes.
2. Initial HTTP suite against existing API: five expected failures (missing Members/Loans), Books regression passed.
3. `dotnet build --no-restore -p:UseSharedCompilation=false -m:1`: passed, zero errors. Also run by the test runner after final source changes.
4. `python3 tests/run_milestone4.py`: final run passed all **7 tests**, 0.454 seconds of test execution. Each run migrated a fresh `CampusLibraryM4Tests_<random>` SQL database, launched a separate API on an ephemeral localhost port, and dropped only that database in cleanup.
5. Covered Members CRUD, unchanged registration date, uniqueness on create/update, concurrent uniqueness, required/length/email validation, invalid IDs, missing entities, loan filters, 14-day borrowing, duplicate borrow, return/repeated return, immutable return date, reborrowing, retained history and Books CRUD regression.
6. Eight simultaneous borrows: exactly one 201 and seven 409. Eight simultaneous returns: exactly one 204 and seven 409. Concurrent member deletion: exactly one 204 and seven 404, repeated three times.
7. Code review found concurrent member deletion could return 500. Regression reproduced `[204,404,404,404,500,500,500,500]`; repository handling was fixed, full suite passed, and reviewer confirmed the fix.
8. `dotnet test --no-build --no-restore`: exit 0; no .NET test projects exist, so this is not a count of automated tests. The seven tests above are Python HTTP integration tests.
9. `dotnet ef database update -p src/CampusLibrary.Data -s src/CampusLibrary.WebApi --no-build`: applied the new migration to the existing development DB successfully.
10. `dotnet ef migrations has-pending-model-changes -p src/CampusLibrary.Data -s src/CampusLibrary.WebApi --no-build`: no changes since last migration.

Sandbox restrictions blocked compiler IPC/local sockets; successful runtime checks used approved local execution outside the sandbox. Existing container configuration was retained.

## Known limits / next milestones

- Pre-existing NU1902/NU1903 warnings remain for transitive System.Security.Cryptography.Pkcs 9.0.11. Dependency remediation was not included in Members/Loans work; these are real unresolved moderate/high vulnerability warnings.
- Existing dotnet-ef 10.0.12 warns that runtime 10.0.14 is newer. Migration generation/application and drift checks succeeded; no global tools were changed.
- Existing Books validation and response conventions remain as implemented in Milestone 3, except history-safe deletion via the new error handler.
- API authentication and paging are outside the existing specification. This remains a local educational application.
- Web MVC, Android, CORS and interactive Swagger UI remain in their existing state. Existing Development OpenAPI JSON remains at `/openapi/v1.json`.
- The pre-existing API process on port 5143 was left running; restart it to load the newly built endpoints.
