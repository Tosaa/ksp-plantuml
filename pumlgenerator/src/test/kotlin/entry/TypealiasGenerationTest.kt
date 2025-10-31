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
    val advancedTypeAliasOfCollectionCode = """
        package extensions
        data class Page(val text:String)
        typealias Book = List<Pair<Int,Page>>
    """.trimIndent()

    val typeAliasOfCollectionCode = """
        package extensions
        typealias Book = List<String>
    """.trimIndent()

    val typeAliasOfPrimitiveCode = """
        package extensions
        typealias Page = String
    """.trimIndent()

    val typeAliasOfPairOfPrimitivesCode = """
        package extensions
        typealias Item = Pair<Int,String>
    """.trimIndent()

    val typeAliasOfPairOfClassesCode = """
        package extensions
        data class Page(val text:String)
        typealias Item = Pair<Int,Page>
    """.trimIndent()

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


    val classInPackageB = """
        package com.b
        class Test(){}
    """.trimIndent()

    val typeAliasOfPackageBInPackageACode = """
        package com.a
        import com.b.Test
        
        typealias TestClass = Test
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
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
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
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
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
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "class \"BigItem\" as basic_BigItem")
        assertContains(generatedFile, "TypeAlias of Item")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Typealias can reference any class`() {
        val code = """
            package com.test           
            typealias Text = String
        """.trimIndent()
        val files = listOf(SourceFile.kotlin("AliasOfString.kt", code))
        val compilation = newCompilation(DEFAULT_OPTIONS.copy(includedPackages = listOf("com.test")), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "class \"Text\" as com_test_Text")
        assertContains(generatedFile, "TypeAlias of String")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Typealias can reference collections with generics as elements`() {
        val files = listOf(SourceFile.kotlin("AliasCode.kt", advancedTypeAliasOfCollectionCode))
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "TypeAlias of List<Pair<Int,Page>>")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Typealias can reference collections with primitive types`() {
        val files = listOf(SourceFile.kotlin("AliasCode.kt", typeAliasOfCollectionCode))
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "TypeAlias of List<String>")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Typealias can reference primitive types`() {
        val files = listOf(SourceFile.kotlin("AliasCode.kt", typeAliasOfPrimitiveCode))
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "TypeAlias of String")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Typealias can reference Pairs of primitive types`() {
        val files = listOf(SourceFile.kotlin("AliasCode.kt", typeAliasOfPairOfPrimitivesCode))
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "TypeAlias of Pair<Int,String>")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Typealias can reference Pairs of classes`() {
        val files = listOf(SourceFile.kotlin("AliasCode.kt", typeAliasOfPairOfClassesCode))
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "TypeAlias of Pair<Int,Page>")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Typealias of class in excluded package is shown`() {

        val files = listOf(
            SourceFile.kotlin("Test.kt", classInPackageB),
            SourceFile.kotlin("TestAlias.kt", typeAliasOfPackageBInPackageACode)
        )
        val compilation = newCompilation(DEFAULT_OPTIONS.copy(excludedPackages = listOf("com.b")), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "TypeAlias of Test")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Typealias of class in not included package is shown`() {

        val files = listOf(
            SourceFile.kotlin("Test.kt", classInPackageB),
            SourceFile.kotlin("TestAlias.kt", typeAliasOfPackageBInPackageACode)
        )
        val compilation = newCompilation(DEFAULT_OPTIONS.copy(includedPackages = listOf("com.a")), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "TypeAlias of Test")
    }

}