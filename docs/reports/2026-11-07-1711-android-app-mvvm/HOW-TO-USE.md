# How to Use — Android App (Book List, Detail, Borrow, My Loans)

**Milestones:** 6-7 — Android MVVM App  
**Date:** 2026-11-07  
**For:** Students testing the Android app

---

## What Was Built

The **CampusLibrary Android app** is now complete with:

1. **4 screens** (book list, book detail, my loans, navigation)
2. **MVVM architecture** (ViewModels + StateFlow)
3. **Jetpack Compose UI** (Material3 design)
4. **Retrofit API client** (Gson serialization)
5. **Repository pattern** (Result<T> error handling)
6. **Loading/error states** (on all screens)
7. **Borrow & return** (for active loans)

**Note:** Hardcoded member ID (S001 - Alice Johnson) for demo. Production would use authentication.

---

## Prerequisites

Before you proceed, verify:

1. **Android Studio** with Kotlin plugin installed
   - Download: https://developer.android.com/studio

2. **Android SDK** (min API 26)
   - Check: Android Studio → Settings → SDK Manager
   - Expected: API 26, 37, 37.1 installed

3. **Android Emulator** running
   - Check: Android Studio → Device Manager
   - Expected: API level 26-37 emulator available

4. **.NET SDK 10.0** installed (for Web API)
   - Check: `dotnet --version`
   - Expected: 10.0.400 or higher

5. **SQL Server container** running
   - Check: `docker ps --filter "name=campuslibrary-mssql"`
   - Expected: Status "Up" on port 14335

6. **Web API** running
   - Check: `cd src/CampusLibrary.WebApi && dotnet run`
   - Expected: Listening on `http://localhost:5143`

---

## Step-by-Step: Running the App

### Step 1: Start the Web API

In one terminal:

```bash
cd /Users/stephen/CampusLibrary/src/CampusLibrary.WebApi
dotnet run
```

**Expected output:**
```
Now listening on: http://localhost:5143
Application started. Press Ctrl+C to shut down.
```

**Note:** Port 5143 is auto-assigned. If different, update `RetrofitClient.kt` BASE_URL.

---

### Step 2: Verify SQL Server Container

In another terminal:

```bash
docker ps --filter "name=campuslibrary-mssql"
```

**Expected output:**
```
CONTAINER ID   IMAGE                          STATUS        PORTS
970abab...     mcr.microsoft.com/azure-sql... Up 2 days     0.0.0.0:14335->1433/tcp
```

If not running:
```bash
docker start campuslibrary-mssql
```

---

### Step 3: Open Android Project

1. **Open Android Studio**
2. **File → Open**
3. Navigate to `/Users/stephen/CampusLibrary/android/CampusLibraryApp/`
4. **Click Open**

**Expected:** Android Studio opens project, starts Gradle sync (takes 1-2 minutes first time)

---

### Step 4: Create/Start Android Emulator

1. **Tools → Device Manager** (or Android Studio → Device Manager)
2. **Create Device** if none exists:
   - Phone: Pixel 4, 5, or 6
   - API Level: 26-37
   - Click Create
3. **Launch Emulator:** Click play button next to device name

**Expected:** Emulator window opens with Android home screen in 30-60 seconds

---

### Step 5: Build and Run the App

1. **Build → Make Project** (or Cmd+F9)
   - Wait for build to complete (30-60 seconds)
   - Check for "Build successful" at bottom

2. **Run → Run 'app'** (or Shift+F10)
   - Select the running emulator
   - Click OK

**Expected:** App launches on emulator with "Campus Library" title

---

### Step 6: Test the Book List Screen

**What you should see:**
- Top app bar with "Campus Library" title and "My Loans" button
- List of 5 books (loaded from API):
  1. Clean Code
  2. The Pragmatic Programmer
  3. Design Patterns
  4. Introduction to Algorithms
  5. Code Complete
- Each book shows title, author, ISBN, year

**If you see a loading spinner:**
- Wait 2-3 seconds for API call to complete

**If you see an error:**
- Check that Web API is running: `dotnet run`
- Check that SQL Server container is running: `docker ps`
- Look at Android Studio logcat for error details

---

### Step 7: Test Book Detail Screen

1. **Tap any book** (e.g., "Clean Code")

**Expected screen:**
- Title at top
- Author, ISBN, year published
- "Borrow This Book" button at bottom
- Back button to return to list

**If button is disabled:**
- The API is still loading (wait a moment)

---

### Step 8: Test Borrow Functionality

1. **Tap "Borrow This Book"**

**Expected:**
- Button shows "Borrowing..." with spinner
- After 1-2 seconds: Success dialog appears
- Dialog: "Success" title, "Book borrowed successfully!" message
- Button: "OK"

**Click OK:**
- Dialog closes
- App navigates to "My Loans" screen automatically

**If you see error "This book is already on loan":**
- That book is already borrowed by member S001 (Alice Johnson)
- Try a different book

---

### Step 9: Test My Loans Screen

**What you should see:**
- List of active loans (books borrowed by Alice Johnson)
- Each loan shows:
  - Book title
  - Author
  - Borrow date
  - Due date
  - "Return Book" button

**If empty:**
- You haven't borrowed any books yet
- Go back and borrow one

---

### Step 10: Test Return Functionality

1. **In My Loans, tap "Return Book"** on any loan

**Expected:**
- Button shows "Returning..." with spinner
- After 1-2 seconds: Loan disappears from list
- List refreshes to show remaining loans

---

### Step 11: Test Navigation

**From Book List:**
- Tap book → goes to detail
- Tap "My Loans" button → goes to loans list

**From Book Detail:**
- Tap back button → returns to list
- Tap "Borrow This Book" → borrows and navigates to loans

**From My Loans:**
- Tap back button → returns to list

---

## Testing Checklist

Use this to verify all functionality:

- [ ] Book list loads with 5 books
- [ ] Click book → detail screen shows correct info
- [ ] Click "Borrow This Book" → success dialog
- [ ] After borrow → navigates to "My Loans"
- [ ] "My Loans" shows active loans
- [ ] Click "Return Book" → loan removed from list
- [ ] Click back button → returns to previous screen
- [ ] Click "My Loans" button from list → navigates to loans

---

## Troubleshooting

### Problem 1: "Failed to load books" error on Book List

**Cause:** Web API is not running or network unreachable.

**Fix:**
1. Verify Web API is running: `dotnet run` in `src/CampusLibrary.WebApi/`
2. Check port: Should be `http://localhost:5143`
3. If different port, update `android/.../data/api/RetrofitClient.kt`:
   ```kotlin
   private const val BASE_URL = "http://10.0.2.2:YOUR_PORT/api/"
   ```
4. Rebuild and re-run app

---

### Problem 2: Emulator can't reach Web API

**Cause:** Android emulator uses `10.0.2.2` (not `localhost`) to reach host machine.

**Fix:**
- The app is already configured for this (`10.0.2.2:5143`)
- Verify in `RetrofitClient.kt` line: `private const val BASE_URL = "http://10.0.2.2:5143/api/"`
- If Web API runs on different port, update the URL

---

### Problem 3: Borrow fails with "This book is already on loan"

**Cause:** Book has an active loan already.

**Fix:**
- The business rule enforces: one book = max one open loan
- Try borrowing a different book
- Or return the loan first, then borrow again

---

### Problem 4: "Borrowing..." button is stuck spinning

**Cause:** API request timed out or network issue.

**Fix:**
1. Wait 10 seconds (timeout is 30 seconds)
2. Check logcat for error: Android Studio → View → Tool Windows → Logcat
3. Verify Web API is still running
4. Try again

---

### Problem 5: Android Studio says "Gradle sync failed"

**Cause:** Missing Gradle wrapper or network issue.

**Fix:**
1. File → Invalidate Caches → Invalidate and Restart
2. Wait for Android Studio to restart and sync again (2-3 minutes)
3. If still failing, delete `android/CampusLibraryApp/.gradle/` folder and sync again

---

### Problem 6: "No such file or directory: gradlew"

**Cause:** Gradle wrapper not generated (expected for new projects).

**Fix:**
- Android Studio should auto-generate it on first sync
- If not, try: File → Sync Now
- Or manually: `cd android/CampusLibraryApp && ./gradlew build`

---

## What You Should See

### Book List Screen
```
┌──────────────────────────────┐
│ Campus Library        My Loans│
├──────────────────────────────┤
│ Clean Code                   │
│ by Robert C. Martin          │
│ ISBN: 978-0132350884         │
│ Year: 2008                   │
├──────────────────────────────┤
│ The Pragmatic Programmer     │
│ by Andrew Hunt               │
│ ... (4 more books)           │
└──────────────────────────────┘
```

### Book Detail Screen
```
┌──────────────────────────────┐
│ ← Book Details               │
├──────────────────────────────┤
│ Clean Code                   │
│                              │
│ Author: Robert C. Martin     │
│ ISBN: 978-0132350884         │
│ Published: 2008              │
│                              │
│                              │
│ ┌──────────────────────────┐ │
│ │ Borrow This Book         │ │
│ └──────────────────────────┘ │
└──────────────────────────────┘
```

### My Loans Screen
```
┌──────────────────────────────┐
│ ← My Loans                   │
├──────────────────────────────┤
│ Clean Code                   │
│ by Robert C. Martin          │
│                              │
│ Borrowed: Nov 07, 2026       │
│       Due: Nov 21, 2026      │
│ ┌──────────────────────────┐ │
│ │ Return Book              │ │
│ └──────────────────────────┘ │
├──────────────────────────────┤
│ (more loans if any)          │
└──────────────────────────────┘
```

---

## Architecture Overview

```
User (Emulator)
    ↓
Compose UI (4 screens)
    ↓
ViewModel (StateFlow)
    ↓
Repository (Result<T>)
    ↓
Retrofit API Client
    ↓
Web API (http://10.0.2.2:5143)
    ↓
SQL Server (port 14335)
```

**Key points:**
- Each layer independent and testable
- Reactive UI updates via StateFlow
- Error handling with Result<T>
- API calls on background thread (Dispatchers.IO)

---

## API Used

The app calls these endpoints from the Web API:

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/books` | Get all books |
| GET | `/api/books/{id}` | Get single book |
| POST | `/api/loans` | Borrow book (create loan) |
| GET | `/api/loans/member/{memberId}` | Get member's loans |
| POST | `/api/loans/{id}/return` | Return book |

All endpoints built and tested in previous milestones.

---

## Demo Member

The app uses hardcoded member ID for demo:
- **Member ID:** 1
- **Name:** Alice Johnson
- **Email:** alice.johnson@campus.edu
- **Student ID:** S001

In production, you would:
1. Implement login screen
2. Store JWT token
3. Use authenticated user's ID
4. Secure API with token validation

---

## Next Steps

**Milestones complete!**

You now have a full-stack library management system:
- ✅ Database with EF Core
- ✅ REST API with CRUD operations
- ✅ Web MVC admin site
- ✅ Android mobile app

**Future enhancements:**
- User authentication & registration
- Book reservations
- Recommendation engine
- Notification system
- Loan history & analytics

---

## Summary

✅ **What works:**
- Book list loads from API
- Browse individual books
- Borrow books
- View and return active loans
- Full MVVM + Compose architecture

⏭️ **What doesn't work:**
- User login/authentication (demo uses hardcoded user)
- Advanced features (search, filters, reservations)

**This is expected!** The app focuses on core functionality and clean architecture. Authentication can be added in a future phase.
