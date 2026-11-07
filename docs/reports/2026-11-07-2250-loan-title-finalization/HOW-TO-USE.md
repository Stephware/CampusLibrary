# How to use — My loans now shows book titles

## Prerequisites
- Existing .NET 10 project and SQL Server connection. Host port 14335 on the user's Mac. Keep the existing database; this update needs **no migrations**.
- Android Studio, JDK 17, SDK 35, Gradle wrapper, and Android emulator.

## Run and verify
1. Pull the updated branch or merged commit and **restart** the currently running API: `dotnet run --project src/CampusLibrary.WebApi --launch-profile http` (http://localhost:5143).
2. Optionally start MVC: `dotnet run --project src/CampusLibrary.WebMvc --launch-profile http` (http://localhost:5080).
3. Inspect `http://localhost:5143/swagger` (Development only) or run `curl 'http://localhost:5143/api/loans?memberId=1&openOnly=true'`.
4. Open `android/CampusLibraryApp` in Android Studio, select JDK 17, start the emulator, and run the debug application. Its API base is `http://10.0.2.2:5143/api/`.
5. Open My loans. Its row should display the **book's actual title and author**, followed by borrowed/due dates and Return book. Try an available book: borrow → snackbar confirmation on My loans → return → catalog → borrow again.

## API response example
The response is an array; this is illustrative, **not** real database data:

```json
[{"id":7,"bookId":6,"bookTitle":"Example Book Title","bookAuthor":"Example Author","memberId":1,"borrowDate":"2026-11-07T00:00:00Z","dueDate":"2026-11-21T00:00:00Z","returnDate":null}]
```

`bookId` is the Books table primary key, **not** the book name or its position in the list. The new fields are additive; old API consumers can ignore them.

## Regression checks to run locally
- `dotnet build`
- `python3 tests/run_milestone5.py` (isolated database integration suite)
- `python3 tests/verify_publish.py`
- `cd android/CampusLibraryApp && ./gradlew assembleDebug testDebugUnitTest lintDebug`
- In emulator: inspect the real title and author, loading/error/retry, return/reborrow, light and dark theme, large font and landscape. Perform Impeccable finish review.

## Troubleshooting
- **Still Book #6:** restart the updated API. Check for `bookTitle` in the loan JSON; then rebuild Android. A live old API will not return the new field.
- **My loans empty:** demo account uses member ID 1. A loan belonging to a different member is not shown.
- **Retrofit connection failure:** emulator connects to the Mac via `10.0.2.2`, not `localhost`. Verify the API runs on port 5143 and that the debug APK is current.
- **409 conflict:** book already on loan; do not delete loan history to bypass the rule.

## Verification status
The remote source changes and tests were authored, but the current environment cannot run the user's .NET/SQL/Android toolchain. Run the commands and emulator checks before declaring the app fully submission-verified.
