# Milestone 4 — Flowcharts

> Historical milestone record. Statements and verification below describe the original task, not a fresh run. For current setup and known limitations, use [the latest report index](../INDEX.md).

## Feature logic

```mermaid
flowchart TD
    A[HTTP request] --> B{Valid DTO and positive IDs?}
    B -->|No| E400[400 ValidationProblemDetails]
    B -->|Yes| C{Operation}
    C -->|Member create or update| M[Trim fields and check uniqueness]
    M --> M1{Duplicate email or student ID?}
    M1 -->|Yes, including SQL race| E409[409 Conflict]
    M1 -->|No| M2[Save member; create assigns registration date]
    M2 --> OK[200 / 201 / 204]
    C -->|Delete member or book| D{Any loan history?}
    D -->|Yes| E409
    D -->|No| D1[Delete with foreign-key protection]
    D1 --> OK
    C -->|Borrow| F{Book and member exist?}
    F -->|No| E404[404 Not Found]
    F -->|Yes| G{Book has open loan?}
    G -->|Yes| E409
    G -->|No| H[Set UTC borrow date and due date 14 days later]
    H --> I[Insert with unique filtered index]
    I -->|Concurrent borrow wins first| E409
    I -->|Saved| OK
    C -->|Return| J{Loan exists?}
    J -->|No| E404
    J -->|Yes| K{Already returned?}
    K -->|Yes| E409
    K -->|No| L[Update ReturnDate only where still null]
    L -->|Another return wins| E409
    L -->|One row changed| OK
    C -->|Read| Q[Query DTOs; filter loans by member and open state]
    Q --> OK
    D1 -->|Row concurrently deleted| E404
    M2 -->|Row concurrently deleted| E404
```

Missing detail/update/delete targets return 404; a loan list filter with no matches returns an empty 200 list. Unexpected failures return generic 500 ProblemDetails and log server-side.

## Layer sequence

```mermaid
sequenceDiagram
    actor User as API client
    participant C as LoansController
    participant S as LoanService
    participant R as Repositories
    participant EF as LibraryDbContext / EF Core
    participant DB as MSSQL
    User->>C: POST /api/loans with bookId and memberId
    C->>C: Validate request
    C->>S: BorrowBookAsync(dto)
    S->>R: Find book, member and open loan
    R->>EF: Async LINQ queries
    EF->>DB: SELECT
    DB-->>S: Results via EF and repositories
    alt Missing book or member
        S-->>C: KeyNotFoundException
        C-->>User: Handler returns 404 ProblemDetails
    else Book unavailable
        S-->>C: InvalidOperationException
        C-->>User: Handler returns 409 ProblemDetails
    else Available
        S->>S: Assign UTC dates, due in 14 days
        S->>R: AddAsync(loan)
        R->>EF: SaveChangesAsync
        EF->>DB: INSERT guarded by unique open-loan index
        alt Concurrent insert already borrowed book
            DB-->>R: Unique index violation
            R-->>C: Conflict through service and handler
            C-->>User: 409 ProblemDetails
        else Insert succeeds
            DB-->>S: Persisted loan via EF and repository
            S-->>C: LoanDto
            C-->>User: 201 Created with Location
        end
    end
    User->>C: POST /api/loans/{id}/return
    C->>S: ReturnBookAsync(id)
    S->>R: GetByIdAsync; then ReturnAsync(id, utcNow)
    R->>EF: ExecuteUpdateAsync where ReturnDate is null
    EF->>DB: Conditional UPDATE
    DB-->>S: Affected row count via EF and repository
    S-->>C: Success or already-returned conflict
    C-->>User: 204 or 409; missing loan is 404
```

Members use the same Controller → Service → Repository → EF Core → MSSQL sequence. The exception handler maps service failures to HTTP responses; controllers never access DbContext.

## Database relationships

```mermaid
erDiagram
    BOOK ||--o{ LOAN : has_history
    MEMBER ||--o{ LOAN : borrows
    BOOK {
        int Id PK
        string Title
        string Isbn UK
    }
    MEMBER {
        int Id PK
        string Name
        string Email UK
        string StudentId UK
        datetime RegisteredDate
    }
    LOAN {
        int Id PK
        int BookId FK
        int MemberId FK
        datetime BorrowDate
        datetime DueDate
        datetime ReturnDate "nullable"
    }
```

Migration `20261107074756_EnforceOneOpenLoanPerBook` changes the Loans.BookId index to UNIQUE WHERE ReturnDate IS NULL. Multiple historical returned loans remain allowed. Existing restrictive foreign keys retain book/member loan history. No columns or tables changed.
