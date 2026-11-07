# CampusLibrary — Final code review

## Summary

Reviewed reported Milestones 1–7 component by component rather than rebuilding them.
Fixed confirmed validation, uniqueness, credential handling, packaging and Android build/runtime defects.
Fresh SQL-backed, browser and emulator checks verify the core local workflows after the fixes.
The local educational application is functionally verified; production deployment is not approved while the limitations below remain.

## Projects touched

WEB API, WEB MVC, shared EF CORE/Data configuration/repositories, and ANDROID KOTLIN. MSSQL was used for isolated verification; no schema, container configuration, development records or credentials were changed. No Git commits exist; previous work was untracked. No staging, resetting or committing was performed. A source-hash baseline was used to identify changes.

## Milestone completeness

| Milestone | Current evidence/status |
|---|---|
| 1 — Solution/projects | Existing .NET10 projects and Android project retained; .NET Debug/Release and Android Debug/Release builds pass. Missing Android wrapper/build pieces are now fixed. |
| 2 — EF/MSSQL | Entities, seed data, relationships and migrations present; fresh databases migrate successfully; no model drift. Existing SQL Edge host is retired (deployment issue below). |
| 3 — Books API | CRUD verified; invalid input now rejected before SQL; ISBN case-only edits corrected. Development OpenAPI JSON is available; interactive Swagger UI is not supplied. |
| 4 — Members/Loans API | CRUD, borrowing, return, member filters, unique open loans and history protection verified, including concurrent requests. |
| 5 — MVC | Required Books CRUD, Members list/create, open Loans/return are connected and verified; anti-forgery and record selection protections retained. |
| 6 — Android list/details | Compiles and runs on the review emulator; catalog/details and Back behavior verified; stale loads and large-text scrolling corrected. |
| 7 — Android borrowing/My Loans | Actual API contract corrected; emulator borrow/loans/return/revisit flow passed. Demo member1 is intentionally retained. |

The older Android report claimed completeness without a successful Android build. Its incorrect endpoint/type and build assumptions have been superseded by this review's fresh evidence. Earlier reports were preserved except for credential redaction.

## Confirmed findings and resolutions

| Priority | Confirmed problem | Resolution/evidence |
|---|---|---|
| P1 | Current SQL password copied into documentation/spec/task text, despite ignored local settings | Redacted 16 copies across six files; independent commit-candidate equality scan found zero remaining copies. Local credentials were not rotated. |
| P1 | Release publish copied local Development settings containing credentials | Both web projects exclude the file from publish. Fresh packaging regression failed before the fix and passed for both hosts afterward. |
| P1 | NuGet audit reported high/moderate PKCS advisories through SqlClient | Data pins patched System.Security.Cryptography.Pkcs10.0.13. Fresh audit reports no known vulnerabilities; .NET builds now have zero warnings. |
| P1 | Delayed Android detail response could display A while borrowing route book B | Cancel the previous load, preserve cancellation and require the displayed ID to match. Two regressions failed before and pass after the fix. |
| P2 | Books API accepted blanks/oversized values or reached SQL errors instead of required400 | Added request annotations matching schema/MVC limits and positive-ID validation; regression cases pass. |
| P2 | API/MVC case-only ISBN edit conflicts with its own row under SQL collation | Shared repository query accepts excluded book ID; both callers use it; regression passes and genuine duplicates remain rejected. |
| P2 | Android My Loans requested a nonexistent route and return expected a Loan from204 | Uses `GET /api/loans?memberId=1&openOnly=true` and `Response<Unit>` for return; real HTTP regression and emulator flow pass. |
| P2 | Android missing wrapper/resources/logging dependency/release placeholder prevented reliable builds | Restored standard wrapper, removed broken resource references and body logging, supplied variant configuration; both APK variants compile. |
| P2 | Re-entering loans/details retained stale state/dialogs; native Back exited child flow | Reload on loans entry, derive success dialog from current state, implement Back handler; emulator return/revisit checks pass. |
| P2 | At200% text size in landscape, Borrow was absent/unreachable | Scrollable detail content with viewport minimum height; emulator scrolling and successful borrow verified. |
| P3 | Direct status-page requests accepted invalid/non-error status codes | Values outside400–599 map to404; regression covers missing, negative,204 and9999. |

Patched PKCS version is supported by [Microsoft's high-severity advisory](https://github.com/advisories/GHSA-846f-f764-76mx) and [moderate-severity advisory](https://github.com/advisories/GHSA-2fmv-rv2c-59x7). Audit scope is the configured NuGet feed, not a guarantee that every dependency/platform is vulnerability-free.

## Component review conclusions

### Web API

Controller → Service → Repository → EF → MSSQL boundaries remain intact; DTOs do not expose entity graphs. Book requests now enforce required title/ISBN, lengths200/20, optional author100 and publication year1–9999. Writes normalize strings. Bad IDs/input return400; missing records404; duplicates/unavailable books409. Member and loan validation, UTC dates,14-day lending policy, filtered lists and atomic returns are preserved. CORS remains narrow; it is browser policy, not authentication or emulator routing.

### MVC

Controllers/services/ViewModels/Razor were reviewed separately. All mutation actions validate anti-forgery tokens and use route-only IDs. HTML encoding, POST/redirect/GET, duplicate feedback, friendly errors, history-protected deletion and responsive layouts remain. Open loans load with related book/member data in one query. At the stated classroom/local load, in-memory ordering and unpaged lists are acceptable; no N+1/query-loop blocker was found.

### EF Core and MSSQL

Required/max-length fields, unique ISBN/email/student ID indexes, restrictive loan foreign keys, deterministic seed records and migration snapshots were checked. The unique filtered Loans.BookId index protects concurrent borrowing; conditional return updates preserve the first return timestamp. Tests include eight concurrent borrows/returns and member-write races. No additional schema/concurrency defect was found. No migration was added.

### Android Kotlin

Compose → ViewModel/StateFlow → repository → Retrofit architecture is retained. Request URLs and bodyless return handling now match actual API DTOs. Cancellation is rethrown rather than converted into ordinary failures. Build configuration uses JDK17, Gradle8.9, existing AGP8.7.3/Kotlin2.1, and SDK35/minSDK26. The original target37 was not supported by that AGP; [AGP8.7's supported maximum is35](https://developer.android.com/build/releases/agp-8-7-0-release-notes). This avoids a broad stack upgrade while making the existing project buildable.

Debug HTTP is allowed only in debug; release cleartext is false and body logging removed. Debug API URL is configurable with `-PcampusApiBaseUrl=...`; default remains emulator host5143. Release uses HTTPS7282 and is unsigned. Three tests cover the real Retrofit HTTP contract and detail-state races. One test-only coroutine library matches the existing runtime; no new production Android dependency was added.

## Key code

```csharp
// Validation before SQL writes.
[Required, MaxLength(200)]
public required string Title { get; set; }

// ISBN comparison stays in SQL and excludes only the edited row.
.AnyAsync(b => b.Isbn == isbn && (!excludeId.HasValue || b.Id != excludeId.Value));
```

```xml
<Content Update="appsettings.Development.json" CopyToPublishDirectory="Never" />
```

```kotlin
@GET("loans")
suspend fun getMemberLoans(@Query("memberId") memberId: Int,
                          @Query("openOnly") openOnly: Boolean = true): List<Loan>
@POST("loans/{id}/return")
suspend fun returnBook(@Path("id") loanId: Int): Response<Unit>
```

## Files changed

Statuses are relative to the start of this review, not Git history. Every added source/test/bootstrap file is explained here.

| Path | Status | Why |
|---|---|---|
| `android/CampusLibraryApp/app/build.gradle.kts` | modified | Uses the AGP-supported SDK35, generates build-specific API URLs, disables release cleartext, and adds test-only coroutine support. |
| `android/CampusLibraryApp/app/src/main/AndroidManifest.xml` | modified | Removes unresolved icon/theme resources and uses the build-specific network policy. |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/MainActivity.kt` | modified | Makes Android Back return from child screens to the catalog. |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/data/api/LibraryApi.kt` | modified | Aligns loan queries and the bodyless 204 return response with the actual API. |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/data/api/RetrofitClient.kt` | modified | Uses generated API configuration and removes unresolved/verbose body logging. |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/data/repository/LibraryRepository.kt` | modified | Handles successful bodyless returns, preserves coroutine cancellation and supports deterministic tests. |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/ui/screens/BookDetailScreen.kt` | modified | Keeps borrowing reachable with large text and prevents stale success dialogs. |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/ui/screens/MyLoansScreen.kt` | modified | Reloads active loans whenever the screen is entered. |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/ui/viewmodels/BookDetailViewModel.kt` | modified | Cancels stale detail loads and prevents borrowing a different book from the one displayed. |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/ui/viewmodels/LoansViewModel.kt` | modified | Allows the loans screen to reload fresh state on entry. |
| `src/CampusLibrary.Data/CampusLibrary.Data.csproj` | modified | Pins patched PKCS 10.0.13 instead of the vulnerable transitive version. |
| `src/CampusLibrary.Data/Repositories/BookRepository.cs` | modified | Excludes the edited book from ISBN uniqueness checks while retaining SQL collation. |
| `src/CampusLibrary.Data/Repositories/Interfaces/IBookRepository.cs` | modified | Adds the optional excluded ID to the existing ISBN query contract. |
| `src/CampusLibrary.WebApi/CampusLibrary.WebApi.csproj` | modified | Excludes local Development settings from published API artifacts. |
| `src/CampusLibrary.WebApi/Controllers/BooksController.cs` | modified | Rejects nonpositive route IDs with validation problem details. |
| `src/CampusLibrary.WebApi/DTOs/BookDtos.cs` | modified | Validates required strings, schema lengths and optional publication year before SQL writes. |
| `src/CampusLibrary.WebApi/Services/BookService.cs` | modified | Normalizes book fields and checks uniqueness without conflicting with the current row. |
| `src/CampusLibrary.WebMvc/CampusLibrary.WebMvc.csproj` | modified | Excludes local Development settings from published MVC artifacts. |
| `src/CampusLibrary.WebMvc/Controllers/HomeController.cs` | modified | Maps invalid status-page codes to a safe 404. |
| `src/CampusLibrary.WebMvc/Services/BookAdminService.cs` | modified | Uses the shared excluded-ID ISBN check for case-only edits. |
| `.gitignore` | modified | Keeps the required checksum-pinned Gradle wrapper available for version control. |
| `SPEC.md` | modified | Replaces copied local database credentials with a placeholder; other historical content is retained. |
| `docs/reports/2026-11-07-1516-ef-core-entities-dbcontext/DEVELOPER-DOCS.md` | modified | Replaces copied local database credentials with a placeholder; other historical content is retained. |
| `docs/reports/2026-11-07-1516-ef-core-entities-dbcontext/HOW-TO-USE.md` | modified | Replaces copied local database credentials with a placeholder; other historical content is retained. |
| `docs/reports/2026-11-07-1450-solution-setup/HOW-TO-USE.md` | modified | Replaces copied local database credentials with a placeholder; other historical content is retained. |
| `docs/reports/2026-11-07-0650-solution-setup/DEVELOPER-DOCS.md` | modified | Replaces copied local database credentials with a placeholder; other historical content is retained. |
| `tasks/todo.md` | modified | Replaces copied local database credentials with a placeholder; other historical content is retained. |
| `tasks/final-review-plan.md` | added | Records review scope, confirmed findings and verification progress. |
| `tests/test_final_review.py` | added | Adds failing-then-passing API/MVC regressions for validated input, ISBN edits and status codes. |
| `tests/verify_publish.py` | added | Publishes both hosts into fresh temporary directories and asserts local settings are absent. |
| `android/CampusLibraryApp/gradlew` | added | Starts checksum-pinned Gradle builds on macOS/Linux. |
| `android/CampusLibraryApp/gradlew.bat` | added | Starts the same Gradle wrapper on Windows. |
| `android/CampusLibraryApp/gradle/wrapper/gradle-wrapper.jar` | added | Bootstraps the standard Gradle distribution. |
| `android/CampusLibraryApp/gradle/wrapper/gradle-wrapper.properties` | added | Pins Gradle8.9 and its official SHA256 checksum. |
| `android/CampusLibraryApp/app/src/test/java/com/campus/library/data/api/LibraryApiContractTest.kt` | added | Exercises real local HTTP route/query and 204-response behavior. |
| `android/CampusLibraryApp/app/src/test/java/com/campus/library/ui/viewmodels/BookDetailViewModelTest.kt` | added | Checks delayed-response and mismatched-borrow regressions with controlled coroutine scheduling. |
| `docs/reports/INDEX.md` | modified | Links the final review report. |
| `docs/reports/2026-11-07-1817-final-code-review/DEVELOPER-DOCS.md` | added | Documents component findings, fixes, verification and limitations. |
| `docs/reports/2026-11-07-1817-final-code-review/HOW-TO-USE.md` | added | Explains local startup and reproducible verification. |
| `docs/reports/2026-11-07-1817-final-code-review/FLOWCHARTS.md` | added | Shows validated data flow and review verification. |
| `docs/reports/2026-11-07-1817-final-code-review/evidence/` | added | Preserves fresh build/test/audit reports and actual browser/emulator captures. |

## Database changes

**None.** Latest migration remains `20261107074756_EnforceOneOpenLoanPerBook`. Test databases were generated and removed; CampusLibraryDb and its container were not reset. Existing API process on5143 was left untouched, so restart it to load current source before using the default Android APK.

## Fresh verification

| Check | Result |
|---|---|
| `dotnet build --no-restore -p:UseSharedCompilation=false -m:1` | Pass, zero warnings/errors |
| `dotnet build -c Release --no-restore -p:UseSharedCompilation=false -m:1` | Pass, zero warnings/errors |
| `python3 tests/run_milestone5.py` | 18 API/MVC integration tests pass against a new MSSQL database |
| `python3 tests/verify_publish.py` | Both fresh Release artifacts omit local Development settings |
| `dotnet list package --vulnerable --include-transitive --format json` | No vulnerable NuGet packages reported |
| `dotnet test --no-build --no-restore` | Exit0; no .NET test projects exist (not a claim of18.NET unit tests) |
| EF `has-pending-model-changes` | No drift; existing tool10.0.12/runtime10.0.14 warning remains |
| Existing headless Chrome verification | 21 rendered states pass at1440×1000 and390×844; labels, focus, menu, validation, overflow, assets and persisted return checked |
| Android `assembleDebug testDebugUnitTest assembleRelease lintDebug --no-daemon` | Pass;3 tests, zero failures/errors; lint zero errors/**51 warnings** |
| Android default debug rebuild | Pass; on-disk APK restored to default5143 URL |
| Pixel9 review emulator + isolated API + MSSQL | Catalog/details → borrow → My Loans → return → empty, Back, large text/landscape and revisit verified |
| Cleanup | Review emulator stopped without snapshot changes; isolated API/MVC stopped and database dropped |

Regression failures were observed before fixes: API/MVC ISBN conflicts, validation/status failures, release credential copying, Android404/204 mismatch and delayed-detail/wrong-borrow tests. Final checks above are fresh; historical success claims were not used as substitutes.

Evidence is preserved under [evidence/](evidence/), including build logs, test XML, lint output, NuGet audit,21 browser captures and actual Android captures. Tooling was isolated in `/tmp` because host JDK25/26 cannot run Gradle8.9; no global tool or SDK configuration was changed. Gradle wrapper distribution SHA256 is pinned. Current APKs are in `android/CampusLibraryApp/app/build/outputs/apk/`.

## Remaining limitations and final verdict

1. **Supported database deployment unresolved.** The current `azure-sql-edge:latest` container is retired and receives no further security fixes. [Microsoft lifecycle notice](https://learn.microsoft.com/lifecycle/announcements/azure-products-retirement-september-2025). Replacing/migrating that stateful service was not performed because it would change the user's database/container configuration. Choose and verify a supported SQL host before deployment.
2. **Demo security scope remains.** No authentication was specified; API/MVC are unauthenticated and Android uses member1. Do not treat this as a multi-user production authorization design. If earlier credential-bearing files were shared, rotate that password separately; this review only redacted repository copies and publish artifacts.
3. **Android distribution setup remains.** Release signing, a real trusted HTTPS endpoint/certificate and store-target policy alignment are not configured. The unsigned HTTPS-emulator APK is build verification, not a production deployment. My Loans identifies books by ID because LoanDto has no nested Book; that fallback is retained.
4. **Android lint:**51 warnings:3 AGP-version,27 dependency-version,20 version-catalog recommendations and1 missing custom app icon. No broad dependency refresh or branding feature was introduced. No dedicated Android dependency vulnerability scan was run.
5. **Coverage limits:** One emulator profile; no physical device, minimumAPI26 device, TalkBack, full manual offline matrix or every screen/font/width combination. Core live flow and the confirmed large-text defect were verified. Browser captures are not a screen-reader audit.
6. **Tooling/API documentation:** Local builds require compatible JDK17/SDK35/build-tools34; temporary downloaded tools are not durable project configuration. EF tool patch warning remains. OpenAPI JSON is present; interactive Swagger UI is not supplied.

**Verdict:** Core local Milestones1–7 workflows are implemented and verified after the fixes. The review is complete for that scope; production deployment remains conditional on the explicit unresolved items above.
