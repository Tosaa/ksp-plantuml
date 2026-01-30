import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.Deflater
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


plugins {
    kotlin("jvm")
    id("maven-publish")
    id("com.vanniktech.maven.publish") version "0.36.0"
    id("com.google.devtools.ksp") version "2.3.3"

}

group = "io.github.tosaa.puml.ksp"
version = "0.0.9"

repositories {
    mavenCentral()
}

ksp {
    arg("puml.allowEmptyPackage", "true")
    arg("puml.includedPackages", "graph,uml")
    arg("puml.excludedClassNames", "Options,OptionConstants")
    arg("puml.showPropertyRelations", "true")
    arg("puml.showFunctionRelations", "false")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.3")
    testImplementation(kotlin("test"))
    testImplementation("dev.zacsweers.kctfork:ksp:0.12.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    ksp("io.github.tosaa.puml.ksp:ksp-plantuml-generator:0.0.+")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    register("docsToPNG") {
        val directory = file(layout.projectDirectory.file("../doc/plantuml/"))
        directory.listFiles()?.forEach {
            val newPNGFile = file(it.parentFile.path + "/" + it.nameWithoutExtension + ".png").also {
                it.createNewFile()
            }
            createImageByServer(it, newPNGFile)
        }
    }

    register("projectDiagram") {
    dependsOn("kspKotlin")

        val generatedDiagram = layout.buildDirectory.file("generated/ksp/main/resources/generated/puml").get().asFile.listFiles { pathname: File ->
            pathname.name.endsWith(".puml")
        }!!.firstOrNull()
        println("Diagram file: $generatedDiagram")
        generatedDiagram?.let {
            // val newPNGFile = file(generatedDiagram.parentFile.path + "/" + generatedDiagram.nameWithoutExtension + ".png")
            val newPNGFile = layout.projectDirectory.file("../doc/contributing/overview.png").asFile.also {
                it.createNewFile()
            }
            println(newPNGFile)
            createImageByServer(it, newPNGFile)
        }
    }
}

kotlin {
    jvmToolchain(17)
}


mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "ksp-plantuml-generator", version.toString())

    pom {
        name = "ksp-plantuml-generator"
        description = "A Kotlin Symbol processor for Plantuml diagram generation."
        inceptionYear = "2025"
        url = "https://github.com/tosaa/ksp-plantuml/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "tosaa-ksp-plantuml"
                name = "Tosaa"
                url = "https://github.com/tosaa/"
            }
        }
        scm {
            url = "https://github.com/tosaa/ksp-plantuml/"
            connection = "scm:git:git://github.com/tosaa/ksp-plantuml.git"
            developerConnection = "scm:git:ssh://git@github.com/tosaa/ksp-plantuml.git"
        }
    }
}

val plantumlAlphabet = (('0'..'9') + ('A'..'Z') + ('a'..'z') + "-_").joinToString("")
val base64Alphabet = (('A'..'Z') + ('a'..'z') + ('0'..'9') + "+/").joinToString("")
val b64ToPlantuml: Map<Char, Char> = base64Alphabet.zip(plantumlAlphabet).associate { it.first to it.second }

fun compress(inputBytes: ByteArray): ByteArray {

    // Create Deflater instance with default compression level
    val deflater = Deflater(Deflater.BEST_COMPRESSION)
    deflater.setInput(inputBytes)
    deflater.finish()

    // Output buffer for compressed data
    val outputStream = ByteArrayOutputStream()
    var len: Int = inputBytes.size * 2
    val buffer = ByteArray(len)

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
    // Optimized text
    val optimized = text.lines().mapNotNull {
        var line = if (it.contains('\'')) {
            it.replaceAfter('\'', "").replace("\'", "")
        } else {
            it
        }
        line = line.trim()
        if (line.isEmpty() || line.isBlank()) {
            null
        } else {
            line
        }
    }.joinToString("\n")
    // Convert string to bytes using UTF-8
    val inputBytes = optimized.toByteArray(Charsets.UTF_8)
    val compressedBytes = compress(inputBytes)
    val compressedBytesSlice = compressedBytes.drop(2).dropLast(4).toByteArray()
    val compressedText = Base64.Default.encode(compressedBytesSlice)
    val plantumlBase64Encoded = compressedText.map { b64ToPlantuml[it] ?: '*' }.joinToString("")
    return plantumlBase64Encoded
}

fun createImageByServer(pumlDiagram: File, targetFile: File): Result<Unit> {
    if (!pumlDiagram.name.endsWith(".puml")) {
        return Result.failure(IllegalArgumentException("File $pumlDiagram must end with '.puml'"))
    }
    println("Diagram File: " + pumlDiagram.name)
    val plantumlBase64Encoded = deflateAndEncode(pumlDiagram.readText())
    println("\tRequest image generation with ${plantumlBase64Encoded.length} payload")
    val url = URL("https://www.plantuml.com/plantuml/png/$plantumlBase64Encoded")
    val connection = url.openConnection() as HttpURLConnection
    connection.connect()
    val filePath = targetFile.path

    return if (connection.responseCode == HttpURLConnection.HTTP_OK) {
        val inputStream = connection.inputStream
        val fileOutputStream = FileOutputStream(filePath)

        val buffer = ByteArray(2048)
        var bytesRead: Int

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            fileOutputStream.write(buffer, 0, bytesRead)
        }

        fileOutputStream.close()
        inputStream.close()
        println("\tPNG saved to $filePath")
        Result.success(Unit)
    } else {
        println("\tFailed to download image for ${pumlDiagram.name}. HTTP response code: ${connection.responseCode}")
        println("\t${connection.responseMessage}")
        println("\tURL: $url")
        Result.failure(RuntimeException("Server responded with error: ${connection.responseCode}, ${connection.responseMessage}"))
    }
}