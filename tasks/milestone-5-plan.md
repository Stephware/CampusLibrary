# Milestone 5 — MVC admin pages

Authority: CLAUDE.md §3.4, AI_DEVELOPMENT_GUIDE.md, user request to implement Milestone 5.

## Scope and design

Extend the existing Bootstrap Razor app: Books list/create/edit/delete, Members list/create, Loans open list/return. Preserve .NET 10, EF Core, MSSQL settings, API endpoints and Android files. No packages/projects, authentication, dashboard, member editing or MVC borrowing are added.

MVC controllers → MVC services (interfaces) → existing Data repositories → LibraryDbContext → MSSQL. Do not reference the executable WebApi project. ViewModels hold validated form input and display data. Add one repository query for open loans with book/member details; no migration.

Use the incumbent Bootstrap system: system typography, white content, restrained blue actions, clear table hierarchy, responsive navigation and tables, visible labels/errors/focus, explicit empty states and success feedback. Full-page forms and delete confirmation; anti-forgery validation on every POST. This is an Operate surface. Optional school branding remains unspecified; default to existing Bootstrap.

## Tasks

- [x] Add failing HTTP tests for all page flows, validation, CSRF, duplicate records, history-safe deletion, returns, missing IDs and HTML encoding; retain API regression suite.
- [x] Add ViewModels, MVC service interfaces/implementations, controllers and DI; extend Data repositories only as needed for open-loan details and safe book write conflicts.
- [x] Add Bootstrap Razor pages, shared navigation and styles; home opens Books, show clear empty/error/success states.
- [x] Build and run complete API/MVC tests against an isolated MSSQL database; review and fix findings; inspect desktop/mobile if browser access is available.
- [x] Generate timestamped DEVELOPER-DOCS.md, HOW-TO-USE.md and FLOWCHARTS.md; update INDEX.md; stop before Android.

## Review focus

CSRF rejection causes no mutation; client cannot select a different edit/delete target through form values; duplicate SQL races show usable validation; loan history stays protected; concurrent returns are handled; long text and narrow screens remain usable. Generic error pages must not reveal database details.

## Completion evidence

- Build passed with zero errors and six pre-existing dependency warnings.
- 13 API/MVC integration tests passed against a disposable MSSQL database.
- 21 headless Chrome rendered states passed; final screenshots saved with reports.
- Code review: no important findings. Impeccable finish review: ship, no fixes.
- No migration/model drift. No Android or API source changes.
- Required reports generated; admin site runs at its existing localhost:5080 address.
