# CampusLibrary — Final submission verification on macOS

This workflow is designed to be run after pulling the latest main branch. It preserves your existing CampusLibraryDb, Docker volume and DBeaver configuration.

## Step 1 — Refresh and run your .NET applications
From the repository root:

```bash
git pull origin main
dotnet build
dotnet run --project src/CampusLibrary.WebApi --launch-profile http
```

Open another Terminal at the repository root:

```bash
dotnet run --project src/CampusLibrary.WebMvc --launch-profile http
```

The API is expected at http://localhost:5143 and MVC at http://localhost:5080. Keep your actual connection string in ignored Development settings; do not paste passwords into reports.

## Step 2 — Read-only check against your current CampusLibraryDb
With the API and MVC still running:

```bash
python3 tests/verify_live_app.py --report docs/reports/2026-11-07-2313-submission-readiness/live-check-results.json
```

This checks Books, configured demo member #1, every open loan's **real book title and author**, loan availability, demo member loans, Development Swagger and MVC Books/Members/Loans pages. **It does not create/update/delete records.** The generated JSON file is real execution evidence only after this command has actually run. A success message from the mock harness is not proof of the real database.

If needed, review in DBeaver:

```sql
SELECT l.Id AS LoanId, l.BookId, b.Title, b.Author, l.BorrowDate, l.DueDate, l.ReturnDate
FROM dbo.Loans AS l
JOIN dbo.Books AS b ON b.Id = l.BookId
ORDER BY l.Id DESC;
```

## Step 3 — Run comprehensive isolated tests
From the repository root:

```bash
python3 tests/run_milestone5.py
python3 tests/verify_publish.py
```

The integration runner creates and drops **only its own disposable test database**. It must not be pointed manually at CampusLibraryDb.

## Step 4 — Android build and emulator test
Open `android/CampusLibraryApp` in Android Studio using JDK 17 and Android SDK 35. Launch your emulator. From that directory:

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Then run the latest debug build on the emulator and verify all three screens:

- **Catalog:** title/author/availability; tapping an unavailable book keeps Borrow disabled. Test empty/error/retry where feasible.
- **Book details:** author, ISBN, publication year, disabled state for an already-on-loan copy; Borrow navigates once and does not auto-navigate if details are reopened.
- **My loans:** shows the real book title and author (instead of Book #6), borrowed/due dates, success message and functioning Return.
- **Lifecycle:** after return, revisit catalog and reborrow, check Back, rotate, enlarge system font to 200%, and toggle dark mode. A network-offline retry should give a useful message.

The Android emulator connects to the Mac API at `http://10.0.2.2:5143/api/`. Restart the updated API before testing; otherwise the old loan JSON lacks bookTitle/bookAuthor.

## Step 5 — Impeccable final acceptance
Run your locally installed **Impeccable** workflow against both running surfaces. Check consistency with `DESIGN.md`, contrast, touch sizes, keyboard navigation, screen overflow, form errors, large type, dark mode and return/borrow feedback. Attach actual screenshots/results to this report if your course requires them.

## Final evidence status
The local database/emulator/Impeccable checks are **pending until executed on your Mac**. GitHub Actions independently runs code compilation/unit checks; it does not connect to local SQL or emulate your screens. Never record "passed" without its command output.
