package options

import Options
import assertContainsNot
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

class VisibilityOptionsTest : OptionsTest() {

    @Test
    fun `Only public properties and functions are visualized`() {
        val puml = compile(
            Options(
                showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                showInternalClasses = false, showInternalFunctions = false, showInternalProperties = false,
                showPrivateClasses = false, showPrivateFunctions = false, showPrivateProperties = false,
            ), listOf(kotlinClassCode("public", "TestClass"))
        )
        // Public
        assertContains(puml, "one : Int")
        assertContains(puml, "two : Int")
        assertContains(puml, "firstFun() : Unit")
        assertContains(puml, "secondFun() : Unit")
        // Internal
        assertContainsNot(puml, "three : Int")
        assertContainsNot(puml, "thirdFun() : Unit")
        // Private
        assertContainsNot(puml, "four : Int")
        assertContainsNot(puml, "fourthFun() : Unit")
    }

    @Test
    fun `Only internal properties and functions are visualized`() {
        val puml = compile(
            Options(
                showPublicClasses = false, showPublicFunctions = false, showPublicProperties = false,
                showInternalClasses = true, showInternalFunctions = true, showInternalProperties = true,
                showPrivateClasses = false, showPrivateFunctions = false, showPrivateProperties = false,
            ), listOf(kotlinClassCode("internal", "TestClass"))
        )
        // Public
        assertContainsNot(puml, "one : Int")
        assertContainsNot(puml, "two : Int")
        assertContainsNot(puml, "firstFun() : Unit")
        assertContainsNot(puml, "secondFun() : Unit")
        // Internal
        assertContains(puml, "three : Int")
        assertContains(puml, "thirdFun() : Unit")
        // Private
        assertContainsNot(puml, "four : Int")
        assertContainsNot(puml, "fourthFun() : Unit")
    }

    @Test
    fun `Only private properties and functions are visualized`() {
        val puml = compile(
            Options(
                showPublicClasses = false, showPublicFunctions = false, showPublicProperties = false,
                showInternalClasses = false, showInternalFunctions = false, showInternalProperties = false,
                showPrivateClasses = true, showPrivateFunctions = true, showPrivateProperties = true,
            ), listOf(kotlinClassCode("private", "TestClass"))
        )
        // Public
        assertContainsNot(puml, "one : Int")
        assertContainsNot(puml, "two : Int")
        assertContainsNot(puml, "firstFun() : Unit")
        assertContainsNot(puml, "secondFun() : Unit")
        // Internal
        assertContainsNot(puml, "three : Int")
        assertContainsNot(puml, "thirdFun() : Unit")
        // Private
        assertContains(puml, "four : Int")
        assertContains(puml, "fourthFun() : Unit")
    }

    @Test
    fun `public and Internal properties and functions are visualized`() {
        val puml = compile(
            Options(
                showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                showInternalClasses = true, showInternalFunctions = true, showInternalProperties = true,
                showPrivateClasses = false, showPrivateFunctions = false, showPrivateProperties = false
            ), listOf(kotlinClassCode("public", "TestClass"))
        )
        // Public
        assertContains(puml, "one : Int")
        assertContains(puml, "two : Int")
        assertContains(puml, "firstFun() : Unit")
        assertContains(puml, "secondFun() : Unit")
        // Internal
        assertContains(puml, "three : Int")
        assertContains(puml, "thirdFun() : Unit")
        // Private
        assertContainsNot(puml, "four : Int")
        assertContainsNot(puml, "fourthFun() : Unit")
    }

    @Test
    fun `visibility modifiers can be shown`() {
        val puml = compile(
            Options(
                showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                showInternalClasses = true, showInternalFunctions = true, showInternalProperties = true,
                showPrivateClasses = true, showPrivateFunctions = true, showPrivateProperties = true,
                showVisibilityModifiers = true
            ), listOf(kotlinClassCode("public", "TestClass"))
        )
        // Public
        assertContains(puml, "+ one : Int")
        assertContains(puml, "+ two : Int")
        assertContains(puml, "+ firstFun() : Unit")
        assertContains(puml, "+ secondFun() : Unit")
        // Internal
        assertContains(puml, "# three : Int")
        assertContains(puml, "# thirdFun() : Unit")
        // Private
        assertContains(puml, "- four : Int")
        assertContains(puml, "- fourthFun() : Unit")
    }

    @Test
    fun `visibility modifiers can be hidden`() {
        val puml = compile(
            Options(
                showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                showInternalClasses = true, showInternalFunctions = true, showInternalProperties = true,
                showPrivateClasses = true, showPrivateFunctions = true, showPrivateProperties = true,
                showVisibilityModifiers = false
            ), listOf(kotlinClassCode("public", "TestClass"))
        )
        // Public
        assertContainsNot(puml, "+ one : Int")
        assertContains(puml, "one : Int")
        assertContainsNot(puml, "+ two : Int")
        assertContains(puml, "two : Int")
        assertContainsNot(puml, "+ firstFun() : Unit")
        assertContains(puml, "firstFun() : Unit")
        assertContainsNot(puml, "+ secondFun() : Unit")
        assertContains(puml, "secondFun() : Unit")
        // Internal
        assertContainsNot(puml, "# three : Int")
        assertContains(puml, "three : Int")
        assertContainsNot(puml, "# thirdFun() : Unit")
        assertContains(puml, "thirdFun() : Unit")
        // Private
        assertContainsNot(puml, "- four : Int")
        assertContains(puml, "four : Int")
        assertContainsNot(puml, "- fourthFun() : Unit")
        assertContains(puml, "fourthFun() : Unit")
    }

    @Test
    fun `Only public classes are visualized`() {
        val puml = compile(
            Options(
                showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                showInternalClasses = false, showInternalFunctions = true, showInternalProperties = true,
                showPrivateClasses = false, showPrivateFunctions = true, showPrivateProperties = true,
            ), listOf(
                kotlinClassCode("public", "FirstClass"),
                kotlinClassCode("internal", "SecondClass"),
                kotlinClassCode("public", "ThirdClass"),
            )
        )
        assertContains(puml, "FirstClass")
        assertContainsNot(puml, "SecondClass")
        assertContains(puml, "ThirdClass")
    }

}