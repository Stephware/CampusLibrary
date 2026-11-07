# AI Development Guide — ASP.NET Core MVC

## 1. Purpose

This guide defines the standards and instructions an AI assistant must follow when generating, modifying, or reviewing code for this project.

The application uses:

- C#
- ASP.NET Core MVC
- Entity Framework Core
- SQL Server
- Service–Repository Pattern

The AI must follow the existing project architecture and coding conventions instead of introducing unrelated architectural patterns.

---

# 2. Core Architecture

The application must follow this flow:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Entity Framework Core / DbContext
    ↓
SQL Server
```

### Responsibilities

#### Controller

Controllers are responsible for:

- Receiving HTTP requests.
- Validating request-level input.
- Calling the appropriate service.
- Returning Views, JSON, redirects, or appropriate HTTP responses.
- Handling MVC-specific concerns.

Controllers should **not**:

- Directly access `DbContext`.
- Directly query Entity Framework entities.
- Contain business logic.
- Contain complex database operations.

Example:

```csharp
public async Task<IActionResult> Index()
{
    var data = await _sitePricingService.GetAllAsync();

    return View(data);
}
```

---

## 3. Service Layer

Services contain business logic and application-level operations.

Services are responsible for:

- Applying business rules.
- Coordinating multiple repository calls.
- Transforming data when appropriate.
- Updating entity properties based on business requirements.
- Calling repositories to persist changes.

Example:

```csharp
public async Task<List<TblSitePricing>> UpdatePricingAsync(
    List<TblSitePricing> pricingList)
{
    foreach (var pricing in pricingList)
    {
        pricing.ApprovedPrice = pricing.SuggestedPrice;
    }

    return await _repository.UpdateRangeAsync(pricingList);
}
```

The service should not create raw SQL queries unless the existing project explicitly requires it.

---

# 4. Repository Layer

Repositories are responsible for database access.

Repositories are responsible for:

- Querying entities.
- Adding entities.
- Updating entities.
- Deleting entities.
- Saving changes when that is the established project convention.
- Encapsulating Entity Framework Core database operations.

Example:

```csharp
public async Task<List<TblSitePricing>> GetAllAsync()
{
    return await _context.TblSitePricing
        .ToListAsync();
}
```

Update example:

```csharp
public async Task<List<TblSitePricing>> UpdateRangeAsync(
    List<TblSitePricing> entities)
{
    _context.TblSitePricing.UpdateRange(entities);

    await _context.SaveChangesAsync();

    return entities;
}
```

---

# 5. Entity Framework Core Rules

Use Entity Framework Core for database operations.

Prefer:

```csharp
_context.TblSitePricing
```

over manually constructed SQL when EF Core can perform the operation cleanly.

Use asynchronous database operations:

```csharp
ToListAsync()
FirstOrDefaultAsync()
SingleOrDefaultAsync()
AnyAsync()
CountAsync()
SaveChangesAsync()
```

Avoid synchronous database calls such as:

```csharp
.ToList()
.First()
.SaveChanges()
```

when an asynchronous equivalent is available.

---

# 6. Query Guidelines

Build queries in the repository layer.

Example:

```csharp
public async Task<List<TblSitePricing>> GetBySiteIdAsync(int siteId)
{
    return await _context.TblSitePricing
        .Where(x => x.SiteId == siteId)
        .ToListAsync();
}
```

Use projection when only specific fields are required:

```csharp
var result = await _context.TblSitePricing
    .Where(x => x.SiteId == siteId)
    .Select(x => new
    {
        x.SiteId,
        x.FuelId,
        x.ApprovedPrice
    })
    .ToListAsync();
```

Do not load an entire entity when only a small subset of data is required unless the entity itself is needed.

---

# 7. Entity Updates

When an entity is retrieved from the repository and passed to another application layer, that layer may modify the entity properties.

Example flow:

```text
Repository
    ↓
List<TblSitePricing>
    ↓
Service
    ↓
Modify properties
    ↓
Repository
    ↓
UpdateRange()
    ↓
SaveChangesAsync()
```

Example:

```csharp
var pricingList = await _repository.GetPricingAsync();

foreach (var pricing in pricingList)
{
    pricing.ApprovedPrice = pricing.SuggestedPrice;
}

await _repository.UpdateRangeAsync(pricingList);
```

If the entities are tracked by the same `DbContext`, avoid unnecessary calls to `Attach()` or `Update()` for every individual entity.

Use `UpdateRange()` when appropriate for detached entities or when the project's established repository implementation requires it.

---

# 8. LINQ Guidelines

Use LINQ for collection and database operations.

For database queries, make sure the expression can be translated by Entity Framework Core.

Example:

```csharp
var pricingData = pricingList
    .Select(x => new
    {
        FuelPrice = (x.ApprovedPrice ?? x.SuggestedPrice) * 100M,
        FuelCode = productList
            .Where(z => z.PfsProductId == x.PfsProductId)
            .Select(z => z.PfsProduct.QldProductIdentifier)
            .FirstOrDefault()
    })
    .ToList();
```

When working with a `List<T>`, LINQ methods such as `Select`, `Where`, `FirstOrDefault`, and `ToList` are valid.

Do not incorrectly assume a collection is `IEnumerable<dynamic>` if its actual type is a strongly typed list.

Prefer strongly typed models whenever possible.

---

# 9. Dependency Injection

Use ASP.NET Core dependency injection.

Example:

```csharp
public class PricingService : IPricingService
{
    private readonly IPricingRepository _repository;

    public PricingService(IPricingRepository repository)
    {
        _repository = repository;
    }
}
```

Register services and repositories in the application's dependency injection configuration.

Example:

```csharp
builder.Services.AddScoped<IPricingService, PricingService>();
builder.Services.AddScoped<IPricingRepository, PricingRepository>();
```

Do not instantiate services or repositories manually:

```csharp
var service = new PricingService(...);
```

---

# 10. Interfaces

Use interfaces for services and repositories.

Example:

```csharp
public interface IPricingRepository
{
    Task<List<TblSitePricing>> GetAllAsync();

    Task<List<TblSitePricing>> UpdateRangeAsync(
        List<TblSitePricing> entities);
}
```

Service:

```csharp
public interface IPricingService
{
    Task<List<TblSitePricing>> GetAllAsync();
}
```

Implementations:

```csharp
public class PricingRepository : IPricingRepository
{
}
```

```csharp
public class PricingService : IPricingService
{
}
```

---

# 11. MVC Rules

Views should be responsible for presentation.

Do not place database queries inside Razor views.

Avoid:

```csharp
@inject AppDbContext DbContext
```

and querying the database directly from the view.

Instead:

```text
Controller → Service → Repository
```

The controller should provide the required model to the view.

---

# 12. View Models

Use ViewModels when the view requires data that does not directly represent a database entity.

Example:

```csharp
public class PricingViewModel
{
    public int SiteId { get; set; }

    public string SiteName { get; set; }

    public decimal Price { get; set; }
}
```

Avoid exposing database entities directly to the UI when doing so creates unnecessary coupling.

---

# 13. API / HTTP Calls

When the application communicates with an external API:

```text
Controller
    ↓
Service
    ↓
API Client
    ↓
External API
```

Do not put external API calls directly inside controllers.

Use `HttpClient` through dependency injection.

Example:

```csharp
public class PricingApiClient : IPricingApiClient
{
    private readonly HttpClient _httpClient;

    public PricingApiClient(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }
}
```

---

# 14. Configuration

Application configuration should be stored in configuration files and accessed through configuration/options patterns.

Example:

```json
{
  "API": {
    "PRICING": {
      "BASE_URL": "https://example.com"
    }
  }
}
```

Do not hard-code URLs, API keys, passwords, or other environment-specific values in C# source code.

Secrets should not be committed to source control.

---

# 15. Error Handling

Use appropriate exception handling.

Do not silently swallow exceptions:

```csharp
try
{
    // operation
}
catch
{
}
```

If an exception is handled, log it or handle it meaningfully.

Example:

```csharp
try
{
    await _service.ProcessAsync();
}
catch (Exception ex)
{
    _logger.LogError(ex, "Error processing pricing data.");

    throw;
}
```

Do not expose sensitive exception details to end users.

---

# 16. Logging

Use the application's existing logging framework.

Prefer structured logging:

```csharp
_logger.LogInformation(
    "Processing pricing for SiteId {SiteId}",
    siteId);
```

Avoid:

```csharp
_logger.LogInformation(
    "Processing pricing for SiteId " + siteId);
```

Do not log:

- Passwords
- API keys
- Access tokens
- Connection strings
- Sensitive personal information

---

# 17. Naming Conventions

Use standard C# naming conventions.

### Classes

```csharp
PricingService
PricingRepository
PricingViewModel
```

### Interfaces

```csharp
IPricingService
IPricingRepository
```

### Methods

```csharp
GetPricingAsync()
UpdatePricingAsync()
DeletePricingAsync()
```

### Variables

```csharp
pricingList
siteId
approvedPrice
```

### Private fields

```csharp
private readonly IPricingRepository _repository;
```

---

# 18. Async/Await

Use asynchronous programming for I/O operations.

Preferred:

```csharp
public async Task<List<TblSitePricing>> GetPricingAsync()
{
    return await _repository.GetPricingAsync();
}
```

Avoid blocking asynchronous calls:

```csharp
var result = service.GetPricingAsync().Result;
```

or:

```csharp
var result = service.GetPricingAsync().GetAwaiter().GetResult();
```

---

# 19. Database Performance

Avoid unnecessary database calls.

Bad:

```csharp
foreach (var site in sites)
{
    var pricing = await repository.GetPricingBySiteIdAsync(site.Id);
}
```

This can produce an N+1 query problem.

Prefer retrieving the required data in an appropriate query when practical.

Also consider:

- Filtering in SQL instead of memory.
- Projection.
- `AsNoTracking()` for read-only queries.
- Appropriate indexes.
- Avoiding unnecessary `Include()` calls.

Example:

```csharp
public async Task<List<TblSitePricing>> GetPricingAsync()
{
    return await _context.TblSitePricing
        .AsNoTracking()
        .Where(x => x.IsActive)
        .ToListAsync();
}
```

---

# 20. Existing Code Takes Priority

When modifying an existing project:

1. Inspect the existing architecture.
2. Follow existing naming conventions.
3. Reuse existing interfaces.
4. Reuse existing repositories and services.
5. Avoid creating duplicate abstractions.
6. Do not move interfaces or classes unless explicitly requested.
7. Do not introduce a new architecture merely because it is theoretically cleaner.

The goal is to make the requested change while minimizing unrelated changes.

---

# 21. Do Not Over-Engineer

Do not introduce unnecessary:

- MediatR
- CQRS
- Generic repositories
- Unit of Work
- AutoMapper
- Domain-driven design
- Event sourcing
- Additional projects
- Additional abstraction layers

unless the project already uses them or the user explicitly requests them.

The required architecture is:

```text
MVC Controller
      ↓
Service
      ↓
Repository
      ↓
Entity Framework Core
      ↓
SQL Server
```

---

# 22. AI Response Requirements

When generating code, the AI should:

1. Explain where each file/class belongs.
2. Show complete relevant code.
3. Keep examples compatible with the project's ASP.NET Core version.
4. Use C#.
5. Use Entity Framework Core.
6. Follow the Service–Repository Pattern.
7. Prefer async database operations.
8. Reuse existing project structures.
9. Avoid unrelated refactoring.
10. Point out assumptions when required.

When modifying existing code, show the changed section and clearly identify where it belongs.

---

# 23. How-To Guide Requirement

For implementation requests, the AI should provide a practical implementation guide.

The guide should generally contain:

```text
1. What will be changed
2. Project/file structure
3. Entity/model
4. Repository interface
5. Repository implementation
6. Service interface
7. Service implementation
8. Controller
9. View/ViewModel if required
10. Dependency Injection registration
11. Configuration if required
12. Database considerations
13. Testing steps
14. Common errors/troubleshooting
```

Only include sections relevant to the requested feature.

---

# 24. Standard Implementation Example

For a feature called `Pricing`:

```text
Controllers/
    PricingController.cs

Services/
    Interfaces/
        IPricingService.cs
    PricingService.cs

Repositories/
    Interfaces/
        IPricingRepository.cs
    PricingRepository.cs

Models/
    TblSitePricing.cs

ViewModels/
    PricingViewModel.cs

Views/
    Pricing/
        Index.cshtml
```

Flow:

```text
PricingController
        ↓
IPricingService
        ↓
IPricingRepository
        ↓
Entity Framework Core
        ↓
SQL Server
```

---

# 25. Final AI Checklist

Before providing a solution, verify:

- [ ] Is the solution ASP.NET Core MVC?
- [ ] Is the code written in C#?
- [ ] Is Entity Framework Core used for database access?
- [ ] Is the Service–Repository Pattern followed?
- [ ] Does the controller avoid direct database access?
- [ ] Does the service contain business logic?
- [ ] Does the repository contain database logic?
- [ ] Are async EF operations used?
- [ ] Are interfaces used for services/repositories?
- [ ] Is dependency injection used?
- [ ] Are secrets/configuration kept out of source code?
- [ ] Does the solution reuse the existing architecture?
- [ ] Does it avoid unnecessary architectural changes?
- [ ] Is the implementation guide included when requested?
- [ ] Are testing and troubleshooting steps included when relevant?

---

# 26. Priority Rules

When instructions conflict, use this priority:

1. Explicit user requirement.
2. Existing project architecture and conventions.
3. This AI development guide.
4. Standard ASP.NET Core / EF Core best practices.
5. General architectural preferences.

The AI should not override an explicit project requirement simply because another approach is considered more modern or theoretically cleaner.

---

# 27. Project outline


        