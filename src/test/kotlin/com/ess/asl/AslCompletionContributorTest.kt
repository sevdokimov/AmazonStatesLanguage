package com.ess.asl

class AslCompletionContributorTest : AslTestBase() {

    fun testTypePropertyIsSuggestedInStateObject() {
        configure(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "<caret>"
                }
              }
            }
            """
        )

        assertCompletionVariantsPresent("Type")
    }

    fun testTaskStatePropertiesAreSuggested() {
        configure(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "Type": "Task",
                  "Resource": "arn:aws:lambda:::function:myFunction",
                  "<caret>"
                }
              }
            }
            """
        )

        assertCompletionVariantsPresent("Comment", "InputPath", "OutputPath", "Assign", "Next", "End",
            "ResultPath", "Parameters", "ResultSelector", "Retry", "Catch")
    }

    fun testExistingPropertiesAreNotSuggested() {
        configure(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "Type": "Task",
                  "Comment": "My task",
                  "<caret>"
                }
              }
            }
            """
        )

        assertCompletionVariantsAbsent("Type", "Comment")
        assertCompletionVariantsPresent("Next", "End", "InputPath")
    }

    fun testJSONataTaskPropertiesAreSuggested() {
        configure(
            """
            {
              "QueryLanguage": "JSONata",
              "StartAt": "First",
              "States": {
                "First": {
                  "Type": "Task",
                  "Resource": "arn:aws:lambda:::function:myFunction",
                  "<caret>"
                }
              }
            }
            """
        )

        assertCompletionVariantsPresent("Comment", "Output", "Assign", "Next", "End", "Arguments", "Retry", "Catch")
        assertCompletionVariantsAbsent("InputPath", "OutputPath", "Parameters", "ResultPath", "ResultSelector")
    }

    fun testNoCompletionOutsideStatesObject() {
        configure(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "Type": "Task"
                }
              },
              "<caret>": true
            }
            """
        )

        assertCompletionVariantsAbsent("Type", "Next", "End")
    }

    fun testNoCompletionInNonAslFile() {
        configure(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "Type": "Task",
                  "<caret>"
                }
              }
            }
            """,
            fileName = "workflow.json"
        )

        assertCompletionVariantsAbsent("Next", "End", "Comment")
    }

    fun testNoCompletionInPropertyValuePosition() {
        configure(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "Type": "Task",
                  "Comment": "<caret>"
                }
              }
            }
            """
        )

        assertCompletionVariantsAbsent("Next", "End", "InputPath")
    }

    fun testCompleteIncompleteProperty() {
        configure(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "Type": "Task",
                  "<caret>"
                }
              }
            }
            """
        )

        assertCompletionVariantsPresent("Comment", "InputPath", "OutputPath", "Assign", "Next", "End",
            "ResultPath", "Parameters", "ResultSelector", "Retry", "Catch")
    }

    fun testTaskNameCompletions() {
        configure(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "Type": "<caret>",
                }
              }
            }
            """
        )

        assertCompletionVariantsPresent("Task", "Parallel", "Map", "Pass", "Wait")
    }

    fun testQueryLanguageCompletion() {
        configure(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "Type": "Task",
                  "QueryLanguage": "<caret>",
                }
              }
            }
            """
        )

        assertCompletionVariantsPresent("JSONPath", "JSONata")
    }

    fun testEndPropertyCompletion() {
        configure(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "Type": "Task",
                  "End<caret>"
                }
              }
            }
            """
        )

        myFixture.completeBasic()

        myFixture.checkResult(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "Type": "Task",
                  "End": <selection>true<caret></selection>
                }
              }
            }
            """.trimIndent())
    }

    fun testPayloadTemplatePropertyCompletion() {
        configure(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "Type": "Task",
                  "Paramet<caret>"
                }
              }
            }
            """
        )

        myFixture.completeBasic()

        myFixture.checkResult(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "Type": "Task",
                  "Parameters": {
                    <caret>
                  }
                }
              }
            }
            """.trimIndent())
    }

    fun testFieldWithArrayValueCompletion() {
        configure(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "Type": "Task",
                  "Retr<caret>"
                }
              }
            }
            """
        )

        myFixture.completeBasic()

        myFixture.checkResult(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "Type": "Task",
                  "Retry": [<caret>]
                }
              }
            }
            """.trimIndent())
    }

    fun testCompleteTypeProperty() {
        configure(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "Typ<caret>"
                }
              }
            }
            """
        )

        myFixture.completeBasic()
        myFixture.checkResult(
            """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "Type": "<caret>"
                }
              }
            }
            """.trimIndent()
        )
    }

    fun testNoCompletionInNonAslFiles() {
        val fileText = """
            {
              "StartAt": "First",
              "States": {
                "First": {
                  "<caret>"
                }
              }
            }
            """

        configure(fileText, "workflow.asl.json")
        assertCompletionVariantsPresent("Type")

        configure(fileText, "workflow.json")
        assertCompletionVariantsAbsent("Type")
    }

}
