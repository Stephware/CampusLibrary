# CampusLibrary — Run and verify the reviewed solution

## Prerequisites

- Existing .NET10 SDK and working local SQL connection settings. The current Mac container uses port14335. LocalDB is not supported on macOS.
- For Android: JDK17, Android SDK35 and build-tools34.0.0, plus an emulator. Select compatible tools in Android Studio or set JAVA_HOME/ANDROID_HOME to their local installed paths. The wrapper downloads/checks Gradle8.9.
- Node22+ and installed macOS Chrome only if repeating the headless browser checks.

The existing SQL Edge container works for the local tests but is retired; a supported database deployment is still required before distribution. Do not recreate/drop your development database just to run these checks.

## Start the API and admin site

1. From `~/CampusLibrary`, run `dotnet restore` and `dotnet build`.
2. Confirm `campuslibrary-mssql` is running. Keep passwords in ignored development settings, environment configuration or user-secrets; do not put them in reports/source.
3. Apply the existing migrations only if setting up a new database:
   ```bash
   dotnet ef database update -p src/CampusLibrary.Data -s src/CampusLibrary.WebApi
   ```
4. Stop any old API process in its original terminal, then start current source:
   ```bash
   dotnet run --project src/CampusLibrary.WebApi --launch-profile http
   ```
   Existing HTTP port:5143. The review preserved the older running process; it must be restarted to use the fixes.
5. In another terminal:
   ```bash
   dotnet run --project src/CampusLibrary.WebMvc --launch-profile http
   ```
   Open http://localhost:5080. Books supports add/edit/delete; Members registers borrowers; Loans records returns. OpenAPI JSON is at http://localhost:5143/openapi/v1.json in Development.

## Android

1. Open `android/CampusLibraryApp` in Android Studio, select JDK17 and install SDK35/build-tools34 if missing. Keep local SDK paths out of source control.
2. Start an emulator, run the current API HTTP profile, then build/run debug:
   ```bash
   cd android/CampusLibraryApp
   ./gradlew assembleDebug
   ```
3. Debug API defaults to `http://10.0.2.2:5143/api/`;10.0.2.2 is the emulator alias for the Mac. For a different local test port:
   ```bash
   ./gradlew assembleDebug -PcampusApiBaseUrl=http://10.0.2.2:PORT/api/
   ```
4. Open a book, choose Borrow This Book, acknowledge success, then open My Loans. Choose Return Book; the loan disappears from active loans. All demo borrowing uses seeded member1. Android Back returns to the catalog.
5. With larger system text or landscape orientation, scroll details to reach borrowing. Reopening details after a return should show the book rather than an old success dialog.

APK outputs: `app/build/outputs/apk/debug/app-debug.apk` and `app/build/outputs/apk/release/app-release-unsigned.apk`. The final debug APK uses default5143, not the temporary review port. Release disables cleartext and uses HTTPS7282; distribution requires a trusted real endpoint and signing configuration.

## Repeat the verification

From the repository root:

```bash
python3 tests/run_milestone5.py
python3 tests/verify_publish.py
dotnet list package --vulnerable --include-transitive
```

The first command builds and migrates a new disposable SQL database, runs18 API/MVC tests, stops its temporary apps and drops only its test database. It needs SQL create/drop permissions. The second publishes both hosts into fresh temporary directories and checks that local Development settings are not shipped.

For browser checks, start `python3 tests/run_milestone5.py --keep-running`, copy its printed MVC/API URLs and run:

```bash
node tests/verify_milestone5.mjs <mvc-url> <api-url> /tmp/campuslibrary-review
```

Press Enter in the first terminal afterward to clean up. Use only those disposable endpoints; browser checks create test records.

From the Android project, with compatible JAVA_HOME/ANDROID_HOME:

```bash
./gradlew assembleDebug testDebugUnitTest assembleRelease lintDebug --no-daemon
```

Expected current results:18 API/MVC tests,3 Android tests,21 browser states; zero .NET build warnings/errors; Android lint zero errors and51 warnings. `dotnet test` alone does not run the Python or Kotlin suites.

## Example API validation

```bash
curl -i -X POST http://localhost:5143/api/books   -H 'Content-Type: application/json'   -d '{"title":" ","isbn":"REVIEW-EXAMPLE"}'
```

Expect400 with validation problem details. A valid title is required; title/ISBN/author limits are200/20/100. A case-only change to a book's own ISBN now saves; an ISBN belonging to another book still conflicts.

## Troubleshooting

- **Android Gradle fails immediately with Java25/26:** Use JDK17 for Gradle and SDK35/build-tools34. The temporary review tool paths were not written into project defaults.
- **Android network error, My Loans404 or old behavior:** Restart the API from current source, verify port5143 or the debug URL override, and use the HTTP debug profile. Release requires trusted HTTPS; do not disable certificate validation to make it work.
- **Publish cannot connect / missing database setting:** Development settings are intentionally excluded from published artifacts. Supply the deployment connection securely through environment configuration. No database/password was automatically migrated or rotated by this review.
