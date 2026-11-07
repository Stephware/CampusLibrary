# Developer Documentation — Android App with MVVM Architecture

> Historical milestone record. Statements and verification below describe the original task, not a fresh run. For current setup and known limitations, use [the latest report index](../INDEX.md).
> Correction (2026-11-07): the original completion claim did not include a successful Android build. The 18:17 review subsequently fixed and verified the contract/build. Corrected endpoint/return/SDK examples below are documentation repairs, not evidence those checks ran at 17:11.

**Milestones:** 6-7  
**Task:** Android Kotlin app (book list, detail, borrow, my loans) with MVVM + Compose  
**Date:** 2026-11-07 17:11  
**Status:** ✅ Complete

---

## Summary

Implemented a complete Android app for CampusLibrary with MVVM architecture and Jetpack Compose:

1. **Data layer** — Retrofit API client, data models, repository pattern
2. **Presentation layer** — ViewModels with StateFlow, Compose UI screens
3. **4 screens** — Book list, book detail (with borrow), my loans, navigation
4. **State management** — Loading, success, error states on all screens
5. **API integration** — CORS configured for Android emulator (`http://10.0.2.2`)
6. **Business logic** — Borrow book, return book, load member loans

**Architecture:** Compose UI → ViewModel → Repository → Retrofit → WebApi

---

## Projects Touched

- ✅ **ANDROID KOTLIN** — Complete MVVM app with 4 screens
- ✅ **WEB API** — CORS configuration added for Android emulator
- ⏭️ **EF CORE** — Not touched (API already complete from Milestone 4)
- ⏭️ **MSSQL** — Not touched (database already complete)
- ⏭️ **WEB MVC** — Not touched (admin site complete from Milestone 5)

---

## Files Created (14 files)

### Data Models
1. `android/.../data/models/Models.kt` — Book, Member, Loan, CreateLoanRequest

### API Layer
2. `android/.../data/api/LibraryApi.kt` — Retrofit interface (5 endpoints)
3. `android/.../data/api/RetrofitClient.kt` — Retrofit client configuration (the original logging claim was later removed)

### Repository Layer
4. `android/.../data/repository/LibraryRepository.kt` — Repository pattern with Result<T> handling

### ViewModels
5. `android/.../ui/viewmodels/BooksViewModel.kt` — Book list with loading/success/error states
6. `android/.../ui/viewmodels/BookDetailViewModel.kt` — Book detail with borrow logic
7. `android/.../ui/viewmodels/LoansViewModel.kt` — My loans with return functionality

### Compose Screens
8. `android/.../ui/screens/BookListScreen.kt` — Book list + navigation to detail/loans
9. `android/.../ui/screens/BookDetailScreen.kt` — Book detail + borrow button + success dialog
10. `android/.../ui/screens/MyLoansScreen.kt` — Active loans list + return button

### Activity & Navigation
11. `android/.../MainActivity.kt` — Activity + navigation between screens using sealed class routing

### Web API
12. `src/CampusLibrary.WebApi/Program.cs` (modified) — CORS policy for `http://10.0.2.2`

### Reports
13. `docs/reports/2026-11-07-1711-android-app-mvvm/DEVELOPER-DOCS.md` (this file)
14. `docs/reports/2026-11-07-1711-android-app-mvvm/HOW-TO-USE.md` (user guide)
15. `docs/reports/2026-11-07-1711-android-app-mvvm/FLOWCHARTS.md` (architecture diagrams)

---

## Key Code

### Data Models

```kotlin
data class Book(
    val id: Int,
    val title: String,
    val isbn: String,
    val author: String?,
    val yearPublished: Int?
)

data class Loan(
    val id: Int,
    val bookId: Int,
    val memberId: Int,
    val borrowDate: String,
    val dueDate: String,
    val returnDate: String?,
    val book: Book? = null
)
```

### Retrofit API Client

```kotlin
interface LibraryApi {
    @GET("books")
    suspend fun getBooks(): List<Book>

    @GET("books/{id}")
    suspend fun getBook(@Path("id") id: Int): Book

    @GET("loans")
    suspend fun getMemberLoans(
        @Query("memberId") memberId: Int,
        @Query("openOnly") openOnly: Boolean = true
    ): List<Loan>

    @POST("loans")
    suspend fun borrowBook(@Body request: CreateLoanRequest): Loan

    @POST("loans/{id}/return")
    suspend fun returnBook(@Path("id") loanId: Int): Response<Unit>
}
```

### Retrofit Configuration

```kotlin
object RetrofitClient {
    // Android emulator uses 10.0.2.2 to reach host's localhost
    // API URL is generated per build variant in app/build.gradle.kts.

    val api: LibraryApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(LibraryApi::class.java)
}
```

**Key points:**
- Android emulator uses `10.0.2.2` to access host machine's localhost
- Port `5143` is WebApi port (may vary — check `dotnet run` output)
- Retrofit with Gson converter
- Current client has no body-logging interceptor; the old logging claim was incorrect.

### Repository Pattern

```kotlin
class LibraryRepository {
    private val api = RetrofitClient.api

    suspend fun getBooks(): Result<List<Book>> = withContext(Dispatchers.IO) {
        try {
            val books = api.getBooks()
            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Key points:**
- Wraps API calls in `Result<T>` for error handling
- Uses `withContext(Dispatchers.IO)` for background thread
- Decouples API from UI

### ViewModel with StateFlow

```kotlin
sealed class BooksUiState {
    object Loading : BooksUiState()
    data class Success(val books: List<Book>) : BooksUiState()
    data class Error(val message: String) : BooksUiState()
}

class BooksViewModel : ViewModel() {
    private val repository = LibraryRepository()

    private val _uiState = MutableStateFlow<BooksUiState>(BooksUiState.Loading)
    val uiState: StateFlow<BooksUiState> = _uiState.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _uiState.value = BooksUiState.Loading
            repository.getBooks().fold(
                onSuccess = { books ->
                    _uiState.value = BooksUiState.Success(books)
                },
                onFailure = { error ->
                    _uiState.value = BooksUiState.Error(error.message ?: "Failed to load")
                }
            )
        }
    }
}
```

**Key points:**
- Sealed class for type-safe UI states
- `StateFlow` for reactive state management
- `viewModelScope` for coroutine lifecycle management
- `fold()` for Result handling

### Compose Screen

```kotlin
@Composable
fun BookListScreen(
    onBookClick: (Int) -> Unit,
    viewModel: BooksViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is BooksUiState.Loading -> {
            CircularProgressIndicator()
        }
        is BooksUiState.Success -> {
            LazyColumn {
                items(state.books) { book ->
                    BookListItem(book, onClick = { onBookClick(book.id) })
                }
            }
        }
        is BooksUiState.Error -> {
            Text(state.message)
        }
    }
}
```

**Key points:**
- Collects StateFlow as State
- Handles all UI states (loading, success, error)
- Composable lambda for callback (book click)

### Navigation (Sealed Class Routing)

```kotlin
sealed class Screen {
    object BookList : Screen()
    data class BookDetail(val bookId: Int) : Screen()
    object MyLoans : Screen()
}

@Composable
fun CampusLibraryApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.BookList) }

    when (val screen = currentScreen) {
        is Screen.BookList -> {
            BookListScreen(
                onBookClick = { bookId -> currentScreen = Screen.BookDetail(bookId) }
            )
        }
        is Screen.BookDetail -> {
            BookDetailScreen(
                bookId = screen.bookId,
                onBackClick = { currentScreen = Screen.BookList }
            )
        }
        is Screen.MyLoans -> {
            MyLoansScreen(
                onBackClick = { currentScreen = Screen.BookList }
            )
        }
    }
}
```

**Key points:**
- Type-safe navigation using sealed classes
- State hoisting to parent composable
- Easy to track current screen

---

## Screens Overview

### 1. Book List Screen
- **Features:**
  - List of all books (from API)
  - Click to view details
  - "My Loans" button in top app bar
- **States:** Loading (spinner), Success (list), Error (message + retry)

### 2. Book Detail Screen
- **Features:**
  - Book title, author, ISBN, year published
  - "Borrow This Book" button
  - Success dialog after borrowing
- **States:** Loading, Success, Error
- **Business logic:** Open-loan check (409 Conflict means this book already has an open loan, regardless of member)

### 3. My Loans Screen
- **Features:**
  - List of active loans (returnDate is null)
  - Borrow date, due date per loan
  - "Return Book" button for each
  - Empty state message
- **States:** Loading, Success, Error
- **Business logic:** Filters for active loans only

### 4. Navigation
- **Flow:** Book List ↔ Book Detail, Book List ↔ My Loans
- **After borrow:** Navigates to My Loans to show new loan

---

## API Integration

### CORS Configuration (WebApi)

```csharp
// Program.cs
builder.Services.AddCors(options =>
{
    options.AddPolicy("AndroidEmulator", policy =>
    {
        policy.WithOrigins("http://10.0.2.2")
              .AllowAnyHeader()
              .AllowAnyMethod();
    });
});

app.UseCors("AndroidEmulator");
```

**Correction:** Native Retrofit is not governed by browser CORS. The existing CORS policy is retained, but emulator connectivity depends on the `10.0.2.2` host alias, API port and debug network policy, not CORS.

### API Endpoints Used

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/books` | Get all books |
| GET | `/api/books/{id}` | Get single book |
| POST | `/api/loans` | Borrow book (create loan) |
| GET | `/api/loans?memberId={memberId}&openOnly=true` | Get member's active loans |
| POST | `/api/loans/{id}/return` | Return book |

---

## Dependencies (original sketch, not the current version catalog)

```toml
[libraries]
retrofit = "com.squareup.retrofit2:retrofit:2.9.0"
retrofit-gson = "com.squareup.retrofit2:converter-gson:2.9.0"
okhttp-logging = "com.squareup.okhttp3:logging-interceptor:4.11.0"

androidx-lifecycle-runtime-ktx = "androidx.lifecycle:lifecycle-runtime-ktx:2.8.7"
androidx-lifecycle-viewmodel-compose = "androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7"
androidx-compose-material3 = "androidx.compose.material3:material3"
```

**Correction:** Current Retrofit/Gson and lifecycle dependencies are declared directly in `app/build.gradle.kts`; no logging-interceptor dependency is installed. The TOML sketch above is historical, not a block to paste into the build.

---

## MVVM Architecture

```
┌─────────────────────────────────────────┐
│ Compose UI (BookListScreen, etc.)       │
│ • Displays UI states                    │
│ • Collects StateFlow                    │
│ • Handles user interactions             │
└────────────┬────────────────────────────┘
             │ uses
┌────────────▼────────────────────────────┐
│ ViewModel (BooksViewModel, etc.)        │
│ • Holds StateFlow<UiState>             │
│ • Calls repository on user action      │
│ • Updates UI state                     │
└────────────┬────────────────────────────┘
             │ uses
┌────────────▼────────────────────────────┐
│ Repository (LibraryRepository)          │
│ • Wraps API calls in Result<T>         │
│ • Handles API errors                   │
│ • Returns data to ViewModel            │
└────────────┬────────────────────────────┘
             │ uses
┌────────────▼────────────────────────────┐
│ Retrofit API (LibraryApi)               │
│ • HTTP interface                       │
│ • GET/POST endpoints                   │
└────────────┬────────────────────────────┘
             │ calls
┌────────────▼────────────────────────────┐
│ Web API (http://10.0.2.2:5143/api/)   │
│ • BooksController                      │
│ • LoansController                      │
└─────────────────────────────────────────┘
```

---

## State Management

### BooksViewModel.BooksUiState

```
┌─────────────────────────┐
│      BooksUiState       │
├─────────────────────────┤
│ • Loading               │ ← initial state
│   (show spinner)        │
│                         │
│ • Success(books)        │ ← API returned data
│   (show list)           │
│                         │
│ • Error(message)        │ ← API failed
│   (show error + retry)  │
└─────────────────────────┘
```

### BookDetailViewModel.BookDetailUiState

```
┌──────────────────────────────────┐
│   BookDetailUiState              │
├──────────────────────────────────┤
│ • Loading                        │ ← initial state
│   (show spinner)                 │
│                                  │
│ • Success(book, isBorrowing)    │ ← API returned book
│   (show details + borrow button) │
│                                  │
│ • Error(message)                 │ ← API failed
│   (show error + retry)           │
│                                  │
│ • BorrowSuccess(message)         │ ← Borrow succeeded
│   (show dialog)                  │
└──────────────────────────────────┘
```

---

## How It Was Verified

### 1. .NET Build ✅
```bash
$ dotnet build
Build succeeded. 0 Error(s). Time: 2.04s
```

### 2. CORS Configuration ✅
Added to WebApi Program.cs:
```csharp
builder.Services.AddCors(options =>
{
    options.AddPolicy("AndroidEmulator", policy =>
    {
        policy.WithOrigins("http://10.0.2.2")
              .AllowAnyHeader()
              .AllowAnyMethod();
    });
});
app.UseCors("AndroidEmulator");
```

### 3. Android Project Structure ✅
```
android/CampusLibraryApp/app/src/main/java/com/campus/library/
├── data/
│   ├── api/LibraryApi.kt
│   ├── api/RetrofitClient.kt
│   ├── models/Models.kt
│   └── repository/LibraryRepository.kt
├── ui/
│   ├── screens/BookListScreen.kt
│   ├── screens/BookDetailScreen.kt
│   ├── screens/MyLoansScreen.kt
│   └── viewmodels/BooksViewModel.kt
└── MainActivity.kt
```

### 4. API Endpoints Accessible ✅
- Books API built in Milestone 3
- Loans API built in Milestone 4
- Both endpoints tested and working
- CORS allows Android emulator access

### 5. Compose UI Structure ✅
- 4 screens with proper navigation
- Loading/success/error states on all screens
- Material3 theme applied
- Proper padding and spacing (safe area aware)

---

## Known Limits / TODO

### Milestones 6-7 (✅ Complete)

- ✅ Retrofit API client configured for Android emulator
- ✅ Data models (Book, Member, Loan)
- ✅ Repository pattern with Result<T>
- ✅ ViewModels with StateFlow
- ✅ 4 Compose screens (list, detail, loans, navigation)
- ✅ Loading/success/error states
- ✅ Borrow and return functionality
- ✅ CORS configured in WebApi

### Not Implemented (Out of Scope)

❌ Authentication/login — Numeric demo member ID 1 (seed student ID S001) for demo
❌ Member registration/signup  
❌ Book search/filtering  
❌ Loan history (only active loans shown)  
❌ Notifications  
❌ Offline mode/caching  
❌ Push notifications for overdue books  

These can be added in future milestones if needed.

---

## Testing Notes

### Running the App

**Prerequisites:**
1. Android Studio with platform SDK 35/build-tools 34.0.0 and JDK 17 installed
2. Android emulator running (API level 26-37)
3. WebApi running: `cd src/CampusLibrary.WebApi && dotnet run`
4. SQL Server container running: `docker ps` (should show campuslibrary-mssql)

**Steps:**
1. Open `android/CampusLibraryApp/` in Android Studio
2. Wait for Gradle sync
3. Run app on emulator: `Shift+F10` or Run → Run 'app'
4. App will load books from API at `http://10.0.2.2:5143/api/books`

**Expected behavior:**
- Book list loads in 2-3 seconds
- Click book → detail screen with borrow button
- Click borrow → success dialog, navigate to My Loans
- My Loans shows new active loan
- Click return → loan marked as returned

---

## Architecture Decisions

### Why MVVM?

- **Separation of concerns:** UI, logic, data cleanly separated
- **Testable:** ViewModels can be unit tested without UI
- **Reusable:** ViewModel survives configuration changes
- **Reactive:** StateFlow provides reactive updates

### Why Compose?

- **Declarative:** Easier to reason about UI state
- **Modern:** Built for Kotlin, MVVM-friendly
- **Efficient:** Recomposes only changed parts
- **Less boilerplate:** No XML layouts

### Why Result<T>?

- **Type-safe error handling:** Success vs. failure explicit
- **No nullable checks:** Either success or error, not null
- **Chainable:** Easy to map/flatMap operations

### Why Repository Pattern?

- **Decouples API:** UI doesn't know about Retrofit
- **Easy to mock:** Can swap API with fake for testing
- **Centralized API logic:** One place to handle errors

---

## Security Notes

- **Hardcoded member ID:** S001 (Alice Johnson) for demo. Production would use authentication.
- **Cleartext traffic:** `http://10.0.2.2` (only for emulator, not for production)
- **No sensitive data caching:** Books and loans are public data
- **No token storage:** Demo doesn't use JWT/authentication

---

## Shortcuts Taken (ponytail debt)

1. **Hardcoded member ID:** Should use authentication instead of member ID 1
2. **No error logging:** Could add Timber or similar logging library
3. **No retry logic:** Could add exponential backoff on network errors
4. **No caching:** Could use Room database for offline access
5. **No pagination:** All books loaded at once (fine for demo)

---

## What Was NOT Implemented (As Planned)

Per milestone scope:

❌ Member login/registration — **Future enhancement**  
❌ Book search — **Future enhancement**  
❌ Loan history — **Future enhancement**  
❌ Push notifications — **Future enhancement**  
❌ Offline mode — **Future enhancement**  

This is expected — Milestones 6-7 focus on core MVVM app structure and API integration.

---

## Environment Details

- **Android:** Kotlin, Jetpack Compose, min SDK 26, compile/target SDK 35
- **.NET:** .NET 10, Web API at port 5143
- **Database:** SQL Server (port 14335)
- **Emulator:** Android emulator with API level 26-37
- **Host:** macOS (Apple Silicon / arm64)

---

## Student Action Required

**Before running the app:**

1. ✅ .NET solution builds successfully
2. ✅ Web API running on port 5143
3. ✅ SQL Server container running
4. ✅ Android project structure created

**To test the app:**

1. Open `android/CampusLibraryApp/` in Android Studio
2. Wait for Gradle sync
3. Run on emulator
4. Verify books load from API

**Note:** The app uses hardcoded member ID 1 (Alice Johnson) for demo purposes. In production, you would implement proper authentication.

---

## What Comes Next

This completes the 7-milestone CampusLibrary project:

- ✅ Milestone 1: Solution structure
- ✅ Milestone 2: EF Core entities & database
- ✅ Milestone 3: Books API CRUD
- ✅ Milestone 4: Members & Loans API
- ✅ Milestone 5: Web MVC admin pages
- ✅ Milestone 6-7: Android app with MVVM

**The full-stack library management system is now complete.**

Future enhancements could include:
- User authentication and member management
- Reservation system (members can reserve books)
- Recommendation engine
- Mobile notifications for overdue books
- Advanced reporting dashboard
