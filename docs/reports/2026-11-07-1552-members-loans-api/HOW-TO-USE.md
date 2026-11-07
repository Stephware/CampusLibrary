# Milestone 4 — How to use Members and Loans

## Prerequisites

Use the existing .NET 10 SDK and running `campuslibrary-mssql` Docker container. This Mac uses MSSQL on `localhost,14335`; LocalDB is not supported on macOS. Keep the existing connection string in the ignored `src/CampusLibrary.WebApi/appsettings.Development.json` (or supply secrets through environment configuration). Do not paste passwords into source files or reports.

No emulator or MVC site is needed for these API endpoints.

## Run and use

1. Open a terminal in `~/CampusLibrary` and confirm `docker ps` shows `campuslibrary-mssql` running.
2. Run `dotnet build`.
3. Apply migrations if setting up another copy (already applied on this machine):
   ```bash
   dotnet ef database update -p src/CampusLibrary.Data -s src/CampusLibrary.WebApi
   ```
4. Stop the old API in its original terminal with Ctrl+C, then start the updated API:
   ```bash
   dotnet run --project src/CampusLibrary.WebApi --launch-profile http
   ```
   The existing HTTP profile uses `http://localhost:5143`.
5. View books and members to select valid IDs:
   ```bash
   curl http://localhost:5143/api/books
   curl http://localhost:5143/api/members
   ```
6. Optionally create a member (use a fresh email/student ID each time):
   ```bash
   curl -i -X POST http://localhost:5143/api/members      -H 'Content-Type: application/json'      -d '{"name":"Dana Cruz","email":"dana@example.com","studentId":"S004"}'
   ```
   Expect 201 Created, a Location header and JSON containing `id`, the member fields and a server-generated `registeredDate`. Use the returned `id` for later requests.
7. Borrow an available book, using real IDs (example uses seeded book/member 1):
   ```bash
   curl -i -X POST http://localhost:5143/api/loans      -H 'Content-Type: application/json'      -d '{"bookId":1,"memberId":1}'
   ```
   Expect 201 Created and a Location header. Example response; actual ID and dates vary:
   ```json
   {"id":1,"bookId":1,"memberId":1,"borrowDate":"2026-11-07T08:00:00Z","dueDate":"2026-11-21T08:00:00Z","returnDate":null}
   ```
   A book may have one open loan. The loan period is 14 days. Dates are assigned by the server in UTC.
8. View a member's open loans, or omit the filters for all history:
   ```bash
   curl 'http://localhost:5143/api/loans?memberId=1&openOnly=true'
   curl http://localhost:5143/api/loans/1
   ```
9. Return the loan using its actual loan ID:
   ```bash
   curl -i -X POST http://localhost:5143/api/loans/1/return
   ```
   Expect 204 No Content. A later GET shows `returnDate`; returning again gives 409. The book can now be borrowed again.
10. Edit or delete members:
    ```bash
    curl -i -X PUT http://localhost:5143/api/members/4       -H 'Content-Type: application/json'       -d '{"name":"Dana Cruz Updated","email":"dana@example.com","studentId":"S004"}'
    curl -i -X DELETE http://localhost:5143/api/members/4
    ```
    Replace 4 with the created ID. Successful update/delete returns 204. A member or book with any loan history cannot be deleted, even after return (409), preserving the history.

## Run automated checks

```bash
python3 tests/run_milestone4.py
```

Requires Python 3.9+, existing dotnet-ef, and SQL credentials allowed to create/drop databases. This command builds, migrates an isolated test database, starts a separate temporary API, runs seven integration tests, then removes that database. It does not stop the development API or reset CampusLibraryDb. Avoid manually pointing `test_milestone4.py` at your regular database; tests create loan history intentionally.

## Troubleshooting

- **400 / 404:** Supply nonblank member fields, a valid email and positive IDs. Length limits are name/email 100, student ID 20. GET books/members to confirm IDs; copy the loan ID from POST. A 404 on every new endpoint with an old API process means it needs restarting.
- **409 Conflict:** A book is already borrowed, a loan was already returned, an email/student ID is duplicated, or deletion would remove loan history. Read the problem `detail`; return the existing loan before borrowing again, or choose a distinct member identifier.
- **Cannot connect / port in use / database failure:** Start the existing SQL container and check its port 14335 and local development credentials. Stop the older API process in its original terminal before restarting on port 5143. Apply the migration before borrowing; do not recreate or drop your development database.
