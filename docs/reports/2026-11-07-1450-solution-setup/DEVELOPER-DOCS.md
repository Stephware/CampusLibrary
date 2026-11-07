# Milestone 1 — Solution setup developer record

## Summary and provenance

The indexed Milestone 1 report folder contained HOW-TO-USE.md and FLOWCHARTS.md, but its developer document was stored separately at [06:50 DEVELOPER-DOCS.md](../2026-11-07-0650-solution-setup/DEVELOPER-DOCS.md).
This document repairs the indexed folder without deleting, retiming or silently rewriting the original evidence.
The original developer record says 06:50; the index/folder says 14:50. The available records do not establish why they differ.
No setup commands or historical tests were rerun by this documentation repair; reported results below are attributed to that original record.

## Projects touched at Milestone 1

EF CORE/Data, WEB API, WEB MVC, ANDROID KOTLIN project scaffolding; MSSQL container setup. No domain application implementation was claimed at setup time.

## Files changed at Milestone 1

This is a reconstruction from the original report, not a new Git diff. Its complete file table remains in the linked 06:50 record.

| Path | Added/modified/deleted at setup | Why |
|---|---|---|
| `CampusLibrary.sln` (original report name; current file is `CampusLibrary.slnx`) | added | Groups the three .NET projects |
| `src/CampusLibrary.Data/CampusLibrary.Data.csproj` | added | net10.0 class library scaffold |
| `src/CampusLibrary.WebApi/` | added | net10.0 API template with Data reference |
| `src/CampusLibrary.WebMvc/` | added | net10.0 MVC template with Data reference |
| `android/CampusLibraryApp/` | added | Kotlin/Compose scaffold and Gradle configuration |
| `.gitignore`, `docs/reports/INDEX.md`, `tasks/plan.md`, `tasks/todo.md` | added | Exclusions, report navigation and setup planning |
| `CLAUDE.md`, `SPEC.md` | modified | Architecture and development guidance |
| `docs/reports/2026-11-07-1450-solution-setup/DEVELOPER-DOCS.md` | added during later documentation repair | Completes this indexed folder and records the split-report provenance |

## Key configuration

Both web projects reference Data:

```xml
<ProjectReference Include="..\CampusLibrary.Data\CampusLibrary.Data.csproj" />
```

All three .NET project files currently contain:

```xml
<TargetFramework>net10.0</TargetFramework>
```

The original Android target SDK 37 and missing-wrapper assumptions were superseded by the 18:17 review: current compile/target SDK is 35, min SDK 26, AGP 8.7.3, Kotlin 2.1.0, Gradle wrapper 8.9, JDK 17. See [SPEC current implementation notes](../../../SPEC.md#current-implementation-versus-the-original-setup-plan).

## Database changes

At Milestone 1, the original report claimed a running SQL Edge container on host port 14335 but no database schema or migrations yet. Later milestones created the schema and seed data. This repair adds no migration and changes no data or infrastructure. SQL Edge is now retired; the historical container choice is not a recommendation for supported deployment.

## How it was verified — historical attribution

The 06:50 record reports `dotnet build` succeeded with zero warnings/errors in 1.97 seconds, `dotnet sln list` listed three projects, `docker ps` showed the container running on 14335, and `git branch --show-current` returned main. Those are original claims, not newly observed results.

That same record explicitly required manual Android Studio/build verification; it did not record a successful Android build. Later checked setup-plan boxes and HOW-TO-USE expected outputs must not be treated as executed Android tests. Actual later Android build/runtime evidence is recorded in the [18:17 review](../2026-11-07-1817-final-code-review/DEVELOPER-DOCS.md).

## Known limits / TODO

- The 06:50/14:50 timestamp mismatch is preserved, not guessed away.
- Original scaffold filenames, ports, proposed schema and future tasks describe setup time, not the current application.
- Current startup and verification guidance belongs to the newest report linked in [INDEX.md](../INDEX.md).
- Documentation repair itself does not establish fresh API, MVC, emulator or SQL health.
