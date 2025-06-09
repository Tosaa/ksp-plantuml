package relation

import CompilationTest
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InheritanceTest : CompilationTest() {
    val animalInterfaceCode = """
    package explorer.database
    public interface Animal {
        val name : String
        val isExtinct : Boolean
        fun makeSound() : String
    }
    """
    val catImplementationCode = """
    package explorer.database.pets
    import explorer.database.Animal
    internal class Cat : Animal{
        override val name : String = "CAT"
        override val isExtinct : Boolean = false
        override fun makeSound() : String {
            return "Meowww"
        }
    }
    """
    val dogImplementationCode = """
    package explorer.database.pets
    import explorer.database.Animal
    internal class Dog : Animal{
        override val name : String = "DOG"
        override val isExtinct : Boolean = false
        override fun makeSound() : String {
            return "Wuff"
        }
    }
    """
    val trexImplementationCode = """
    package explorer.database.forensic
    import explorer.database.Animal
    internal class TRex : Animal{
        override val name : String = "T-REX"
        override val isExtinct : Boolean = true
        override fun makeSound() : String {
            return "Raaaarrrrr"
        }
    }
    """

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Create inheritance for Interface and implementation`() {
        val fileNames = listOf("Animal.kt", "Cat.kt", "Dog.kt", "TRex.kt")
        val codes = listOf(animalInterfaceCode, catImplementationCode, dogImplementationCode, trexImplementationCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "\"Animal\" as explorer_database_Animal")
        assertContains(generatedFile, "\"Cat\" as explorer_database_pets_Cat")
        assertContains(generatedFile, "\"Dog\" as explorer_database_pets_Dog")
        assertContains(generatedFile, "\"TRex\" as explorer_database_forensic_TRex")
        assertContains(generatedFile, "explorer_database_Animal <|-- explorer_database_pets_Cat")
        assertContains(generatedFile, "explorer_database_Animal <|-- explorer_database_pets_Dog")
        assertContains(generatedFile, "explorer_database_Animal <|-- explorer_database_forensic_TRex")

    }
}