package options

import Options
import assertContainsNot
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

class PackageOptionsTest : OptionsTest() {

    @Test
    fun `Excluded package is ignored`() {
        val puml = compile(
            Options(
                showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                excludedPackages = listOf("com.inactive")
            ), listOf(kotlinClassCode("public", "TestClass", "package com.inactive"))
        )
        assertContainsNot(puml, "class \"TestClass\"")

    }

    @Test
    fun `Subpackage of Excluded package is ignored`() {
        val puml = compile(
            Options(
                showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                excludedPackages = listOf("com.inactive")
            ), listOf(kotlinClassCode("public", "TestClass", "package com.inactive.test"))
        )
        assertContainsNot(puml, "class \"TestClass\"")
    }

    @Test
    fun `Not included package is ignored`() {
        val puml = compile(
            Options(
                showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                includedPackages = listOf("test")
            ), listOf(kotlinClassCode("public", "TestClass", packagePrefix = "package active"))
        )
        assertContainsNot(puml, "class \"TestClass\"")
    }

    @Test
    fun `Subpackage of included package is used`() {
        val puml = compile(
            Options(
                showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                includedPackages = listOf("test")
            ), listOf(kotlinClassCode("public", "TestClass", packagePrefix = "package test.active"))
        )
        assertContains(puml, "class \"TestClass\"")
    }

    @Test
    fun `Empty package is respected`() {
        val puml = compile(
            Options(
                showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                allowEmptyPackage = true
            ), listOf(kotlinClassCode("public", "TestClass"))
        )
        assertContains(puml, "class \"TestClass\"")
    }

    @Test
    fun `Empty package is ignored`() {
        val puml = compile(
            Options(
                showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                allowEmptyPackage = false
            ), listOf(kotlinClassCode("public", "TestClass"))
        )
        assertContainsNot(puml, "class \"TestClass\"")
    }

    @Test
    fun `All package settings are respected`() {
        val puml = compile(
            Options(
                showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                allowEmptyPackage = true,
                excludedPackages = listOf("com.excluded"),
                includedPackages = listOf("com")
            ), listOf(
                kotlinClassCode("public", "NoPackageTestClass"),
                kotlinClassCode("public", "ExcludedPackageTestClass", "package com.excluded"),
                kotlinClassCode("public", "ExcludedSubPackageTestClass", "package com.excluded.anything"),
                kotlinClassCode("public", "IncludedPackageTestClass", "package com"),
                kotlinClassCode("public", "IncludedSubPackageTestClass", "package com.included"),
                kotlinClassCode("public", "IncludedSubSubPackageTestClass", "package com.test.still.included"),
            )
        )
        assertContains(puml,"class \"NoPackageTestClass\"")
        assertContainsNot(puml,"class \"ExcludedPackageTestClass\"")
        assertContainsNot(puml,"class \"ExcludedSubPackageTestClass\"")
        assertContains(puml,"class \"IncludedPackageTestClass\"")
        assertContains(puml,"class \"IncludedSubPackageTestClass\"")
        assertContains(puml,"class \"IncludedSubSubPackageTestClass\"")
    }

}