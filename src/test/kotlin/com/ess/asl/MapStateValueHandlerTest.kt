package com.ess.asl

class MapStateValueHandlerTest : AslTestBase() {

    fun testIteratorNotPresentInCompletion() {
        configure("""
            {
              "States": {
                "c": {
                  "Type": "Map",
                  "<caret>"
                }
              }
            }
        """.trimIndent())

        assertCompletionVariantsPresent("ItemsPath", "ItemReader")
        assertCompletionVariantsAbsent("Iterator")
    }

    fun testIteratorSupported() {
        configure("""
            {
              "States": {
                "c": {
                  "Type": "Map",
                  "Iterator": {
                    "<caret>"
                  }
                }
              }
            }
        """.trimIndent())

        assertCompletionVariants("StartAt", "States")
    }
}