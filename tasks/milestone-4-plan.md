# Milestone 4 implementation plan

Goal: implement Members CRUD and Loans borrow/return using the existing Controller → Service → Repository → EF Core → MSSQL layers.
Authority: user request, CLAUDE.md, AI_DEVELOPMENT_GUIDE.md, existing milestone reports.

## Decisions
- Preserve .NET 10 / EF Core 10, existing container, ports and all untracked work. No packages or projects added.
- Members: GET list/detail, POST, PUT, DELETE; validate required trimmed strings, lengths and email; server assigns registration date. Unique email/student ID conflicts return 409. Keep loan history: deletion with any loan returns 409.
- Loans: GET list/detail (optional memberId/openOnly filters), POST borrow, POST {id}/return. Positive IDs; missing entities 404; unavailable book or repeated return 409. Server uses UTC and a 14-day due date (implementation choice; no period specified by existing requirements).
- Filtered unique Loans.BookId index for ReturnDate IS NULL closes concurrent borrow races; conditional update closes concurrent return races. Foreign-key races map to safe HTTP errors.
- Books deletion must return 409 when loan history prevents deletion; other Books behavior stays intact.
- Tests use Python standard-library unittest over real HTTP and SQL Server, avoiding new NuGet dependencies or test projects. Run on an isolated database, never reset CampusLibraryDb.

## Tasks
- [x] Add failing HTTP tests covering validation, CRUD, conflicts, borrowing, return, filters, concurrency, history preservation and Books regression.
- [x] Implement repository/service interfaces and classes, DTOs/controllers, DI; add migration for filtered index.
- [x] Build, migrate isolated database, run complete tests, inspect/fix failures; apply verified migration to development database without deleting data.
- [x] Review changed files, check migration drift; write three timestamped reports and update INDEX.md. Stop before Milestone 5.

## Review focus
Duplicate member writes including concurrent requests; concurrent borrow/return; malformed IDs and whitespace; loan-history deletion; unchanged seed data and Books CRUD.

## Completion evidence

- Final isolated HTTP/MSSQL suite: 7 tests passed; generated test database dropped.
- Build: zero errors; six pre-existing vulnerability warnings.
- Development index migration applied; no pending model changes.
- Code review finding reproduced and fixed: concurrent member deletion now returns 404.
- Reports generated and linked. No MVC/Android implementation.
