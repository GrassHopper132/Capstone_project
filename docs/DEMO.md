# Demo runbook

## Five minutes before

Run the health check. All four numbers must be non-zero.

    mysql -h 127.0.0.1 -P 3306 -u root -p museum_db -e "SELECT (SELECT COUNT(*) FROM roles WHERE name='VISITOR') AS visitor_role, (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='museum_db' AND column_name='image_url') AS image_column, (SELECT COUNT(image_url) FROM artifacts) AS images_loaded, (SELECT COUNT(*) FROM users WHERE email='los@museum.org') AS demo_account;"

Expect 1, 1, 15, 1.

Window 1, backend:

    cd C:\dev\Capstone_project
    $env:DB_PASSWORD = '<password>'
    .\mvnw.cmd spring-boot:run

Wait for "Started CapstoneProjectApplication". Do not redirect this to a file.

Window 2, frontend:

    cd C:\dev\Capstone_project\frontend
    npm run dev

Open these four tabs before anyone is watching:

1. http://localhost:5173/visit
2. http://localhost:5173
3. http://localhost:8080/swagger-ui.html
4. The Jira board

Close every MySQL Workbench tab containing schema.sql. Executing one drops the database.

## The demo, about eight minutes

### 1. Start with the visitor (1 min)

Open /visit in an incognito window. No sign-in. Today's exhibitions, real
photographs, opening hours, directions.

Say: the public sees only what is open today. Nothing here exposes storage
locations, condition grades or acquisition records.

### 2. Sign in as staff (1 min)

los@museum.org / Password123!

Dashboard, then Artifacts. Filter by status.

### 3. Show the seasonal rotation (2 min)

Exhibitions, open one that is running. Add an object.

Say: the object was in storage a second ago. Adding it to a show that is open
today puts it on display automatically, because an exhibition is a date range
rather than a flag. Remove it and it returns to storage, unless another open
show still has it.

### 4. Show that the roles are real (2 min)

In Bruno, run requests 25 to 30.

Say: no token is 401. A visitor on the staff catalogue is 403. A visitor
deaccessioning an artifact is 403. A duplicate accession number is 409. Empty
required fields is 400. Registration asking for ADMIN returns VISITOR.

That last one is the point. The registration endpoint is unauthenticated, so it
ignores the role field entirely.

### 5. Show the engineering (2 min)

Swagger UI: every endpoint, live.

Then the coverage report at target/site/jacoco/index.html. 115 tests, 77 percent,
and the build fails below 75.

Mention honestly: the collection caught a defect the unit tests could not. The
status-change endpoint returned 500 because the unit test mocked the repository
and never exercised the lazy association.

### 6. The board (1 min)

Jira: 28 stories, what is done, what is not. Condition reports and restoration
jobs are in the schema with no service or screens, and that is stated in the
README rather than hidden.

## If something breaks

Backend will not start: DB_PASSWORD is not set in that window, or port 8080 is
held by an earlier instance.

    Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue

Login fails: the demo account was wiped. Register and promote.

    $reg = @{ email='los@museum.org'; password='Password123!'; fullName='Carlos Rhymer' } | ConvertTo-Json
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" -Method Post -Body $reg -ContentType "application/json"
    mysql -u root -p museum_db -e "UPDATE users SET role_id = (SELECT id FROM roles WHERE name='ADMIN') WHERE email='los@museum.org';"

A 500 anywhere: the root cause is in the backend window, logged as
"Root cause on /api/v1/...". The response body deliberately says nothing.