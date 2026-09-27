package com.ess.asl

import com.intellij.openapi.util.text.StringUtil.trimEnd
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Path

abstract class AslTestBase : BasePlatformTestCase() {

    protected fun configure(text: String, fileName: String = "workflow.asl.json") {
        myFixture.configureByText(fileName, text.trimIndent())
    }

    protected fun assertCompletionVariants(vararg expected: String) {
        val variants = myFixture.completeBasic() ?: emptyArray()

        assertEquals(expected.toSet(), variants.map { it.lookupString }.toSet())
    }

    protected fun assertCompletionVariantsPresent(vararg expected: String) {
        val variants = myFixture.completeBasic() ?: emptyArray()
        val lookupStrings = variants.map { it.lookupString }

        for (variant in expected) {
            assertTrue(
                "Expected variant '$variant' is absent. Actual variants: $lookupStrings",
                lookupStrings.contains(variant)
            )
        }
    }

    protected fun assertCompletionVariantsAbsent(vararg expected: String) {
        val variants = myFixture.completeBasic() ?: emptyArray()
        val lookupStrings = variants.map { it.lookupString }

        for (variant in expected) {
            assertFalse(
                "Variant '$variant' must be absent. Actual variants: $lookupStrings",
                lookupStrings.contains(variant)
            )
        }
    }

    override fun getTestDataPath(): String? {
        val classFileName = AslTestBase::class.qualifiedName!!.replace('.', '/') + ".class"
        val classPath = javaClass.classLoader.getResource(classFileName)!!.path

        var dir = Path.of(trimEnd(classPath, classFileName))

        while (dir.fileName.toString() != "build") {
            dir = dir.parent
        }

        dir = dir.parent // skip "build" directory

        return dir.resolve("src/test/testData").toString()
    }

}