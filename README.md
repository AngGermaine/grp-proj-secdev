# Milestone # 1

Short and direct steps to build and run this Spring Boot app.

Note: Defaults are in `src/main/resources/application.properties` — change env vars or that file to override.

1) Prerequisites

- Install JDK 21.
- (Optional) MySQL for production. Tests use an in-memory H2 DB.
- Use the included Maven wrapper (`./mvnw`) so you don't need a system Maven.

macOS tip (zsh):

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
chmod +x ./mvnw
```

2) Build

From the repository root:

```bash
./mvnw clean package        # build jar (use -DskipTests to skip tests)
```

Output: `target/project-0.0.1-SNAPSHOT.jar` (executable jar).

3) Run

Quick run (dev):

```bash
./mvnw spring-boot:run
```

Run packaged jar (with env overrides):

```bash
DB_URL='jdbc:mysql://localhost:3306/secdev_db' \
DB_USER=root DB_PASS='' SERVER_PORT=8080 \
java -jar target/project-0.0.1-SNAPSHOT.jar
```

4) Tests

```bash
./mvnw test
```

5) Important config (short)

- DB: set env vars `DB_URL`, `DB_USER`, `DB_PASS` (defaults are in `application.properties`).
- Port: `SERVER_PORT` or Spring `server.port`.
- Uploads dir: `file.upload-dir` (default `./uploads/profile-photos`) — create and make writable.
- Brute-force: `security.bruteforce.max-email-attempts`, `max-ip-attempts`, `window-minutes`, `lock-minutes`.
- BCrypt strength: `security.bcrypt.strength` (default 12).

Note: the app reads `X-Forwarded-For` for the client IP when present. If you use a proxy, ensure it forwards this header.

6) Minimal production tips

- Do NOT run with `spring.jpa.hibernate.ddl-auto=update` in production; use `validate` or migrations (Flyway/Liquibase).
- Use a dedicated DB user and store DB credentials in environment variables or a secrets manager.
- Ensure the uploads directory is on durable storage and writable by the app user.

7) Need help?

- I can add a Dockerfile, a simple GitHub Actions workflow, or a Flyway migration if you want—tell me which and I'll add it.

8) Windows (quick)

- Use `mvnw.cmd` or run `.\mvnw` from PowerShell (no chmod needed).

PowerShell (recommended):

```powershell
# set JDK for current session (or set persistently via System settings)
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21'
.\mvnw clean package
.\mvnw test
$env:DB_URL='jdbc:mysql://localhost:3306/secdev_db'
$env:DB_USER='root'
$env:DB_PASS=''
$env:SERVER_PORT='8080'
java -jar .\target\project-0.0.1-SNAPSHOT.jar
```

cmd.exe (simple):

```cmd
set DB_URL=jdbc:mysql://localhost:3306/secdev_db
set DB_USER=root
set DB_PASS=
set SERVER_PORT=8080
mvnw.cmd clean package
java -jar target\project-0.0.1-SNAPSHOT.jar
```

Notes:
- `file.upload-dir` works on Windows (use a full path like `C:\data\uploads` if you prefer).
- To run as a Windows service use Docker or a service helper like NSSM.
- If behind a proxy, ensure `X-Forwarded-For` is forwarded so brute-force blocking works correctly.


