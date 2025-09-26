package entry

import CompilationTest
import assertContainsNot
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TypealiasGenerationTest : CompilationTest() {
    val bigBoxExtentionCode = """
        package extensions
        import basic.BigItem 
        val BigItem.size : String 
            get() = "XL"
        fun BigItem.describe(): String {
            return "BigBox with description ${'$'}{this.description}"
        }
    """.trimIndent()

    val typeAliasOfItemInPackageBasic = """
        package basic
        typealias BigItem = Item
    """.trimIndent()

    val typeAliasOfItemInPackageAdvanced = """
        package advanced
        import basic.Item
        typealias BigItem = Item
    """.trimIndent()

    val boxCode = """
        package basic
        interface Box {
            val index : Int
            val content : BigItem
        }
        interface Item {
            val description : String
        }
    """.trimIndent()

    val boxCodeWithImportAdvancedBigItem = """
        package basic
        import advanced.BigItem
        interface Box {
            val index : Int
            val content : BigItem
        }
        interface Item {
            val description : String
        }
    """.trimIndent()

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Typealias of classes can be resolved`() {
        val files = listOf(SourceFile.kotlin("BoxAlias.kt", typeAliasOfItemInPackageBasic), SourceFile.kotlin("Box.kt", boxCode))
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "basic_Box --* basic_BigItem")
        assertContainsNot(generatedFile, "basic_Box --* box_Item")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Extensions on Typealias are shown`() {
        val files = listOf(SourceFile.kotlin("BoxAlias.kt", typeAliasOfItemInPackageBasic), SourceFile.kotlin("Box.kt", boxCode), SourceFile.kotlin("BigBoxExtension.kt", bigBoxExtentionCode))
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "describe() : String")
        assertContains(generatedFile, "size : String")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `TypeAlias references omits package when TypeAlias and class are in the same package`() {
        val files = listOf(SourceFile.kotlin("BoxAlias.kt", typeAliasOfItemInPackageBasic), SourceFile.kotlin("Box.kt", boxCode))
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "class \"BigItem\" as basic_BigItem")
        assertContains(generatedFile, "TypeAlias of Item")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `TypeAlias references other package with class if its not in the same package`() {
        val files = listOf(SourceFile.kotlin("BoxAlias.kt", typeAliasOfItemInPackageAdvanced), SourceFile.kotlin("Box.kt", boxCodeWithImportAdvancedBigItem))
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "class \"BigItem\" as advanced_BigItem")
        assertContains(generatedFile, "TypeAlias of basic.Item")
    }

}