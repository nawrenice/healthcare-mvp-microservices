## Running the Project with Docker

This project uses Docker Compose to run the Spring Boot microservices and their MySQL databases.

### Services

| Service | Port | Description |
|---|---:|---|
| Auth Service | 8081 | Handles login, signup, JWT authentication |
| User Service | 8082 | Handles user profile REST APIs |
| Auth MySQL DB | 3307 | Database for auth-service |
| User MySQL DB | 3308 | Database for user-service |



# Docker Notes
- Each Spring Boot microservice (auth-service, user-service) is containerized using its own Dockerfile.
- Docker Compose is used to run multiple services (Spring Boot apps + MySQL databases) together.
- Each service has its own dedicated MySQL database, following microservices best practices.
- Services communicate through Docker’s internal network using service names (e.g., auth-db, user-db) instead of localhost.
- Environment variables are used for configuration (DB URL, username, password, JWT secret), avoiding hardcoding sensitive data.
- Docker volumes are used to persist MySQL data even if containers are restarted or removed.
- Health checks ensure that services start only after the database is ready, preventing connection errors.
- The entire backend system can be started with a single command:

### Run the Project

From the root folder:

```bash
docker compose up --build
```