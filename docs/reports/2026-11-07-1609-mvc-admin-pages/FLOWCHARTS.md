# Milestone 5 — Flowcharts

## Admin request flow

```mermaid
flowchart TD
    A[Librarian opens admin site] --> B[Books catalog]
    B --> C{Choose a task}
    C -->|Add or edit book| F[Open labeled book form]
    C -->|Add member| M[Open member registration form]
    F --> P[Submit POST with anti-forgery token]
    M --> P
    P --> T{Valid anti-forgery token?}
    T -->|No| E400[400: open a fresh form]
    T -->|Yes| V{Valid input?}
    V -->|No| ERR[Redisplay entered fields and errors]
    V -->|Yes| S[Service applies uniqueness and normalization]
    S --> R[Repository persists through EF Core and MSSQL]
    R -->|Duplicate identifier| ERR
    R -->|Saved| OK[Redirect to list with success message]
    C -->|Delete book| D[Show book confirmation]
    D --> D1[Confirm anti-forgery-protected POST]
    D1 --> H{Has loan history?}
    H -->|Yes| KEEP[Keep record and show explanation]
    H -->|No| R
    C -->|Record return| L[Open loans ordered by due date]
    L --> RT[POST Return with token and route ID]
    RT --> AT[Service validates loan; repository updates only if open]
    AT -->|Returned| OK
    AT -->|Already returned| NOTICE[Refresh list with notice]
    AT -->|Missing| E404[404 with catalog recovery link]
    C -->|Record no longer exists| E404
```

Every POST validates its anti-forgery token. Controllers use route-only IDs, services own business decisions, and database constraints guard concurrent writes.

## Cross-layer sequence

```mermaid
sequenceDiagram
    actor L as Librarian browser
    participant C as MVC Controller
    participant S as Admin Service
    participant R as Data Repository
    participant EF as LibraryDbContext / EF Core
    participant DB as MSSQL
    L->>C: GET Books/Create
    C-->>L: Razor form with validation and anti-forgery token
    L->>C: POST book fields and token
    C->>C: Validate token and ModelState
    alt Invalid fields
        C-->>L: Form with retained values and field errors
    else Valid request
        C->>S: CreateAsync(viewModel)
        S->>R: Check unique ISBN
        R->>EF: AnyAsync
        EF->>DB: SELECT
        DB-->>S: Uniqueness result through EF and repository
        alt Duplicate
            S-->>C: Domain conflict
            C-->>L: Form with recovery message
        else Available ISBN
            S->>R: AddAsync(book)
            R->>EF: SaveChangesAsync
            EF->>DB: INSERT with unique index protection
            DB-->>C: Success through repository and service
            C-->>L: Redirect to Books with success feedback
        end
    end
    L->>C: GET Loans
    C->>S: GetOpenAsync
    S->>R: GetOpenWithDetailsAsync
    R->>EF: AsNoTracking with related book/member
    EF->>DB: One SELECT ordered by due date
    DB-->>C: Open loan ViewModels through repository/service
    C-->>L: Open loans with return forms
    L->>C: POST Loans/Return/id with token
    C->>S: ReturnAsync(routeId)
    S->>R: Validate loan and conditional ReturnAsync
    R->>EF: ExecuteUpdateAsync where ReturnDate is null
    EF->>DB: Atomic UPDATE
    DB-->>C: Success or conflict via service
    C-->>L: Redirect with success or already-returned notice
```

No database schema changed, so no new ER diagram is required. Existing Books/Members/Loans relationships and the one-open-loan-per-book filtered index remain as documented in Milestone 4.
