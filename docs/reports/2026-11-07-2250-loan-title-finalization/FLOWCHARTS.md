# CampusLibrary — Loan titles and borrower flows

## UI logic

```mermaid
flowchart TD
    A[Open My loans] --> B[GET /api/loans?memberId=1&openOnly=true]
    B --> C{Request succeeds?}
    C -->|No| D[Show error and retry]
    D --> B
    C -->|Yes| E[Repository includes related Book]
    E --> F[API returns bookTitle/bookAuthor and loan dates]
    F --> G{Any active loans?}
    G -->|No| H[Show empty state]
    G -->|Yes| I{Title populated?}
    I -->|Yes| J[Display actual title and author]
    I -->|No: older API| K[Use nested book or Book ID fallback]
    J --> L[Show due date and Return button]
    K --> L
    L --> M{Return selected?}
    M -->|No| N[Continue browsing]
    M -->|Yes| O[POST /api/loans/id/return]
    O --> P{Return successful?}
    P -->|Yes| Q[Reload loans and refresh availability]
    P -->|No| R[Keep loan visible and show error]
```

## Layer sequence

```mermaid
sequenceDiagram
    actor User
    participant UI as MyLoansScreen
    participant VM as LoansViewModel
    participant HTTP as Retrofit Repository
    participant API as LoansController
    participant S as LoanService
    participant R as LoanRepository
    participant SQL as MSSQL
    User->>UI: Open My loans
    UI->>VM: loadLoans()
    VM->>HTTP: getMemberLoans(DemoMember.ID)
    HTTP->>API: GET /api/loans?memberId=1&openOnly=true
    API->>S: GetAllLoansAsync(1, true)
    S->>R: GetAllAsync(1, true)
    R->>SQL: Read Loans JOIN Books
    SQL-->>R: Loan with related Book
    R-->>S: Loan rows
    S-->>API: LoanDto including BookTitle and BookAuthor
    API-->>HTTP: 200 JSON
    HTTP-->>VM: Result.success
    VM-->>UI: StateFlow Success
    UI-->>User: Book title, author and dates
    User->>UI: Return selected book
    UI->>VM: returnBook(loanId)
    VM->>HTTP: returnBook(loanId)
    HTTP->>API: POST /api/loans/id/return
    API->>S: ReturnBookAsync
    S->>R: ReturnAsync
    R->>SQL: Conditional UPDATE ReturnDate
    SQL-->>API: 204 via layers, or conflict
    API-->>HTTP: 204 / 409
    HTTP-->>VM: Result
    VM-->>UI: Refresh or retain loan with error
```

No schema change; the existing Books–Loans foreign key and filtered unique index remain authoritative. An additional ER diagram is unnecessary.
