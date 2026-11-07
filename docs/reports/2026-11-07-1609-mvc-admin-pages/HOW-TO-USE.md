# Milestone 5 — How to use the admin site

> Historical milestone record. Statements and verification below describe the original task, not a fresh run. For current setup and known limitations, use [the latest report index](../INDEX.md).

## Prerequisites

Use the existing .NET 10 SDK and running `campuslibrary-mssql` container. On this Apple Silicon Mac, the existing SQL port is 14335; LocalDB is not used. Keep the connection string in the ignored MVC `appsettings.Development.json` or environment configuration, not in source code.

The admin site accesses the database through the Data project. You do not need to run the Web API or an Android emulator to manage books/members and return existing loans.

## Start the site

1. Open a terminal in `~/CampusLibrary` and check `docker ps` for the running SQL container.
2. Run `dotnet build`.
3. If setting up a fresh database, apply the existing migrations:
   ```bash
   dotnet ef database update -p src/CampusLibrary.Data -s src/CampusLibrary.WebApi
   ```
   No new migration is needed for Milestone 5.
4. Start the MVC app using its existing HTTP profile:
   ```bash
   dotnet run --project src/CampusLibrary.WebMvc --launch-profile http
   ```
5. Open **http://localhost:5080**. The root opens Books. The completed app was also left running at this address at the end of implementation; if it is still running, open it directly instead of starting a second copy.

## Manage books

1. Open **Books** and choose **Add book**.
2. Enter the title and unique ISBN. Author and publication year are optional.
3. Choose **Add book**. On success, the catalog opens with confirmation.
4. Choose a record's **Edit**, change its details and choose **Save changes**.
5. To delete a book, choose **Delete**, verify the title/ISBN, then choose **Delete book** on the confirmation page. Choose **Keep book** to cancel.

A GET/viewing a confirmation page does not delete anything. Books with loan history cannot be deleted, even when all loans were returned. A duplicate ISBN or invalid field keeps the form open with instructions.

## Register members

1. Open **Members** and choose **Add member**.
2. Enter the full name, valid email address and unique campus student ID.
3. Choose **Add member**. The register opens with confirmation; the registration date is assigned automatically.

## Return books

1. Open **Loans** to see open loans, ordered by earliest due date.
2. Find the book and member. Review the due date and On loan/Overdue label.
3. Choose **Return book** when the physical book is received.
4. A confirmation appears and the loan leaves the open list. Its history remains stored, and the book becomes available again.

Example: borrow book 1 for member 1 using the existing API (`POST /api/loans` with `{"bookId":1,"memberId":1}`), then refresh Loans. Its row shows the book title, member, borrow/due dates and return button. Borrowing is not an admin-page feature in this milestone.

On a phone, open the navigation menu to switch sections. Table rows become labeled records so the edit/delete/return actions remain accessible without sideways scrolling. Dates on the site are shown in UTC.

## Verify locally

```bash
python3 tests/run_milestone5.py
```

This builds both apps, migrates a new disposable database, runs 13 integration tests and removes the test database. Requires Python 3.9+, existing dotnet-ef and SQL create/drop permissions. It leaves CampusLibraryDb and existing processes alone.

Optional browser checks require Node 22+ and the installed macOS Google Chrome (no npm packages):

```bash
python3 tests/run_milestone5.py --keep-running
```

Copy the temporary MVC/API URLs printed after the tests. In another terminal:

```bash
node tests/verify_milestone5.mjs <mvc-url> <api-url> /tmp/campuslibrary-m5-review
```

Return to the first terminal and press Enter to stop the temporary apps and remove the test database. Run this browser script only against those disposable test apps, because it creates verification records.

## Troubleshooting

- **Duplicate record or form errors:** Read the inline/summary messages. Supply required fields, use a valid email and choose a unique ISBN/email/student ID. Cancel returns to the list without saving.
- **Book cannot be deleted / loan already returned:** Loan history is protected. Keep that book record. Another request may have returned the loan first; the list refreshes with a notice.
- **Page fails, token expires, or port is occupied:** Confirm SQL is running and the local MVC development connection points to port 14335. Open a fresh form if submission was rejected. If port 5080 is already in use, open the running site or stop its original process before starting another copy. Share the displayed request ID with the maintainer if an unexpected error persists.
