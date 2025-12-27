# Repository Guidelines

## Project Structure & Module Organization
The Spring Boot entry point lives in src/main/java/utcn/isp/quizapp/QuizApplication.java, with controller, model, and service packages mirroring the MVC flow (HTTP handling, domain records, and quiz/session services). Views are Thymeleaf templates under src/main/resources/templates, and static data (questions.txt, leaderboard_data.txt) sit in src/main/resources and the repository root; keep them in sync with gameplay changes. Docker assets (Dockerfile, docker-compose.yml) and infrastructure docs (docs/, .github/workflows/) support container builds and CI.

## Build, Test, and Development Commands
- ./mvnw clean package – resolves dependencies, runs tests, and creates the runnable JAR in 	arget/.
- ./mvnw spring-boot:run – starts the application on port 8888 using local resources; ideal for iterative work.
- ./mvnw test – executes the JUnit/Spring Boot test suite once it exists; use before every push.
- docker compose up --build – rebuilds the image and runs the app the same way CI/CD does.

## Coding Style & Naming Conventions
Use Java 21 features sparingly and prefer explicit null-safe logic to ease grading. Follow the existing four-space indentation, PascalCase for classes (QuizSessionService), camelCase for methods/fields, and uppercase snake case for constants. Favor constructor injection for new Spring components, log with LoggerFactory, and keep controller methods small, delegating to services.

## Testing Guidelines
Add JUnit 5 tests in src/test/java that mirror the production package structure (e.g., utcn.isp.quizapp.service). Name classes with the *Test suffix and cover session timing, leaderboard ordering, and question parsing. When file interaction is involved, prefer fixture copies under src/test/resources so CI stays hermetic. Target meaningful coverage for newly added logic before opening a PR.

## Commit & Pull Request Guidelines
Recent history uses short imperative commits ("Update readme", "Demo NTD"); keep messages under 72 characters and focus on what changes, not how. Every PR should include: a clear summary of behavior changes, manual/test evidence (./mvnw test, screenshots for UI tweaks), references to related issues if applicable, and notes about migrations or file format updates (questions/leaderboard). Keep branches rebased onto main and request review before merging.

## Security & Configuration Tips
Never hard-code credentials in pplication.properties; rely on environment overrides when deploying. The leaderboard and question files are user-visible, so validate input and avoid storing PII. When touching Docker or Azure artifacts, double-check image tags and registry names to prevent leaking private repositories.
