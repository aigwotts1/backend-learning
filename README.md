# Task Tracker — backend learning

A small project that grows one milestone at a time. Current milestone: **1 — Java and Spring Boot REST basics**.

## Run

Requires Java 17 and Maven 3.6.3 or newer.

```powershell
mvn test
mvn spring-boot:run
```

In a second PowerShell terminal:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/tasks -ContentType 'application/json' -Body '{"title":"Learn Spring Boot"}'
Invoke-RestMethod -Uri http://localhost:8080/api/tasks
```

POST returns HTTP 201 and a task with `id`, `title`, and `completed`. GET returns a JSON array. Blank, missing, or over-120-character titles return HTTP 400. Tasks are stored in memory and disappear on restart. There is no database setup yet.

## Understand this milestone

Read these classes in order:

1. `TaskTrackerApplication`: starts Spring Boot and discovers components.
2. `Task`: a Java record representing the response data.
3. `CreateTaskRequest`: a record with input validation constraints.
4. `TaskService`: owns task creation and storage. Spring creates this service.
5. `TaskController`: receives HTTP requests and calls the service through constructor injection.
6. `TaskApiTest`: exercises real Spring request handling without starting a separate server.

Request flow: HTTP request → controller → service → Java object → JSON response. `@Valid` checks the request before the controller creates a task. Jackson converts between Java records and JSON. The map stores tasks; the atomic counter generates IDs safely when requests arrive concurrently.

## Practice before continuing

- Create three tasks and list them.
- Send a blank title and observe HTTP 400.
- Restart the app and explain why the list is empty.
- Explain `@SpringBootApplication`, `@RestController`, `@Service`, `@GetMapping`, and constructor injection in your own words.
- Trace a POST request through the code and explain where its ID comes from.
- Change the title length limit, run the tests, and see which expectation needs updating.

When these make sense, say **“I learned milestone 1”**. We will build the next milestone together.

## Roadmap

| Milestone | What we will add | What you will learn |
| --- | --- | --- |
| 1 (current) | Create and list tasks in memory | Java records, collections, HTTP, JSON, Spring injection, validation, basic tests |
| 2 | PostgreSQL and a JPA repository | SQL, tables, entities, persistence, configuration, migrations |
| 3 | Get, update, complete, and delete tasks | CRUD, status codes, error handling, transactions, integration tests |
| 4 | Containerize the app and database | Dockerfiles, images, Compose, networks, volumes |
| 5 | Add a notification service | Microservice boundaries, HTTP communication, failure handling |
| 6 | Send task events with Kafka | Topics, producers, consumers, retries, duplicate handling |
| 7 | Deploy locally with Kubernetes | Pods, Deployments, Services, configuration, probes, scaling |
| 8 | Improve operational quality | Logs, metrics, security basics, CI |

Future milestones are a plan; their code will be added only as you reach them. We will keep the same Task Tracker domain throughout.

## GitHub Actions tests

`.github/workflows/ci.yml` runs automatically on every push and pull request. You can also run it from the GitHub Actions tab using **Run workflow**.

The runner checks out the code, installs Java 17, caches Maven dependencies, and runs `mvn --batch-mode --no-transfer-progress clean verify`. This compiles the application, runs the tests, and packages the JAR. A failed test makes the workflow fail. No secrets or database are needed for the current tests.

After pushing this workflow, open the repository's **Actions** tab and select **Java CI**, then **Build and test**, to inspect the results. This is continuous integration (CI). When deployment is added, its job should depend on `test` with `needs: test`, so deployment only runs after the checks pass.

To require passing tests before merging, configure a branch rule for `main` under **Settings > Rules > Rulesets**, requiring pull requests and the **Build and test** status check. The check must run first to appear in the selector; rule availability depends on the repository and GitHub plan. Direct pushes are still possible until an applicable rule is configured.
