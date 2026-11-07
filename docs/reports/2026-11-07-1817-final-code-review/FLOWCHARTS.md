# CampusLibrary — Final review flowcharts

## Validated application behavior

```mermaid
flowchart TD
    A[Client submits input] --> B{Valid required fields, lengths and IDs?}
    B -->|No| E400[400 validation problem or MVC field errors]
    B -->|Yes| C[Service normalizes and applies rules]
    C --> D{Book update?}
    D -->|Yes| I[Check ISBN in SQL, excluding current ID]
    I -->|Another row owns ISBN| E409[409 or form conflict]
    I -->|Unique| R[Repository persists through EF]
    D -->|Borrow| O{Book and member exist and book open?}
    O -->|Missing| E404[404]
    O -->|Already borrowed| E409
    O -->|Available| R
    R --> DB[MSSQL indexes and foreign keys]
    DB -->|Concurrent duplicate or protected history| E409
    DB -->|Saved| OK[Success DTO or redirect]
    A -->|Android detail navigation| L[Cancel previous load; display current book]
    L --> G{Borrowed ID equals displayed ID?}
    G -->|No| STOP[Do not send borrow]
    G -->|Yes| C
    A -->|Return loan| U[Conditional update only while ReturnDate is null]
    U -->|First successful return| N[204 No Content]
    N --> AND[Android accepts bodyless success and refreshes loans]
    U -->|Already returned| E409
```

## Cross-component borrowing and return

```mermaid
sequenceDiagram
    actor User
    participant UI as Compose screen
    participant VM as ViewModel / StateFlow
    participant Client as Repository / Retrofit
    participant API as API Controller
    participant S as Service
    participant R as Data Repository / EF Core
    participant DB as MSSQL
    User->>UI: Open book and choose Borrow
    UI->>VM: Load latest detail; borrow displayed ID
    VM->>VM: Cancel stale load and check ID match
    VM->>Client: borrowBook(bookId, member1)
    Client->>API: POST /api/loans
    API->>S: Validate DTO and borrow
    S->>R: Check book/member/open loan
    R->>DB: Queries then constrained INSERT
    DB-->>API: Saved loan via repository/service
    API-->>Client: 201 LoanDto
    Client-->>VM: Result success
    VM-->>UI: Current BorrowSuccess state
    User->>UI: Open My Loans
    UI->>VM: Reload on screen entry
    VM->>Client: getMemberLoans(1)
    Client->>API: GET /api/loans?memberId=1&openOnly=true
    API-->>UI: Active loans via service/repository/client/state
    User->>UI: Return book
    Client->>API: POST /api/loans/id/return
    API->>S: Return loan
    S->>R: Conditional ReturnDate update
    R->>DB: UPDATE where ReturnDate is null
    DB-->>API: One affected row via repository/service
    API-->>Client: 204 without body
    Client-->>VM: Result<Unit> success
    VM-->>UI: Reload active loans
```

MVC uses Controller → MVC Service → the same Data Repository/EF → MSSQL directly; it does not require an API process.

## Verification and deployment boundary

```mermaid
flowchart TD
    A[Review each existing component] --> B[Reproduce confirmed defects]
    B --> C[Apply focused fixes]
    C --> D[Build Debug and Release]
    D --> E[Run SQL integration and Kotlin regressions]
    E --> F[Check browser and emulator flows]
    F --> G[Audit dependencies and publish artifacts]
    G --> H[Stop test apps and drop only isolated database]
    H --> I[Write reports and remaining limitations]
    I --> J[Local educational workflows verified]
    J --> K{Deployment prerequisites complete?}
    K -->|No| L[Supported SQL host, auth scope, trusted HTTPS and signing remain]
    K -->|Yes, separately verified| M[Deployment decision]
```

No database schema changed; existing entity relationships and the open-loan filtered unique index remain as documented in Milestones2/4.
