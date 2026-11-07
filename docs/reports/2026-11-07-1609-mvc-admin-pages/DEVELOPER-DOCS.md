# Milestone 5 — MVC admin pages

> Historical milestone record. Statements and verification below describe the original task, not a fresh run. For current setup and known limitations, use [the latest report index](../INDEX.md).

## Summary

Implemented the specified librarian admin pages for Books, Members and open Loans.
Kept Controller → Service → Repository → EF Core → MSSQL with MVC-owned services and ViewModels.
Applied Impeccable guidance to the existing Bootstrap UI, including responsive records and clear form feedback.
Built, tested and reviewed the result; Android remains for Milestones 6–7.

## Projects touched

WEB MVC and the shared EF CORE/Data repositories. MSSQL was queried through existing EF mappings; no schema change. WEB API source and ANDROID KOTLIN were not changed. The API regression suite was run because Data repositories are shared.

## Files changed

Statuses are relative to this prompt. Git has no commits, and prior files were already untracked. No existing work was reset, staged or committed.

| Path | Status | Why |
|---|---|---|
| `PRODUCT.md` | added | Records existing product requirements and the MVC surface context. |
| `DESIGN.md` | added | Documents the implemented Bootstrap visual system and reusable design tokens. |
| `.impeccable/design.json` | added | Records design extensions and component examples for future visual work. |
| `tasks/milestone-5-plan.md` | added | Defines the scope, architecture, visual direction and verification work. |
| `src/CampusLibrary.WebMvc/Program.cs` | modified | Registers the DbContext, repositories and MVC services, and safe error/status pages. |
| `src/CampusLibrary.WebMvc/Controllers/HomeController.cs` | modified | Opens Books from the root and renders status pages. |
| `src/CampusLibrary.WebMvc/Views/_ViewImports.cshtml` | modified | Makes admin ViewModels available to Razor views. |
| `src/CampusLibrary.WebMvc/Views/Shared/_Layout.cshtml` | modified | Provides navigation, skip link, active section, feedback messages and shared assets. |
| `src/CampusLibrary.WebMvc/Views/Shared/_Layout.cshtml.css` | modified | Removes starter scoped rules that conflict with the responsive shared layout. |
| `src/CampusLibrary.WebMvc/Views/Shared/Error.cshtml` | modified | Shows a useful error message without development or database internals. |
| `src/CampusLibrary.WebMvc/Views/Home/Status.cshtml` | added | Shows friendly missing-record and failed-request recovery pages. |
| `src/CampusLibrary.WebMvc/wwwroot/css/site.css` | modified | Defines shared tokens, accessible controls, page hierarchy and stacked mobile tables. |
| `src/CampusLibrary.WebMvc/wwwroot/js/site.js` | modified | Shows form submission progress and restores controls on back navigation. |
| `src/CampusLibrary.Data/Repositories/BookRepository.cs` | modified | Translates unique and concurrent-write failures into domain errors usable by forms. |
| `src/CampusLibrary.Data/Repositories/LoanRepository.cs` | modified | Loads open loans with book/member details in one asynchronous SQL query. |
| `src/CampusLibrary.Data/Repositories/Interfaces/ILoanRepository.cs` | modified | Exposes the new open-loan query to MVC services. |
| `tests/test_milestone5.py` | added | Checks MVC form contracts, validation, CSRF, encoding, record targeting and loan rules. |
| `tests/run_milestone5.py` | added | Builds and starts isolated API/MVC apps with a disposable MSSQL database and cleanup. |
| `tests/verify_milestone5.mjs` | added | Uses installed headless Chrome to verify responsive pages and browser interaction without npm packages. |
| `docs/reports/INDEX.md` | modified | Links this milestone report. |
| `src/CampusLibrary.WebMvc/ViewModels/BookViewModels.cs` | added | Defines validated book form and catalog display models. |
| `src/CampusLibrary.WebMvc/ViewModels/MemberViewModels.cs` | added | Defines validated member registration and display models. |
| `src/CampusLibrary.WebMvc/ViewModels/LoanViewModel.cs` | added | Defines open-loan presentation fields and overdue state. |
| `src/CampusLibrary.WebMvc/Services/BookAdminService.cs` | added | Applies catalog uniqueness, normalization and CRUD rules. |
| `src/CampusLibrary.WebMvc/Services/Interfaces/IBookAdminService.cs` | added | Defines the book admin service boundary. |
| `src/CampusLibrary.WebMvc/Services/MemberAdminService.cs` | added | Applies registration, uniqueness and normalized member fields. |
| `src/CampusLibrary.WebMvc/Services/Interfaces/IMemberAdminService.cs` | added | Defines the member admin service boundary. |
| `src/CampusLibrary.WebMvc/Services/LoanAdminService.cs` | added | Maps open-loan details and records atomic returns. |
| `src/CampusLibrary.WebMvc/Services/Interfaces/ILoanAdminService.cs` | added | Defines the loan admin service boundary. |
| `src/CampusLibrary.WebMvc/Controllers/BooksController.cs` | added | Handles book pages and validated create/edit/delete POST requests. |
| `src/CampusLibrary.WebMvc/Controllers/MembersController.cs` | added | Handles member listing and validated registration. |
| `src/CampusLibrary.WebMvc/Controllers/LoansController.cs` | added | Handles open-loan listing and anti-forgery-protected returns. |
| `src/CampusLibrary.WebMvc/Views/Books/Index.cshtml` | added | Lists catalog records and actions with an empty state. |
| `src/CampusLibrary.WebMvc/Views/Books/Create.cshtml` | added | Renders the add-book form. |
| `src/CampusLibrary.WebMvc/Views/Books/Edit.cshtml` | added | Renders the edit-book form. |
| `src/CampusLibrary.WebMvc/Views/Books/_Form.cshtml` | added | Shares labeled and validated book input fields. |
| `src/CampusLibrary.WebMvc/Views/Books/Delete.cshtml` | added | Confirms the selected book and explains deletion/history rules. |
| `src/CampusLibrary.WebMvc/Views/Members/Index.cshtml` | added | Lists registered members with an empty state. |
| `src/CampusLibrary.WebMvc/Views/Members/Create.cshtml` | added | Renders member registration and validation feedback. |
| `src/CampusLibrary.WebMvc/Views/Loans/Index.cshtml` | added | Lists open loans and accessible return buttons, or an empty state. |
| `docs/reports/2026-11-07-1609-mvc-admin-pages/DEVELOPER-DOCS.md` | added | Explains implementation, file responsibilities and verification. |
| `docs/reports/2026-11-07-1609-mvc-admin-pages/HOW-TO-USE.md` | added | Explains starting and using the admin pages. |
| `docs/reports/2026-11-07-1609-mvc-admin-pages/FLOWCHARTS.md` | added | Illustrates request handling, validation and layer interactions. |
| `docs/reports/2026-11-07-1609-mvc-admin-pages/screenshots/` | added | Preserves all 21 synthetic-test visual captures and browser check results. |
| `docs/reports/2026-11-07-1609-mvc-admin-pages/design-detector.json` | added | Preserves the empty Impeccable detector finding list. |

## Architecture and behavior

MVC services depend on existing repository interfaces in CampusLibrary.Data. They do not call the API or reference the executable WebApi project. DbContext and repositories are scoped to each MVC request; no controller or view accesses EF directly.

| Page/action | Behavior |
|---|---|
| `/` | Redirects to `/Books` |
| `/Books` | Alphabetical catalog; Add, Edit and Delete links |
| GET/POST `/Books/Create` | Validated book registration |
| GET/POST `/Books/Edit/{id}` | Edits the route-selected book |
| GET/POST `/Books/Delete/{id}` | Confirmation GET; only POST deletes; loan history prevents deletion |
| `/Members` | Alphabetical member register |
| GET/POST `/Members/Create` | Unique email/student ID, server-generated registration date |
| `/Loans` | Open loans with book/member details, earliest due first; overdue status |
| POST `/Loans/Return/{id}` | Atomic return, success feedback, or already-returned notice |

All mutation actions have ValidateAntiForgeryToken. Route IDs use FromRoute so posted form values cannot select another record. ModelState validation checks required fields, database length limits, member email and optional publication year (1–9999). Invalid forms retain entered data and show field errors; duplicate identifiers show a summary. Success uses POST/redirect/GET plus TempData messages.

Missing records return friendly 404 pages. Unexpected exceptions render a generic error page with a request ID; details remain in server diagnostics. Razor encodes user-entered text. Dates are displayed as UTC, with no client timezone assumptions.

### Key code

```csharp
[HttpPost, ValidateAntiForgeryToken]
public async Task<IActionResult> Edit([FromRoute] int id, BookFormViewModel model)
```

```csharp
// LoanRepository: one query, rather than a database call per record.
_context.Loans.AsNoTracking()
    .Where(l => l.ReturnDate == null)
    .Include(l => l.Book).Include(l => l.Member)
    .OrderBy(l => l.DueDate).ThenBy(l => l.Id)
    .ToListAsync();
```

LoanAdminService reuses the conditional ReturnAsync operation implemented in Milestone 4. The SQL filtered unique index and foreign keys remain in force. Book writes now translate SQL duplicate errors and concurrent deletion into safe domain errors, following the existing MemberRepository pattern.

## Impeccable implementation and visual review

- Operate mode: existing Bootstrap vocabulary, system typography, restrained blue primary actions, white surfaces and a cool neutral background.
- Consistent page headings, active navigation, visible labels, required/optional guidance, useful empty states, cancel links, explicit delete confirmation and success/notice messages.
- Keyboard skip link, visible focus, table captions, contextual action labels and 44px minimum primary and row-action button targets.
- At phone widths, tables stack into labeled records with all actions visible. Long content wraps without page-level overflow.
- Forms progressively enhance submission feedback; server-side validation and navigation remain functional without custom JavaScript.
- Computer Use was unavailable. The user explicitly authorized headless browser verification instead.
- `impeccable detect --json` on the MVC views and authored CSS/JS reported `[]`; it is a static detector, not a full accessibility audit.
- A fresh finish reviewer visually inspected all 21 captures and returned **ship**, no material fixes required. A separate code review reported no important findings.

## Database changes

**None.** No new migrations, tables, columns or seed changes. Existing development connection settings, MSSQL container and port 14335 were retained. The latest migration remains `20261107074756_EnforceOneOpenLoanPerBook`.

## Verification

- Before implementation: the new MVC tests failed on missing page/action routes and root behavior; the seven existing API tests passed.
- `python3 tests/run_milestone5.py`: **13 tests passed** (7 API regressions + 6 MVC integration tests). Final execution with `--keep-running` also passed all 13 in 0.892 seconds before browser checks.
- The runner executes `dotnet build --no-restore -p:UseSharedCompilation=false -m:1`: **zero errors**, six pre-existing dependency warnings.
- MVC checks cover navigation, forms/CRUD, validation and duplicates, HTML encoding, missing/forged anti-forgery tokens, record selection protected against form ID overrides, history-safe deletion, persisted returns and repeated-return recovery.
- The API suite retains concurrent borrowing/returning/member-write checks from Milestone 4.
- `node tests/verify_milestone5.mjs <isolated-mvc-url> <isolated-api-url> <output>`: **21 rendered states passed** at desktop 1440×1000 and mobile 390×844. Assertions checked document overflow, one H1, input labels, keyboard skip-link focus, mobile menu, visible server errors, real return-button click and persisted ReturnDate, static assets and no uncaught JavaScript exceptions.
- See [browser results](screenshots/results.json), [desktop catalog](screenshots/desktop-books.png), [mobile loans](screenshots/mobile-loans.png), [form validation](screenshots/mobile-validation.png) and [return feedback](screenshots/mobile-return-success.png). All screenshot content is synthetic verification data from the isolated database.
- `dotnet test --no-build --no-restore`: exit 0. No .NET test projects exist; the 13 actual integration tests are Python unittest cases.
- `dotnet ef migrations has-pending-model-changes -p src/CampusLibrary.Data -s src/CampusLibrary.WebApi --no-build`: model unchanged since the last migration.
- Test databases use generated `CampusLibraryM5Tests_<random>` names and are dropped by the runner after both apps stop. Development data was not reset or populated by tests.

## Known limits / later milestones

- Existing moderate/high NU1902/NU1903 warnings for transitive System.Security.Cryptography.Pkcs 9.0.11 remain. No dependencies or global tools were changed.
- Existing dotnet-ef 10.0.12 warns about runtime 10.0.14; the migration check succeeded.
- Visual evidence covers the two recorded widths; no manual screen-reader audit or every intermediate width is claimed. Empty Loans was rendered; empty Books/Members branches are implemented and reviewed but seeded test databases do not exercise those branches.
- This retains the specification's local educational admin scope; authentication, search/paging, MVC borrowing, member edit/delete and Android are not added.
- Existing starter Home/Privacy views remain in source, but the active admin navigation points to Books, Members and Loans.
