# Mokksy Mock OpenAI Server

A standalone Kotlin application that uses Mokksy's `MockOpenai` to mock OpenAI's chat completions API with SSE streaming support.

## Requirements

- Java 21+ (currently using Java 24)
- Gradle 8.5+ (included via wrapper)
- Docker (optional, for containerized deployment)

## Building

```bash
./gradlew build
```

## Running Locally

```bash
./gradlew run
```

The server will start on port 8080.

## Docker Deployment

### Build and Run with Docker Compose

```bash
docker-compose up -d
```

### Build Docker Image Manually

```bash
docker build -t mokksy-mock-openai:latest .
docker run -p 8080:8080 -e PORT=8080 -e VERBOSE=true mokksy-mock-openai:latest
```

### Environment Variables

- `PORT` - Server port (default: 8080)
- `VERBOSE` - Enable verbose logging (default: true)

### Using Portainer

1. In Portainer, go to **Stacks**
2. Click **Add stack**
3. Name it `mokksy-mock-openai`
4. Paste the contents of `docker-compose.yml`
5. Click **Deploy the stack**

The service will be available at `http://localhost:8080`

## Testing

Test the streaming endpoint with curl:

```bash
curl -N -X POST http://localhost:8080/v1/chat/completions \
  -H 'Content-Type: application/json' \
  -d '{"model":"gpt-4","messages":[{"role":"user","content":"Hello"}],"stream":true}'
```

## Configuration

The mock server is configured in `src/main/kotlin/MockServer.kt`:

- Port: Configurable via `PORT` environment variable (default: 8080)
- Verbose logging: Configurable via `VERBOSE` environment variable (default: true)
- Response chunks: Configured word-by-word streaming
- Delay: 100ms initial delay
- Delay between chunks: 50ms

## Dependencies

- `dev.mokksy.aimocks:ai-mocks-openai-jvm:0.6.1` - Mokksy OpenAI mock library

## Docker Image

The Dockerfile uses a multi-stage build:
- **Build stage**: Gradle 8.5 with JDK 21 Alpine
- **Runtime stage**: Eclipse Temurin JRE 21 Alpine (minimal image size)


