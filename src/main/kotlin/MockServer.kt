import dev.mokksy.aimocks.openai.MockOpenai
import io.github.serpro69.kfaker.Faker
import kotlin.time.Duration.Companion.milliseconds
import kotlin.random.Random

val faker = Faker()

/**
 * Speed configurations for streaming delays
 * Returns Pair<initialDelay, delayBetweenChunks>
 */
val speedConfigs = mapOf(
    "slow" to Pair(200.milliseconds, 100.milliseconds),
    "normal" to Pair(100.milliseconds, 50.milliseconds),
    "fast" to Pair(50.milliseconds, 20.milliseconds),
    "superfast" to Pair(10.milliseconds, 5.milliseconds),
    "randomized" to Pair(
        Random.nextInt(10, 200).milliseconds,
        Random.nextInt(5, 100).milliseconds
    )
)

/**
 * Data class holding pre-loaded quote pools for each theme
 */
data class QuotePool(
    val starWars: List<String>,
    val computer: List<String>,
    val rickAndMorty: List<String>,
    val gameOfThrones: List<String>,
    val lotr: List<String>,
    val matrix: List<String>
)

/**
 * Load 10 quotes for each theme using kotlin-faker
 */
fun loadQuotes(): QuotePool {
    return QuotePool(
        starWars = (1..10).map { faker.starWars.characters() },
        computer = (1..10).map { faker.computer.platform() },
        rickAndMorty = (1..10).map { faker.rickAndMorty.characters() },
        gameOfThrones = (1..10).map { faker.gameOfThrones.characters() },
        lotr = (1..10).map { faker.lordOfTheRings.characters() },
        matrix = (1..10).map { "The Matrix ${faker.name.name()}" }
    )
}

/**
 * Main entry point for the mock OpenAI server
 */

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val verbose = System.getenv("VERBOSE")?.toBoolean() ?: true

    println("Loading quote pools...")
    val quotes = loadQuotes()
    println("Loaded ${quotes.starWars.size} quotes per theme")

    val mockOpenai = MockOpenai(
        port = port,
        verbose = verbose
    )

    // Simple catchall - random theme and speed
    mockOpenai.completion {
    } respondsStream {
        val allQuotes = listOf(
            quotes.starWars,
            quotes.computer,
            quotes.rickAndMorty,
            quotes.gameOfThrones,
            quotes.lotr,
            quotes.matrix
        ).random()
        
        val quote = allQuotes.random()
        val (initialDelay, chunkDelay) = speedConfigs.values.random()
        
        responseChunks = quote.split(" ").map { "$it " }
        delay = initialDelay
        delayBetweenChunks = chunkDelay
        finishReason = "stop"
    }

    println("╔══════════════════════════════════════════════════════════════╗")
    println("║  JSG LLM API - Mock OpenAI Server                            ║")
    println("╠══════════════════════════════════════════════════════════════╣")
    println("║  Endpoint: ${mockOpenai.baseUrl()}/chat/completions        ║")
    println("║  Port: ${mockOpenai.port()}                                              ║")
    println("╠══════════════════════════════════════════════════════════════╣")
    println("║  Themes: Star Wars, Computer, Rick & Morty, GoT, LOTR, Matrix║")
    println("║  Speeds: slow, normal, fast, superfast, randomized          ║")
    println("║  Note: Random selection per request (no dynamic control)    ║")
    println("╠══════════════════════════════════════════════════════════════╣")
    println("║  Press Ctrl+C to stop                                        ║")
    println("╚══════════════════════════════════════════════════════════════╝")

    Thread.sleep(Long.MAX_VALUE)
}

