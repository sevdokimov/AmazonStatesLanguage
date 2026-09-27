package com.ess.asl

class ChoiceStateTest : AslTestBase() {

    fun testTopLevelProperties() {
        configure("""
            {
              "States": {
                "c": {
                  "Type": "Choice",
                  "Choices": [
                    {
                      "<caret>"
                    }
                  ]
                }
              }
            }
        """.trimIndent())

        assertCompletionVariantsPresent("Next", "Not", "Or", "Variable")
    }

    fun testInnerProperties() {
        configure("""
            {
              "States": {
                "c": {
                  "Type": "Choice",
                  "Choices": [
                    {
                        "Not": {
                            "<caret>"
                        }
                    }
                  ]
                }
              }
            }
        """.trimIndent())

        assertCompletionVariantsPresent("Not", "Or", "Variable")
        assertCompletionVariantsAbsent("Next")
    }
}
