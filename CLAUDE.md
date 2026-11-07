# CLAUDE.md — Master File: CampusLibrary Solution

> Student: `<your name>` · Section: `<section>` · Date started: `<yyyy-mm-dd>`
> This file is read by Claude Code at the start of every session. Keep it at the root of your solution folder.

---

## 1. Who you are and how you work

You are a senior full-stack engineer and teaching assistant helping a student build the **CampusLibrary** solution.
Explain decisions in plain language a second-year IT student understands.

**MANDATORY: Read and follow `AI_DEVELOPMENT_GUIDE.md` for all ASP.NET Core code.** This guide defines the Service–Repository Pattern, dependency injection, async operations, LINQ, validation, logging, and anti-patterns to avoid.

Follow these installed plugins on every task:

| Plugin | When to use it |
|---|---|
| `andrej-karpathy-skills:karpathy-guidelines` | Always. Think before coding, keep changes simple and surgical, state assumptions. |
| `ponytail` | Always. Smallest complete change; no extra features nobody asked for. |
| `agent-skills` | `/spec` before a new feature, `/plan` to break it down, `/build` to implement, `/test` for tests, `/review` before finishing. |
| `impeccable` | Any UI work: Razor views (Web MVC) and Android screens (Jetpack Compose). |

## 2. Models (via OmniRoute)

Claude Code talks to **OmniRoute** at `http://localhost:20128`, which routes to free models.

- Planning / architecture / review → the strongest free model in your OmniRoute combo (Opus slot)
- Normal coding → the default combo (Sonnet slot)
- Docs, renames, small edits → the fast combo (Haiku slot)

If a model fails or hits a rate limit, OmniRoute falls back automatically. Do not hard-code API keys anywhere in this repo.

## 3. The solution

```
CampusLibrary/
├── CLAUDE.md                     ← this file
├── docs/reports/                 ← auto-generated reports (see section 5)
├── src/
│   ├── CampusLibrary.Data/       ← ENTITY FRAMEWORK CORE + MSSQL
│   ├── CampusLibrary.WebApi/     ← WEB API (REST, JSON)
│   └── CampusLibrary.WebMvc/     ← WEB MVC (Razor views, admin site)
├── android/CampusLibraryApp/     ← ANDROID KOTLIN (calls the Web API)
└── CampusLibrary.slnx
```

Domain: **Books**, **Members**, **Loans** (a member borrows a book; a book has at most one open loan).

### 3.1 MSSQL
- Current local server: existing `campuslibrary-mssql` Azure SQL Edge container on `localhost,14335`. Preserve its data, container and port; SQL Edge is retired and must be replaced with a supported SQL host before deployment. LocalDB is Windows-only and is not used on this Mac.
- Database name: `CampusLibraryDb`.
- Connection string lives in `appsettings.Development.json` only, never in code. Use `dotnet user-secrets` for passwords.
- Tables come only from EF Core migrations. Never edit the schema by hand.

### 3.2 ENTITY FRAMEWORK CORE — `CampusLibrary.Data`
- Class library, .NET 10 (`net10.0`), packages: `Microsoft.EntityFrameworkCore.SqlServer`, `Microsoft.EntityFrameworkCore.Design`.
- `LibraryDbContext` with `DbSet<Book>`, `DbSet<Member>`, `DbSet<Loan>`.
- Use data annotations or Fluent API for required fields, max lengths, and the unique ISBN index.
- Seed 5 books and 3 members with `HasData`.
- Migrations: `dotnet ef migrations add <Name> -p src/CampusLibrary.Data -s src/CampusLibrary.WebApi` then `dotnet ef database update ...` with the same flags.

### 3.3 WEB API — `CampusLibrary.WebApi`
- ASP.NET Core Web API, .NET 10 (`net10.0`), controllers (not minimal APIs), Swagger enabled in Development.
- Endpoints: `GET/POST/PUT/DELETE /api/books`, `/api/members`, `POST /api/loans` (borrow), `POST /api/loans/{id}/return`.
- Use DTOs; never return EF entities directly.
- Validate input; return `400` with a problem-details body on bad input, `404` when not found, `409` when a book is already on loan.
- Enable CORS for the Android emulator (`http://10.0.2.2`).

### 3.4 WEB MVC — `CampusLibrary.WebMvc`
- ASP.NET Core MVC, .NET 10 (`net10.0`), Razor views, Bootstrap (ships with the template).
- Uses MVC services → shared `CampusLibrary.Data` repositories → DbContext for the librarian admin site; controllers/views never query DbContext directly, and MVC does not call the API.
- Pages: Books (list/create/edit/delete), Members (list/create), Loans (list open loans, return button).
- Use `[ValidateAntiForgeryToken]` on every POST. Use tag helpers and model validation messages.
- Run `/impeccable` on views before calling a UI task done.

### 3.5 ANDROID KOTLIN — `android/CampusLibraryApp`
- Android Studio, Kotlin 2.1.0, Jetpack Compose, min SDK 26; compile/target SDK 35 (not the original proposed 37). Existing AGP 8.7.3 supports SDK 35; use the checked-in Gradle 8.9 wrapper with JDK 17 and Android build-tools 34.0.0.
- Retrofit + Gson uses `BuildConfig.API_BASE_URL`: debug defaults to `http://10.0.2.2:5143/api/` and accepts `-PcampusApiBaseUrl=...`; release uses HTTPS and disallows cleartext. Do not edit source URLs or disable certificate validation for local testing.
- My Loans uses `GET /api/loans?memberId=1&openOnly=true`; returning a loan yields bodyless 204 (`Response<Unit>`). Demo member 1 is not authentication or authorization.
- Screens: Book list, Book detail with a "Borrow" button, My loans.
- MVVM: `ViewModel` + `StateFlow`; repository class wraps Retrofit.
- Show loading, empty and error states on every screen.
- `INTERNET` permission in the manifest; `usesCleartextTraffic` only in the debug build.

## 4. Rules

- Run `dotnet build` (and `dotnet test` if tests exist) after every .NET change; run `./gradlew assembleDebug` after every Android change. Report the result honestly.
- One feature per prompt. If my prompt is vague, ask one question before coding.
- Never commit secrets, connection-string passwords or OmniRoute keys.
- Explain every new file you create in one sentence.

## 5. Automatic reports — REQUIRED after EVERY prompt

After you finish **every** prompt that creates or changes code (skip only pure questions), and before your final reply, create a new folder:

```
docs/reports/<yyyy-MM-dd-HHmm>-<short-task-slug>/
```

and write these three files in it:

### 5.1 `DEVELOPER-DOCS.md`
- **Summary**: what was asked and what was done (3–5 lines).
- **Projects touched**: which of WEB MVC / WEB API / EF CORE / MSSQL / ANDROID KOTLIN.
- **Files changed**: table of `path | added/modified/deleted | why`.
- **Key code**: the important classes, methods, endpoints or migrations, with short code excerpts.
- **Database changes**: migration name and tables/columns affected, or "none".
- **How it was verified**: build/test commands run and their result.
- **Known limits / TODO**.

### 5.2 `HOW-TO-USE.md`
Written for a user or classmate, not a developer:
- **Prerequisites** (what must be running: SQL Server, API, emulator).
- **Step-by-step** numbered instructions to run and use the new feature.
- **Example**: sample request/response (API), screen description (MVC/Android).
- **Troubleshooting**: 2–3 common errors and fixes.

### 5.3 `FLOWCHARTS.md`
Mermaid diagrams (they render on GitHub and in VS Code):
- One `flowchart TD` of the feature's logic (user action → validation → data → result, including error branches).
- One `sequenceDiagram` across the layers involved (e.g. Android → Web API → EF Core → MSSQL).
- If the database changed, one `erDiagram` of the affected tables.

Then append one line to `docs/reports/INDEX.md`:
`- <yyyy-MM-dd HH:mm> — <task> — [folder](./<folder>/)`

End your reply with: `📄 Reports written to docs/reports/<folder>/`.

## 6. Project milestones (do in order, one prompt each)

1. Create the solution and the 4 projects (`/spec` then `/plan` first).
2. EF Core entities, DbContext, seed data, first migration on MSSQL.
3. Web API Books CRUD + Swagger.
4. Web API Members + Loans (borrow/return rules).
5. Web MVC admin pages.
6. Android book list + detail calling the API.
7. Android borrow + my loans.
8. `/review` of the whole solution; fix findings.
