# Museum Artifact Manager

A collection management system for a museum: staff catalogue objects, plan
exhibitions, and rotate what is on display as shows open and close. The public
side shows visitors what is on the walls today.

Built for UCI 2123, Systems Engineering with AWS.

## Stack

| Layer | Technology |
|---|---|
| Frontend | React 19, Vite, React Router 7 |
| Backend | Spring Boot 4.1.1, Spring Security 7, Spring Data JPA |
| Database | MySQL 8 |
| Auth | JWT (jjwt 0.12.6), BCrypt strength 10 |
| Tests | JUnit 5, Mockito, AssertJ, H2 in-memory |
| Coverage | JaCoCo 0.8.15, enforced at 75% |
| API docs | springdoc-openapi 3.1.1 |
| Build | Maven wrapper, JDK 26 |

## Running it

### 1. Database

    mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS museum_db;"
    Get-Content database/schema.sql -Raw | mysql -u root -p museum_db
    Get-Content database/seed.sql -Raw | mysql -u root -p museum_db
    Get-Content database/seed-images.sql -Raw | mysql -u root -p museum_db

schema.sql creates the nine tables with their constraints and performance
indexes. seed.sql loads reference data and fifteen artifacts. seed-images.sql
attaches an open-access photograph to each one.

### 2. Backend

    $env:DB_PASSWORD = 'your-mysql-password'
    ./mvnw.cmd spring-boot:run

Starts on port 8080. Wait for "Started CapstoneProjectApplication".

The JWT signing key defaults to a development value. Set JWT_SECRET to override
it; it must be at least 48 bytes, because tokens are signed with HS384.

### 3. Frontend

    cd frontend
    npm install
    npm run dev

Starts on port 5173.

### 4. Open it

| Address | What it is |
|---|---|
| http://localhost:5173/visit | Public gallery page, no sign-in |
| http://localhost:5173 | Staff application |
| http://localhost:8080/swagger-ui.html | Live API documentation |

## Demo credentials

| Email | Password | Role |
|---|---|---|
| los@museum.org | Password123! | ADMIN |

Registering through the UI or POST /api/v1/auth/register always creates a
VISITOR, whatever role the request asks for. Staff accounts are promoted by an
administrator, not self-assigned.

## Roles

| | ADMIN | CURATOR | RESTORER | VISITOR | Anonymous |
|---|---|---|---|---|---|
| Browse the catalogue | yes | yes | yes | no | no |
| Accession and edit artifacts | yes | yes | no | no | no |
| Change an artifact's status | yes | yes | yes | no | no |
| Deaccession an artifact | yes | no | no | no | no |
| Schedule and edit exhibitions | yes | yes | no | no | no |
| Add and remove objects from a show | yes | yes | no | no | no |
| Delete an exhibition | yes | no | no | no | no |
| Full exhibition archive | yes | yes | yes | yes | no |
| What is on show today | yes | yes | yes | yes | yes |

Enforced by @PreAuthorize on every mutating endpoint and verified by the test
suite. A CURATOR attempting a deaccession receives 403, not 500.

## Seasonal rotation

An exhibition is a date range, not a flag. Its phase (UPCOMING, CURRENT, PAST)
is computed from the calendar every time it is read, so a show opens and closes
on its own.

Adding an object to a show that is open today moves it to ON_DISPLAY
immediately. Removing it returns it to STORED, unless some other open show still
has it, in which case it stays on display because it is still hanging on a wall.

## Tests

    ./mvnw.cmd clean verify

115 tests, 77% instruction coverage. The build fails below 75%; the JaCoCo report
lands at target/site/jacoco/index.html.

Tests run against in-memory H2 and need no MySQL.

## Design notes

**Exhibition to Artifact is not @ManyToMany.** The join carries a display_order
column, which a plain @ManyToMany cannot hold, so it is modelled as its own
ExhibitionArtifact entity with a composite key.

**spring.jpa.open-in-view is off.** Lazy relationships are fetched explicitly
with @EntityGraph or a JOIN FETCH, so a forgotten fetch fails during development
rather than issuing N+1 queries in production.

**ddl-auto=validate.** database/schema.sql is the single source of truth for the
schema. Hibernate checks the entities against it at startup and refuses to run
if they disagree.

**Images are referenced, not hosted.** seed-images.sql points at the
Metropolitan Museum of Art's Open Access collection (CC0) for demonstration
purposes. A production deployment would host its own assets.

**The error handler never reports internals.** Unhandled exceptions are logged
with their full root cause and answered with a generic message. Exception class
names are reconnaissance.

## Layout

    database/     schema.sql, seed.sql, seed-images.sql
    docs/         ERD, API reference, architecture notes, Jira import, Bruno collection
    frontend/     React application
    src/main/     Spring Boot application
    src/test/     101 tests

## Not built

Condition reports and restoration jobs exist in the schema with repositories,
but have no service, endpoints or screens. The RESTORER role can move artifacts
between lifecycle states but cannot yet file a condition report. That is the
next vertical slice.