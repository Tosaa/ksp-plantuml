package relation

import CompilationTest
import Options
import assertContainsNot
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InheritanceTest : CompilationTest() {
    val animalInterfaceCode = """
    package explorer.database
    public interface Animal {
        val name : String
        val isExtinct : Boolean
        fun makeSound() : String
        fun run(distance:Int) : Unit
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
        override fun run(distance:Int) = Unit
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
        override fun run(distance:Int) = Unit
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
        override fun run(distance:Int) = Unit
        fun run(distance:Long) = Unit
    }
    """

    val abstractAnimalClassCode = """
    package explorer.database
    public abstract class AbstractAnimal {
        abstract val name : String
        val isExtinct : Boolean = false
        fun makeSound() : String = ""
        open fun run(distance:Int) = Unit
    }
    """

    val abstractAnimalInheritingAnimalInterfaceClassCode = """
    package explorer.database
    import explorer.database.Animal
    public abstract class AbstractAnimal : Animal {
        override val isExtinct : Boolean = false
        override fun makeSound() : String = ""
        open override fun run(distance:Int) = Unit
    }
    """

    val catImplementationCodeWithAbstractParent = """
    package explorer.database.pets
    import explorer.database.AbstractAnimal
    internal class Cat : AbstractAnimal(){
        override val name : String = "CAT"
        override fun run(distance:Int) = Unit
    }
    """

    val dogImplementationCodeWithAbstractParent = """
    package explorer.database.pets
    import explorer.database.AbstractAnimal
    internal class Dog : AbstractAnimal(){
        override val name : String = "DOG"
    }
    """

    val allRelationsGivenCode = """
        package a
        interface C {
        
        }
        interface B : C {
            val variableC : C
            fun functionC() : C
        }
        interface D {
            val variableC : C
            fun functionC(): C
        }
        interface E {
            fun functionC(): C
        }
    """.trimIndent()

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
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
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

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Create inheritance for Interface and implementation with showInheritedProperties option false`() {
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
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "\"Animal\" as explorer_database_Animal")
        assertContains(generatedFile, "\"Cat\" as explorer_database_pets_Cat")
        assertContains(generatedFile, "\"Dog\" as explorer_database_pets_Dog")
        assertContains(generatedFile, "\"TRex\" as explorer_database_forensic_TRex")
        val sections = generatedFile.split("'")
        sections.find { it.contains("explorer_database_pets_Cat") }.let {
            assertNotNull(it, "Cat class description should exist")
            assertContainsNot(it, "name : String")
            assertContainsNot(it, "isExtinct : Boolean")
            assertContainsNot(it, "run(Int) : Unit")
        }
        sections.find { it.contains("explorer_database_pets_Dog") }.let {
            assertNotNull(it, "Dog class description should exist")
            assertContainsNot(it, "name : String")
            assertContainsNot(it, "isExtinct : Boolean")
            assertContainsNot(it, "run(Int) : Unit")
        }
        sections.find { it.contains("explorer_database_forensic_TRex") }.let {
            assertNotNull(it, "TRex class description should exist")
            assertContainsNot(it, "name : String")
            assertContainsNot(it, "isExtinct : Boolean")
            assertContainsNot(it, "run(Int) : Unit")
            assertContains(it, "run(Long) : Unit")
        }
        sections.find { it.contains("explorer_database_Animal") }.let {
            assertNotNull(it, "Animal class description should exist")
            assertContains(it, "name : String")
            assertContains(it, "isExtinct : Boolean")
            assertContains(it, "run(Int) : Unit")
        }
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Create inheritance for Interface and implementation with showInheritedFunctions option false`() {
        val fileNames = listOf("Animal.kt", "Cat.kt", "Dog.kt", "TRex.kt")
        val codes = listOf(animalInterfaceCode, catImplementationCode, dogImplementationCode, trexImplementationCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(DEFAULT_OPTIONS.also {
            println(it)
        }, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "\"Animal\" as explorer_database_Animal")
        assertContains(generatedFile, "\"Cat\" as explorer_database_pets_Cat")
        assertContains(generatedFile, "\"Dog\" as explorer_database_pets_Dog")
        assertContains(generatedFile, "\"TRex\" as explorer_database_forensic_TRex")
        val sections = generatedFile.split("'")
        sections.find { it.contains("explorer_database_pets_Cat") }.also {
            assertNotNull(it, "Cat class description should exist")
            assertContainsNot(it, "makeSound() : String")
        }
        sections.find { it.contains("explorer_database_pets_Dog") }.also {
            assertNotNull(it, "Dog class description should exist")
            assertContainsNot(it, "makeSound() : String")
        }
        sections.find { it.contains("explorer_database_forensic_TRex") }.also {
            assertNotNull(it, "TRex class description should exist")
            assertContainsNot(it, "makeSound() : String")
        }
        sections.find { it.contains("explorer_database_Animal") }.also {
            assertNotNull(it, "Animal class description should exist")
            assertContains(it, "makeSound() : String")
        }
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Create inheritance for AbstractClass and Interface with showInheritedFunctions option false`() {
        val fileNames = listOf("AnimalInterface.kt", "AbstractAnimal.kt", "Cat.kt")
        val codes = listOf(animalInterfaceCode, abstractAnimalInheritingAnimalInterfaceClassCode, catImplementationCodeWithAbstractParent)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(DEFAULT_OPTIONS.also {
            println(it)
        }, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "\"Animal\" as explorer_database_Animal")
        assertContains(generatedFile, "\"AbstractAnimal\" as explorer_database_AbstractAnimal")
        assertContains(generatedFile, "\"Cat\" as explorer_database_pets_Cat")
        val sections = generatedFile.split("'")
        sections.find { it.contains("explorer_database_Animal") }.let {
            assertNotNull(it, "Animal class description should exist")
            assertContains(it, "name : String")
            assertContains(it, "makeSound() : String")
            assertContains(it, "isExtinct : Boolean")
        }
        sections.find { it.contains("explorer_database_AbstractAnimal") }.let {
            assertNotNull(it, "AbstractAnimal class description should exist")
            assertContainsNot(it, "name : String")
            assertContainsNot(it, "isExtinct : Boolean")
            assertContainsNot(it, "makeSound() : String")
        }
        sections.find { it.contains("explorer_database_pets_Cat") }.let {
            assertNotNull(it, "Cat class description should exist")
            assertContainsNot(it, "name : String")
            assertContainsNot(it, "isExtinct : Boolean")
            assertContainsNot(it, "makeSound() : String")
        }
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Create inheritance for Abstract class and implementation`() {
        val fileNames = listOf("Animal.kt", "Cat.kt", "Dog.kt")
        val codes = listOf(abstractAnimalClassCode, catImplementationCodeWithAbstractParent, dogImplementationCodeWithAbstractParent)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "\"AbstractAnimal\" as explorer_database_AbstractAnimal")
        assertContains(generatedFile, "\"Cat\" as explorer_database_pets_Cat")
        assertContains(generatedFile, "\"Dog\" as explorer_database_pets_Dog")
        assertContains(generatedFile, "explorer_database_AbstractAnimal <|-- explorer_database_pets_Cat")
        assertContains(generatedFile, "explorer_database_AbstractAnimal <|-- explorer_database_pets_Dog")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Don't create inheritance for Abstract class and implementation when showInheritance=false`() {
        val fileNames = listOf("Animal.kt", "Cat.kt", "Dog.kt")
        val codes = listOf(abstractAnimalClassCode, catImplementationCodeWithAbstractParent, dogImplementationCodeWithAbstractParent)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(Options(showInheritance = false), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "\"AbstractAnimal\" as explorer_database_AbstractAnimal")
        assertContains(generatedFile, "\"Cat\" as explorer_database_pets_Cat")
        assertContains(generatedFile, "\"Dog\" as explorer_database_pets_Dog")
        assertContainsNot(generatedFile, "explorer_database_AbstractAnimal <|-- explorer_database_pets_Cat")
        assertContainsNot(generatedFile, "explorer_database_AbstractAnimal <|-- explorer_database_pets_Dog")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Assert order of relations that are drawn`() {
        val fileNames = listOf("Test.kt")
        val codes = listOf(allRelationsGivenCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "a_C <|-- a_B")
        assertContains(generatedFile, "a_B --* a_C")
        assertContainsNot(generatedFile, "a_B --> a_C")
        assertContainsNot(generatedFile, "a_C <|-- a_D")
        assertContains(generatedFile, "a_D --* a_C")
        assertContainsNot(generatedFile, "a_D --> a_C")
        assertContainsNot(generatedFile, "a_C <|-- a_E")
        assertContainsNot(generatedFile, "a_E --* a_C")
        assertContains(generatedFile, "a_E --> a_C")
    }
}