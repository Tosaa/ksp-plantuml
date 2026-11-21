import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * Processors that generate code, like the ones of Hilt, Dagger or Room, cause a further processing round for the generated files.
 * The diagram is written after the last round, when the symbols of the earlier rounds cannot be resolved anymore.
 */
class MultiRoundTest : CompilationTest() {

    /**
     * Generates a class in the first round, which leads to a second round.
     */
    private class GeneratingProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
        private var isFirstRound = true

        override fun process(resolver: Resolver): List<KSAnnotated> {
            if (isFirstRound) {
                isFirstRound = false
                codeGenerator.createNewFile(Dependencies(false), "shop", "GeneratedCatalog").use {
                    it.write("package shop\nopen class GeneratedCatalog\n".toByteArray())
                }
            }
            return emptyList()
        }
    }

    // Is processed before Product.kt, therefore the element of Product exists before the class is visited
    private val extensionCode = """
    package shop
    fun Product.describe(): String = name
    """

    private val productCode = """
    package shop
    enum class Category { FOOD, TOYS }
    class Product(val name: String, val category: Category)
    """

    // Cannot be processed in the first round, since its supertype is generated in that round
    private val shopCode = """
    package shop
    class Shop(val products: List<Product>) : GeneratedCatalog()
    """

    @OptIn(ExperimentalCompilerApi::class)
    private fun generateInTwoRounds(vararg sources: SourceFile): String {
        val compilation = newCompilation(DEFAULT_OPTIONS, sources.toList()).apply {
            symbolProcessorProviders += SymbolProcessorProvider { environment -> GeneratingProcessor(environment.codeGenerator) }
        }
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        return result.sourcesGeneratedBySymbolProcessor.first { it.extension == "puml" }.readText()
    }

    @Test
    fun `Classes of the first round are rendered completely after a second round`() {
        val generatedFile = generateInTwoRounds(
            SourceFile.kotlin("Extensions.kt", extensionCode),
            SourceFile.kotlin("Product.kt", productCode),
            SourceFile.kotlin("Shop.kt", shopCode),
        )
        assertContains(generatedFile, "class \"Product\" as shop_Product")
        assertContains(generatedFile, "+ name : String")
        assertContains(generatedFile, "+ category : Category")
        assertContains(generatedFile, "enum \"Category\" as shop_Category")
        assertContains(generatedFile, "FOOD")
        assertContains(generatedFile, "TOYS")
        assertContains(generatedFile, "shop_Product --* shop_Category")
    }

    @Test
    fun `Classes that depend on generated code are rendered with the generated classes`() {
        val generatedFile = generateInTwoRounds(
            SourceFile.kotlin("Extensions.kt", extensionCode),
            SourceFile.kotlin("Product.kt", productCode),
            SourceFile.kotlin("Shop.kt", shopCode),
        )
        assertContains(generatedFile, "class \"GeneratedCatalog\" as shop_GeneratedCatalog")
        assertContains(generatedFile, "class \"Shop\" as shop_Shop")
        assertContains(generatedFile, "+ products : List<Product>")
        assertContains(generatedFile, "shop_GeneratedCatalog <|-- shop_Shop")
        assertContains(generatedFile, "shop_Shop ..* shop_Product")
    }

    @Test
    fun `Extensions are rendered once even though their file is visited in every round`() {
        val generatedFile = generateInTwoRounds(
            SourceFile.kotlin("Extensions.kt", extensionCode),
            SourceFile.kotlin("Product.kt", productCode),
            SourceFile.kotlin("Shop.kt", shopCode),
        )
        assertEquals(1, generatedFile.split("<ext> describe() : String").size - 1, generatedFile)
    }

    @Test
    fun `An extension of a class that depends on generated code does not hide its inheritance`() {
        val generatedFile = generateInTwoRounds(
            SourceFile.kotlin("ShopExtensions.kt", "package shop\nfun Shop.open(): Boolean = true"),
            SourceFile.kotlin("Product.kt", productCode),
            SourceFile.kotlin("Shop.kt", shopCode),
        )
        assertContains(generatedFile, "<ext> open() : Boolean")
        assertContains(generatedFile, "+ products : List<Product>")
        assertContains(generatedFile, "shop_GeneratedCatalog <|-- shop_Shop")
    }
}
