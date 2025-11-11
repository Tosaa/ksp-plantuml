package docgeneration

import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.test.Test

class UIGeneration {

    val path = "https://carbon.now.sh/?bg=rgba%28222%2C171%2C99%2C1%29&t=duotone-dark&wt=none&l=auto&width=878&ds=true&dsyoff=20px&dsblur=68px&wc=true&wa=false&pv=56px&ph=56px&ln=true&fl=1&fm=Hack&fs=16.5px&lh=141%25&si=false&es=2x&wm=false&code="
    @Test
    fun generateUIComponnents() {
        val plantumlBase64Encoded = """
            fun foo(){
            
            }
        """.let {
            URLEncoder.encode(it, Charsets.UTF_8)
        }

        println("\tRequest image with ${plantumlBase64Encoded.length} payload")
        val url = URL("$path/$plantumlBase64Encoded")
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()
        val filePath = File.createTempFile("tmp", ".png")

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
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
        } else {
            println("\t${connection.responseMessage}")
            println("\tURL: $url")
        }

    }
}