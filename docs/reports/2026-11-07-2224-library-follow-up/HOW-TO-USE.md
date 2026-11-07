# CampusLibrary — Use the updated library surfaces

## Prerequisites

- .NET 10 SDK; existing SQL container/local connection settings with the existing migrations applied. This Mac uses SQL host port 14335. Keep credentials in ignored development settings, user-secrets or secure environment configuration.
- Android Studio, JDK 17, Android platform 35/build-tools 34.0.0 and a running emulator (minimum supported device API 26). Use the checked-in Gradle 8.9 wrapper.
- This is a local unauthenticated teaching demo. SQL Edge is retired; do not deploy it as a supported service or change/delete its data to try these screens.

## Start and browse

1. Open a terminal at `/Users/stephen/Documents/CampusLibrary` and check the existing SQL container is running. Do not recreate it.
2. Build with `dotnet build`. Stop an older API/MVC process in its original terminal before starting current source.
3. Start API using `dotnet run --project src/CampusLibrary.WebApi --launch-profile http`.
4. In Development, open `http://localhost:5143/swagger`; the JSON document remains `http://localhost:5143/openapi/v1.json`. Choose an endpoint and use **Try it out**. Write requests change local records; choose disposable test data and preserve loan history.
5. Start MVC in a second terminal with `dotnet run --project src/CampusLibrary.WebMvc --launch-profile http`. Open `http://localhost:5080` for the Bound in Ink catalog, member registration and open-loan returns. Existing required fields, validation messages and anti-forgery protection still apply.
6. Open `android/CampusLibraryApp` in Android Studio with JDK 17, start an emulator, and run debug. Command-line build from that folder: `./gradlew assembleDebug`.
7. Android debug uses `http://10.0.2.2:5143/api/`. If testing another port, rebuild with `./gradlew assembleDebug -PcampusApiBaseUrl=http://10.0.2.2:PORT/api/`; do not edit a source `BASE_URL` constant. Release uses HTTPS and cannot use cleartext debug instructions.
8. Read the visible demo-member label. Borrowing and My Loans use numeric member ID 1, not a signed-in user. Open a book, review availability, borrow an available book, acknowledge success and open My Loans. Return the loan and revisit the catalog/details to refresh availability. Exact text/appearance is subject to the final visual acceptance recorded in the developer report.

## Examples

Read books without changing data:

```bash
curl http://localhost:5143/api/books/1
curl 'http://localhost:5143/api/loans?memberId=1&openOnly=true'
```

Illustrative book response, not a captured verification result (values depend on stored data):

```json
{"id":1,"title":"Clean Code","isbn":"978-0132350884","author":"Robert C. Martin","yearPublished":2008,"isAvailable":true}
```

`isAvailable` becomes false while any member has an open loan for that book. Another borrower can win between viewing and borrowing; a 409 conflict is valid and should be shown as useful feedback. Returning uses the actual **loan ID**, not the book ID, and succeeds with **204 No Content**, not a Loan JSON object. My Loans shows only demo member 1's open loans; it cannot identify every other member's borrowing.

## Troubleshooting

- **Old appearance, missing Swagger or missing availability:** Restart the API/MVC from current source; confirm Development and the HTTP launch profile. Historical reports describe earlier builds. Swagger is intentionally not a Production feature.
- **Android cannot connect or Java/Gradle fails:** Use JDK 17 and SDK 35/build-tools 34.0.0, the checked-in wrapper, and emulator host `10.0.2.2`, not `localhost`. CORS is browser policy and does not fix native Retrofit connectivity. Check the debug URL override and rebuild; do not disable HTTPS certificate validation in release.
- **Borrow fails or My Loans is empty:** Confirm numeric member 1 still exists and select an available book. A 409 can mean a loan already exists for any member. Refresh and choose another book or return its existing loan through the appropriate interface; never delete history/reset the database just to clear the state.

## Verification status

Fresh verification relayed by the coordinator (not independently executed by the documentation agent):

- **19 isolated SQL integration tests** passed (two final runs), including concurrent-borrow behavior.
- **21 browser-rendered states** passed at desktop (1440px) and mobile (390px); no overflow, missing labels or JS exceptions; keyboard skip, menu, server validation and persisted return confirmed.
- **Development Swagger/OpenAPI 200**, **Production docs 404**.
- **Release publish** excludes local Development settings.
- **.NET build zero warnings/errors**, `dotnet test` exit 0 (no .NET test projects).
- **Android** `assembleDebug testDebugUnitTest lintDebug` pass with **8 unit tests**; native lifecycle, font and night-mode checks are still pending.
- Impeccable finish review pending.

These are documented relayed results; do not substitute historical 18:17 review outcomes. Native Android and Impeccable acceptance remain open until the coordinator records their actual outcomes and cleanup.
