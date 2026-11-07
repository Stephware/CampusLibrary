# CampusLibrary — Actual loan titles and final design documentation

## Summary
- Correct Android My loans showing `Book #6` rather than the real title.
- Add `bookTitle` / `bookAuthor` to the existing LoanDto and load related Book data in the repository.
- Show a borrow-confirmation snackbar upon navigating to My loans.
- Bring `DESIGN.md` into line with the shipped Bound in Ink MVC and Android visual identity.
- Extend integration/Retrofit contract assertions without changing schema, ports, Docker or records.

## Projects touched
WEB API, EF CORE (query only), ANDROID KOTLIN, documentation. MVC presentation was reviewed and documented; this patch does not change MVC behavior. MSSQL schema/data unchanged.

## Files changed

| Path | Change | Purpose |
|---|---|---|
| `src/CampusLibrary.Data/Repositories/LoanRepository.cs` | Modified | Load loan-associated books with a single joined query |
| `src/CampusLibrary.WebApi/DTOs/LoanDtos.cs` | Modified | Expose nullable `BookTitle` and `BookAuthor` |
| `src/CampusLibrary.WebApi/Services/LoanService.cs` | Modified | Populate titles/authors in list, details and borrow responses |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/data/models/Models.kt` | Modified | Map new optional JSON fields |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/ui/screens/MyLoansScreen.kt` | Modified | Display actual titles/authors, show confirmation |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/MainActivity.kt` | Modified | Route one-time borrow success feedback to loans |
| `tests/test_milestone4.py` | Modified | Assert book metadata returned in loan responses |
| `android/CampusLibraryApp/app/src/test/java/com/campus/library/data/api/LibraryApiContractTest.kt` | Modified | Verify Retrofit maps book title/author |
| `DESIGN.md` | Replaced | Document implemented colors, typography, responsive patterns and constraints |
| `docs/reports/INDEX.md` | Modified | Index this follow-up |
| `docs/reports/2026-11-07-2250-loan-title-finalization/DEVELOPER-DOCS.md` | Added | Implementation/evidence |
| `docs/reports/2026-11-07-2250-loan-title-finalization/HOW-TO-USE.md` | Added | Local run and troubleshooting instructions |
| `docs/reports/2026-11-07-2250-loan-title-finalization/FLOWCHARTS.md` | Added | Workflow and layer sequence |

## Key code
- `IQueryable<Loan>` reads `Include(l => l.Book)` with optional member/open filters; no per-loan database lookup.
- Mapping uses `BookTitle = (book ?? loan.Book)?.Title` and `BookAuthor = (book ?? loan.Book)?.Author`. The already-loaded Book is supplied explicitly for a new borrow.
- Android first displays `loan.bookTitle`, then the legacy nested `loan.book?.title`, otherwise the numeric fallback.
- The borrow confirmation appears on My loans as a one-time Snackbar; return feedback remains unchanged.

## Database changes
**None.** Loan already has its BookId foreign key and Book already contains Title/Author. No migration, persisted availability flag, record deletion, configuration reset or credential change.

## Verification and limitations
- Source-file changes were applied through GitHub with strict target-content matching and reviewed for the DTO, EF Core query and Kotlin JSON contracts.
- **Not executed in this environment:** `dotnet build`, .NET/MSSQL integration tests, Android Gradle build or emulator, and local Impeccable CLI finish review. The current assistant environment has no .NET SDK and no access to the user's local SQL Server/emulator. Committing regression tests is not evidence they have passed.
- Prior historical reports documented 19 API/MVC tests, 8 Android unit tests and 21 MVC browser states for the **previous** commit. These do not prove this change passes.
- Source-level UI review and the supplied emulator screenshot identify the corrected label behavior; actual native large-font/night/rotation checks remain pending.

## Known limits / TODO
1. Run `dotnet build`, `python3 tests/run_milestone5.py` and `python3 tests/verify_publish.py` against an isolated SQL test database.
2. Run `cd android/CampusLibraryApp && ./gradlew assembleDebug testDebugUnitTest lintDebug`.
3. Restart the new API, install/run the new Android debug APK and confirm a loan's **title and author** replace `Book #6`; verify borrow → return → reborrow and dark/large-text/rotation.
4. Complete the locally installed Impeccable finish pass and record its findings/screenshots before calling the task fully verified.
5. Release APK distribution still requires real trusted HTTPS and signing; authentication is intentionally out of classroom scope.
