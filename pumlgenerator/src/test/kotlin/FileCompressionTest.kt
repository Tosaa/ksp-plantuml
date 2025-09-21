import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.assertEquals

class FileCompressionTest {
    val plantumlAlphabet = (('0'..'9') + ('A'..'Z') + ('a'..'z') + "-_").joinToString("")
    val base64Alphabet = (('A'..'Z') + ('a'..'z') + ('0'..'9') + "+/").joinToString("")
    val b64ToPlantuml: Map<Char, Char> = base64Alphabet.zip(plantumlAlphabet).associate { it.first to it.second }


    @OptIn(ExperimentalEncodingApi::class)
    fun compress(inputBytes: ByteArray): ByteArray {

        // Create Deflater instance with default compression level
        val deflater = Deflater(Deflater.DEFAULT_COMPRESSION)
        deflater.setInput(inputBytes)
        deflater.finish()

        // Output buffer for compressed data
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(inputBytes.size)

        // Perform compression
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer) // Compress data into buffer
            outputStream.write(buffer, 0, count)
        }

        // Clean up
        deflater.end()

        // Encode compressed bytes to Base64 for readability
        return outputStream.toByteArray()
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun deflateAndEncode(text: String): String {
        // Convert string to bytes using UTF-8
        val inputBytes = text.toByteArray(Charsets.UTF_8)
        val compressedBytes = compress(inputBytes)
        val compressedBytesSlice = compressedBytes.drop(2).dropLast(4).toByteArray()
        val compressedText = Base64.Default.encode(compressedBytesSlice)
        val plantumlBase64Encoded = compressedText.map { b64ToPlantuml[it] ?: '*' }.joinToString("")
        return plantumlBase64Encoded
    }

    @Test
    fun hellowWorldExample() {
        val code = """@startuml
Bob -> Alice : hello
@enduml""".trimIndent()
        val expected = "SoWkIImgAStDuNBAJrBGjLDmpCbCJbMmKiX8pSd9vt98pKi1IW80"
        val computed = deflateAndEncode(code)
        assertEquals(expected, computed)
    }

    @Test
    fun hellowWorldExampleWithColor() {
        val code = """@startuml
Bob -[#ff2266]> Alice : hello
@enduml""".trimIndent()
        val expected = "SoWkIImgAStDuNBAJrBGZLPEIpCoCZEBjLDmpCbCJbMmKiX8pSd9vt98pKi1gW80"
        val computed = deflateAndEncode(code)
        assertEquals(expected, computed)
    }

    @Test
    fun anotherWebsiteExample() {
        val code = """@startuml
Alice -> Bob: Authentication Request
Bob --> Alice: Authentication Response
@enduml""".trimIndent()
        val expected = "SoWkIImgAStDuNBCoKnELT2rKt3AJx9IS2mjoKZDAybCJYp9pCzJ24ejB4qjBk42oYde0jM05MDHLLoGdrUSokMGcfS2D1C0"
        val computed = deflateAndEncode(code)
        assertEquals(expected, computed)
    }
}