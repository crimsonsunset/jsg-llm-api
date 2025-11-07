import dev.mokksy.aimocks.openai.MockOpenai
import kotlin.time.Duration.Companion.milliseconds

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val verbose = System.getenv("VERBOSE")?.toBoolean() ?: true

    val mockOpenai = MockOpenai(
        port = port,
        verbose = verbose
    )

    mockOpenai.completion {
    } respondsStream {
        responseChunks = listOf(
            "Hello",
            " there",
            "!",
            " How",
            " can",
            " I",
            " help",
            " you",
            " today",
            "?"
        )
        delay = 100.milliseconds
        delayBetweenChunks = 50.milliseconds
        finishReason = "stop"
    }

    println("Mock OpenAI server running at ${mockOpenai.baseUrl()}")
    println("Port: ${mockOpenai.port()}")
    println("Press Ctrl+C to stop")

    Thread.sleep(Long.MAX_VALUE)
}

