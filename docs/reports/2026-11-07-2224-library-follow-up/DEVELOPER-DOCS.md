# CampusLibrary — Library surfaces follow-up

## Summary

Complete the missing indexed Milestone 1 developer report, correct stale Android examples and reconcile documentation with actual build targets.
The concurrent implementation adds Development Swagger UI, loan-derived book availability, a centralized visible demo member, and the Bound in Ink MVC/Android visual refresh.
Historical reports retain their original evidence and now explicitly identify it as historical, not verification of this follow-up.
Integrated SQL/browser checks, packaging, .NET build and Android build/unit/lint checks passed as recorded below. Native emulator lifecycle/font/night checks and the Impeccable finish review remain pending.

## Projects touched

WEB API, WEB MVC, EF CORE/Data repositories and ANDROID KOTLIN; documentation/task records. MSSQL uses the existing schema and configuration; no database migration is requested. No credentials, container changes or commits are included.

## Current build facts

| Component | Actual setting/source |
|---|---|
| Solution / framework | `CampusLibrary.slnx`; all three C# projects target `net10.0` |
| Local SDK observation | `dotnet --version` returned `10.0.400`; no global.json pin |
| EF Core | SqlServer/Design 10.0.14; PKCS pin 10.0.13 |
| API OpenAPI | Microsoft.AspNetCore.OpenApi 10.0.11; Development Swagger UI wraps existing `/openapi/v1.json` |
| Android | min SDK 26; compile/target 35; AGP 8.7.3; Kotlin 2.1.0; Gradle wrapper 8.9 |
| Android Java / Compose | JDK/JVM 17; build-tools 34.0.0; Compose BOM 2024.10.00 |

The originally proposed SDK 37 and host Java 26 inventory are not current Android build requirements. No stack upgrade was performed by this documentation work.

## Behavior and key code

- Development `/swagger` exposes interactive API documentation; OpenAPI JSON remains `/openapi/v1.json`. The UI is not intended for Production.
- Book response `isAvailable` is additive and derived from the absence of a loan with `ReturnDate == null`, not a persisted Book column. Read projections stay in the Data repository; existing borrow checks and unique filtered SQL index remain authoritative when availability changes after a read.
- Android uses `DemoMember.ID = 1` and visible `DemoMember.LABEL`; this is a classroom demo, not authentication. A missing seed member should produce an API error, not silently select another identity.
- My Loans remains `GET /api/loans?memberId=1&openOnly=true`; `POST /api/loans/{id}/return` returns 204 without JSON.
- MVC catalog also receives additive availability through the existing admin service/ViewModel, using the shared repository projection; no direct Razor/Controller database queries are added.
- Bound in Ink changes presentation, not domain permissions or existing anti-forgery, validation, error/retry and cancellation protections. MVC self-hosts Source Serif 4 with its OFL license; no external font fetch is needed.

```kotlin
@POST("loans/{id}/return")
suspend fun returnBook(@Path("id") loanId: Int): Response<Unit>
```

## Files changed

The inventory below is a near-completion Git working-tree snapshot, not a final commit diff. The pre-existing `android/CampusLibraryApp/gradle.properties` modification is user/baseline work and must not be attributed to this follow-up. Other agents may still finish source, tests and visual documentation after this snapshot; the coordinator must refresh it before final delivery.

| Path | Added/modified/deleted | Why |
|---|---|---|
| `CLAUDE.md` | modified | Reconciles actual framework, SDK, solution and architecture facts |
| `SPEC.md` | modified | Reconciles actual framework, SDK, solution and architecture facts |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/MainActivity.kt` | modified | Bound in Ink presentation, availability and centralized visible demo-member behavior |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/data/models/Models.kt` | modified | Bound in Ink presentation, availability and centralized visible demo-member behavior |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/ui/screens/BookDetailScreen.kt` | modified | Bound in Ink presentation, availability and centralized visible demo-member behavior |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/ui/screens/BookListScreen.kt` | modified | Bound in Ink presentation, availability and centralized visible demo-member behavior |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/ui/screens/MyLoansScreen.kt` | modified | Bound in Ink presentation, availability and centralized visible demo-member behavior |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/ui/viewmodels/BookDetailViewModel.kt` | modified | Bound in Ink presentation, availability and centralized visible demo-member behavior |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/ui/viewmodels/BooksViewModel.kt` | modified | Bound in Ink presentation, availability and centralized visible demo-member behavior |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/ui/viewmodels/LoansViewModel.kt` | modified | Bound in Ink presentation, availability and centralized visible demo-member behavior |
| `android/CampusLibraryApp/app/src/test/java/com/campus/library/ui/viewmodels/BookDetailViewModelTest.kt` | modified | Regression/contract or browser verification for follow-up behavior |
| `android/CampusLibraryApp/gradle.properties` | modified | Pre-existing user modification; preserved, not authored by this follow-up |
| `docs/reports/2026-11-07-0650-solution-setup/DEVELOPER-DOCS.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1450-solution-setup/FLOWCHARTS.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1450-solution-setup/HOW-TO-USE.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1516-ef-core-entities-dbcontext/DEVELOPER-DOCS.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1516-ef-core-entities-dbcontext/FLOWCHARTS.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1516-ef-core-entities-dbcontext/HOW-TO-USE.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1535-books-api-crud/DEVELOPER-DOCS.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1535-books-api-crud/FLOWCHARTS.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1535-books-api-crud/HOW-TO-USE.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1552-members-loans-api/DEVELOPER-DOCS.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1552-members-loans-api/FLOWCHARTS.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1552-members-loans-api/HOW-TO-USE.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1609-mvc-admin-pages/DEVELOPER-DOCS.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1609-mvc-admin-pages/FLOWCHARTS.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1609-mvc-admin-pages/HOW-TO-USE.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1711-android-app-mvvm/DEVELOPER-DOCS.md` | modified | Labels historical evidence; Android report also repairs stale contract/setup examples |
| `docs/reports/2026-11-07-1711-android-app-mvvm/FLOWCHARTS.md` | modified | Labels historical evidence; Android report also repairs stale contract/setup examples |
| `docs/reports/2026-11-07-1711-android-app-mvvm/HOW-TO-USE.md` | modified | Labels historical evidence; Android report also repairs stale contract/setup examples |
| `docs/reports/2026-11-07-1817-final-code-review/DEVELOPER-DOCS.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1817-final-code-review/FLOWCHARTS.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/2026-11-07-1817-final-code-review/HOW-TO-USE.md` | modified | Labels historical evidence or updates report navigation |
| `docs/reports/INDEX.md` | modified | Labels historical evidence or updates report navigation |
| `src/CampusLibrary.Data/Repositories/BookRepository.cs` | modified | Development Swagger UI or shared loan-derived availability contract/projection |
| `src/CampusLibrary.Data/Repositories/Interfaces/IBookRepository.cs` | modified | Development Swagger UI or shared loan-derived availability contract/projection |
| `src/CampusLibrary.WebApi/CampusLibrary.WebApi.csproj` | modified | Development Swagger UI or shared loan-derived availability contract/projection |
| `src/CampusLibrary.WebApi/DTOs/BookDtos.cs` | modified | Development Swagger UI or shared loan-derived availability contract/projection |
| `src/CampusLibrary.WebApi/Program.cs` | modified | Development Swagger UI or shared loan-derived availability contract/projection |
| `src/CampusLibrary.WebApi/Services/BookService.cs` | modified | Development Swagger UI or shared loan-derived availability contract/projection |
| `src/CampusLibrary.WebMvc/Services/BookAdminService.cs` | modified | Bound in Ink MVC catalog/layout and additive availability |
| `src/CampusLibrary.WebMvc/ViewModels/BookViewModels.cs` | modified | Bound in Ink MVC catalog/layout and additive availability |
| `src/CampusLibrary.WebMvc/Views/Books/Index.cshtml` | modified | Bound in Ink MVC catalog/layout and additive availability |
| `src/CampusLibrary.WebMvc/Views/Shared/_Layout.cshtml` | modified | Bound in Ink MVC catalog/layout and additive availability |
| `src/CampusLibrary.WebMvc/wwwroot/css/site.css` | modified | Bound in Ink MVC catalog/layout and additive availability |
| `tasks/plan.md` | modified | Marks original setup plans as historical rather than current status |
| `tasks/todo.md` | modified | Marks original setup plans as historical rather than current status |
| `tests/run_milestone5.py` | modified | Regression/contract or browser verification for follow-up behavior |
| `tests/test_milestone4.py` | modified | Regression/contract or browser verification for follow-up behavior |
| `tests/test_milestone5.py` | modified | Regression/contract or browser verification for follow-up behavior |
| `.impeccable/config.json` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/library-surfaces.md` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/desktop-books.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/desktop-create-book.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/desktop-create-member.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/desktop-delete-book.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/desktop-edit-book.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/desktop-keyboard-focus.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/desktop-loans.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/desktop-members.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/desktop-not-found.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/desktop-validation.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/mobile-books.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/mobile-create-book.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/mobile-create-member.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/mobile-delete-book.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/mobile-edit-book.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/mobile-loans.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/mobile-members.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/mobile-navigation.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/mobile-not-found.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/mobile-return-success.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/mobile-validation.png` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `.impeccable/review/web/results.json` | added | Visual-agent-owned design contract/documentation; not edited by documentation agent |
| `android/CampusLibraryApp/app/src/main/java/com/campus/library/ui/screens/LibraryComponents.kt` | added | Bound in Ink presentation, availability and centralized visible demo-member behavior |
| `android/CampusLibraryApp/app/src/test/java/com/campus/library/ui/viewmodels/LoansViewModelTest.kt` | added | Regression/contract or browser verification for follow-up behavior |
| `docs/reports/2026-11-07-1450-solution-setup/DEVELOPER-DOCS.md` | added | Repairs canonical Milestone 1 developer report with historical provenance |
| `docs/reports/2026-11-07-2224-library-follow-up/DEVELOPER-DOCS.md` | added | Required final follow-up developer, user or flow documentation |
| `docs/reports/2026-11-07-2224-library-follow-up/FLOWCHARTS.md` | added | Required final follow-up developer, user or flow documentation |
| `docs/reports/2026-11-07-2224-library-follow-up/HOW-TO-USE.md` | added | Required final follow-up developer, user or flow documentation |
| `src/CampusLibrary.Data/Repositories/BookAvailability.cs` | added | Development Swagger UI or shared loan-derived availability contract/projection |
| `src/CampusLibrary.WebMvc/wwwroot/fonts/OFL.txt` | added | Self-hosted Source Serif 4 font and OFL licensing for MVC |
| `src/CampusLibrary.WebMvc/wwwroot/fonts/source-serif-4-semibold.ttf` | added | Self-hosted Source Serif 4 font and OFL licensing for MVC |

## Database changes

None: no added tables, columns, seed edits or migration. Latest migration remains `20261107074756_EnforceOneOpenLoanPerBook`. Availability is computed from existing Loans. Do not reset development data or replace the existing retired SQL Edge container as part of a UI/documentation repair.

## Verification — follow-up only

Observed by the documentation agent:
- Inspected all report Markdown, index, CLAUDE.md, AI_DEVELOPMENT_GUIDE.md, SPEC.md, historical task plans and actual build files/contracts.
- `dotnet --version`: 10.0.400.
- Confirmed canonical Milestone 1 folder now contains all three required files; the original 06:50 developer record remains intact with a historical notice.
- Documentation self-check passed: every indexed report folder has the required three Markdown files; introduced local report links resolve; corrected Android examples contain no nonexistent `/api/loans/member/` route. `git diff --check` passed, including the repaired historical Android trailing whitespace. These are documentation checks, not application tests.

Fresh results relayed by the coordinator and implementation agents (not independently executed by the documentation agent):

| Check | Result |
|---|---|
| .NET build | Pass; zero warnings/errors |
| `dotnet test` | Exit 0; no .NET test projects, so not a count of .NET unit tests |
| Development Swagger assets/schema; Production documentation routes | Development 200; Production 404 |
| Full isolated SQL runner, including `python3 tests/run_milestone5.py --browser` | Two final runs passed all **19 integration tests**, including concurrent-borrow behavior |
| Browser checks at 1440px / 390px | **21 rendered states passed**; zero page overflow, unlabelled inputs or JavaScript exceptions; keyboard skip link, menu, server validation and persisted return checked |
| `python3 tests/verify_publish.py` | Pass; both Release outputs exclude local Development settings |
| Android `assembleDebug testDebugUnitTest lintDebug` | Pass; **8 unit tests** passed; this is not native runtime verification |
| API/MVC code review | No verified material findings |

The initial SQL run passed 18/19 because a test expected inline Swagger bootstrap while the implementation uses external bootstrap. The assertion was corrected before the two passing final runs. The earlier intended browser count of 22 was not the observed result: actual reported evidence is 21 states. Current screenshots are under `.impeccable/review/web/` with `results.json`; the final CSS also corrects Bootstrap link RGB values.

Mutation checks use isolated SQL test databases, not CampusLibraryDb. No application records, schema, credentials or container configuration were intentionally changed by the follow-up. These fresh outcomes do not reuse the historical 18:17 review results.

**Still pending:** native live Android lifecycle, emulator font/night-mode checks and Impeccable finish acceptance. Coordinator must record their actual outcomes/evidence and cleanup before claiming those checks complete. Physical-device, minimum-API-26, TalkBack and screen-reader coverage are not implied by the browser/unit results.

## Known limits / TODO

- Complete integrated verification and refresh the source inventory after all agents finish.
- Original Milestone 1 06:50/14:50 mismatch remains unexplained; canonical developer report links the source rather than inventing a timestamp.
- Native Retrofit does not require browser CORS; host alias, URL/port and debug network policy determine connectivity.
- Retired SQL Edge, unauthenticated local demo scope, trusted deployment HTTPS and release signing remain unresolved deployment boundaries.
- No physical-device, minimum-API-26, TalkBack or screen-reader check is claimed without fresh evidence.
