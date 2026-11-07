# CampusLibrary Tasks

> Historical Milestone 1 planning checklist, not current task status. The split setup report is repaired in [the canonical Milestone 1 folder](../docs/reports/2026-11-07-1450-solution-setup/DEVELOPER-DOCS.md). Current build facts are recorded in [SPEC.md](../SPEC.md#current-implementation-versus-the-original-setup-plan); SDK 37, host Java 26, absent wrapper, port 1433 and “no application code” statements below describe the original plan only.

**Milestone 1:** Solution Structure Setup  
**Date:** 2026-11-07  
**Status:** Ready to implement

---

## Phase 1: .NET Solution Structure

- [ ] Task 1.1: Create solution file
  - Acceptance: `CampusLibrary.sln` exists at repo root
  - Verify: `dotnet sln list` shows the solution
  - Files: `CampusLibrary.sln`

- [ ] Task 1.2: Create CampusLibrary.Data class library
  - Acceptance: Project created under `src/CampusLibrary.Data/` with net10.0 target
  - Verify: `dotnet build src/CampusLibrary.Data/CampusLibrary.Data.csproj` succeeds
  - Files: `src/CampusLibrary.Data/CampusLibrary.Data.csproj`, `src/CampusLibrary.Data/Class1.cs` (template default)

- [ ] Task 1.3: Create CampusLibrary.WebApi project
  - Acceptance: Web API project created under `src/CampusLibrary.WebApi/` with net10.0 target
  - Verify: `dotnet build src/CampusLibrary.WebApi/CampusLibrary.WebApi.csproj` succeeds
  - Files: `src/CampusLibrary.WebApi/CampusLibrary.WebApi.csproj`, `Program.cs`, `appsettings.json`, `appsettings.Development.json`

- [ ] Task 1.4: Create CampusLibrary.WebMvc project
  - Acceptance: MVC project created under `src/CampusLibrary.WebMvc/` with net10.0 target
  - Verify: `dotnet build src/CampusLibrary.WebMvc/CampusLibrary.WebMvc.csproj` succeeds
  - Files: `src/CampusLibrary.WebMvc/CampusLibrary.WebMvc.csproj`, `Program.cs`, `appsettings.json`, `appsettings.Development.json`, default MVC structure

- [ ] Task 1.5: Add projects to solution
  - Acceptance: All 3 projects listed in solution file
  - Verify: `dotnet sln list` shows Data, WebApi, WebMvc
  - Files: `CampusLibrary.sln` (modified)

- [ ] Task 1.6: Add project references
  - Acceptance: WebApi references Data, WebMvc references Data
  - Verify: Inspect `.csproj` files for `<ProjectReference>` elements
  - Files: `src/CampusLibrary.WebApi/CampusLibrary.WebApi.csproj`, `src/CampusLibrary.WebMvc/CampusLibrary.WebMvc.csproj` (modified)

- [ ] Task 1.7: Build solution
  - Acceptance: `dotnet build` at repo root succeeds with 0 errors
  - Verify: Command output shows "Build succeeded"
  - Files: None (verification only)

---

## Phase 2: SQL Server Container

- [ ] Task 2.1: Check if port 1433 is available
  - Acceptance: Port 1433 is free or we choose an alternative
  - Verify: `lsof -i :1433` returns nothing, or we document the alternative port
  - Files: None (verification only)

- [ ] Task 2.2: Pull Azure SQL Edge image
  - Acceptance: `mcr.microsoft.com/azure-sql-edge:latest` image exists locally
  - Verify: `docker images | grep azure-sql-edge`
  - Files: None (Docker image)

- [ ] Task 2.3: Start SQL Server container
  - Acceptance: Container `campuslibrary-mssql` running on port 1433
  - Verify: `docker ps` shows container in "Up" state
  - Files: None (Docker container)

- [ ] Task 2.4: Verify SQL Server connectivity
  - Acceptance: Can connect to `localhost:1433` with SA credentials
  - Verify: `docker exec campuslibrary-mssql /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P <your-local-password> -No -Q "SELECT @@VERSION"`
  - Files: None (verification only)

---

## Phase 3: Android Project Structure

- [ ] Task 3.1: Create Android directory structure
  - Acceptance: `android/CampusLibraryApp/app/src/main/` directories exist
  - Verify: `ls -R android/CampusLibraryApp/`
  - Files: Directory structure

- [ ] Task 3.2: Create root build.gradle.kts
  - Acceptance: Valid Gradle build file for Android project
  - Verify: File contains plugins and buildscript config
  - Files: `android/CampusLibraryApp/build.gradle.kts`

- [ ] Task 3.3: Create app/build.gradle.kts
  - Acceptance: Valid app module build file with com.campus.library package, min SDK 26, target SDK 37
  - Verify: File contains application plugin, dependencies (Compose, Retrofit)
  - Files: `android/CampusLibraryApp/app/build.gradle.kts`

- [ ] Task 3.4: Create settings.gradle.kts
  - Acceptance: Settings file includes app module
  - Verify: File contains `include(":app")`
  - Files: `android/CampusLibraryApp/settings.gradle.kts`

- [ ] Task 3.5: Create AndroidManifest.xml
  - Acceptance: Valid manifest with package, INTERNET permission, application tag
  - Verify: XML is well-formed
  - Files: `android/CampusLibraryApp/app/src/main/AndroidManifest.xml`

- [ ] Task 3.6: Create placeholder MainActivity.kt
  - Acceptance: Basic Compose activity exists
  - Verify: File compiles (will be verified when Studio opens it)
  - Files: `android/CampusLibraryApp/app/src/main/java/com/campus/library/MainActivity.kt`

- [ ] Task 3.7: Create gradle.properties
  - Acceptance: Android Gradle properties set (Kotlin code generation, JVM target)
  - Verify: File exists with required properties
  - Files: `android/CampusLibraryApp/gradle.properties`

---

## Phase 4: Repository Setup

- [ ] Task 4.1: Create docs/reports structure
  - Acceptance: `docs/reports/` directory exists with `INDEX.md`
  - Verify: `cat docs/reports/INDEX.md` shows header
  - Files: `docs/reports/INDEX.md`

- [ ] Task 4.2: Create .gitignore
  - Acceptance: `.gitignore` covers .NET, Android, and Docker artifacts
  - Verify: File includes `bin/`, `obj/`, `.vs/`, `.idea/`, `build/`, `*.apk`
  - Files: `.gitignore`

- [ ] Task 4.3: Switch git branch to main
  - Acceptance: Current branch is `main`, not `master`
  - Verify: `git branch --show-current` returns `main`
  - Files: None (git operation)

---

## Phase 5: Documentation

- [ ] Task 5.1: Generate DEVELOPER-DOCS.md report
  - Acceptance: Report in `docs/reports/2026-11-07-HHMM-solution-setup/DEVELOPER-DOCS.md`
  - Verify: File contains summary, files changed, verification results
  - Files: `docs/reports/2026-11-07-HHMM-solution-setup/DEVELOPER-DOCS.md`

- [ ] Task 5.2: Generate HOW-TO-USE.md report
  - Acceptance: User-facing guide in same report folder
  - Verify: File contains prerequisites, step-by-step instructions, troubleshooting
  - Files: `docs/reports/2026-11-07-HHMM-solution-setup/HOW-TO-USE.md`

- [ ] Task 5.3: Generate FLOWCHARTS.md report
  - Acceptance: Mermaid diagrams in same report folder
  - Verify: File contains component diagram, sequence diagram
  - Files: `docs/reports/2026-11-07-HHMM-solution-setup/FLOWCHARTS.md`

- [ ] Task 5.4: Update INDEX.md
  - Acceptance: New report entry appended to `docs/reports/INDEX.md`
  - Verify: File contains link to new report folder
  - Files: `docs/reports/INDEX.md` (modified)

---

## Summary

- **Total tasks:** 22
- **Can be parallelized:** Phases 1, 2, 3 (Phases 1-3 are independent)
- **Must be sequential:** Phase 4 (cleanup) and Phase 5 (documentation) run last
- **Estimated time:** 15-20 minutes (mostly project creation and Docker pull)
- **No application code yet:** This milestone is infrastructure only
