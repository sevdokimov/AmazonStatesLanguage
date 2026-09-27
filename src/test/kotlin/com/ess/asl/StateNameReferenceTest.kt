package com.ess.asl

class StateNameReferenceTest : AslTestBase() {

    fun testReferenceAtStartAt() {
        configure(
            """
            {
                "StartAt": "<caret>",
                "States": {
                    "FirstState": {
                        "Type" : "Task",
                    }
                    "SecondState": {
                        "Type" : "Task",
                    }
                }
            }
            """)

        assertCompletionVariants("FirstState", "SecondState")
    }

    fun testCompletionInNext() {
        configure(
            """
            {
                "StartAt": "",
                "States": {
                    "FirstState": {
                        "Type" : "Task",
                        "Next": "<caret>"
                    }
                    "SecondState": {
                        "Type" : "Task",
                    }
                    "ThirdState": {
                        "Type" : "Task",
                    }
                }
            }
            """)

        assertCompletionVariants("SecondState", "ThirdState")
    }

    fun testInsideItemProcessor() {
        configure(
            """
{
  "StartAt": "ProcessSegments",
  "States": {
    "ProcessSegments": {
      "Type": "Map",
      "ItemProcessor": {
        "StartAt": "BackfillSegment",
        "States": {
          "BackfillSegment": {
            "Type": "Task",
            "Resource": "BackfillWorkerFunctionArn",
            "TimeoutSeconds": 900,
            "Next": "<caret>"
          },
          "Aaa": {
            "Type": "Succeed"
          },
          "Bbb": {
            "Type": "Succeed"
          }
        }
      }
    }
  }
}
            """)

        assertCompletionVariants("Aaa", "Bbb")
    }

    fun testRename() {
        configure(
            """
            {
                "StartAt": "A",
                "States": {
                    "A": {
                        "Type" : "Task",
                        "Next": "SecondState<caret>"
                    },
                    "B": {
                        "Type" : "Task",
                        "Next": "SecondState"
                    },
                    "SecondState": {
                        "Type" : "Task"
                    }
                }
            }
            """)

        myFixture.renameElementAtCaret("EndState")

        myFixture.checkResult("""
            {
                "StartAt": "A",
                "States": {
                    "A": {
                        "Type" : "Task",
                        "Next": "EndState<caret>"
                    },
                    "B": {
                        "Type" : "Task",
                        "Next": "EndState"
                    },
                    "EndState": {
                        "Type" : "Task"
                    }
                }
            }
        """.trimIndent())

    }

    fun testNoReferenceInNonAslFiles() {
        val fileText = """
            {
                "StartAt": "<caret>",
                "States": {
                    "FirstState": {
                        "Type" : "Task",
                    }
                    "SecondState": {
                        "Type" : "Task",
                    }
                }
            }
            """

        configure(fileText, "workflow.asl.json")
        assertCompletionVariants("FirstState", "SecondState")

        configure(fileText, "workflow.json")
        assertCompletionVariantsAbsent("FirstState", "SecondState")
    }
}
