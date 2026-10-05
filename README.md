# Task Tracker - backend learning

Current milestone: **2 - PostgreSQL persistence**. The same create/list APIs now store tasks in PostgreSQL, so restarting the application keeps your data.

## Local database setup

Requires Java 17, Maven, and PostgreSQL 15 or 16. PostgreSQL 15 is already installed on this machine. Open **SQL Shell (psql)** from the Windows Start menu, connect as your PostgreSQL administrator, and run these statements once:

```sql
CREATE USER task_tracker WITH PASSWORD 'choose-your-local-password';
CREATE DATABASE task_tracker OWNER task_tracker;
CREATE USER task_tracker_test WITH PASSWORD 'choose-your-test-password';
CREATE DATABASE task_tracker_test OWNER task_tracker_test;
```

Use your own passwords. The application user owns its database so Flyway can create tables. The test user owns a separate database so tests never write to your application database.

In PowerShell, configure and run the application:

```powershell
$env:DB_PASSWORD = 'choose-your-local-password'
mvn spring-boot:run
```

The default database URL is `jdbc:postgresql://localhost:5432/task_tracker` and username is `task_tracker`. Override `DB_URL` or `DB_USERNAME` if needed. Passwords are environment variables; do not commit them. Spring Boot does not automatically read a `.env` file.

In a second terminal:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/tasks -ContentType 'application/json' -Body '{"title":"Learn PostgreSQL"}'
Invoke-RestMethod -Uri http://localhost:8080/api/tasks
```

Stop the application with Ctrl+C, start it again in the original terminal, and list tasks. Your task should still be there. PostgreSQL must be running whenever you start the app. Maven runs the app and tests with the JVM timezone set to UTC so PostgreSQL connections behave consistently across machines. For a packaged JAR, use `java -Duser.timezone=UTC -jar target/task-tracker-0.0.1-SNAPSHOT.jar`.

## Run tests

Tests require the **separate test database**. With the native PostgreSQL setup above:

```powershell
$env:TEST_DB_URL = 'jdbc:postgresql://localhost:5432/task_tracker_test'
$env:TEST_DB_USERNAME = 'task_tracker_test'
$env:TEST_DB_PASSWORD = 'choose-your-test-password'
mvn --batch-mode --no-transfer-progress clean verify
```

The test profile defaults to port **5433**, while the application defaults to **5432**. Set `TEST_DB_URL` as above when using the native server. Do not point tests at your application database.

Optional: use Docker just to provide an isolated local test database, matching CI:

```powershell
docker run --detach --name task-tracker-test --publish 127.0.0.1:5433:5432 --env POSTGRES_DB=task_tracker_test --env POSTGRES_USER=task_tracker_test --env POSTGRES_PASSWORD=local_test_password postgres:16
$env:TEST_DB_URL = 'jdbc:postgresql://localhost:5433/task_tracker_test'
$env:TEST_DB_USERNAME = 'task_tracker_test'
$env:TEST_DB_PASSWORD = 'local_test_password'
mvn --batch-mode --no-transfer-progress clean verify
```

Wait until the database is ready (`docker logs task-tracker-test`) before running tests. Afterward, stop the container with `docker stop task-tracker-test`; start it again with `docker start task-tracker-test`. We will study Docker properly in milestone 4.

The API tests roll back their changes. The persistence test commits a row, reads it in a new transaction, and deletes its own row afterward. IDs can have gaps after rollbacks; they are identifiers, not row counts.

## Understand milestone 2

Read in this order:

1. `db/migration/V1__create_tasks.sql`: the actual SQL table definition.
2. `TaskEntity`: maps Java fields to columns. JPA requires a no-argument constructor. PostgreSQL generates the ID.
3. `TaskRepository`: Spring Data implements this interface, providing `save`, `findAll`, and other database operations.
4. `TaskService`: uses the repository instead of the old map and counter. Transactions define the database unit of work.
5. `Task`: remains a response record, so database mapping stays separate from the JSON API.
6. `application.properties`: connection settings, schema validation, and environment variables.
7. `TaskApiTest` and `TaskPersistenceTest`: API validation and real database persistence checks.

Request flow: HTTP -> controller -> service -> repository -> Hibernate/JDBC -> PostgreSQL. JPA is the mapping API; Hibernate implements it; JDBC communicates with the database. Spring creates and injects the repository.

Flyway applies `V1__create_tasks.sql` on first startup and records it in `flyway_schema_history`. On later starts it sees that V1 already ran. `ddl-auto=validate` checks the mapping without changing tables. For future schema changes, add a V2 migration instead of editing an already-applied V1.

POST `/api/tasks` still returns HTTP 201 with `id`, `title`, and `completed`. GET returns tasks ordered by ID. Invalid titles still return HTTP 400.

## Practice before continuing

- Create a task, restart the app, and confirm it survives.
- Connect SQL Shell to `task_tracker` and run `SELECT * FROM tasks ORDER BY id;`.
- Run `SELECT version, description, success FROM flyway_schema_history;` and explain the row.
- Explain why `TaskEntity` is a class while `Task` is a record.
- Trace how `repository.save` causes an INSERT without handwritten Java SQL.
- Explain why tests use their own database and why rollback can leave gaps in IDs.

When ready, say **"I learned milestone 2"**. Next we will add get, update, complete, and delete APIs with error handling.

## GitHub Actions

`.github/workflows/ci.yml` runs on pushes, pull requests, and manual dispatch. It starts a fresh PostgreSQL 16 service, waits for its health check, sets Java 17, and runs `mvn clean verify`. The password in that workflow is only for its temporary test database. No external database credentials are needed.

Once these changes are pushed, inspect **Actions > Java CI > Build and test**. Future deployment should depend on the test job with `needs: test`. To require passing tests before merging, configure a rule for `main` requiring pull requests and the **Build and test** status check; availability depends on your GitHub plan and repository.

## Roadmap

| Milestone | What we add |
| --- | --- |
| 1 (completed) | Java records, HTTP, JSON, injection, validation, create/list APIs |
| 2 (current) | PostgreSQL, JPA entities, repositories, Flyway, database tests |
| 3 | Full CRUD, error handling, transaction behavior |
| 4 | Dockerfile, Compose, networks, volumes |
| 5 | Notification microservice and HTTP communication |
| 6 | Kafka task events, retries, duplicate handling |
| 7 | Local Kubernetes deployment, probes, configuration, scaling |
| 8 | Logs, metrics, security basics; CI is already introduced |

Future milestone code will be added as you reach it.
