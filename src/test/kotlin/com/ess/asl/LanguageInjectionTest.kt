package com.ess.asl

import com.ess.asl.ilang.AslIntrinsicLanguage
import com.intellij.json.JsonElementTypes
import com.intellij.json.JsonLanguage
import com.intellij.jsonpath.JsonPathLanguage
import com.intellij.lang.Language
import com.intellij.psi.impl.source.tree.LeafPsiElement

class LanguageInjectionTest : AslTestBase() {

    val jsonpathVariants = arrayOf("concat()", "avg()", "keys()")

    fun testInsideParameters() {
        configure(
            """
            {
                "StartAt": "DefaultState",
                "States": {
                    "FirstMatchState": {
                        "Type" : "Task",
                        "Parameters": {
                            "foo.$": "$.<caret>"
                        }
                    }
                }
            }
            """)

        assertCompletionVariantsPresent(*jsonpathVariants)
    }

    fun testNoInjectionInsidePropertyName() {
        configure(
            """
            {
                "StartAt": "DefaultState",
                "States": {
                    "FirstMatchState": {
                        "Type" : "Task",
                        "Parameters": {
                            "foo.<caret>$": "$."
                        }
                    }
                }
            }
            """)

        assertNoLanguaIngestion()
    }

    fun testInsideParametersNotAVar() {
        configure(
            """
            {
                "StartAt": "DefaultState",
                "States": {
                    "FirstMatchState": {
                        "Type" : "Task",
                        "Parameters": {
                            "foo": "$.<caret>"
                        }
                    }
                }
            }
            """)

        assertCompletionVariantsAbsent(*jsonpathVariants)
    }

    fun testDoubleDollar() {
        checkLangInjection("\$\$.s<caret>ss", JsonPathLanguage.INSTANCE, "$.sss")
    }

    fun testIntrinsicLanguageInjection() {
        checkLangInjection("foo<caret>()", AslIntrinsicLanguage.INSTANCE, "foo()")
        checkLangInjection("foo($.aaa.b<caret>bb)", JsonPathLanguage.INSTANCE, "$.aaa.bbb")
        checkLangInjection("fo<caret>o($.aaa.bbb)", AslIntrinsicLanguage.INSTANCE, "foo(1)")
        checkLangInjection("foo($.aaa.bbb, 777<caret>)", AslIntrinsicLanguage.INSTANCE, "foo(1, 777)")
        checkLangInjection("foo($.aaa.bbb, 33, $.fo<caret>o", JsonPathLanguage.INSTANCE, "$.foo")
        checkLangInjection("foo('a\\\"a<caret>')", AslIntrinsicLanguage.INSTANCE, "foo('a\"a')")
        checkLangInjection("foo(111, $['a\\\"a<caret>'])", JsonPathLanguage.INSTANCE, "$['a\"a']")
    }

    private fun checkLangInjection(property: String, expectedLang: Language, fileContent: String) {
        configure(
            """
            {
                "StartAt": "DefaultState",
                "States": {
                    "FirstMatchState": {
                        "Type" : "Task",
                        "Parameters": {
                            "foo.$": "$property"
                        }
                    }
                }
            }
            """)

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertEquals(expectedLang, element!!.language)
        assertEquals(fileContent, element.containingFile.text)
    }

    fun testNoInjectionInsideAssign() {
        configure(
            """
            {
                "StartAt": "DefaultState",
                "States": {
                    "DefaultState": {
                        "Type" : "Task",
                        "Assign": {
                            "foo.$": "$.s<caret>ss"
                        }
                    }
                }
            }
            """)

        assertNoLanguaIngestion()
    }

    fun testNoInjectionInNonAslFiles() {
        val fileText = """
            {
                "StartAt": "DefaultState",
                "States": {
                    "FirstMatchState": {
                        "Type" : "Task",
                        "Parameters": {
                            "foo.$": "$.<caret>"
                        }
                    }
                }
            }
        """

        configure(fileText, "workflow.asl.json")
        assertCompletionVariantsPresent(*jsonpathVariants)

        configure(fileText, "workflow.json")
        assertCompletionVariantsAbsent(*jsonpathVariants)
    }

    private fun assertNoLanguaIngestion() {
        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertEquals(JsonLanguage.INSTANCE, element!!.language)
        assertEquals(JsonElementTypes.DOUBLE_QUOTED_STRING, (element as LeafPsiElement).elementType)
    }
}
