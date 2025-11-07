# Mokksy Mock OpenAI Server

A standalone Kotlin application that uses Mokksy's `MockOpenai` to mock OpenAI's chat completions API with SSE streaming support.

## Requirements

- Java 21+ (currently using Java 24)
- Gradle 8.5+ (included via wrapper)

## Building

```bash
./gradlew build
```

## Running

```bash
./gradlew run
```

The server will start on port 8080.

## Testing

Test the streaming endpoint with curl:

```bash
curl -N -X POST http://localhost:8080/v1/chat/completions \
  -H 'Content-Type: application/json' \
  -d '{"model":"gpt-4","messages":[{"role":"user","content":"Hello"}],"stream":true}'
```

## Configuration

The mock server is configured in `src/main/kotlin/MockServer.kt`:

- Port: 8080
- Verbose logging: enabled
- Response chunks: configured word-by-word streaming
- Delay: 100ms initial delay
- Delay between chunks: 50ms

## Dependencies

- `dev.mokksy.aimocks:ai-mocks-openai-jvm:0.6.1` - Mokksy OpenAI mock library

