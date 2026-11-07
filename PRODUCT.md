# CampusLibrary

<!-- impeccable:product-schema 1 -->

## Platform
web

## Users
The MVC admin site serves a campus librarian managing books, registering members and recording returns, as specified in CLAUDE.md. The wider solution also has an Android client, outside this surface's scope.

## Product Purpose
Maintain a book catalog and member register, and track borrowing and returns without losing loan history.

## Operating Context
A local educational project on Apple Silicon macOS, using ASP.NET Core MVC/Razor, Bootstrap, EF Core and MSSQL. MVC uses the Data project through services and repositories; it does not require the API process.

## Capabilities and Constraints
Milestone 5: Books list/create/edit/delete; Members list/create; Loans open list/return. POST actions require anti-forgery protection. A book has at most one open loan. Book/member records with loan history cannot be deleted. Preserve existing implementations and ports; Android is a later milestone.

## Brand Commitments
CampusLibrary is the existing product name. No school logo, institution name or custom brand assets have been supplied. Use the project's existing Bootstrap conventions for this milestone; optional school branding is undecided.

## Evidence on Hand
Product requirements: CLAUDE.md and AI_DEVELOPMENT_GUIDE.md. Implementation reports: docs/reports/INDEX.md. Real seed data: CampusLibrary.Data/LibraryDbContext.cs.

## Product Principles
- Keep librarian tasks direct and easy to scan.
- Validate changes before persistence and preserve loan history.
- Show useful recovery instructions with errors.
