package options

import Options
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

class CustomPumlContentTests : OptionsTest() {

    @Test
    fun `title and prefix and postfix are appended`() {
        val puml = compile(
            Options(title = "Diagram without anything", prefix = "'start", postfix = "'end"), listOf()
        )
        assertContains(puml, "title Diagram without anything")
        assertContains(puml, "'start")
        assertContains(puml, "'end")
    }
}