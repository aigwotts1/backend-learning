# Task Tracker - backend learning

Current milestone: **3 - Full CRUD and error handling**. Tasks are stored in PostgreSQL. You can now fetch, update, complete, and delete them, with consistent HTTP error responses.

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

The API tests roll back their changes. The persistence tests commit rows, check reads and updates in separate transactions, and clean up their own rows afterward. IDs can have gaps after rollbacks; they are identifiers, not row counts.

## Package structure

Code is grouped by feature (`task`), then by responsibility:

```text
com.aigwotts1.tasktracker
|-- TaskTrackerApplication.java
|-- common/exception/ApiExceptionHandler.java
`-- task
    |-- controller/TaskController.java
    |-- service/TaskService.java
    |-- repository/TaskRepository.java
    |-- entity/TaskEntity.java
    |-- exception/TaskNotFoundException.java
    `-- dto
        |-- CreateTaskRequest.java
        |-- UpdateTaskRequest.java
        `-- Task.java
```

Controllers handle HTTP, services coordinate business logic and transactions, repositories access the database, entities map database rows, and DTOs carry API request/response data. Keep future features alongside `task` with their own folders. The application class remains above these packages so Spring discovers them automatically.

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

Milestone 2 introduced persistence. Continue below for the current milestone.

## Milestone 3: CRUD and error handling

| Method | Endpoint | Body | Successful response |
| --- | --- | --- | --- |
| POST | `/api/tasks` | `{"title":"Learn CRUD"}` | 201 + task, with its URL in `Location` |
| GET | `/api/tasks` | none | 200 + tasks ordered by ID |
| GET | `/api/tasks/{id}` | none | 200 + task |
| PUT | `/api/tasks/{id}` | `{"title":"New title","completed":false}` | 200 + updated task |
| PATCH | `/api/tasks/{id}/complete` | none | 200 + completed task |
| DELETE | `/api/tasks/{id}` | none | 204 with no body |

PUT requires **both** fields; `completed` uses `Boolean` rather than `boolean` in the request record so a missing value can fail `@NotNull` validation. You can reopen a task by sending `completed: false`. PATCH complete changes only completion; repeating it leaves the task complete. A missing task returns 404 for all operations by ID, including deleting it again. Titles must be nonblank and at most 120 characters before trimming.

With the application running, use these PowerShell commands:

```powershell
$task = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/tasks -ContentType 'application/json' -Body '{"title":"Learn CRUD"}'
$taskId = $task.id
Invoke-RestMethod -Uri "http://localhost:8080/api/tasks/$taskId"
Invoke-RestMethod -Method Put -Uri "http://localhost:8080/api/tasks/$taskId" -ContentType 'application/json' -Body '{"title":"Understand transactions","completed":false}'
Invoke-RestMethod -Method Patch -Uri "http://localhost:8080/api/tasks/$taskId/complete"
Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/tasks/$taskId"
```

To inspect an error body, run `curl.exe -i http://localhost:8080/api/tasks/9223372036854775807`. It returns `application/problem+json`, for example:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Task with ID 9223372036854775807 was not found.",
  "instance": "/api/tasks/9223372036854775807"
}
```

Validation errors return 400 and add an `errors` object such as `{"title":"must not be blank"}`. Malformed JSON and nonnumeric IDs also return 400. `ApiExceptionHandler` extends Spring's `ResponseEntityExceptionHandler`, which supplies standard Problem Detail responses for Spring MVC errors. `@RestControllerAdvice` centralizes that handling so controllers stay focused on HTTP operations. `TaskNotFoundException` communicates a missing task from the service to the handler.

### Transactions and updates

The service loads an entity inside `@Transactional`. That entity is managed by Hibernate. Calling `setTitle` or `setCompleted` changes it in memory; Hibernate detects the change and writes an UPDATE when the transaction commits (dirty checking). A second `repository.save` is unnecessary for this managed entity. Exceptions roll back the transaction. Fetching first also ensures update and delete return 404 for missing tasks.

Lombok generates getters, the protected JPA constructor, and setters only for editable fields. The ID has no setter. Existing table columns already support these operations, so there is no new migration and V1 stays unchanged.

### Practice before continuing

- Create a task and run the entire PowerShell sequence above.
- Observe the `Location` header on POST and the empty body on DELETE.
- Call complete twice, then reopen the task using PUT.
- Leave `completed` out of PUT and inspect the 400 response.
- Request a deleted task and inspect the 404 response.
- Update a task, restart the app, and check that the change survives.
- Explain why updates persist without an explicit `save` call.
- Run the tests using the test database configuration above. MockMvc uses real Spring components and PostgreSQL, while avoiding a network server. Persistence tests separately verify committed changes.

When ready, say **"I learned milestone 3"**. Next we will containerize the app and database with Docker Compose.

## GitHub Actions

`.github/workflows/ci.yml` runs on pushes, pull requests, and manual dispatch. It starts a fresh PostgreSQL 16 service, waits for its health check, sets Java 17, and runs `mvn clean verify`. The password in that workflow is only for its temporary test database. No external database credentials are needed.

Once these changes are pushed, inspect **Actions > Java CI > Build and test**. Future deployment should depend on the test job with `needs: test`. To require passing tests before merging, configure a rule for `main` requiring pull requests and the **Build and test** status check; availability depends on your GitHub plan and repository.

## Roadmap

| Milestone | What we add |
| --- | --- |
| 1 (completed) | Java records, HTTP, JSON, injection, validation, create/list APIs |
| 2 (completed) | PostgreSQL, JPA entities, repositories, Flyway, database tests |
| 3 (current) | Full CRUD, error handling, transaction behavior |
| 4 | Dockerfile, Compose, networks, volumes |
| 5 | Notification microservice and HTTP communication |
| 6 | Kafka task events, retries, duplicate handling |
| 7 | Local Kubernetes deployment, probes, configuration, scaling |
| 8 | Logs, metrics, security basics; CI is already introduced |

Future milestone code will be added as you reach it.
