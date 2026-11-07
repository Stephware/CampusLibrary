# How to Use — CampusLibrary Solution

**Milestone:** 1 — Solution Setup  
**Date:** 2026-11-07  
**For:** Students, classmates, and end users

---

## What Was Built

The **CampusLibrary solution structure** is now ready for development. This includes:

1. **3 .NET projects** for the backend (Data, WebApi, WebMvc)
2. **1 Android Kotlin project** for the mobile app
3. **SQL Server database** running in Docker
4. **Git repository** on the `main` branch

**Note:** No features are implemented yet. This milestone only created the project structure. Books, members, loans, API endpoints, admin pages, and mobile screens will be added in future milestones.

---

## Prerequisites

Before you can run the projects, make sure you have these installed:

### Required Software

1. **.NET SDK 10.0** or higher
   - Check: `dotnet --version`
   - Download: https://dotnet.microsoft.com/download

2. **Docker Desktop**
   - Check: `docker --version`
   - Download: https://www.docker.com/products/docker-desktop

3. **Android Studio** (for Android development)
   - Download: https://developer.android.com/studio
   - Required: Android SDK 26+ installed

4. **Git**
   - Check: `git --version`

5. **A code editor** (recommended: Visual Studio Code, Visual Studio, or JetBrains Rider)

---

## Step-by-Step: Running the Solution

### Step 1: Clone or Open the Repository

```bash
cd /Users/stephen/CampusLibrary
```

### Step 2: Verify SQL Server Container is Running

```bash
docker ps --filter "name=campuslibrary-mssql"
```

**Expected output:** Container `campuslibrary-mssql` should show status `Up`.

**If not running:**
```bash
docker start campuslibrary-mssql
```

**If container doesn't exist (first time setup):**
```bash
docker run -d \
  --name campuslibrary-mssql \
  -e 'ACCEPT_EULA=Y' \
  -e 'MSSQL_SA_PASSWORD=<your-local-password>' \
  -p 14335:1433 \
  mcr.microsoft.com/azure-sql-edge:latest
```

**Important:** The database uses **port 14335**, not the default 1433.

---

### Step 3: Build the .NET Solution

```bash
# From the repository root
dotnet build
```

**Expected output:**
```
Build succeeded.
    0 Warning(s)
    0 Error(s)

Time Elapsed 00:00:01.97
```

---

### Step 4: Run the Web API (Optional — no endpoints yet)

```bash
cd src/CampusLibrary.WebApi
dotnet run
```

**Expected output:**
```
Now listening on: http://localhost:5000
Now listening on: https://localhost:5001
```

**Open in browser:** http://localhost:5000/swagger (Swagger UI will show no endpoints yet)

**Press Ctrl+C to stop.**

---

### Step 5: Run the Web MVC Admin Site (Optional)

```bash
cd src/CampusLibrary.WebMvc
dotnet run
```

**Expected output:**
```
Now listening on: http://localhost:5002
Now listening on: https://localhost:5003
```

**Open in browser:** http://localhost:5002 (Default MVC home page will appear)

**Press Ctrl+C to stop.**

---

### Step 6: Open the Android Project in Android Studio

1. **Open Android Studio**
2. **File → Open** → Navigate to `/Users/stephen/CampusLibrary/android/CampusLibraryApp/`
3. **Wait for Gradle sync** (first time takes 2-5 minutes)
   - Studio will download dependencies
   - Studio will generate the Gradle wrapper (`gradlew`)

4. **Build the project:**
   - Click **Build → Make Project** (or press Cmd+F9)

5. **Run on emulator or device:**
   - Click **Run → Run 'app'** (or press Ctrl+R)
   - Expected: "Campus Library" text appears in the center of the screen

**Alternatively, build from command line:**
```bash
cd android/CampusLibraryApp
./gradlew assembleDebug
```

---

## Example: Checking the Database Connection

Since no database or tables exist yet, you can only verify the container is accessible:

### Using Docker CLI

```bash
docker exec -it campuslibrary-mssql /bin/bash
```

**Note:** Azure SQL Edge doesn't include `sqlcmd` by default. For now, just verify the container is running.

### Connection String (for Milestone 2+)

```
Server=localhost,14335;Database=CampusLibraryDb;User Id=sa;Password=<your-local-password>;TrustServerCertificate=True;
```

**Port 14335** is used instead of 1433 (see Troubleshooting).

---

## What You Should See

### .NET Build
- ✅ All 3 projects build with 0 errors
- ✅ DLL files created in `bin/Debug/net10.0/`

### SQL Server
- ✅ Container `campuslibrary-mssql` running
- ✅ Port 14335 accessible

### Web API
- ✅ Swagger UI loads at http://localhost:5000/swagger
- ⚠️ No endpoints yet (they will be added in Milestone 3)

### Web MVC
- ✅ Default home page loads at http://localhost:5002
- ⚠️ No library pages yet (they will be added in Milestone 5)

### Android
- ✅ App builds in Android Studio
- ✅ App runs on emulator/device showing "Campus Library" text
- ⚠️ No screens or features yet (they will be added in Milestones 6-7)

---

## Troubleshooting

### Problem 1: `dotnet build` fails with "SDK not found"

**Cause:** .NET SDK 10.0 is not installed or not in PATH.

**Fix:**
1. Check installed SDKs: `dotnet --list-sdks`
2. Install .NET 10 SDK: https://dotnet.microsoft.com/download
3. Restart your terminal

---

### Problem 2: SQL Server container won't start

**Error:** `port 1433 already in use`

**Cause:** Another SQL Server container or service is using port 1433.

**Fix:** This project uses port **14335** to avoid conflicts. Use the updated connection string:
```
Server=localhost,14335;...
```

**If the container doesn't exist:**
```bash
docker ps -a | grep campuslibrary-mssql
# If not found, run the docker run command from Step 2
```

---

### Problem 3: Android Studio says "Gradle sync failed"

**Cause:** Missing Gradle wrapper or network issues.

**Fix:**
1. Click **File → Invalidate Caches → Invalidate and Restart**
2. Wait for Android Studio to download dependencies (2-5 minutes first time)
3. Check internet connection
4. If still failing, delete `android/CampusLibraryApp/.gradle/` and sync again

---

### Problem 4: Android app won't connect to API (future milestones)

**Cause:** Android emulator can't reach `localhost`.

**Fix:** Use `http://10.0.2.2:<port>` instead of `localhost` in the Android app (already configured for milestone 6+).

---

### Problem 5: "Permission denied" when running `./gradlew`

**Cause:** Gradle wrapper doesn't have execute permissions.

**Fix:**
```bash
chmod +x android/CampusLibraryApp/gradlew
./gradlew assembleDebug
```

---

## Next Steps

### For Students

1. **Fill in your details** at the top of `CLAUDE.md` (name, section, date)
2. **Commit this milestone to git:**
   ```bash
   git add .
   git commit -m "Milestone 1: Solution structure setup"
   ```
3. **Ready for Milestone 2:** Entity classes, DbContext, and database migrations

### For Classmates Using This Solution

- **Review the code structure** in `src/` and `android/`
- **Read `CLAUDE.md`** to understand the project requirements
- **Read `AI_DEVELOPMENT_GUIDE.md`** to understand the coding standards
- **Check `SPEC.md`** for the technical specification

---

## Summary

✅ **What works now:**
- .NET solution builds successfully
- SQL Server container running
- Android project structure ready
- Git repository on `main` branch

⚠️ **What doesn't work yet:**
- No database tables (Milestone 2)
- No API endpoints (Milestone 3-4)
- No admin pages (Milestone 5)
- No Android screens (Milestone 6-7)

**This is expected!** Milestone 1 only sets up the infrastructure. Features will be added in future milestones.
