# CampusLibrary — Final submission readiness audit

## Summary
Integrated the earlier loan-title fix into main, reconciled DESIGN.md with the shipped Bound in Ink visual system, and audited the remaining source/UI contracts. A fresh GitHub Actions build gate was added to detect compilation and Android build/test regressions. It caught a real C# method-group compile error in LoanService; this was corrected by using an explicit lambda. Android CI runner SDK setup was also revised after an action bootstrap failure. Android borrow navigation now consumes success to prevent reopening details from automatically redirecting; snackbar delivery is consumed once.

## Projects touched
WEB API (LoanService build correction), ANDROID KOTLIN (ViewModel/Compose feedback), verification workflow/script, documentation. No changes to SQL schema or data, Docker, DBeaver, ports, or credentials. MVC source not changed as part of this audit.

## Actual changes and paths
- `src/CampusLibrary.WebApi/Services/LoanService.cs`: explicit `Select(loan => ToDto(loan))` to compile the newly optional book metadata argument.
- `android/CampusLibraryApp/app/src/main/java/com/campus/library/ui/viewmodels/BookDetailViewModel.kt`: explicit one-time success acknowledgment.
- `android/CampusLibraryApp/app/src/main/java/com/campus/library/ui/screens/BookDetailScreen.kt`: consume success before navigating away.
- `android/CampusLibraryApp/app/src/main/java/com/campus/library/ui/screens/MyLoansScreen.kt`: one-shot borrow feedback that doesn't repeat after leaving mid-snackbar.
- `android/CampusLibraryApp/app/src/test/java/com/campus/library/ui/viewmodels/BookDetailViewModelTest.kt`: reopen-after-success regression assertion.
- `tests/verify_live_app.py`: read-only verification of the running API/MVC, demo member, actual loan titles/authors and availability; optional JSON report.
- `.github/workflows/verify.yml`: reproducible .NET Release and Android debug/unit/lint jobs on main pushes.
- `.impeccable/review/SUBMISSION-REVIEW.md`: measured visual source audit and explicit native-check limitations.
- `docs/reports/2026-11-07-2313-submission-readiness/`: developer, how-to and flowchart documents; index updated.

## Source-level findings
- Backend Controller → Service → Repository → EF Core layering remains unchanged; Book–Loan relationship and filtered unique index enforce one open loan.
- Book titles/authors are loaded via one EF query with related Book, then mapped as optional fields on LoanDto; My loans prioritizes the title instead of a numeric ID.
- Current DESIGN.md reflects aubergine/lilac Bound in Ink styling in CSS and Compose, and UI uses status text plus color.
- No new DB migration or write/mutation is required for the title change.

## Evidence and limits
- **Executed in this environment:** Python source syntax check and a synthetic local HTTP server run of `verify_live_app.py` (8/8 read-only test endpoints passed against mock responses). These checks validate the harness, **not CampusLibraryDb**.
- **Executed in this environment:** WCAG contrast calculations for 8 named source-color pairs (all >= 4.5:1).
- **GitHub workflow:** an initial run failed due a real C# compiler error and GitHub Android SDK action bootstrap. Both causes were patched on main. Use the latest workflow run for actual build status, rather than the historical failure. Tests in this environment do not connect to your Mac.
- **Previously reported (older commit):** 19 SQL integration tests, 8 Android unit tests, 21 MVC browser states. Re-run after current fixes to claim current passing verification.
- **Not executable remotely:** the user's actual `CampusLibraryDb` on localhost, Android emulator, physical-device accessibility, or locally installed Impeccable CLI. These remain manual gates; no fabricated pass result has been recorded.

## Known blockers to a complete sign-off
1. Latest CI jobs must be green.
2. Run local `dotnet build`, isolated API/MVC integration suite and the read-only current-db smoke tool.
3. Complete emulator catalog/detail/loans and borrow/return/reborrow with real book title, dark mode, large fonts and rotation.
4. Complete an actual local Impeccable finish pass, document the findings, and attach screenshots if required.

Do not claim the assignment has been fully runtime verified until these checks are completed.

## Verified GitHub CI result (2026-11-07 23:15 PHT)

- [GitHub Actions run 42385231694](https://github.com/Stephware/CampusLibrary/actions/runs/42385231694), commit `b6d52c34`, **conclusion: success**.
- The `.NET 10 Release Build` job completed successfully, including `dotnet restore` and `dotnet build -c Release --no-restore`.
- The `Android Debug, Unit Tests and Lint` job completed successfully. Gradle executed `assembleDebug`, `testDebugUnitTest`, and `lintDebug`, and reported `BUILD SUCCESSFUL`.
- The subsequent documentation-only commit does not alter the compiled application code. Its own CI run is a separate check.
- **Not inferred from CI:** connected emulator navigation, actual local `CampusLibraryDb` data, automated SQL integration tests, physical accessibility, or Impeccable CLI completion. These remain on-Mac verification requirements.
