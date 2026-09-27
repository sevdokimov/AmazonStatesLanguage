package com.ess.asl

class ParallelStateValueHandlerTest : AslTestBase() {

    private fun filename(): String {
        return getTestDataPath() + "/parallel/" + getTestName(true) + ".asl.json"
    }

    fun testTopLevelStartAtReference() {
        myFixture.testCompletionVariants(filename(), "level1Task1", "level1Task2", "level1Parallel")
    }

    fun testTopLevelNext() {
        myFixture.testCompletionVariants(filename(), "level1Task2", "level1Parallel")
    }

    fun testStartAtInParallel() {
        myFixture.testCompletionVariants(filename(), "level1A", "level1B", "level1C")
    }

    fun testNextInParallel() {
        myFixture.testCompletionVariants(filename(), "level1B", "level1C")
    }

    fun testTaskPropertyNameCompletion() {
        myFixture.configureByFiles(filename())
        myFixture.completeBasic()
        assertCompletionVariantsPresent("InputPath", "OutputPath", "Next")
        assertCompletionVariantsAbsent("Type", "Output", "Arguments")
    }
}