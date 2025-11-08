# JSG LLM API - Mock OpenAI Server

A standalone Kotlin mock server using Mokksy that provides varied, realistic OpenAI-compatible streaming responses for testing and development.

## Project Names Reference

All components use the `jsg-llm-api` naming convention:

- **Service name**: `jsg-llm-api` (docker-compose service)
- **Container name**: `jsg-llm-api` (Docker container)
- **Docker image**: `jsg-llm-api:latest`
- **Portainer stack**: `jsg-llm-api` (suggested name)

---

## What It Does

This mock server provides **randomized, varied responses** to OpenAI chat completion requests with proper SSE streaming.

### Features

✅ **OpenAI-Compatible API** - `/v1/chat/completions` endpoint  
✅ **SSE Streaming** - Proper Server-Sent Events with word-by-word chunks  
✅ **6 Themed Quote Pools** - Pre-loaded with 10 quotes each:
- Star Wars (characters)
- Computer (platforms)
- Rick and Morty (characters)
- Game of Thrones (characters)  
- Lord of the Rings (characters)
- Matrix (character names)

✅ **5 Speed Variations** - Random streaming delays:
- slow (200ms initial, 100ms between chunks)
- normal (100ms initial, 50ms between chunks)
- fast (50ms initial, 20ms between chunks)
- superfast (10ms initial, 5ms between chunks)
- randomized (10-200ms random delays)

✅ **Random Selection** - Each request gets a random theme, speed, and quote for variety

---

## Limitations

❌ **No Dynamic Parameter Control** - Cannot specify theme or speed via request headers/body/params  
❌ **Pre-loaded Quotes Only** - Responses are from fixed pools, not dynamic AI  
❌ **Random Only** - No way to request specific theme or speed per request

### Why These Limitations?

Mokksy's architecture evaluates response configurations at **server startup**, not per-request. The DSL blocks don't have access to runtime request data (headers, body, params).

### Alternatives for Dynamic Control

If you need per-request control:
- **Ollama** - Real local AI with OpenAI API (see `OLLAMA-ALTERNATIVE.md`)
- **Custom Ktor Server** - Full control but requires custom implementation
- **30 Static Endpoints** - Define all theme/speed combinations at startup

---

## Requirements

- Java 21+ (tested with Java 24)
- Gradle 8.5+ (included via wrapper)
- Docker (optional, for containerized deployment)

---

## Quick Start

### Local Development

```bash
# Run the mock server locally
./gradlew dev

# Server starts on http://localhost:8080
```

The `dev` command automatically kills any existing gradle processes before starting.

### Docker Deployment

```bash
# Build and run Docker container
./gradlew docker-run

# Stop container
./gradlew docker-stop

# View logs (follows)
./gradlew docker-logs

# Just build image (without running)
./gradlew docker-build
```

### Docker Compose

```bash
# Start with docker-compose
docker-compose up -d

# Stop
docker-compose down
```

---

## Testing

### Basic Streaming Request

```bash
curl -N -X POST http://localhost:8080/v1/chat/completions \
  -H 'Content-Type: application/json' \
  -d '{
    "model": "gpt-4",
    "messages": [{"role": "user", "content": "Hello"}],
    "stream": true
  }'
```

### Expected Response Format

```
data:{"id":"chatcmpl-1","object":"chat.completion.chunk","created":1234567890,"model":"gpt-4","choices":[{"index":0,"delta":{"role":"assistant","content":""},"logprobs":null,"finish_reason":null}]}

data:{"id":"chatcmpl-1","object":"chat.completion.chunk","created":1234567890,"model":"gpt-4","choices":[{"index":0,"delta":{"content":"Darth "},"logprobs":null,"finish_reason":null}]}

data:{"id":"chatcmpl-1","object":"chat.completion.chunk","created":1234567890,"model":"gpt-4","choices":[{"index":0,"delta":{"content":"Vader "},"logprobs":null,"finish_reason":null}]}

data:{"id":"chatcmpl-1","object":"chat.completion.chunk","created":1234567890,"model":"gpt-4","choices":[{"index":0,"delta":{},"logprobs":null,"finish_reason":"stop"}]}
```

### Multiple Requests Show Variety

Run several requests to see different themes, speeds, and quotes:

```bash
# Request 1 might get: "Éomer" (LOTR, normal speed)
# Request 2 might get: "OpenBSD" (Computer, superfast speed)
# Request 3 might get: "Melesa Crakehall" (GoT, slow speed)
```

---

## Configuration

### Environment Variables

- `PORT` - Server port (default: 8080)
- `VERBOSE` - Enable verbose logging (default: true)

Example:
```bash
PORT=9000 VERBOSE=false ./gradlew run
```

### Modifying Quote Pools

Edit `src/main/kotlin/MockServer.kt` and update the `loadQuotes()` function to customize the 10 quotes per theme.

### Adding More Themes

1. Add new theme to `QuotePool` data class
2. Load quotes in `loadQuotes()` function
3. Add to the `allQuotes` list in the response block

---

## Deployment

### Portainer Deployment

1. In Portainer, go to **Stacks** → **Add stack**
2. Name it `jsg-llm-api`
3. Paste the contents of `docker-compose.yml`
4. Click **Deploy the stack**

The service will be available at `http://localhost:8080`

### Manual Docker Deployment

```bash
# Build
docker build -t jsg-llm-api:latest .

# Run
docker run -d --name jsg-llm-api --network host \
  -e PORT=8080 -e VERBOSE=true \
  jsg-llm-api:latest

# Stop
docker stop jsg-llm-api && docker rm jsg-llm-api
```

---

## Project Structure

```
llm-api/
├── src/main/kotlin/
│   └── MockServer.kt           # Main application code
├── build.gradle.kts            # Build config with custom tasks
├── settings.gradle.kts         # Project settings
├── docker-compose.yml          # Docker Compose configuration
├── Dockerfile                  # Multi-stage Docker build
├── README.md                   # This file
└── OLLAMA-ALTERNATIVE.md       # Alternative using real AI
```

---

## Available Gradle Tasks

### Application Tasks
```bash
./gradlew dev      # Run locally (kills existing processes first)
./gradlew run      # Standard run task
```

### Docker Tasks
```bash
./gradlew docker-build   # Build Docker image
./gradlew docker-run     # Build and run container
./gradlew docker-stop    # Stop and remove container
./gradlew docker-logs    # View container logs (follows)
```

View all tasks:
```bash
./gradlew tasks
```

---

## Dependencies

- **Mokksy** (`dev.mokksy.aimocks:ai-mocks-openai-jvm:0.6.1`) - OpenAI mock library with SSE support
- **kotlin-faker** (`io.github.serpro69:kotlin-faker:1.16.0`) - Realistic fake data generation for quotes

---

## Docker Image

The Dockerfile uses a multi-stage build:
- **Build stage**: Gradle 8 with JDK 21 Alpine (~600MB build layer)
- **Runtime stage**: Eclipse Temurin JRE 21 Alpine (~200MB final image)

Network mode is set to `host` to ensure the Ktor server is accessible from outside the container.

---

## How It Works

1. **Server Startup**: Loads 10 quotes per theme (6 themes × 10 = 60 total quotes) using kotlin-faker
2. **Request Received**: Any POST to `/v1/chat/completions` triggers the mock
3. **Random Selection**: 
   - Picks random theme (Star Wars, Computer, etc.)
   - Picks random quote from that theme's pool
   - Picks random speed configuration
4. **Response Streaming**: 
   - Splits quote into words
   - Streams each word as an SSE chunk with configured delays
   - Returns OpenAI-format JSON in each chunk

---

## Troubleshooting

### Port Already in Use

Kill existing processes:
```bash
pkill -f "gradle.*run"
# or
lsof -ti:8080 | xargs kill -9
```

The `./gradlew dev` command does this automatically.

### Docker Build Fails

Ensure Docker is running:
```bash
docker ps
```

Clean rebuild:
```bash
./gradlew clean
./gradlew docker-build
```

### Can't Access Server in Docker

The server uses `network_mode: "host"` which works on Linux but not Docker Desktop (Mac/Windows). For Mac/Windows, use port mapping instead:

```yaml
ports:
  - "8080:8080"
# Remove: network_mode: "host"
```

---

## Comparison: Mock vs Real AI

| Feature | This Mock Server | Ollama (Alternative) |
|---------|------------------|---------------------|
| Setup | Medium complexity | Simple |
| Responses | Hardcoded quotes | Real AI |
| Variety | 60 pre-loaded quotes | Infinite variations |
| Control | Random only | Full prompt control |
| Resources | ~100MB RAM | ~2-3GB RAM |
| Cost | Free | Free |
| Speed | Instant | ~1-5 tokens/sec |
| Use Case | Testing with variety | Development with real AI |

See `OLLAMA-ALTERNATIVE.md` for details on using real local AI instead.

---

## When to Use This Mock

✅ **Testing** - Verify streaming logic without API costs  
✅ **Development** - Work offline with varied responses  
✅ **Demos** - Show streaming behavior predictably  
✅ **CI/CD** - Automated tests without external dependencies

## When to Use Ollama Instead

✅ **Home Assistant Integration** - Native HA support  
✅ **Real Conversations** - Need actual AI understanding  
✅ **Flexible Responses** - Want different responses to same question  
✅ **Production-like Testing** - Test with real LLM behavior

---

## License

MIT - See project repository for details

---

## Contributing

This is a personal project. Feel free to fork and adapt for your needs.
