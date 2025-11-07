# QueueCtl - Java Spring Boot Job Queue CLI

## Overview

`queuectl` is a minimal Java Spring Boot CLI tool for managing jobs with workers, retries (exponential backoff), and a Dead Letter Queue (DLQ). Jobs are persisted in an H2 file database.

Features:

* Enqueue and manage jobs.
* Multiple worker threads for concurrent processing.
* Retry mechanism with exponential backoff.
* Dead Letter Queue for failed jobs.
* CLI-first interface using Picocli.

---

## Requirements

* Java 17+
* Maven 3+
* Unix/Linux/Mac or Windows (some commands differ slightly on Windows)

---

## Build

```bash
# Clean and build the JAR
mvn clean package

# Run the application
java -jar target/queuectl-springboot-1.0.0.jar <COMMAND>
```

---

## CLI Commands

### 1. Enqueue a job

Add a job to the queue.

```bash
java -jar target/queuectl.jar enqueue '<jsonJob>'
```

**Parameters:**

* `<jsonJob>` - JSON string describing the job.

**Example:**

```bash
java -jar target/queuectl.jar enqueue '{"command":"echo hello","maxRetries":3}'
```

---

### 2. List jobs by state

```bash
java -jar target/queuectl.jar list --state=<state>
```

**Parameters:**

* `--state=<state>` - Filter jobs by state (`pending`, `processing`, `completed`, `failed`, `dead`).

**Example:**

```bash
java -jar target/queuectl.jar list --state pending
```

**Output:**

```
Jobs (state=pending):
  1  attempts=0
  2  attempts=0
```

---

### 3. Worker management

Manage worker threads that process jobs.

```bash
java -jar target/queuectl.jar worker <subcommand>
```

**Subcommands:**

| Subcommand | Description              | Options                                   |<br>
| start      | Start worker threads     | --count <n> Number of workers (default 1) |<br>
| stop       | Stop all running workers | None                                      |

**Examples:**

```bash
# Start 2 workers
java -jar target/queuectl.jar worker start --count 2

# Stop workers
java -jar target/queuectl.jar worker stop
```

**Worker output:**

```
Job 1 completed.
Job 2 failed.
Job 3 moved to DLQ.
```

---

### 4. Dead Letter Queue (DLQ) commands

Inspect jobs that exceeded max retries.

```bash
java -jar target/queuectl.jar dlq list
```

**Example Output:**

```
Jobs in DLQ:
  3  attempts=4  command="some-failing-command"
```

---

### 5. Configuration commands

Adjust queue parameters dynamically.

```bash
java -jar target/queuectl.jar config set <key> <value>
```

**Supported keys:**

* `backoff-base` - Base for exponential retry backoff.

**Example:**

```bash
java -jar target/queuectl.jar config set backoff-base 3
```

---

## Database & Persistence

* **H2 file database**: `./data/queuectl`
* **Spring Data JPA** handles persistence automatically.
* **H2 Console**: Available at `http://localhost:8083/h2-console` if enabled.

---

## Notes & Assumptions

* Commands run via `/bin/sh -c` on Unix/Linux/Mac and `cmd.exe /c` on Windows.
* `worker start` runs in-process and blocks the terminal. Use `&` to run in background.
* Default max retries: `3`.
* Exponential backoff delay: `backoff-base ^ attempts` seconds.
* Output is minimal by default; only job statuses are printed.

---

## Examples

### Enqueue a job

```bash
java -jar target/queuectl.jar enqueue '{"command":"echo hello","maxRetries":3}'
```

### Start workers

```bash
java -jar target/queuectl.jar worker start --count 2
```

### Stop workers

```bash
java -jar target/queuectl.jar worker stop
```

### List pending jobs

```bash
java -jar target/queuectl.jar list --state pending
```

### Check DLQ

```bash
java -jar target/queuectl.jar dlq list
```

### Change backoff configuration

```bash
java -jar target/queuectl.jar config set backoff-base 3
```

---

## Troubleshooting

* **Job fails:** Ensure the command runs successfully in your OS shell.
* **Workers do not start:** Ensure Java version >=17 and no port conflicts.
* **Verbose logs:** Spring Boot logs can be suppressed by setting logging level in `application.yml`:

```yaml
logging:
  level:
    root: ERROR
```
